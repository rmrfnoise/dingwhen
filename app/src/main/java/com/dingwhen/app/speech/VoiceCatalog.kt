package com.dingwhen.app.speech

/**
 * 预置音色：中文女声 + 中文男声，全部为豆包语音合成 2.0（`*_uranus_bigtts`）。
 *
 * 2.0 的中文音色官方能力标注统一为「指令遵循」，也就是都能通过 context_texts
 * 接收语气/情感指令——这是本项目"要有感情"的前提，男声同样适用。
 *
 * 选型原则：尽量收录，不预设好坏。角色型音色（武则天、TVB 女声、熊二、猴哥）
 * 一并保留——合不合适由用户试听决定，代码不该替人判断。只略去童声
 * （儿童绘本、奶气萌娃这类），它们在提醒场景里辨识度太低。
 *
 * 音色 ID 取自官方 2.0 音色列表。不同账号开通的范围可能不同，真正的可用性
 * 以控制台为准：界面上不显示 ID，遇到未开通时换一个再试听即可。
 */
enum class VoiceGender { FEMALE, MALE }

data class VoiceOption(
    val id: String,
    val labelZh: String,
    val labelEn: String,
    val gender: VoiceGender = VoiceGender.FEMALE
)

object VoiceCatalog {

    /** 只支持 2.0，X-Api-Resource-Id 固定值 */
    const val RESOURCE_ID = "seed-tts-2.0"

    const val DEFAULT_VOICE_ID = "zh_female_vv_uranus_bigtts"

    val options: List<VoiceOption> = listOf(
        // 女声 · 自然 / 情感
        VoiceOption(DEFAULT_VOICE_ID, "Vivi · 情感最丰富", "Vivi · most expressive"),
        VoiceOption("zh_female_xiaohe_uranus_bigtts", "小何 · 自然亲切", "Xiaohe · natural"),
        VoiceOption("zh_female_wenrouxiaoya_uranus_bigtts", "温柔小雅", "Xiaoya · gentle"),
        VoiceOption("zh_female_qinqienv_uranus_bigtts", "亲切女声", "Friendly female"),
        VoiceOption("zh_female_cancan_uranus_bigtts", "知性灿灿", "Cancan · intellectual"),
        VoiceOption("zh_female_linjianvhai_uranus_bigtts", "邻家女孩", "Girl next door"),

        // 男声 · 沉稳 / 亲切
        VoiceOption("zh_male_m191_uranus_bigtts", "云舟 · 沉稳大气", "Yunzhou · steady", VoiceGender.MALE),
        VoiceOption("zh_male_liufei_uranus_bigtts", "刘飞 · 成熟解说", "Liufei · mature", VoiceGender.MALE),
        VoiceOption("zh_male_wennuanahu_uranus_bigtts", "温暖阿虎", "Ahu · warm", VoiceGender.MALE),
        VoiceOption("zh_male_ruyaqingnian_uranus_bigtts", "儒雅青年", "Refined youth", VoiceGender.MALE),
        VoiceOption("zh_male_qingcang_uranus_bigtts", "擎苍 · 低沉", "Qingcang · deep", VoiceGender.MALE),
        VoiceOption("zh_male_shaonianzixin_uranus_bigtts", "少年梓辛", "Zixin · youth", VoiceGender.MALE)
    )

    /** 展示顺序：女声在前，与 options 里的排列顺序一致 */
    val genderOrder = listOf(VoiceGender.FEMALE, VoiceGender.MALE)

    /** 某一性别下的全部音色，给选择器按栏取用 */
    fun byGender(gender: VoiceGender): List<VoiceOption> =
        options.filter { it.gender == gender }

    fun find(id: String): VoiceOption? = options.firstOrNull { it.id == id }

    /** 界面只显示名称，ID 是内部标识。取不到（已下架）返回 null，由调用方决定回落。 */
    fun labelOf(id: String?, en: Boolean): String? =
        id?.let { find(it) }?.let { if (en) it.labelEn else it.labelZh }

    /**
     * 凡是列表外的音色（1.0 老配置、已下架的男声、以前手动粘贴的 ID）一律回落默认值，
     * 否则列表里没有任何一项会被选中，用户会看到"一个都没选"的空状态。
     */
    fun migrate(storedId: String?): String {
        if (storedId.isNullOrBlank()) return DEFAULT_VOICE_ID
        return if (options.any { it.id == storedId }) storedId else DEFAULT_VOICE_ID
    }
}
