package com.dingwhen.app.data

import com.dingwhen.app.alarm.AlarmScheduler
import com.dingwhen.app.model.NextTrigger
import com.dingwhen.app.model.RepeatType
import kotlinx.coroutines.flow.Flow
import java.time.ZonedDateTime

/**
 * 提醒的唯一写入口：任何会改变触发时刻的操作，都在这里重算 nextTriggerAt 并同步闹钟。
 */
class ReminderRepository(
    private val dao: ReminderDao,
    private val scheduler: AlarmScheduler
) {

    fun observe(): Flow<List<Reminder>> = dao.observeAll()

    suspend fun get(id: Long): Reminder? = dao.getById(id)

    suspend fun getAll(): List<Reminder> = dao.getAll()

    suspend fun save(reminder: Reminder): Long {
        val scheduled = withNextTrigger(reminder)
        val newId = dao.upsert(scheduled)
        val saved = if (scheduled.id == 0L) scheduled.copy(id = newId) else scheduled
        if (saved.enabled) scheduler.schedule(saved) else scheduler.cancel(saved.id)
        return saved.id
    }

    suspend fun setEnabled(reminder: Reminder, enabled: Boolean) {
        val updated = withNextTrigger(reminder.copy(enabled = enabled))
        dao.update(updated)
        if (enabled) scheduler.schedule(updated) else scheduler.cancel(updated.id)
    }

    suspend fun delete(reminder: Reminder) {
        scheduler.cancel(reminder.id)
        dao.delete(reminder)
    }

    /** 触发之后：仅一次 -> 自动停用；周期 -> 立刻排下一次。 */
    suspend fun markFired(reminder: Reminder) {
        val nowMillis = System.currentTimeMillis()
        if (reminder.repeatType == RepeatType.ONCE) {
            dao.update(reminder.copy(enabled = false, lastFiredAt = nowMillis))
            scheduler.cancel(reminder.id)
        } else {
            val next = withNextTrigger(reminder.copy(lastFiredAt = nowMillis))
            dao.update(next)
            scheduler.schedule(next)
        }
    }

    /** 开机 / 换时区 / 权限变化 / 冷启动时，把所有启用项重新对齐。 */
    suspend fun rescheduleAll(): Int {
        val now = ZonedDateTime.now()
        val nowMillis = now.toInstant().toEpochMilli()
        var scheduled = 0
        dao.getAll().forEach { reminder ->
            if (!reminder.enabled) {
                scheduler.cancel(reminder.id)
                return@forEach
            }
            val computed = NextTrigger.next(
                now = now,
                hour = reminder.hour,
                minute = reminder.minute,
                repeat = reminder.repeatType,
                days = reminder.days,
                dayOfMonth = reminder.dayOfMonth,
                date = reminder.date
            ).toInstant().toEpochMilli()

            val target = if (reminder.nextTriggerAt > nowMillis) reminder.nextTriggerAt else computed
            val updated = reminder.copy(nextTriggerAt = target)
            if (updated.nextTriggerAt != reminder.nextTriggerAt) dao.update(updated)
            scheduler.schedule(updated)
            scheduled++
        }
        return scheduled
    }

    private fun withNextTrigger(reminder: Reminder): Reminder {
        val next = NextTrigger.next(
            now = ZonedDateTime.now(),
            hour = reminder.hour,
            minute = reminder.minute,
            repeat = reminder.repeatType,
            days = reminder.days,
            dayOfMonth = reminder.dayOfMonth,
            date = reminder.date
        )
        return reminder.copy(nextTriggerAt = next.toInstant().toEpochMilli())
    }
}
