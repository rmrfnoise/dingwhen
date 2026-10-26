package com.dingwhen.app.ui

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
import com.dingwhen.app.i18n.AppSettings
import com.dingwhen.app.i18n.ThemeMode
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
        // 回到前台时对齐一次调度，防止系统清理或权限变化导致漏排
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            Graph.repository.rescheduleAll()
        }
    }
}
