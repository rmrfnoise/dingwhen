package com.dingwhen.app.alarm

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dingwhen.app.R
import com.dingwhen.app.data.Reminder
import com.dingwhen.app.i18n.AppSettings
import com.dingwhen.app.ui.MainActivity
import com.dingwhen.app.ui.timeText

/**
 * 通知出口。
 *
 * 声音不在这里出：通知渠道是静音的，朗读由 PlaybackService 用闹钟音频流播放。
 * 原因有两个——渠道声音是全应用一个，做不到"每条提醒读不同的话"；
 * 而且渠道声音走通知流，静音模式下会被系统直接压制。
 */
object NotificationHelper {

    /** 旧渠道带声音且无法改属性，只能换 id。 */
    const val CHANNEL_ID = "reminder_alerts_v2"
    private const val LEGACY_CHANNEL_ID = "reminder_alerts"

    @Volatile
    private var cachedChannelName: String? = null

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val s = AppSettings.strings()
        if (cachedChannelName == s.channelName) return

        // 每次建一次：已存在的渠道只会更新名称和描述，重要性保持不变，切语言后名字也跟着变
        val channel = NotificationChannel(
            CHANNEL_ID,
            s.channelName,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = s.channelDesc
            // 静音：声音由 PlaybackService 负责，避免和通知音重叠
            setSound(null, null)
            enableVibration(false)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableLights(true)
        }
        manager.createNotificationChannel(channel)
        runCatching { manager.deleteNotificationChannel(LEGACY_CHANNEL_ID) }
        cachedChannelName = s.channelName
    }

    /** 供 PlaybackService 启动前台服务时复用同一份通知，避免出现两条 */
    fun build(context: Context, reminder: Reminder): Notification {
        val s = AppSettings.strings()
        val repeat = s.repeatLabel(reminder.repeatType, reminder.days, reminder.dayOfMonth)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(reminder.content)
            .setContentText(repeat)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setContentIntent(openAppIntent(context, reminder.id))

        addActions(context, builder, reminder)
        return builder.build()
    }

    fun show(context: Context, reminder: Reminder): Notification? {
        if (!canPost(context)) return null
        ensureChannel(context)
        val notification = build(context, reminder)
        return try {
            NotificationManagerCompat.from(context).notify(notificationId(reminder.id), notification)
            notification
        } catch (e: SecurityException) {
            null
        }
    }

    /**
     * 按配置追加按钮，最多 3 个。
     * 「知道了」不只是清通知——它同时终止升级链，是打卡场景里唯一能提前喊停的入口，
     * 所以按钮文字可以自定义：提醒内容是"打卡了吗？"，按钮就不该也叫"知道了"。
     */
    private fun addActions(
        context: Context,
        builder: NotificationCompat.Builder,
        reminder: Reminder
    ) {
        val s = AppSettings.strings()
        val openPkg = reminder.actionOpenPackage
        if (!openPkg.isNullOrBlank()) {
            val openIntent = openTargetApp(context, openPkg, reminder.id)
            if (openIntent != null) {
                builder.addAction(0, s.openAppAction(appLabel(context, openPkg)), openIntent)
            }
        }
        if (reminder.actionDismiss) {
            val label = reminder.actionDismissLabel?.takeIf { it.isNotBlank() } ?: s.dismissAction
            builder.addAction(
                0,
                label,
                broadcast(context, reminder.id, ReminderReceiver.ACTION_DISMISS, 2)
            )
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

    /**
     * 打开应用等同于点「知道了」：把本应用挂出的提醒通知（含错过的补发）一次性清掉。
     * 否则人已经进应用确认过了，通知栏里那条还挂着，看起来像没处理。
     */
    fun cancelAll(context: Context) {
        runCatching { NotificationManagerCompat.from(context).cancelAll() }
    }

    fun notificationId(reminderId: Long): Int = (reminderId % 100_000L).toInt() + 1000

    private fun canPost(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }

    private fun openAppIntent(context: Context, reminderId: Long): PendingIntent =
        PendingIntent.getActivity(
            context,
            (reminderId % 100_000L).toInt(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun broadcast(
        context: Context,
        reminderId: Long,
        action: String,
        slot: Int
    ): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            (reminderId % 100_000L).toInt() * 10 + slot,
            Intent(context, ReminderReceiver::class.java).apply {
                this.action = action
                putExtra(ReminderReceiver.EXTRA_ID, reminderId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    /**
     * 「打开应用」按钮走一层透明中转页：先记下"用户已经去打卡了"，
     * 再把目标应用拉起来。直接跳过去的话我们观察不到这次点击，后续三轮还会接着响。
     * 应用被卸载或没有启动入口时返回 null（不显示这个按钮）。
     */
    private fun openTargetApp(context: Context, pkg: String, reminderId: Long): PendingIntent? {
        val launch = try {
            context.packageManager.getLaunchIntentForPackage(pkg)
        } catch (e: Exception) {
            null
        } ?: return null
        return PendingIntent.getActivity(
            context,
            (reminderId % 100_000L).toInt() * 10 + 3,
            Intent(context, QuickOpenActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
                putExtra(QuickOpenActivity.EXTRA_ID, reminderId)
                putExtra(QuickOpenActivity.EXTRA_PKG, pkg)
            },
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
