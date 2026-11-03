package com.dingwhen.app.model

/** 重复周期。仅支持需求中明确的 5 种。 */
enum class RepeatType {
    ONCE,
    DAILY,
    WEEKDAYS,
    WEEKLY,
    MONTHLY;

    companion object {
        fun fromName(name: String?): RepeatType =
            entries.firstOrNull { it.name == name } ?: ONCE
    }
}
