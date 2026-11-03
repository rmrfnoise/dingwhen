package com.dingwhen.app.ui

import com.dingwhen.app.i18n.Strings
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Locale

/** "今天 08:57" / "明天 08:57" / "周三 08:57" / "11月2日 08:57" */
fun nextTriggerText(
    epochMillis: Long,
    s: Strings,
    now: ZonedDateTime = ZonedDateTime.now()
): String {
    if (epochMillis <= 0L) return s.unscheduled
    val target = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())
    val days = ChronoUnit.DAYS.between(now.toLocalDate(), target.toLocalDate())
    val datePart = when {
        days == 0L -> s.today
        days == 1L -> s.tomorrow
        days == 2L -> s.dayAfter
        days in 3L..6L -> s.weekday(target.dayOfWeek)
        else -> s.monthDay(target.monthValue, target.dayOfMonth)
    }
    return "$datePart ${timeText(target.hour, target.minute)}"
}

/**
 * 列表时间徽章下面的短标签。空间只有一个词，所以远的日期用 11/2 这种紧凑写法。
 */
fun dayTag(epochMillis: Long, s: Strings, now: ZonedDateTime = ZonedDateTime.now()): String {
    if (epochMillis <= 0L) return s.unscheduled
    val target = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())
    val days = ChronoUnit.DAYS.between(now.toLocalDate(), target.toLocalDate())
    return when {
        days < 0L -> s.overdue
        days == 0L -> s.today
        days == 1L -> s.tomorrow
        days == 2L -> s.dayAfter
        days in 3L..6L -> s.weekday(target.dayOfWeek)
        else -> s.compactDate(target.monthValue, target.dayOfMonth)
    }
}

fun timeText(hour: Int, minute: Int): String = String.format(Locale.US, "%02d:%02d", hour, minute)
