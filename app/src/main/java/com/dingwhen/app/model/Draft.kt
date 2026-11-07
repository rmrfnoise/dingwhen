package com.dingwhen.app.model

import com.dingwhen.app.speech.SpeechLimits
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

/** 草稿缺什么。只描述"缺哪一项"，具体文案交给语言包。 */
enum class DraftProblem { TIME, CONTENT, WEEKLY_DAYS, MONTHLY_DAY, TEXT_TOO_LONG }

/**
 * 编辑页的草稿。所有入口都只填这一份结构，校验与保存逻辑保持一致。
 */
data class Draft(
    val id: Long? = null,
    val content: String = "",
    val hour: Int? = null,
    val minute: Int? = null,
    val repeat: RepeatType = RepeatType.ONCE,
    val date: LocalDate = LocalDate.now(),
    val days: Set<DayOfWeek> = emptySet(),
    val dayOfMonth: Int? = LocalDate.now().dayOfMonth,
    /** 播报文案，留空则朗读 content */
    val speechText: String = "",
    /** 这条提醒用的音色。新建时预填设置里的默认音色，之后各条互不影响 */
    val voiceType: String = "",
    /** 这条提醒的语气。同样预填设置里的默认值，之后各条独立 */
    val emotion: String = "",
    val emotionEnabled: Boolean = false,
    /** 每轮念几遍（1–3），跟着这条提醒走 */
    val playTimes: Int = 1,
    /** 没确认就再响。关掉确认按钮时它无效，保存时不强行改写，重开按钮就恢复原值 */
    val repeatOn: Boolean = true,
    /** 通知按钮：到点打开的应用包名，null 表示不显示该按钮 */
    val actionOpenPackage: String? = null,
    /** 默认开着：它既是提前喊停的入口，也是「未确认就重复提醒」的前提——关掉就只响一次 */
    val actionDismiss: Boolean = true,
    /** 「知道了」按钮的自定义文字，留空用默认文案 */
    val dismissLabel: String = ""
) {

    /** 真正会被朗读的文字：优先播报文案，留空则复用内容。 */
    fun spokenText(): String = speechText.ifBlank { content }

    /** 缺失项，非空则不允许创建。 */
    fun problems(): List<DraftProblem> = buildList {
        if (hour == null || minute == null) add(DraftProblem.TIME)
        if (content.isBlank()) add(DraftProblem.CONTENT)
        if (repeat == RepeatType.WEEKLY && days.isEmpty()) add(DraftProblem.WEEKLY_DAYS)
        if (repeat == RepeatType.MONTHLY && dayOfMonth == null) add(DraftProblem.MONTHLY_DAY)
        if (!SpeechLimits.isWithinLimit(spokenText())) add(DraftProblem.TEXT_TOO_LONG)
    }

    fun isValid(): Boolean = problems().isEmpty()

    /** 时间文本；没选时间返回 null，由界面决定怎么表现"未设置"。 */
    fun clockText(): String? =
        if (hour == null || minute == null) null else String.format(Locale.US, "%02d:%02d", hour, minute)
}
