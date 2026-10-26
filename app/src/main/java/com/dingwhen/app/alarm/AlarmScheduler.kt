package com.dingwhen.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.dingwhen.app.data.Reminder
import com.dingwhen.app.ui.MainActivity

/**
 * 闹钟调度。统一使用 setAlarmClock：Doze 下依然准时，锁屏也会显示闹钟图标，
 * 是 Android 上留给"必须准时"场景（闹钟/提醒）的正式通道。
 * 若精确闹钟权限被回收，则降级为 setAndAllowWhileIdle，并在列表页顶部提示用户。
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun canScheduleExact(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) alarmManager.canScheduleExactAlarms() else true

    fun schedule(reminder: Reminder) {
        val triggerAt = reminder.nextTriggerAt
        if (triggerAt <= 0L) return

        val fireIntent = PendingIntent.getBroadcast(
            context,
            requestCode(reminder.id),
            Intent(context, ReminderReceiver::class.java).apply {
                action = ReminderReceiver.ACTION_FIRE
                putExtra(ReminderReceiver.EXTRA_ID, reminder.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = PendingIntent.getActivity(
            context,
            requestCode(reminder.id),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (canScheduleExact()) {
                alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, showIntent), fireIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, fireIntent)
            }
        } catch (e: SecurityException) {
            // 权限在运行中被回收：降级，绝不静默失败
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, fireIntent)
        }
    }

    fun cancel(reminderId: Long) {
        val fireIntent = PendingIntent.getBroadcast(
            context,
            requestCode(reminderId),
            Intent(context, ReminderReceiver::class.java).apply {
                action = ReminderReceiver.ACTION_FIRE
                putExtra(ReminderReceiver.EXTRA_ID, reminderId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(fireIntent)
        fireIntent.cancel()
    }

    /** 延后 10 分钟：独立通道，不影响周期提醒本身的计划。 */
    fun snooze(reminderId: Long, delayMillis: Long = 10 * 60 * 1000L) {
        val triggerAt = System.currentTimeMillis() + delayMillis
        val intent = snoozeIntent(reminderId)
        try {
            if (canScheduleExact()) {
                alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, null), intent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, intent)
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, intent)
        }
    }

    /** 「知道了」时连待触发的稍后提醒一起撤掉，否则清掉的十分钟后又弹回来。 */
    fun cancelSnooze(reminderId: Long) {
        val intent = snoozeIntent(reminderId)
        alarmManager.cancel(intent)
        intent.cancel()
    }

    private fun snoozeIntent(reminderId: Long): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            SNOOZE_OFFSET + requestCode(reminderId),
            Intent(context, ReminderReceiver::class.java).apply {
                action = ReminderReceiver.ACTION_SNOOZE_FIRE
                putExtra(ReminderReceiver.EXTRA_ID, reminderId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun requestCode(id: Long): Int = (id % 100_000L).toInt()

    private companion object {
        const val SNOOZE_OFFSET = 500_000
    }
}
