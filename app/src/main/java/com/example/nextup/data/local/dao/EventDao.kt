package com.example.nextup.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.nextup.data.local.entity.EventCategory
import com.example.nextup.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Query("SELECT * FROM events ORDER BY target_date ASC")
    fun getAllEventsSortedByNearest(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE target_date >= :minTimestamp ORDER BY target_date ASC")
    fun getActiveEvents(minTimestamp: Long): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE category = :category ORDER BY target_date ASC")
    fun getEventsByCategory(category: EventCategory): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :eventId LIMIT 1")
    fun observeEventById(eventId: Long): Flow<EventEntity?>

    @Query("SELECT * FROM events WHERE id = :eventId LIMIT 1")
    suspend fun getEventById(eventId: Long): EventEntity?

    @Query("SELECT * FROM events ORDER BY target_date ASC")
    suspend fun getAllEventsOneShot(): List<EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEventById(eventId: Long)
}
