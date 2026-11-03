package com.dingwhen.app.ui

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import com.dingwhen.app.Graph
import com.dingwhen.app.alarm.NotificationHelper
import com.dingwhen.app.alarm.PlaybackService
import com.dingwhen.app.data.Reminder
import com.dingwhen.app.i18n.AppSettings
import com.dingwhen.app.i18n.ThemeMode
import com.dingwhen.app.model.RepeatType
import com.dingwhen.app.ui.theme.DingWhenTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Graph.init(applicationContext)
        AppSettings.init(applicationContext)
        NotificationHelper.ensureChannel(this)

        // 仅调试包：截图验证用的示例数据。正式包 debugabble=false，永远不会走到这里。
        if (intent?.getBooleanExtra("seed_demo", false) == true &&
            (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        ) {
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                if (Graph.repository.getAll().isEmpty()) seedDemoData()
            }
        }

        enableEdgeToEdge()
        setContent {
            val mode by AppSettings.theme.collectAsState()
            val lang by AppSettings.lang.collectAsState()
            val dark = when (mode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // 状态栏/导航栏图标跟着主题反色，否则深色页面上看不见系统图标
            SideEffect {
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.isAppearanceLightStatusBars = !dark
                controller.isAppearanceLightNavigationBars = !dark
            }

            DingWhenTheme(darkTheme = dark, lang = lang) {
                AppRoot()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 打开应用本身就算处理过了：先停播报，再记确认，最后对齐调度。
        // 只在"已触发未确认"时才动手，平时进来看列表不会有副作用。
        // 让还在播报的服务自己收尾并撤掉前台通知——单靠 stopService() 撤不掉那条通知
        runCatching {
            startService(
                Intent(this, PlaybackService::class.java).setAction(PlaybackService.ACTION_STOP)
            )
        }
        // 进到应用就当作已确认：通知栏上那条提醒也一起收掉
        NotificationHelper.cancelAll(this)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            runCatching { Graph.repository.acknowledgePending() }
            // 回到前台时对齐一次调度，防止系统清理或权限变化导致漏排
            Graph.repository.rescheduleAll()
        }
    }

    /** 截图用的示例数据；只在可调试包里被调用。 */
    private suspend fun seedDemoData() {
        val demos = listOf(
            Reminder(
                content = "上班打卡",
                hour = 8,
                minute = 30,
                repeatType = RepeatType.WEEKDAYS
            ),
            Reminder(
                content = "喝水",
                hour = 10,
                minute = 0,
                repeatType = RepeatType.DAILY
            ),
            Reminder(
                content = "还信用卡",
                hour = 20,
                minute = 0,
                repeatType = RepeatType.MONTHLY,
                dayOfMonth = 5
            ),
            Reminder(
                content = "吃药",
                hour = 21,
                minute = 30,
                repeatType = RepeatType.DAILY,
                enabled = false
            )
        )
        demos.forEach { Graph.repository.save(it) }
    }
}
