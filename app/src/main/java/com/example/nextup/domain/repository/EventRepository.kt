package com.example.nextup.domain.repository

import com.example.nextup.data.local.entity.EventCategory
import com.example.nextup.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun getAllEvents(): Flow<List<EventEntity>>
    fun getActiveEvents(currentTimeMillis: Long): Flow<List<EventEntity>>
    fun getEventsByCategory(category: EventCategory): Flow<List<EventEntity>>
    fun observeEventById(eventId: Long): Flow<EventEntity?>
    suspend fun getEventById(eventId: Long): EventEntity?
    suspend fun getAllEventsOneShot(): List<EventEntity>
    suspend fun addEvent(event: EventEntity): Long
    suspend fun updateEvent(event: EventEntity)
    suspend fun deleteEvent(event: EventEntity)
    suspend fun deleteEventById(eventId: Long)
}
