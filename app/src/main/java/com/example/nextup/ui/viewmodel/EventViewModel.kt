package com.example.nextup.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nextup.alarm.MilestoneNotificationScheduler
import com.example.nextup.data.local.entity.EventCategory
import com.example.nextup.data.local.entity.EventEntity
import com.example.nextup.data.local.entity.WidgetStyle
import com.example.nextup.domain.model.CountdownTime
import com.example.nextup.domain.repository.EventRepository
import com.example.nextup.widget.WidgetPinHelper
import com.example.nextup.widget.WidgetUpdateScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventUiItem(
    val event: EventEntity,
    val countdown: CountdownTime
)

data class HomeUiState(
    val events: List<EventUiItem> = emptyList(),
    val selectedCategory: EventCategory? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class EventViewModel @Inject constructor(
    private val repository: EventRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val selectedCategoryFilter = MutableStateFlow<EventCategory?>(null)

    private val tickerFlow: Flow<Long> = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1_000L)
        }
    }

    val homeUiState: StateFlow<HomeUiState> = combine(
        repository.getAllEvents(),
        selectedCategoryFilter,
        tickerFlow
    ) { allEvents, categoryFilter, nowMillis ->
        val filtered = if (categoryFilter == null) {
            allEvents
        } else {
            allEvents.filter { it.category == categoryFilter }
        }

        val sortedItems = filtered.map { entity ->
            EventUiItem(
                event = entity,
                countdown = CountdownTime.fromTargetTimestamp(entity.targetDate, nowMillis)
            )
        }.sortedWith(
            compareBy<EventUiItem> { it.countdown.isCompleted }
                .thenBy { it.event.targetDate }
        )

        HomeUiState(
            events = sortedItems,
            selectedCategory = categoryFilter,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    fun observeEventUiItem(eventId: Long): Flow<EventUiItem?> {
        return combine(
            repository.observeEventById(eventId),
            tickerFlow
        ) { entity, nowMillis ->
            entity?.let {
                EventUiItem(
                    event = it,
                    countdown = CountdownTime.fromTargetTimestamp(it.targetDate, nowMillis)
                )
            }
        }
    }

    fun selectCategoryFilter(category: EventCategory?) {
        selectedCategoryFilter.value = category
    }

    fun saveEvent(
        id: Long = 0L,
        title: String,
        targetDate: Long,
        category: EventCategory,
        themeColor: Int,
        backgroundImageUri: String?,
        actionUrl: String?,
        actionPackage: String?,
        widgetStyle: WidgetStyle = WidgetStyle.GLASS_PILL,
        onSaved: (Long) -> Unit = {}
    ) {
        viewModelScope.launch {
            val entity = EventEntity(
                id = id,
                title = title.trim(),
                targetDate = targetDate,
                category = category,
                themeColor = themeColor,
                backgroundImageUri = backgroundImageUri,
                actionUrl = actionUrl?.ifBlank { category.defaultActionUrl } ?: category.defaultActionUrl,
                actionPackage = actionPackage?.ifBlank { category.defaultActionPackage } ?: category.defaultActionPackage,
                widgetStyle = widgetStyle
            )

            val savedId = if (id == 0L) {
                repository.addEvent(entity)
            } else {
                repository.updateEvent(entity)
                id
            }

            val savedEntity = entity.copy(id = savedId)

            MilestoneNotificationScheduler.scheduleMilestonesForEvent(appContext, savedEntity)
            WidgetUpdateScheduler.refreshAllWidgetsAndScheduleNext(appContext)

            onSaved(savedId)
        }
    }

    fun updateWidgetCustomization(
        event: EventEntity,
        widgetStyle: WidgetStyle = event.widgetStyle,
        themeColor: Int = event.themeColor,
        widgetOpacity: Float = event.widgetOpacity,
        showProgress: Boolean = event.widgetShowProgress,
        showDate: Boolean = event.widgetShowDate
    ) {
        viewModelScope.launch {
            val updated = event.copy(
                widgetStyle = widgetStyle,
                themeColor = themeColor,
                widgetOpacity = widgetOpacity,
                widgetShowProgress = showProgress,
                widgetShowDate = showDate
            )
            repository.updateEvent(updated)
            WidgetUpdateScheduler.refreshAllWidgetsAndScheduleNext(appContext)
        }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch {
            MilestoneNotificationScheduler.cancelMilestonesForEvent(appContext, event.id)
            repository.deleteEvent(event)
            WidgetUpdateScheduler.refreshAllWidgetsAndScheduleNext(appContext)
        }
    }

    fun requestPinWidget(eventId: Long) {
        WidgetPinHelper.requestPinNextUpWidget(appContext, eventId)
    }
}
