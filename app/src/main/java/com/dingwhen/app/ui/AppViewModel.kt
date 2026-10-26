package com.dingwhen.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dingwhen.app.Graph
import com.dingwhen.app.data.Reminder
import com.dingwhen.app.model.Draft
import com.dingwhen.app.model.NextTrigger
import com.dingwhen.app.model.RepeatType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZonedDateTime

enum class Route { LIST, EDITOR, PERMISSIONS }

class AppViewModel : ViewModel() {

    private val repository = Graph.repository

    // 初始值是 null 而不是空表：冷启动还没读到数据库时不要先闪一下"还没有提醒"
    val reminders: StateFlow<List<Reminder>?> = repository.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _route = MutableStateFlow(Route.LIST)
    val route: StateFlow<Route> = _route.asStateFlow()

    private val _draft = MutableStateFlow(Draft())
    val draft: StateFlow<Draft> = _draft.asStateFlow()

    fun openManualCreate() {
        _draft.value = Draft()
        _route.value = Route.EDITOR
    }

    fun openPermissions() {
        _route.value = Route.PERMISSIONS
    }

    fun backToList() {
        _route.value = Route.LIST
    }

    fun edit(reminder: Reminder) {
        _draft.value = Draft(
            id = reminder.id,
            content = reminder.content,
            hour = reminder.hour,
            minute = reminder.minute,
            repeat = reminder.repeatType,
            date = reminder.date ?: LocalDate.now(),
            days = reminder.days,
            dayOfMonth = reminder.dayOfMonth ?: LocalDate.now().dayOfMonth,
            actionOpenPackage = reminder.actionOpenPackage,
            actionSnooze = reminder.actionSnooze,
            actionDismiss = reminder.actionDismiss
        )
        _route.value = Route.EDITOR
    }

    fun updateDraft(block: (Draft) -> Draft) {
        _draft.value = block(_draft.value)
    }

    fun saveDraft() {
        val draft = _draft.value
        if (!draft.isValid()) return
        val hour = draft.hour ?: return
        val minute = draft.minute ?: return

        viewModelScope.launch {
            val next = NextTrigger.next(
                now = ZonedDateTime.now(),
                hour = hour,
                minute = minute,
                repeat = draft.repeat,
                days = draft.days,
                dayOfMonth = draft.dayOfMonth,
                date = if (draft.repeat == RepeatType.ONCE) draft.date else null
            )
            val entity = Reminder(
                id = draft.id ?: 0L,
                content = draft.content.trim(),
                hour = hour,
                minute = minute,
                repeatType = draft.repeat,
                daysOfWeek = draft.days.joinToString(",") { it.value.toString() },
                dayOfMonth = if (draft.repeat == RepeatType.MONTHLY) draft.dayOfMonth else null,
                dateEpochDay = if (draft.repeat == RepeatType.ONCE) draft.date.toEpochDay() else null,
                enabled = true,
                nextTriggerAt = next.toInstant().toEpochMilli(),
                actionOpenPackage = draft.actionOpenPackage,
                actionSnooze = draft.actionSnooze,
                actionDismiss = draft.actionDismiss
            )
            repository.save(entity)
            _route.value = Route.LIST
        }
    }

    fun toggle(reminder: Reminder, enabled: Boolean) {
        viewModelScope.launch { repository.setEnabled(reminder, enabled) }
    }

    fun delete(reminder: Reminder) {
        viewModelScope.launch {
            repository.delete(reminder)
            _route.value = Route.LIST
        }
    }

    fun deleteCurrentDraft() {
        val id = _draft.value.id ?: return
        viewModelScope.launch {
            repository.get(id)?.let { repository.delete(it) }
            _route.value = Route.LIST
        }
    }

    /** 权限变化后重新对齐所有闹钟。 */
    fun syncSchedule() {
        viewModelScope.launch { repository.rescheduleAll() }
    }
}
