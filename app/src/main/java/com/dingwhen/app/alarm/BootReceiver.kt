package com.dingwhen.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dingwhen.app.Graph
import com.dingwhen.app.model.RepeatType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 重启恢复 & 时区/时间变更恢复。
 * 这是"重启不丢"的关键一环：AlarmManager 的闹钟在关机后全部丢失，必须重新登记。
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action !in HANDLED_ACTIONS) return

        Graph.init(context)
        val pending = goAsync()
        scope.launch {
            try {
                if (action == Intent.ACTION_BOOT_COMPLETED) {
                   repairMissedOnce(context)
                }
                Graph.repository.rescheduleAll()
            } finally {
                pending.finish()
            }
        }
    }

    /**
     * 关机期间到点的"仅一次"提醒：12 小时内补一条说明，超过就静默停用。
     * 周期提醒不补发——下一次自然会发生。
     */
    private suspend fun repairMissedOnce(context: Context) {
        val nowMillis = System.currentTimeMillis()
        val window = 12 * 60 * 60 * 1000L
        Graph.repository.getAll()
            .filter {
                it.enabled &&
                    it.repeatType == RepeatType.ONCE &&
                    it.nextTriggerAt in 1..nowMillis
            }
            .forEach { overdue ->
                if (nowMillis - overdue.nextTriggerAt <= window) {
                    NotificationHelper.showMissed(context, overdue)
                }
                Graph.repository.markFired(overdue)
            }
    }

    private companion object {
        val HANDLED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
        )
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
