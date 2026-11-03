package com.dingwhen.app.alarm

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.dingwhen.app.Graph
import com.dingwhen.app.i18n.AppSettings
import com.dingwhen.app.speech.SpeechPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 负责"出声"的短时前台服务。
 *
 * 为什么必须是前台服务：广播接收器的存活时间只有十几秒，而播两遍语音加间隔要十来秒，
 * 用 goAsync 容易被系统掐断；前台服务能稳定活到播完，播完立刻退出，不留后台常驻。
 *
 * 启动来源是精确闹钟的 PendingIntent，属于系统允许后台启动前台服务的豁免场景。
 */
class PlaybackService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Graph.init(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 用户打开应用：停止播报，并把前台通知一起撤掉。
        // 只调 stopService() 不够——前台服务的通知在服务结束前取消不掉，
        // 服务收尾时还会用 DETACH 把它留在通知栏。
        if (intent?.action == ACTION_STOP) {
            job?.cancel()
            stopForeground(STOP_FOREGROUND_REMOVE)
            NotificationHelper.cancelAll(this)
            stopSelf()
            return START_NOT_STICKY
        }

        val reminderId = intent?.getLongExtra(EXTRA_ID, -1L) ?: -1L
        val notification = intent?.notificationExtra()
        if (reminderId <= 0L || notification == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        // 复用通知栏上那条提醒，而不是再发一条"正在播报"
        startForeground(NotificationHelper.notificationId(reminderId), notification)

        // 正在播就忽略新的触发：重复间隔三分钟，真撞上说明已经在提醒了，
        // 再叠一层只会两段声音打架
        if (job?.isActive == true) return START_NOT_STICKY

        job = scope.launch {
            try {
                play(reminderId)
            } finally {
                detachAndStop()
            }
        }
        return START_NOT_STICKY
    }

    /**
     * 播报顺序：本地音频 → 系统 TTS → 长振动。
     * 本地音频是云端音色（需密钥）；没填密钥或没合成成功时由系统 TTS 顶上——
     * 音质一般，但"不申请密钥也能听见"比"干脆没声"重要。
     * 每轮念几遍由这条提醒自己决定（默认 1 遍，靠后续重复提醒补，而不是当场把人惹烦）。
     */
    private suspend fun play(reminderId: Long) {
        val reminder = Graph.repository.get(reminderId) ?: return
        val config = AppSettings.speechConfig()
        val times = reminder.playTimes.coerceIn(1, 3)

        SpeechPlayer.requestAlarmFocus(this)
        try {
            SpeechPlayer.vibrate(this, strong = false)

            val file = Graph.audioStore.file(reminder.audioFile)
            if (file != null && SpeechPlayer.playFile(file, times = times)) return

            // 用户主动关掉了语音播报：只留通知和振动，不擅自出声
            if (!config.enabled) return

            val text = reminder.speechText?.takeIf { it.isNotBlank() } ?: reminder.content
            var spoken = false
            for (index in 0 until times) {
                if (!SpeechPlayer.speakText(this, text)) break
                spoken = true
                if (index < times - 1) delay(1_200)
            }
            if (spoken) return

            SpeechPlayer.vibrate(this, strong = true)
        } finally {
            SpeechPlayer.abandonFocus(this)
        }
    }

    private fun detachAndStop() {
        // DETACH：播完把通知留在通知栏让用户仍能确认，而不是一起撤掉
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_DETACH)
        }
        stopSelf()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_ID = "extra_playback_id"
        const val EXTRA_NOTIFICATION = "extra_playback_notification"

        /** 打开应用时发给服务的指令：停止播报并移除通知 */
        const val ACTION_STOP = "com.dingwhen.app.action.STOP_PLAYBACK"
    }
}

@Suppress("DEPRECATION")
private fun Intent.notificationExtra(): Notification? =
    getParcelableExtra(PlaybackService.EXTRA_NOTIFICATION) as? Notification
