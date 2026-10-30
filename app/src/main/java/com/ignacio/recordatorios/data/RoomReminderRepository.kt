package com.ignacio.recordatorios.data

import com.ignacio.recordatorios.data.local.*
import com.ignacio.recordatorios.data.remote.*
import com.ignacio.recordatorios.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/** Room is authoritative. Remote operations never directly mutate Compose state. */
class RoomReminderRepository(private val dao: ReminderDao, private val remote: RemoteService): ReminderRepository {
    private val syncMutex = Mutex()

    override fun observeAll(): Flow<List<Reminder>> = dao.observeAll().map { rows -> rows.map { it.toDomain() } }
    override fun observeById(id: Int): Flow<Reminder?> = dao.observeById(id).map { it?.toDomain() }
    override suspend fun getAll(): List<Reminder> = observeAll().first()
    override suspend fun getById(id: Int): Reminder? = dao.byId(id)?.takeUnless { it.deleted }?.toDomain()
    override suspend fun nextId(): Int = (dao.maxId() ?: 0) + 1
    override suspend fun save(reminder: Reminder): Reminder {
        val entity = reminder.toEntity()
        val localId = dao.insert(entity).toInt()
        require(localId > 0) { "No se pudo insertar el recordatorio" }
        return entity.copy(id = localId).toDomain()
    }
    override suspend fun update(reminder: Reminder) {
        val previous = dao.byId(reminder.id) ?: error("Recordatorio inexistente")
        dao.update(reminder.toEntity(previous))
    }
    override suspend fun delete(id: Int) {
        val previous = dao.byId(id) ?: return
        // Tombstone until the deletion is acknowledged remotely.
        dao.update(previous.copy(deleted = true, syncStatus = "PENDING"))
    }

    override suspend fun sync(): SyncResult = syncMutex.withLock {
        var processed = 0
        try {
            // Retries use a durable key. Actual exactly-once delivery requires SERVER support.
            for (row in dao.pending()) {
                val dto = PostDto(id = row.remoteId, title = row.title,
                    body = row.description ?: "", userId = 1)
                when {
                    row.deleted -> {
                        if (row.remoteId != null) remote.delete(row.remoteId, row.clientKey)
                        dao.delete(row)
                    }
                    row.remoteId == null -> {
                        val response = remote.create(row.clientKey, dto.copy(id = null))
                        dao.update(row.copy(remoteId = response.id, syncStatus = "SYNCED"))
                    }
                    else -> {
                        remote.update(row.remoteId, row.clientKey, dto)
                        dao.update(row.copy(syncStatus = "SYNCED"))
                    }
                }
                processed++
            }
            // Idempotent import of stable remote IDs; no duplicate Room records on repeated pulls.
            remote.list().take(3).filter { it.id != null && it.id > 0 }.forEach { post ->
                dao.insert(ReminderEntity(clientKey = "remote-${post.id}", remoteId = post.id,
                    title = post.title.ifBlank { "Sin título" }, description = post.body,
                    category = "Trabajo", priority = "MEDIUM", status = "PENDING",
                    syncStatus = "SYNCED"))
            }
            SyncResult(processed)
        } catch (e: CancellationException) { throw e }
        catch (e: SocketTimeoutException) { SyncResult(processed, "Tiempo de espera agotado. Los datos locales se conservan.") }
        catch (e: HttpException) {
            val label = when(e.code()) { in 400..499 -> "Solicitud rechazada"; in 500..599 -> "Servidor no disponible"; else -> "Respuesta HTTP inválida" }
            SyncResult(processed, "$label (${e.code()}). Podés reintentar.")
        }
        catch (e: IOException) { SyncResult(processed, "Sin conexión o error de red. Se mantiene pendiente.") }
        catch (e: Exception) { SyncResult(processed, e.message ?: "Falló la sincronización") }
    }
}
