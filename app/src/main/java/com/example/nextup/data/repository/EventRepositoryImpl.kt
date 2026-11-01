package com.example.nextup.data.repository

import com.example.nextup.data.local.dao.EventDao
import com.example.nextup.data.local.entity.EventCategory
import com.example.nextup.data.local.entity.EventEntity
import com.example.nextup.domain.repository.EventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao
) : EventRepository {

    override fun getAllEvents(): Flow<List<EventEntity>> =
        eventDao.getAllEventsSortedByNearest()

    override fun getActiveEvents(currentTimeMillis: Long): Flow<List<EventEntity>> =
        eventDao.getActiveEvents(minTimestamp = currentTimeMillis)

    override fun getEventsByCategory(category: EventCategory): Flow<List<EventEntity>> =
        eventDao.getEventsByCategory(category = category)

    override fun observeEventById(eventId: Long): Flow<EventEntity?> =
        eventDao.observeEventById(eventId = eventId)

    override suspend fun getEventById(eventId: Long): EventEntity? =
        eventDao.getEventById(eventId = eventId)

    override suspend fun getAllEventsOneShot(): List<EventEntity> =
        eventDao.getAllEventsOneShot()

    override suspend fun addEvent(event: EventEntity): Long =
        eventDao.insertEvent(event = event)

    override suspend fun updateEvent(event: EventEntity) =
        eventDao.updateEvent(event = event)

    override suspend fun deleteEvent(event: EventEntity) =
        eventDao.deleteEvent(event = event)

    override suspend fun deleteEventById(eventId: Long) =
        eventDao.deleteEventById(eventId = eventId)
}
