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
    val nextLabel: String
    val allPaused: String
    val settingsDesc: String
    val newReminder: String
    val sectionActive: String
    val sectionPaused: String
    val emptyTitle: String
    val deleteTitle: String
    val bannerBoth: String
    val bannerNotification: String
    val bannerExact: String
    val bannerFix: String

    fun activeSummary(count: Int): String
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
    val dismissTitle: String
    val dismissSub: String
    val dismissLabelHint: String
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

    /* ------------------------------------------------------------ 语音播报 */

    val speechRowTitle: String
    val speechRowSub: String
    val speechToggleSub: String
    val speechPageTitle: String
    val speechNotConfigured: String
    val speechOffSub: String
    val speechSectionKey: String
    val speechApiKey: String
    val speechKeyNote: String
    val speechSectionVoice: String
    val speechVoiceNoKey: String
    /** 编辑页音色行的标题：定这一条的声音 */
    val voiceTitle: String
    /** 设置页音色行：只决定新建时预填什么，改不动已有提醒，标题要说明这点 */
    val defaultVoiceTitle: String
    val voicePickerTitle: String
    val voicePickerHint: String
    val voiceScopeNote: String
    val speechEmotionTitle: String
    /** 设置页语气开关：和音色对称，只决定新建时预填什么 */
    val defaultEmotionTitle: String
    val speechEmotionSub: String
    val speechEmotionHint: String
    val speechSectionAdvanced: String
    val speechRepeatTitle: String
    val speechRepeatSub: String
    /** 关掉确认按钮后重复提醒失效，副标题换成这句 */
    val speechRepeatOffSub: String
    val speechVolumeTitle: String
    val speechTimesTitle: String
    val speechTimesSub: String
    val speechTest: String
    val speechTesting: String
    val speechTestOk: String
    val speechTestOkSystem: String
    val speechTestFail: String
    val speechTestSample: String
    val speechTestLabel: String
    val speechRegen: String
    val speechEditorSection: String
    val speechEditorHint: String
    val speechPreview: String
    val speechStateNone: String
    val speechStatePending: String
    val speechStateReady: String
    val speechStateFailed: String
    val speechStateSystem: String

    fun speechVolumeValue(current: Int, max: Int): String
    fun speechTimesValue(times: Int): String
    fun speechRegenDone(count: Int): String
    fun speechError(reason: String): String

    /* ------------------------------------------------------------ 通知 */

    val channelName: String
    val channelDesc: String
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
    override val overdue = "已错过"
    override val pausedTag = "已停用"
    override val select = "选择"
    override val change = "修改"

    override val listTitle = "提醒"
    override val listTagline = "还没有提醒"
    override val nextLabel = "下一条"
    override val allPaused = "全部已停用"
    override val settingsDesc = "设置"
    override val newReminder = "新建提醒"
    override val sectionActive = "进行中"
    override val sectionPaused = "已停用"
    override val emptyTitle = "还没有提醒"
    override val deleteTitle = "删除这条提醒？"
    override val bannerBoth = "通知和精确闹钟都没开，到点不会响"
    override val bannerNotification = "通知权限没开，到点不会响"
    override val bannerExact = "精确闹钟没开，可能晚一点响"
    override val bannerFix = "开启"

    override fun activeSummary(count: Int) = "$count 条进行中"
    override fun deleteBody(content: String) = "「$content」将被删除，无法恢复。"
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
    override val pickTime = "点一下选时间"
    override val tapToChange = "点一下修改"
    override val sectionRepeat = "重复"
    override val sectionContent = "内容"
    override val contentHint = "一句话，到点念出来"
    override val sectionActions = "通知按钮"
    override val openApp = "打开应用"
    override val noAppPicked = "未设置"
    override val clearAction = "移除这个按钮"
    override val dismissTitle = "确认按钮"
    override val dismissSub = "点一下停止后续重复"
    override val dismissLabelHint = "按钮文字，留空用「知道了」"
    override val actionsNote = "两个都不选，通知只有文字和时间。"
    override val deleteOnlyBody = "删除后无法恢复。"

    override fun problem(kind: DraftProblem) = when (kind) {
        DraftProblem.TIME -> "还没选时间"
        DraftProblem.CONTENT -> "还没写内容"
        DraftProblem.WEEKLY_DAYS -> "还没选星期几"
        DraftProblem.MONTHLY_DAY -> "还没选几号"
    }

    override fun joinProblems(items: List<String>) = items.joinToString("、")

    override val permTitle = "设置"
    override val permSubtitleOk = "权限已就绪"
    override val sectionPermissions = "权限"
    override val permNotificationTitle = "通知权限"
    override val permNotificationDesc = "到点弹出提醒，锁屏也看得到"
    override val permExactTitle = "精确闹钟"
    override val permExactDesc = "准点触发，不被省电策略推迟"
    override val permBatteryTitle = "电池优化白名单"
    override val permBatteryDesc = "防止后台被清理后漏掉提醒"
    override val permAutostartTitle = "自启动 / 后台运行"
    override val permAutostartDesc = "重启后自动恢复；国产手机需单独放行"
    override val grantedOn = "已开启"
    override val goEnable = "开启"
    override val goAllow = "允许"
    override val goSettings = "设置"
    override val romNote =
        "小米 / 华为 / OPPO / VIVO 还要在「自启动管理」里放行，否则清掉后台就收不到通知。"

    override fun permSubtitlePending(count: Int) = "还有 $count 项未开启"

    override val sectionAppearance = "外观"
    override val themeSystem = "跟随系统"
    override val themeLight = "浅色"
    override val themeDark = "深色"
    override val sectionLanguage = "语言"

    override val pickerTitle = "选择应用"
    override val pickerHint = "通知按钮会直接打开它"
    override val searchApps = "搜索应用"
    override val noApps = "没有可打开的应用"
    override val noMatch = "没有匹配项"

    override val speechRowTitle = "语音播报"
    override val speechRowSub = "静音也会念出来"
    override val speechToggleSub = "到点把内容念出来"
    override val speechPageTitle = "语音播报"
    override val speechNotConfigured = "未填 API KEY，用系统语音念"
    override val speechOffSub = "已关闭，只弹通知和振动"
    override val speechSectionKey = "语音服务"
    override val speechApiKey = "API KEY"
    override val speechKeyNote =
        "不填也能用，系统语音会比较机械。API KEY 只存本机；提醒文字只发给豆包合成。"

    override val speechSectionVoice = "音色与语气"
    override val speechVoiceNoKey = "填 API KEY 后才能选这些音色。"
    override val voiceTitle = "音色"
    override val defaultVoiceTitle = "新提醒的默认音色"
    override val voicePickerTitle = "选择音色"
    override val voicePickerHint = "点一下换声音"
    override val voiceScopeNote = "只影响以后新建的提醒；已有提醒在各自的编辑页里改。"
    override val speechEmotionTitle = "语气"
    override val defaultEmotionTitle = "新提醒的默认语气"
    override val speechEmotionSub = "用一句话描述想要的感觉"
    override val speechEmotionHint = "如「温柔一点」「认真一点」"

    override val speechSectionAdvanced = "其他"
    override val speechRepeatTitle = "未确认就重复响"
    override val speechRepeatSub = "每 3 分钟一次，最多 3 次"
    override val speechRepeatOffSub = "打开确认按钮后才能用"
    override val speechVolumeTitle = "系统闹钟音量"
    override val speechTimesTitle = "每轮念几遍"
    override val speechTimesSub = "一轮里念几次；听不清靠重复提醒补"
    override val speechTest = "试听"
    override val speechTesting = "正在合成…"
    override val speechTestOk = "合成好了，正在播放"
    override val speechTestOkSystem = "未填 API KEY，用系统语音念"
    override val speechTestFail = "合成失败"
    override val speechTestSample = "上班记得打卡"
    override val speechTestLabel = "试听内容"
    override val speechRegen = "为已有提醒补做语音"
    override val speechEditorSection = "播报内容"
    override val speechEditorHint = "留空就念上面的内容"
    override val speechPreview = "试听"
    override val speechStateNone = "无语音"
    override val speechStatePending = "待合成"
    override val speechStateReady = "已合成"
    override val speechStateFailed = "合成失败"
    override val speechStateSystem = "系统语音"

    override fun speechVolumeValue(current: Int, max: Int) = "$current / $max"
    override fun speechTimesValue(times: Int) = "$times 遍"
    override fun speechRegenDone(count: Int) = if (count == 0) "都已完成" else "已排队 $count 条"

    override fun speechError(reason: String): String = when (reason) {
        "not_configured" -> "未填 API KEY"
        "empty_text" -> "播报内容是空的"
        "text_too_long" -> "文字太长，单次最多约 300 字"
        "network" -> "网络不通或服务繁忙，稍后会自动重试"
        "auth" -> "密钥不对，检查 API KEY"
        "quota" -> "额度用完了，去控制台看看"
        "voice" -> "这个音色你的账号还没开通，换一个试试"
        "bad_text" -> "这段文字合成不了，换个说法"
        "tts_failed" -> "系统语音没出声，手机可能没有语音引擎；到点用振动提醒"
        else -> "合成失败，换个音色或稍后再试"
    }

    override val channelName = "提醒"
    override val channelDesc = "到点提醒"
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
    override val overdue = "Missed"
    override val pausedTag = "Paused"
    override val select = "Select"
    override val change = "Change"

    override val listTitle = "Reminders"
    override val listTagline = "Nothing scheduled yet"
    override val nextLabel = "UP NEXT"
    override val allPaused = "All paused"
    override val settingsDesc = "Settings"
    override val newReminder = "New reminder"
    override val sectionActive = "ACTIVE"
    override val sectionPaused = "PAUSED"
    override val emptyTitle = "No reminders yet"
    override val deleteTitle = "Delete this reminder?"
    override val bannerBoth = "Notifications and exact alarms are off — nothing will fire"
    override val bannerNotification = "Notifications are off — nothing will fire"
    override val bannerExact = "Exact alarms are off — reminders may run late"
    override val bannerFix = "Turn on"

    override fun activeSummary(count: Int) =
        if (count == 1) "1 active" else "$count active"

    override fun deleteBody(content: String) = "“$content” will be deleted. This cannot be undone."
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
    override val contentHint = "One line to read out loud"
    override val sectionActions = "NOTIFICATION BUTTONS"
    override val openApp = "Open app"
    override val noAppPicked = "Not set"
    override val clearAction = "Remove this button"
    override val dismissTitle = "Confirm button"
    override val dismissSub = "Stops the follow-ups"
    override val dismissLabelHint = "Button text; empty = “Got it”"
    override val actionsNote = "With both buttons off, the notification shows just the text and time."
    override val deleteOnlyBody = "This cannot be undone."

    override fun problem(kind: DraftProblem) = when (kind) {
        DraftProblem.TIME -> "Pick a time"
        DraftProblem.CONTENT -> "Write what to remind you"
        DraftProblem.WEEKLY_DAYS -> "Pick at least one weekday"
        DraftProblem.MONTHLY_DAY -> "Pick a day of the month"
    }

    override fun joinProblems(items: List<String>) = items.joinToString(", ")

    override val permTitle = "Settings"
    override val permSubtitleOk = "All permissions on"
    override val sectionPermissions = "PERMISSIONS"
    override val permNotificationTitle = "Notifications"
    override val permNotificationDesc = "Pops up on time, visible on the lock screen"
    override val permExactTitle = "Exact alarms"
    override val permExactDesc = "Fires on the dot, never delayed by battery saving"
    override val permBatteryTitle = "Ignore battery optimization"
    override val permBatteryDesc = "Keeps reminders alive in the background"
    override val permAutostartTitle = "Autostart / background"
    override val permAutostartDesc = "Restores reminders after a reboot; needed on most Chinese ROMs"
    override val grantedOn = "On"
    override val goEnable = "Enable"
    override val goAllow = "Allow"
    override val goSettings = "Settings"
    override val romNote =
        "On Xiaomi / Huawei / OPPO / VIVO also allow autostart, or reminders stop once you clear the app."

    override fun permSubtitlePending(count: Int) =
        if (count == 1) "1 permission off" else "$count permissions off"

    override val sectionAppearance = "APPEARANCE"
    override val themeSystem = "System"
    override val themeLight = "Light"
    override val themeDark = "Dark"
    override val sectionLanguage = "LANGUAGE"

    override val pickerTitle = "Choose an app"
    override val pickerHint = "The notification button opens it"
    override val searchApps = "Search apps"
    override val noApps = "No apps to open"
    override val noMatch = "No match"

    override val speechRowTitle = "Voice reading"
    override val speechRowSub = "Reads it out loud, even on silent"
    override val speechToggleSub = "Reads the text out loud at the set time"
    override val speechPageTitle = "Voice reading"
    override val speechNotConfigured = "No API KEY — using the system voice"
    override val speechOffSub = "Off — notifications and vibration only"
    override val speechSectionKey = "VOICE SERVICE"
    override val speechApiKey = "API KEY"
    override val speechKeyNote =
        "Optional — without it the system voice reads, and sounds robotic. " +
            "The key stays on this device; the text is sent to Doubao to synthesize."

    override val speechSectionVoice = "VOICE & TONE"
    override val speechVoiceNoKey = "These voices need an API KEY."
    override val voiceTitle = "Voice"
    override val defaultVoiceTitle = "Default voice"
    override val voicePickerTitle = "Choose a voice"
    override val voicePickerHint = "Tap to switch"
    override val voiceScopeNote =
        "Applies to new reminders only. Change an existing one in its editor."
    override val speechEmotionTitle = "Tone"
    override val defaultEmotionTitle = "Default tone"
    override val speechEmotionSub = "Describe the tone in plain words"
    override val speechEmotionHint = "e.g. gentle / cheerful / serious"

    override val speechSectionAdvanced = "OTHER"
    override val speechRepeatTitle = "Repeat until confirmed"
    override val speechRepeatSub = "Every 3 minutes, up to 3 more times"
    override val speechRepeatOffSub = "Needs the confirm button"
    override val speechVolumeTitle = "System alarm volume"
    override val speechTimesTitle = "Reads per alert"
    override val speechTimesSub = "How many times to read it each round"
    override val speechTest = "Test"
    override val speechTesting = "Synthesizing…"
    override val speechTestOk = "Ready — playing"
    override val speechTestOkSystem = "No key — using the system voice"
    override val speechTestFail = "Failed"
    override val speechTestSample = "Time to check in"
    override val speechTestLabel = "Test phrase"
    override val speechRegen = "Add voice to existing reminders"
    override val speechEditorSection = "SPOKEN TEXT"
    override val speechEditorHint = "Leave empty to read the text above"
    override val speechPreview = "Preview"
    override val speechStateNone = "No audio"
    override val speechStatePending = "Queued"
    override val speechStateReady = "Ready"
    override val speechStateFailed = "Failed"
    override val speechStateSystem = "System voice"

    override fun speechVolumeValue(current: Int, max: Int) = "$current / $max"
    override fun speechTimesValue(times: Int) = if (times == 1) "Once" else "$times times"
    override fun speechRegenDone(count: Int) =
        if (count == 0) "Everything is ready" else "Queued $count"

    override fun speechError(reason: String): String = when (reason) {
        "not_configured" -> "No API KEY yet"
        "empty_text" -> "Nothing to read"
        "text_too_long" -> "Text too long, about 300 characters max"
        "network" -> "Network or service busy — will retry later"
        "auth" -> "Bad API KEY — check it again"
        "quota" -> "Quota used up — check the console"
        "voice" -> "This voice isn't enabled for your account — try another one"
        "bad_text" -> "This text cannot be synthesized"
        "tts_failed" -> "The system voice stayed silent — no TTS engine on this phone? Vibration will still fire"
        else -> "Synthesis failed — try another voice or later"
    }

    override val channelName = "Reminders"
    override val channelDesc = "Reminder alerts"
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
