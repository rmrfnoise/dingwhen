package com.dingwhen.app.alarm

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.dingwhen.app.Graph
import com.dingwhen.app.data.Reminder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 到点触发入口。所有动作都通过显式 PendingIntent 发给自己，
 * 因此 receiver 不对外导出，避免被外部应用伪造。
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val reminderId = intent.getLongExtra(EXTRA_ID, -1L)
        if (reminderId <= 0L) return

        Graph.init(context)
        val attempt = intent.getIntExtra(EXTRA_ATTEMPT, 0)
        val pending = goAsync()
        scope.launch {
            try {
                handle(context, action, reminderId, attempt)
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun handle(context: Context, action: String, reminderId: Long, attempt: Int) {
        when (action) {
            ACTION_FIRE -> {
                val reminder = Graph.repository.get(reminderId) ?: return
                if (!reminder.enabled) return

                val notification = NotificationHelper.show(context, reminder) ?: return
                // 先落库再排重复：markFired 会清掉上一轮的确认标记
                Graph.repository.markFired(reminder)
                scheduleRepeats(reminder)
                startPlayback(context, reminderId, notification)
            }

            ACTION_REPEAT -> {
                val reminder = Graph.repository.get(reminderId) ?: return
                // 这条提醒关掉重复后，已排的闹钟可能正好在这一刻触发，这里再挡一次
                if (!reminder.repeatOn) return
                // 已经点过「知道了」就不再吵：重复闹钟仍按计划触发，但这里直接跳过
                val ackedAt = reminder.ackedAt ?: 0L
                val firedAt = reminder.lastFiredAt ?: 0L
                if (ackedAt > 0L && ackedAt >= firedAt) return

                val notification = NotificationHelper.show(context, reminder) ?: return
                startPlayback(context, reminderId, notification)
            }

            ACTION_DISMISS -> {
                // 「知道了」= 确认，终止整条升级链
                Graph.repository.acknowledge(reminderId)
                NotificationHelper.cancel(context, reminderId)
                context.stopService(Intent(context, PlaybackService::class.java))
            }
        }
    }

    /**
     * 一次性把所有重复都排好，进程被杀也不会丢——比服务里的定时器可靠。
     *
     * 只有通知上带确认按钮时才排：没有按钮就没有能让用户喊停的入口，
     * 这时持续重响等于逼人去设置里关掉整条提醒，不如只响一遍。
     */
    private fun scheduleRepeats(reminder: Reminder) {
        // 这条提醒不开重复就只响这一次，不再排后续
        if (!reminder.repeatOn) return
        // 没有确认按钮：响完一遍就结束
        if (!reminder.actionDismiss) return
        val now = System.currentTimeMillis()
        for (attempt in 1..AlarmScheduler.MAX_REPEATS) {
            val at = now + attempt * AlarmScheduler.REPEAT_INTERVAL_MILLIS
            Graph.scheduler.scheduleRepeat(reminder.id, attempt, at)
        }
    }

    private fun startPlayback(context: Context, reminderId: Long, notification: Notification) {
        val intent = Intent(context, PlaybackService::class.java).apply {
            putExtra(PlaybackService.EXTRA_ID, reminderId)
            putExtra(PlaybackService.EXTRA_NOTIFICATION, notification)
        }
        try {
            ContextCompat.startForegroundService(context, intent)
        } catch (e: Exception) {
            // 系统拒绝后台启动前台服务时不要崩：通知已经发出去了，用户仍能看到
        }
    }

    companion object {
        const val ACTION_FIRE = "com.dingwhen.app.action.FIRE"
        const val ACTION_DISMISS = "com.dingwhen.app.action.DISMISS"
        const val ACTION_REPEAT = "com.dingwhen.app.action.REPEAT"
        const val EXTRA_ID = "extra_reminder_id"
        const val EXTRA_ATTEMPT = "extra_attempt"

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
