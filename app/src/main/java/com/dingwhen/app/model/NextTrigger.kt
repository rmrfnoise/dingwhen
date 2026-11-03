package com.dingwhen.app.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZonedDateTime

/**
 * 下一次触发时刻的唯一计算入口。
 * 调度器、保存动作、重启恢复全部复用这里，保证"看到的"和"调度的"完全一致。
 */
object NextTrigger {

    fun next(
        now: ZonedDateTime,
        hour: Int,
        minute: Int,
        repeat: RepeatType,
        days: Set<DayOfWeek> = emptySet(),
        dayOfMonth: Int? = null,
        date: LocalDate? = null
    ): ZonedDateTime {
        val zone = now.zone
        fun at(d: LocalDate): ZonedDateTime = d.atTime(hour, minute).atZone(zone)

        return when (repeat) {
            RepeatType.ONCE -> {
                var d = date ?: now.toLocalDate()
                var candidate = at(d)
                var guard = 0
                while (!candidate.isAfter(now) && guard++ < 400) {
                    d = d.plusDays(1)
                    candidate = at(d)
                }
                candidate
            }

            RepeatType.DAILY -> {
                var d = now.toLocalDate()
                var candidate = at(d)
                if (!candidate.isAfter(now)) {
                    d = d.plusDays(1)
                    candidate = at(d)
                }
                candidate
            }

            RepeatType.WEEKDAYS -> {
                var d = now.toLocalDate()
                repeat(8) {
                    if (d.dayOfWeek.value in 1..5) {
                        val candidate = at(d)
                        if (candidate.isAfter(now)) return candidate
                    }
                    d = d.plusDays(1)
                }
                at(d)
            }

            RepeatType.WEEKLY -> {
                if (days.isEmpty()) {
                    var d = now.toLocalDate()
                    var candidate = at(d)
                    if (!candidate.isAfter(now)) {
                        d = d.plusDays(1)
                        candidate = at(d)
                    }
                    candidate
                } else {
                    var d = now.toLocalDate()
                    repeat(8) {
                        if (days.contains(d.dayOfWeek)) {
                            val candidate = at(d)
                            if (candidate.isAfter(now)) return candidate
                        }
                        d = d.plusDays(1)
                    }
                    at(d)
                }
            }

            RepeatType.MONTHLY -> {
                val dom = dayOfMonth ?: now.toLocalDate().dayOfMonth
                var ym = YearMonth.from(now.toLocalDate())
                repeat(24) {
                    // 31 号遇到小月自动落到月末，避免 2 月 31 直接崩掉
                    val day = minOf(dom, ym.lengthOfMonth())
                    val candidate = ym.atDay(day).atTime(hour, minute).atZone(zone)
                    if (candidate.isAfter(now)) return candidate
                    ym = ym.plusMonths(1)
                }
                at(now.toLocalDate().plusDays(1))
            }
        }
    }
}
