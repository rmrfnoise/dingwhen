package com.dingwhen.app.i18n

import com.dingwhen.app.model.DraftProblem
import com.dingwhen.app.model.RepeatType
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

/** 应用内语言。切语言走本地字符串表，不重启 Activity。 */
enum class Lang(val code: String) {
    ZH("zh"),
    EN("en");

    companion object {
        fun fromCode(code: String?): Lang? = entries.firstOrNull { it.code == code }

        /** 首次启动跟随系统语言，之后以用户选择为准。 */
        fun system(): Lang =
            if (Locale.getDefault().language.startsWith("zh")) ZH else EN
    }
}

/**
 * 全部界面文案的唯一来源。通知也在用，所以字符串必须能脱离 Context 拿到。
 * 需要拼接的文案一律做成方法，避免各页面自己拼出语序不对的英文。
 */
interface Strings {

    val lang: Lang

    /* ------------------------------------------------------------ 通用 */

    val back: String
    val cancel: String
    val delete: String
    val done: String
    val notSet: String
    val unscheduled: String
    val overdue: String
    val pausedTag: String
    val select: String
    val change: String

    /* ------------------------------------------------------------ 列表 */

    val listTitle: String
    val listTagline: String
    val allPaused: String
    val settingsDesc: String
    val newReminder: String
    val sectionActive: String
    val sectionPaused: String
    val emptyTitle: String
    val emptyBody: String
    val deleteTitle: String
    val bannerBoth: String
    val bannerNotification: String
    val bannerExact: String
    val bannerFix: String

    fun activeSummary(count: Int, next: String): String
    fun deleteBody(content: String): String
    fun deletedSnack(content: String): String

    /* ------------------------------------------------------------ 时间 */

    val today: String
    val tomorrow: String
    val dayAfter: String

    fun weekday(day: DayOfWeek): String
    fun weekdayNarrow(day: DayOfWeek): String
    fun monthDay(month: Int, day: Int): String
    fun compactDate(month: Int, day: Int): String

    /* ------------------------------------------------------------ 重复 */

    fun repeatLabel(repeat: RepeatType, days: Set<DayOfWeek>, dayOfMonth: Int?): String
    fun repeatChip(repeat: RepeatType): String
    val monthlyWord: String
    val dayWord: String

    /* ------------------------------------------------------------ 编辑 */

    val editorNew: String
    val editorEdit: String
    val saveReminder: String
    val saveChanges: String
    val pickTime: String
    val tapToChange: String
    val sectionRepeat: String
    val sectionContent: String
    val contentHint: String
    val sectionActions: String
    val openApp: String
    val noAppPicked: String
    val clearAction: String
    val snoozeTitle: String
    val snoozeSub: String
    val dismissTitle: String
    val dismissSub: String
    val actionsNote: String
    val deleteOnlyBody: String

    fun problem(kind: DraftProblem): String
    fun joinProblems(items: List<String>): String

    /* ------------------------------------------------------------ 权限 */

    val permTitle: String
    val permSubtitleOk: String
    val sectionPermissions: String
    val permNotificationTitle: String
    val permNotificationDesc: String
    val permExactTitle: String
    val permExactDesc: String
    val permBatteryTitle: String
    val permBatteryDesc: String
    val permAutostartTitle: String
    val permAutostartDesc: String
    val grantedOn: String
    val goEnable: String
    val goAllow: String
    val goSettings: String
    val romNote: String

    fun permSubtitlePending(count: Int): String

    /* ------------------------------------------------------------ 设置 */

    val sectionAppearance: String
    val themeSystem: String
    val themeLight: String
    val themeDark: String
    val sectionLanguage: String

    /* ------------------------------------------------------- 应用选择器 */

    val pickerTitle: String
    val pickerHint: String
    val searchApps: String
    val noApps: String
    val noMatch: String

    /* ------------------------------------------------------------ 通知 */

    val channelName: String
    val channelDesc: String
    val snoozePrefix: String
    val snoozeAction: String
    val dismissAction: String

    fun openAppAction(label: String): String
    fun missedTitle(content: String): String
    fun missedBody(time: String): String
}

/* ================================================================ 中文 */

object Zh : Strings {

    override val lang = Lang.ZH

    override val back = "返回"
    override val cancel = "取消"
    override val delete = "删除"
    override val done = "完成"
    override val notSet = "未设置"
    override val unscheduled = "未安排"
    override val overdue = "已过期"
    override val pausedTag = "已停用"
    override val select = "选择"
    override val change = "修改"

    override val listTitle = "提醒"
    override val listTagline = "到点提醒，准时通知"
    override val allPaused = "全部已停用"
    override val settingsDesc = "设置"
    override val newReminder = "新建提醒"
    override val sectionActive = "进行中"
    override val sectionPaused = "已停用"
    override val emptyTitle = "还没有提醒"
    override val emptyBody = "点右下角「新建提醒」，\n选好时间，写一句要提醒的事。"
    override val deleteTitle = "删除这条提醒？"
    override val bannerBoth = "通知和精确闹钟都没开，到点可能收不到"
    override val bannerNotification = "通知权限没开，到点不会弹出提醒"
    override val bannerExact = "精确闹钟没开，提醒可能被系统延后"
    override val bannerFix = "去开启"

    override fun activeSummary(count: Int, next: String) = "$count 个进行中 · 下一个 $next"
    override fun deleteBody(content: String) = "「$content」会被永久删除。"
    override fun deletedSnack(content: String) = "已删除「$content」"

    override val today = "今天"
    override val tomorrow = "明天"
    override val dayAfter = "后天"

    override fun weekday(day: DayOfWeek) = "周" + weekdayNarrow(day)

    override fun weekdayNarrow(day: DayOfWeek) = when (day) {
        DayOfWeek.MONDAY -> "一"
        DayOfWeek.TUESDAY -> "二"
        DayOfWeek.WEDNESDAY -> "三"
        DayOfWeek.THURSDAY -> "四"
        DayOfWeek.FRIDAY -> "五"
        DayOfWeek.SATURDAY -> "六"
        DayOfWeek.SUNDAY -> "日"
    }

    override fun monthDay(month: Int, day: Int) = "${month}月${day}日"
    override fun compactDate(month: Int, day: Int) = "$month/$day"

    override fun repeatLabel(repeat: RepeatType, days: Set<DayOfWeek>, dayOfMonth: Int?): String =
        when (repeat) {
            RepeatType.ONCE -> "仅一次"
            RepeatType.DAILY -> "每天"
            RepeatType.WEEKDAYS -> "工作日"
            RepeatType.WEEKLY ->
                if (days.isEmpty()) "每周"
                else "每周" + days.sortedBy { it.value }.joinToString("、") { weekdayNarrow(it) }

            RepeatType.MONTHLY -> if (dayOfMonth == null) "每月" else "每月 $dayOfMonth 号"
        }

    override fun repeatChip(repeat: RepeatType) = when (repeat) {
        RepeatType.ONCE -> "仅一次"
        RepeatType.DAILY -> "每天"
        RepeatType.WEEKDAYS -> "工作日"
        RepeatType.WEEKLY -> "每周"
        RepeatType.MONTHLY -> "每月"
    }

    override val monthlyWord = "每月"
    override val dayWord = "号"

    override val editorNew = "新建提醒"
    override val editorEdit = "编辑提醒"
    override val saveReminder = "保存提醒"
    override val saveChanges = "保存修改"
    override val pickTime = "点一下选择时间"
    override val tapToChange = "点一下修改"
    override val sectionRepeat = "重复"
    override val sectionContent = "提醒内容"
    override val contentHint = "写一句要提醒的事"
    override val sectionActions = "通知按钮"
    override val openApp = "打开应用"
    override val noAppPicked = "未选择 · 通知上不显示这个按钮"
    override val clearAction = "不显示这个按钮"
    override val snoozeTitle = "稍后 10 分钟"
    override val snoozeSub = "点一下，10 分钟后再提醒一次"
    override val dismissTitle = "知道了"
    override val dismissSub = "点一下清掉这条通知"
    override val actionsNote = "不选就只显示内容和时间，通知上不出现按钮。"
    override val deleteOnlyBody = "删除后无法恢复。"

    override fun problem(kind: DraftProblem) = when (kind) {
        DraftProblem.TIME -> "请选择提醒时间"
        DraftProblem.CONTENT -> "请填写提醒内容"
        DraftProblem.WEEKLY_DAYS -> "每周提醒请选择星期几"
        DraftProblem.MONTHLY_DAY -> "每月提醒请选择几号"
    }

    override fun joinProblems(items: List<String>) = items.joinToString("、")

    override val permTitle = "设置"
    override val permSubtitleOk = "都开好了，到点会准时通知"
    override val sectionPermissions = "所需权限"
    override val permNotificationTitle = "通知权限"
    override val permNotificationDesc = "到点弹提醒并播放提示音，锁屏可见"
    override val permExactTitle = "精确闹钟"
    override val permExactDesc = "允许准点触发，不被省电策略拖延"
    override val permBatteryTitle = "电池优化白名单"
    override val permBatteryDesc = "避免后台被清理导致提醒丢失"
    override val permAutostartTitle = "自启动 / 后台运行"
    override val permAutostartDesc = "重启后自动恢复。国产 ROM 需在「自启动管理」放行"
    override val grantedOn = "已开启"
    override val goEnable = "去开启"
    override val goAllow = "去允许"
    override val goSettings = "去设置"
    override val romNote =
        "国产 ROM（小米 / 华为 / OPPO / vivo）通常还要在「自启动管理」「后台运行」里单独放行，否则清后台后收不到通知。"

    override fun permSubtitlePending(count: Int) = "还有 $count 项没开，到点可能收不到通知"

    override val sectionAppearance = "外观"
    override val themeSystem = "跟随系统"
    override val themeLight = "浅色"
    override val themeDark = "深色"
    override val sectionLanguage = "语言"

    override val pickerTitle = "选择应用"
    override val pickerHint = "到点时点通知上的按钮就直接打开它"
    override val searchApps = "搜索应用"
    override val noApps = "没读到可启动的应用"
    override val noMatch = "没有匹配的应用"

    override val channelName = "提醒"
    override val channelDesc = "到点提醒通知，使用系统默认提示音"
    override val snoozePrefix = "稍后提醒"
    override val snoozeAction = "稍后 10 分钟"
    override val dismissAction = "知道了"

    override fun openAppAction(label: String) = "打开$label"
    override fun missedTitle(content: String) = "错过提醒：$content"
    override fun missedBody(time: String) = "原定 $time，已错过"
}

/* ================================================================ English */

object En : Strings {

    override val lang = Lang.EN

    private val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    override val back = "Back"
    override val cancel = "Cancel"
    override val delete = "Delete"
    override val done = "Done"
    override val notSet = "Not set"
    override val unscheduled = "Unscheduled"
    override val overdue = "Overdue"
    override val pausedTag = "Paused"
    override val select = "Select"
    override val change = "Change"

    override val listTitle = "Reminders"
    override val listTagline = "Right on time, every time"
    override val allPaused = "All paused"
    override val settingsDesc = "Settings"
    override val newReminder = "New reminder"
    override val sectionActive = "ACTIVE"
    override val sectionPaused = "PAUSED"
    override val emptyTitle = "No reminders yet"
    override val emptyBody = "Tap “New reminder” below,\npick a time and write what to remind you."
    override val deleteTitle = "Delete this reminder?"
    override val bannerBoth = "Notifications and exact alarms are off"
    override val bannerNotification = "Notifications are off — nothing will pop up"
    override val bannerExact = "Exact alarms are off — reminders may be delayed"
    override val bannerFix = "Fix"

    override fun activeSummary(count: Int, next: String) =
        if (count == 1) "1 active · Next: $next" else "$count active · Next: $next"

    override fun deleteBody(content: String) = "“$content” will be deleted for good."
    override fun deletedSnack(content: String) = "Deleted “$content”"

    override val today = "Today"
    override val tomorrow = "Tomorrow"
    override val dayAfter = "In 2 days"

    override fun weekday(day: DayOfWeek) = when (day) {
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        DayOfWeek.SUNDAY -> "Sun"
    }

    override fun weekdayNarrow(day: DayOfWeek) = weekday(day)

    override fun monthDay(month: Int, day: Int) = "${months[month - 1]} $day"
    override fun compactDate(month: Int, day: Int) = "$month/$day"

    override fun repeatLabel(repeat: RepeatType, days: Set<DayOfWeek>, dayOfMonth: Int?): String =
        when (repeat) {
            RepeatType.ONCE -> "Once"
            RepeatType.DAILY -> "Every day"
            RepeatType.WEEKDAYS -> "Weekdays"
            RepeatType.WEEKLY ->
                if (days.isEmpty()) "Weekly"
                else "Weekly · " + days.sortedBy { it.value }.joinToString(", ") { weekdayNarrow(it) }

            RepeatType.MONTHLY ->
                if (dayOfMonth == null) "Monthly" else "Monthly on the ${ordinal(dayOfMonth)}"
        }

    override fun repeatChip(repeat: RepeatType) = when (repeat) {
        RepeatType.ONCE -> "Once"
        RepeatType.DAILY -> "Daily"
        RepeatType.WEEKDAYS -> "Mon–Fri"
        RepeatType.WEEKLY -> "Weekly"
        RepeatType.MONTHLY -> "Monthly"
    }

    override val monthlyWord = "Day"
    override val dayWord = ""

    override val editorNew = "New reminder"
    override val editorEdit = "Edit reminder"
    override val saveReminder = "Save reminder"
    override val saveChanges = "Save changes"
    override val pickTime = "Tap to pick a time"
    override val tapToChange = "Tap to change"
    override val sectionRepeat = "REPEAT"
    override val sectionContent = "REMINDER"
    override val contentHint = "What should I remind you about?"
    override val sectionActions = "NOTIFICATION BUTTONS"
    override val openApp = "Open app"
    override val noAppPicked = "None · this button stays hidden"
    override val clearAction = "Hide this button"
    override val snoozeTitle = "Snooze 10 min"
    override val snoozeSub = "Remind me again in 10 minutes"
    override val dismissTitle = "Got it"
    override val dismissSub = "Clears this notification"
    override val actionsNote = "Leave these off and the notification shows just the text and time."
    override val deleteOnlyBody = "This cannot be undone."

    override fun problem(kind: DraftProblem) = when (kind) {
        DraftProblem.TIME -> "Pick a time"
        DraftProblem.CONTENT -> "Write what to remind you"
        DraftProblem.WEEKLY_DAYS -> "Pick at least one weekday"
        DraftProblem.MONTHLY_DAY -> "Pick a day of the month"
    }

    override fun joinProblems(items: List<String>) = items.joinToString(", ")

    override val permTitle = "Settings"
    override val permSubtitleOk = "All set — reminders arrive on time"
    override val sectionPermissions = "PERMISSIONS"
    override val permNotificationTitle = "Notifications"
    override val permNotificationDesc = "Popup with sound, visible on the lock screen"
    override val permExactTitle = "Exact alarms"
    override val permExactDesc = "Fire on the dot, never delayed by battery saving"
    override val permBatteryTitle = "Ignore battery optimization"
    override val permBatteryDesc = "Keeps reminders alive when the app is in background"
    override val permAutostartTitle = "Autostart / background"
    override val permAutostartDesc = "Restores reminders after a reboot. Needed on most Chinese ROMs"
    override val grantedOn = "On"
    override val goEnable = "Enable"
    override val goAllow = "Allow"
    override val goSettings = "Settings"
    override val romNote =
        "On Chinese ROMs (Xiaomi / Huawei / OPPO / vivo) also allow autostart and background running, otherwise reminders stop after you clear the app."

    override fun permSubtitlePending(count: Int) =
        if (count == 1) "1 permission missing — may not arrive" else "$count permissions missing — may not arrive"

    override val sectionAppearance = "APPEARANCE"
    override val themeSystem = "System"
    override val themeLight = "Light"
    override val themeDark = "Dark"
    override val sectionLanguage = "LANGUAGE"

    override val pickerTitle = "Choose an app"
    override val pickerHint = "The notification button opens it directly"
    override val searchApps = "Search apps"
    override val noApps = "No launchable apps found"
    override val noMatch = "No match"

    override val channelName = "Reminders"
    override val channelDesc = "Reminder alerts using the system notification sound"
    override val snoozePrefix = "Snoozed"
    override val snoozeAction = "Snooze 10 min"
    override val dismissAction = "Got it"

    override fun openAppAction(label: String) = "Open $label"
    override fun missedTitle(content: String) = "Missed: $content"
    override fun missedBody(time: String) = "Was due at $time"

    private fun ordinal(n: Int): String {
        val suffix = when {
            n % 100 in 11..13 -> "th"
            n % 10 == 1 -> "st"
            n % 10 == 2 -> "nd"
            n % 10 == 3 -> "rd"
            else -> "th"
        }
        return "$n$suffix"
    }
}
