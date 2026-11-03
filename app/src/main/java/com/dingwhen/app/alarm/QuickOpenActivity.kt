package com.dingwhen.app.alarm

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.dingwhen.app.Graph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * 通知按钮「打开钉钉」的透明中转页。
 *
 * 为什么需要它：这个按钮原本直接 PendingIntent 跳到目标应用，我们就收不到
 * "用户已经去打卡了"这个信号，点完钉钉，后面三轮还会继续响。
 * 现在先经过这里——记确认、停播报、再把目标应用拉起来，自己立刻退出。
 *
 * 用透明主题且不在任务栈里留痕，用户看到的就是"点了按钮，钉钉开了"。
 */
class QuickOpenActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Graph.init(applicationContext)

        val reminderId = intent.getLongExtra(EXTRA_ID, -1L)
        val pkg = intent.getStringExtra(EXTRA_PKG)

        // 先把目标应用拉起来，别让用户等我们写数据库
        if (!pkg.isNullOrBlank()) {
            val launch = try {
                packageManager.getLaunchIntentForPackage(pkg)
            } catch (e: Exception) {
                null
            }
            if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                runCatching { startActivity(launch) }
            }
        }

        if (reminderId > 0L) {
            // 打开目标应用 = 已经处理过了：确认、停播报、并像「确认」按钮一样撤掉这条通知。
            // 通知在前台服务里挂着时 cancel() 会被忽略，所以先让服务 REMOVE 收尾。
            NotificationHelper.cancel(this, reminderId)
            runCatching {
                startService(
                    Intent(this, PlaybackService::class.java)
                        .setAction(PlaybackService.ACTION_STOP)
                )
            }
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                runCatching { Graph.repository.acknowledge(reminderId) }
            }
        }

        finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, 0)
    }

    companion object {
        const val EXTRA_ID = "extra_quick_open_id"
        const val EXTRA_PKG = "extra_quick_open_pkg"
    }
}
