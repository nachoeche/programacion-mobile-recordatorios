package com.ignacio.recordatorios.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.ignacio.recordatorios.data.local.*
import com.ignacio.recordatorios.model.Reminder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.util.UUID

/** One instance per authenticated UID. Room remains the only UI data source. */
class CloudReminderRepository(
    private val dao: ReminderDao,
    private val firestore: FirebaseFirestore,
    private val uid: String
) : ReminderRepository {
    private val mutex = Mutex()
    private val collection get() = firestore.collection("users").document(uid).collection("reminders")

    override fun observeAll(): Flow<List<Reminder>> = dao.observeAll().map { rows -> rows.map { it.toDomain() } }
    override fun observeById(id: Int): Flow<Reminder?> = dao.observeById(id).map { it?.toDomain() }
    override suspend fun getAll(): List<Reminder> = observeAll().first()
    override suspend fun getById(id: Int): Reminder? = dao.byId(id)?.takeUnless { it.deleted }?.toDomain()
    override suspend fun nextId(): Int = (dao.maxId() ?: 0) + 1
    override suspend fun save(reminder: Reminder): Reminder {
        val entity = reminder.toEntity().copy(clientKey = UUID.randomUUID().toString(), syncStatus = "PENDING")
        val id = dao.insert(entity)
        check(id > 0) { "No se pudo guardar" }
        return entity.copy(id = id.toInt()).toDomain()
    }
    override suspend fun update(reminder: Reminder) {
        val old = dao.byId(reminder.id) ?: error("Recordatorio inexistente")
        dao.update(reminder.toEntity(old).copy(syncStatus = "PENDING"))
    }
    override suspend fun delete(id: Int) {
        val old = dao.byId(id) ?: return
        dao.update(old.copy(deleted = true, syncStatus = "PENDING"))
    }

    override suspend fun sync(): SyncResult = mutex.withLock {
        var sent = 0
        try {
            // Upserts use the same immutable document ID on every retry.
            for (row in dao.pending()) {
                val ref = collection.document(row.clientKey)
                val fields: Map<String, Any?> = mapOf(
                    "title" to row.title, "description" to row.description,
                    "category" to row.category, "priority" to row.priority,
                    "status" to row.status, "location" to row.location,
                    "createdAt" to row.createdAt, "deleted" to row.deleted,
                    "ownerUid" to uid
                )
                ref.set(fields).await()
                // Recheck before marking synced so a concurrent local edit stays pending.
                val fresh = dao.byId(row.id)
                if (fresh == row) dao.update(row.copy(syncStatus = "SYNCED"))
                sent++
            }
            // Server reads, not cached data, to distinguish a true remote refresh.
            val snapshot = collection.get(Source.SERVER).await()
            for (doc in snapshot.documents) {
                if (doc.getString("ownerUid") != uid) continue
                val existing = dao.byClientKey(doc.id)
                if (existing?.syncStatus == "PENDING") continue
                val title = doc.getString("title")?.trim().orEmpty()
                if (title.isEmpty()) continue
                val row = ReminderEntity(
                    id = existing?.id ?: 0,
                    clientKey = doc.id,
                    title = title,
                    description = doc.getString("description"),
                    category = doc.getString("category") ?: "Personal",
                    priority = doc.getString("priority") ?: "MEDIUM",
                    status = doc.getString("status") ?: "PENDING",
                    location = doc.getString("location"),
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    deleted = doc.getBoolean("deleted") ?: false,
                    syncStatus = "SYNCED"
                )
                if (existing == null) dao.insert(row) else dao.update(row)
            }
            SyncResult(sent)
        } catch (e: CancellationException) { throw e }
        catch (e: Exception) { SyncResult(sent, "Sincronizacion pendiente: ${e.localizedMessage ?: "sin conexion o acceso rechazado"}") }
    }
}
