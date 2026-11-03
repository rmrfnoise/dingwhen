package com.dingwhen.app.ui

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

/**
 * 权限项：只说"哪一项、开没开、怎么去开"。
 * 文案全部交给语言包，这个对象在系统回调里也能安全调用。
 */
data class PermissionEntry(
    val key: String,
    val granted: Boolean,
    val action: () -> Unit,
    /** 系统没有公开 API 的项（如自启动），只能引导用户手动确认 */
    val manual: Boolean = false
)

object Permissions {

    fun notificationsEnabled(context: Context): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    fun exactAlarmEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val manager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false
        return manager.canScheduleExactAlarms()
    }

    fun batteryUnrestricted(context: Context): Boolean {
        val power = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
        return power.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun build(context: Context): List<PermissionEntry> = listOf(
        PermissionEntry(
            key = "notification",
            granted = notificationsEnabled(context),
            action = { openNotificationSettings(context) }
        ),
        PermissionEntry(
            key = "exact_alarm",
            granted = exactAlarmEnabled(context),
            action = { openExactAlarmSettings(context) }
        ),
        PermissionEntry(
            key = "battery",
            granted = batteryUnrestricted(context),
            action = { requestIgnoreBatteryOptimizations(context) }
        ),
        PermissionEntry(
            key = "autostart",
            granted = false,
            action = { openAppDetails(context) },
            manual = true
        )
    )

    fun openNotificationSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (!tryStart(context, intent)) openAppDetails(context)
    }

    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (tryStart(context, intent)) return
        }
        openAppDetails(context)
    }

    fun requestIgnoreBatteryOptimizations(context: Context) {
        val direct = Intent(
            Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (tryStart(context, direct)) return
        val list = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        tryStart(context, list)
    }

    fun openAppDetails(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        tryStart(context, intent)
    }

    private fun tryStart(context: Context, intent: Intent): Boolean = try {
        context.startActivity(intent)
        true
    } catch (e: Exception) {
        false
    }
}
