package com.ghostkey.data.history

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM transform_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<TransformSession>>

    @Query("SELECT * FROM transform_events WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getEventsForSession(sessionId: String): Flow<List<TransformEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TransformSession)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: TransformEvent)

    @Update
    suspend fun updateSession(session: TransformSession)

    @Query("DELETE FROM transform_sessions WHERE startedAt < :before")
    suspend fun purgeSessionsBefore(before: Long)

    @Query("DELETE FROM transform_events WHERE sessionId NOT IN (SELECT id FROM transform_sessions)")
    suspend fun purgeOrphanedEvents()
}
