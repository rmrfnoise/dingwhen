package com.dingwhen.app.data

import com.dingwhen.app.alarm.AlarmScheduler
import com.dingwhen.app.model.NextTrigger
import com.dingwhen.app.model.RepeatType
import com.dingwhen.app.speech.AudioStore
import kotlinx.coroutines.flow.Flow
import java.time.ZonedDateTime

/**
 * 提醒的唯一写入口：任何会改变触发时刻的操作，都在这里重算 nextTriggerAt 并同步闹钟。
 */
class ReminderRepository(
    private val dao: ReminderDao,
    private val scheduler: AlarmScheduler,
    private val audioStore: AudioStore
) {

    fun observe(): Flow<List<Reminder>> = dao.observeAll()

    suspend fun get(id: Long): Reminder? = dao.getById(id)

    suspend fun getAll(): List<Reminder> = dao.getAll()

    suspend fun save(reminder: Reminder): Long {
        val scheduled = withNextTrigger(reminder)
        val newId = dao.upsert(scheduled)
        val saved = if (scheduled.id == 0L) scheduled.copy(id = newId) else scheduled
        if (saved.enabled) {
            scheduler.schedule(saved)
            // 关掉确认按钮或关掉重复，这条只响一次：把可能还挂着的重复闹钟一并撤掉
            if (!saved.actionDismiss || !saved.repeatOn) scheduler.cancelRepeats(saved.id)
        } else {
            scheduler.cancel(saved.id)
            scheduler.cancelRepeats(saved.id)
        }
        return saved.id
    }

    suspend fun setEnabled(reminder: Reminder, enabled: Boolean) {
        val updated = withNextTrigger(reminder.copy(enabled = enabled))
        dao.update(updated)
        if (enabled) {
            scheduler.schedule(updated)
        } else {
            scheduler.cancel(updated.id)
            scheduler.cancelRepeats(updated.id)
        }
    }

    suspend fun delete(reminder: Reminder) {
        scheduler.cancel(reminder.id)
        scheduler.cancelRepeats(reminder.id)
        audioStore.delete(reminder.audioFile)
        dao.delete(reminder)
    }

    /** 用户点了「知道了」：记下确认时刻，升级链据此终止。 */
    suspend fun acknowledge(reminderId: Long) {
        val reminder = dao.getById(reminderId) ?: return
        dao.update(reminder.copy(ackedAt = System.currentTimeMillis()))
        scheduler.cancelRepeats(reminderId)
    }

    /**
     * 打开应用也算"我去处理了"：把已触发但没确认的提醒一次性收掉并撤掉后续重复。
     * 打卡场景里打开应用（或从通知点开钉钉）本来就意味着人已经去了，
     * 再接着响三轮只会变成噪音。
     */
    suspend fun acknowledgePending(): Int {
        val now = System.currentTimeMillis()
        var count = 0
        dao.getAll().forEach { reminder ->
            val fired = reminder.lastFiredAt ?: 0L
            val acked = reminder.ackedAt ?: 0L
            if (fired <= 0L || acked >= fired) return@forEach
            dao.update(reminder.copy(ackedAt = now))
            scheduler.cancelRepeats(reminder.id)
            count++
        }
        return count
    }

    /** 触发之后：仅一次 -> 自动停用；周期 -> 立刻排下一次。 */
    suspend fun markFired(reminder: Reminder) {
        val nowMillis = System.currentTimeMillis()
        // 新一轮触发必须清掉上一轮的确认标记，否则升级链一启动就判定"已确认"
        if (reminder.repeatType == RepeatType.ONCE) {
            dao.update(reminder.copy(enabled = false, lastFiredAt = nowMillis, ackedAt = null))
            scheduler.cancel(reminder.id)
        } else {
            val next = withNextTrigger(reminder.copy(lastFiredAt = nowMillis, ackedAt = null))
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
