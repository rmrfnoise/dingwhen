package com.dingwhen.app.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class NextTriggerTest {

    private val zone = ZoneId.of("Asia/Shanghai")
    private val now: ZonedDateTime = ZonedDateTime.of(2026, 10, 26, 14, 48, 0, 0, zone) // 周一

    @Test
    fun `每天的提醒取明天同一时刻`() {
        val next = NextTrigger.next(now, 8, 0, RepeatType.DAILY)
        assertEquals(ZonedDateTime.of(2026, 10, 27, 8, 0, 0, 0, zone), next)
    }

    @Test
    fun `每天的提醒当天未到点则取今天`() {
        val next = NextTrigger.next(now, 20, 0, RepeatType.DAILY)
        assertEquals(ZonedDateTime.of(2026, 10, 26, 20, 0, 0, 0, zone), next)
    }

    @Test
    fun `工作日跳过周末`() {
        val fridayEvening = ZonedDateTime.of(2026, 10, 30, 19, 0, 0, 0, zone)
        val next = NextTrigger.next(fridayEvening, 9, 0, RepeatType.WEEKDAYS)
        assertEquals(ZonedDateTime.of(2026, 11, 2, 9, 0, 0, 0, zone), next)
    }

    @Test
    fun `每周只在选定星期触发`() {
        val next = NextTrigger.next(now, 9, 0, RepeatType.WEEKLY, setOf(DayOfWeek.WEDNESDAY))
        assertEquals(ZonedDateTime.of(2026, 10, 28, 9, 0, 0, 0, zone), next)
    }

    @Test
    fun `每月31号在小月落到月末`() {
        val jan31 = ZonedDateTime.of(2026, 1, 31, 10, 0, 0, 0, zone)
        val next = NextTrigger.next(jan31, 9, 0, RepeatType.MONTHLY, dayOfMonth = 31)
        assertEquals(ZonedDateTime.of(2026, 2, 28, 9, 0, 0, 0, zone), next)
    }

    @Test
    fun `仅一次的未来日期原样返回`() {
        val next = NextTrigger.next(
            now, 7, 30, RepeatType.ONCE,
            date = LocalDate.of(2026, 11, 5)
        )
        assertEquals(ZonedDateTime.of(2026, 11, 5, 7, 30, 0, 0, zone), next)
    }

    @Test
    fun `仅一次时间已过则顺延一天`() {
        val next = NextTrigger.next(
            now, 9, 0, RepeatType.ONCE,
            date = LocalDate.of(2026, 10, 26)
        )
        assertEquals(ZonedDateTime.of(2026, 10, 27, 9, 0, 0, 0, zone), next)
    }
}
