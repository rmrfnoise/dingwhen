package com.dingwhen.app

import android.app.Application
import android.content.Context
import com.dingwhen.app.alarm.AlarmScheduler
import com.dingwhen.app.alarm.NotificationHelper
import com.dingwhen.app.data.AppDatabase
import com.dingwhen.app.data.ReminderRepository
import com.dingwhen.app.i18n.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 极简手写依赖容器：Application 与 BroadcastReceiver 共用同一份实例，
 * 避免在 Receiver 里重复建库。
 */
object Graph {

    @Volatile
    private var appContext: Context? = null

    fun init(context: Context) {
        if (appContext == null) {
            appContext = context.applicationContext
            // 通知文案要按用户选的语言出，Receiver 里也得能用上，所以在这里一起初始化
            AppSettings.init(context)
        }
    }

    private val ctx: Context
        get() = requireNotNull(appContext) { "Graph 未初始化" }

    val database: AppDatabase by lazy { AppDatabase.get(ctx) }

    val scheduler: AlarmScheduler by lazy { AlarmScheduler(ctx) }

    val repository: ReminderRepository by lazy {
        ReminderRepository(database.reminderDao(), scheduler)
    }
}

class DingWhenApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Graph.init(this)
        NotificationHelper.ensureChannel(this)
        // 每次冷启动都对齐一次调度：能自愈被系统清理、权限变化导致的漏排
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            Graph.repository.rescheduleAll()
        }
    }
}
