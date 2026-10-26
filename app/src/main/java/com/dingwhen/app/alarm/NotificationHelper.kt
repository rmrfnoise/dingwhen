package com.dingwhen.app.alarm

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dingwhen.app.R
import com.dingwhen.app.data.Reminder
import com.dingwhen.app.i18n.AppSettings
import com.dingwhen.app.ui.MainActivity
import com.dingwhen.app.ui.timeText

/**
 * 通知出口。提示音使用系统默认通知铃声（跟随用户在系统设置里的选择），
 * 渠道重要性为 HIGH，锁屏可见，弹出横幅。
 */
object NotificationHelper {

    const val CHANNEL_ID = "reminder_alerts"

    /** 已经建过的渠道名，避免每条通知都往系统里写一次 */
    @Volatile
    private var cachedChannelName: String? = null

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val s = AppSettings.strings()
        if (cachedChannelName == s.channelName) return

        // 每次都创建一次：已存在的渠道只会被更新名称和描述，声音和重要性保持不变，
        // 这样切换语言后系统设置里的渠道名也跟着变。
        val channel = NotificationChannel(
            CHANNEL_ID,
            s.channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = s.channelDesc
            setSound(
                Settings.System.DEFAULT_NOTIFICATION_URI,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 200, 300)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableLights(true)
        }
        manager.createNotificationChannel(channel)
        cachedChannelName = s.channelName
    }

    fun show(context: Context, reminder: Reminder, snoozed: Boolean = false) {
        if (!canPost(context)) return
        ensureChannel(context)

        val s = AppSettings.strings()
        val repeat = s.repeatLabel(reminder.repeatType, reminder.days, reminder.dayOfMonth)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(reminder.content)
            // 正文只说周期；"到点了"这种话和通知本身重复，不如用来交代节奏
            .setContentText(if (snoozed) "${s.snoozePrefix} · $repeat" else repeat)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setContentIntent(openAppIntent(context, reminder.id))

        // 按钮完全由提醒自身的配置决定：没配就一个都不加，通知只显示内容。
        addActions(context, builder, reminder)

        try {
            NotificationManagerCompat.from(context).notify(notificationId(reminder.id), builder.build())
        } catch (e: SecurityException) {
            // 通知权限被回收，静默失败不可接受：交给列表页的权限提示兜底
        }
    }

    /**
     * 按配置追加按钮，最多 3 个，超出部分系统会折叠。
     * 「打开应用」只在包名已设置且目标应用仍安装时出现——被卸载就自动不显示，而不是留一个点了没用的按钮。
     */
    private fun addActions(context: Context, builder: NotificationCompat.Builder, reminder: Reminder) {
        val s = AppSettings.strings()
        val openPkg = reminder.actionOpenPackage
        if (!openPkg.isNullOrBlank()) {
            val openIntent = openTargetApp(context, openPkg, reminder.id)
            if (openIntent != null) {
                builder.addAction(0, s.openAppAction(appLabel(context, openPkg)), openIntent)
            }
        }
        if (reminder.actionSnooze) {
            builder.addAction(0, s.snoozeAction, broadcast(context, reminder.id, ReminderReceiver.ACTION_SNOOZE, 1))
        }
        if (reminder.actionDismiss) {
            builder.addAction(0, s.dismissAction, broadcast(context, reminder.id, ReminderReceiver.ACTION_DISMISS, 2))
        }
    }

    /** 关机期间错过的仅一次提醒，开机后补一条说明。 */
    fun showMissed(context: Context, reminder: Reminder) {
        if (!canPost(context)) return
        ensureChannel(context)
        val s = AppSettings.strings()
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(s.missedTitle(reminder.content))
            .setContentText(s.missedBody(timeText(reminder.hour, reminder.minute)))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent(context, reminder.id))
        try {
            NotificationManagerCompat.from(context)
                .notify(notificationId(reminder.id) + 900_000, builder.build())
        } catch (e: SecurityException) {
        }
    }

    fun cancel(context: Context, reminderId: Long) {
        NotificationManagerCompat.from(context).cancel(notificationId(reminderId))
    }

    private fun canPost(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }

    private fun notificationId(reminderId: Long): Int = (reminderId % 100_000L).toInt() + 1000

    private fun openAppIntent(context: Context, reminderId: Long): PendingIntent =
        PendingIntent.getActivity(
            context,
            (reminderId % 100_000L).toInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun broadcast(context: Context, reminderId: Long, action: String, slot: Int): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            (reminderId % 100_000L).toInt() * 10 + slot,
            Intent(context, ReminderReceiver::class.java).apply {
                this.action = action
                putExtra(ReminderReceiver.EXTRA_ID, reminderId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    /** 直接拉起目标应用的启动页；应用被卸载或没有启动入口时返回 null。 */
    private fun openTargetApp(context: Context, pkg: String, reminderId: Long): PendingIntent? {
        val launch = try {
            context.packageManager.getLaunchIntentForPackage(pkg)
        } catch (e: Exception) {
            null
        } ?: return null
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return PendingIntent.getActivity(
            context,
            (reminderId % 100_000L).toInt() * 10 + 3,
            launch,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** 应用名，取不到就退回包名，保证按钮文案不空。 */
    private fun appLabel(context: Context, pkg: String): String = try {
        val pm = context.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
    } catch (e: Exception) {
        pkg
    }
}