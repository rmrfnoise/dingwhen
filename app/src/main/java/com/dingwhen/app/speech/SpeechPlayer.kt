package com.dingwhen.app.speech

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.util.Locale

/**
 * 播报的三级兜底：本地音频文件 → 系统 TTS → 长振动。
 *
 * 全部走 USAGE_ALARM：这是 Android 上唯一不受静音/振动模式压制的通道，
 * 音量跟随系统闹钟音量，应用不覆盖——用户按侧键调一次就长期有效。
 */
object SpeechPlayer {

    private val focusListener = AudioManager.OnAudioFocusChangeListener { }

    suspend fun playFile(file: File, times: Int = 1): Boolean = withContext(Dispatchers.IO) {
        var played = false
        repeat(times) { index ->
            val ok = playOnce(file)
            if (!ok) return@withContext played
            played = true
            // 两遍之间留一点空，避免连读听不清
            if (index < times - 1) delay(1_200)
        }
        played
    }

    private suspend fun playOnce(file: File): Boolean = withContext(Dispatchers.IO) {
        val done = CompletableDeferred<Boolean>()
        var player: MediaPlayer? = null
        try {
            player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                setDataSource(file.absolutePath)
                setOnCompletionListener { if (!done.isCompleted) done.complete(true) }
                setOnErrorListener { _, _, _ ->
                    if (!done.isCompleted) done.complete(false)
                    true
                }
                prepare()
                start()
            }
            // 超时也算播过：文件坏了会让 await 一直挂着，不能把服务卡死
            withTimeoutOrNull(20_000) { done.await() } ?: true
        } catch (e: Exception) {
            false
        } finally {
            runCatching { player?.release() }
        }
    }

    /** 系统自带 TTS，离线可用，语气机械但读的是同一句话 */
    suspend fun speakText(context: Context, text: String): Boolean {
        if (text.isBlank()) return false
        return withContext(Dispatchers.Main) {
            val ready = CompletableDeferred<Boolean>()
            var engine: TextToSpeech? = null
            engine = TextToSpeech(context.applicationContext) { status ->
                if (!ready.isCompleted) ready.complete(status == TextToSpeech.SUCCESS)
            }
            val initialized = withTimeoutOrNull(3_000) { ready.await() } ?: false
            val tts = engine
            if (!initialized || tts == null) {
                runCatching { tts?.shutdown() }
                return@withContext false
            }

            try {
                val lang = tts.setLanguage(Locale.getDefault())
                if (lang == TextToSpeech.LANG_MISSING_DATA || lang == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(Locale.CHINA)
                }
                tts.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )

                val done = CompletableDeferred<Boolean>()
                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) = Unit
                    override fun onDone(utteranceId: String?) {
                        if (!done.isCompleted) done.complete(true)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        if (!done.isCompleted) done.complete(false)
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        if (!done.isCompleted) done.complete(false)
                    }
                })

                val queued = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dingwhen_tts")
                if (queued != TextToSpeech.SUCCESS) {
                    false
                } else {
                    withTimeoutOrNull(15_000) { done.await() } ?: true
                }
            } catch (e: Exception) {
                false
            } finally {
                runCatching { tts.shutdown() }
            }
        }
    }

    /**
     * 振动。刻意用 USAGE_ALARM 语义：它和铃声模式的关系与通知振动不同，
     * 静音下依然能震，是最后一道能用的通道。
     */
    fun vibrate(context: Context, strong: Boolean) {
        val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vibrator == null || !vibrator.hasVibrator()) return

        val pattern = if (strong) {
            longArrayOf(0, 600, 250, 600, 250, 600)
        } else {
            longArrayOf(0, 400, 200, 400)
        }
        runCatching { vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1)) }
    }

    fun requestAlarmFocus(context: Context) {
        val manager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        runCatching {
            manager.requestAudioFocus(
                focusListener,
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            )
        }
    }

    fun abandonFocus(context: Context) {
        val manager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        runCatching { manager.abandonAudioFocus(focusListener) }
    }

    /** 系统闹钟音量，用于设置页提示——为 0 时任何方案都不会有声 */
    fun alarmVolume(context: Context): Pair<Int, Int> {
        val manager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return 0 to 0
        return runCatching {
            manager.getStreamVolume(AudioManager.STREAM_ALARM) to
                manager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        }.getOrDefault(0 to 0)
    }
}
