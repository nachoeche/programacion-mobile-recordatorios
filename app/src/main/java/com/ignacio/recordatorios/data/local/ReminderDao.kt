package com.ignacio.recordatorios.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE deleted = 0 ORDER BY createdAt DESC, id DESC")
    fun observeAll(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id AND deleted = 0")
    fun observeById(id: Int): Flow<ReminderEntity?>

    @Query("SELECT * FROM reminders WHERE status = :status AND deleted = 0")
    fun observeByStatus(status: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun byId(id: Int): ReminderEntity?

    @Query("SELECT * FROM reminders WHERE syncStatus != 'SYNCED' ORDER BY id")
    suspend fun pending(): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: ReminderEntity): Long

    @Update suspend fun update(entity: ReminderEntity)
    @Delete suspend fun delete(entity: ReminderEntity)

    @Query("SELECT MAX(id) FROM reminders")
    suspend fun maxId(): Int?
}
