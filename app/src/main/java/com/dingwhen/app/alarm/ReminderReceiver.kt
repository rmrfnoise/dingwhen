package com.dingwhen.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dingwhen.app.Graph
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
        val pending = goAsync()
        scope.launch {
            try {
                handle(context, action, reminderId)
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun handle(context: Context, action: String, reminderId: Long) {
        when (action) {
            ACTION_FIRE -> {
                val reminder = Graph.repository.get(reminderId) ?: return
                if (!reminder.enabled) return
                NotificationHelper.show(context, reminder)
                Graph.repository.markFired(reminder)
            }

            ACTION_SNOOZE -> {
                NotificationHelper.cancel(context, reminderId)
                Graph.scheduler.snooze(reminderId)
            }

            ACTION_SNOOZE_FIRE -> {
                val reminder = Graph.repository.get(reminderId) ?: return
                NotificationHelper.show(context, reminder, snoozed = true)
            }

            ACTION_DISMISS -> {
                NotificationHelper.cancel(context, reminderId)
                Graph.scheduler.cancelSnooze(reminderId)
            }
        }
    }

    companion object {
        const val ACTION_FIRE = "com.dingwhen.app.action.FIRE"
        const val ACTION_SNOOZE = "com.dingwhen.app.action.SNOOZE"
        const val ACTION_SNOOZE_FIRE = "com.dingwhen.app.action.SNOOZE_FIRE"
        const val ACTION_DISMISS = "com.dingwhen.app.action.DISMISS"
        const val EXTRA_ID = "extra_reminder_id"

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
