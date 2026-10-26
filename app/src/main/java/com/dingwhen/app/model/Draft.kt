package com.dingwhen.app.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

/** 草稿缺什么。只描述"缺哪一项"，具体文案交给语言包。 */
enum class DraftProblem { TIME, CONTENT, WEEKLY_DAYS, MONTHLY_DAY }

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
    /** 通知按钮：到点打开的应用包名，null 表示不显示该按钮 */
    val actionOpenPackage: String? = null,
    val actionSnooze: Boolean = false,
    val actionDismiss: Boolean = false
) {

    /** 缺失项，非空则不允许创建。 */
    fun problems(): List<DraftProblem> = buildList {
        if (hour == null || minute == null) add(DraftProblem.TIME)
        if (content.isBlank()) add(DraftProblem.CONTENT)
        if (repeat == RepeatType.WEEKLY && days.isEmpty()) add(DraftProblem.WEEKLY_DAYS)
        if (repeat == RepeatType.MONTHLY && dayOfMonth == null) add(DraftProblem.MONTHLY_DAY)
    }

    fun isValid(): Boolean = problems().isEmpty()

    /** 时间文本；没选时间返回 null，由界面决定怎么表现"未设置"。 */
    fun clockText(): String? =
        if (hour == null || minute == null) null else String.format(Locale.US, "%02d:%02d", hour, minute)
}
