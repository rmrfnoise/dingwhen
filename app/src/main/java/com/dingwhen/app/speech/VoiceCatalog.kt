package com.dingwhen.app.speech

/**
 * 预置音色：全部为豆包语音合成 2.0（`*_uranus_bigtts` / `*_tob`）。
 *
 * 这一版按用户指定的顺序收录 50 个音色，不再按性别分栏——音色合不合适由试听决定，
 * 不由性别决定。名称取自官方音色列表，ID 只在内部使用，界面上不显示。
 *
 * 2.0 的中文音色官方能力标注统一为「指令遵循」，也就是都能通过 context_texts
 * 接收语气/情感指令。
 *
 * 不同账号开通的范围可能不同，真正的可用性以控制台为准：遇到未开通时换一个再试听即可。
 */
data class VoiceOption(
    val id: String,
    val labelZh: String,
    val labelEn: String
)

object VoiceCatalog {

    /** 只支持 2.0，X-Api-Resource-Id 固定值 */
    const val RESOURCE_ID = "seed-tts-2.0"

    const val DEFAULT_VOICE_ID = "zh_female_vv_uranus_bigtts"

    val options: List<VoiceOption> = listOf(
        VoiceOption("zh_female_tuanzi_uranus_bigtts", "团子", "Tuanzi"),
        VoiceOption("zh_female_vv_uranus_bigtts", "Vivi", "Vivi"),
        VoiceOption("zh_female_xiaohe_uranus_bigtts", "小何", "Xiaohe"),
        VoiceOption("zh_male_wennuanahu_uranus_bigtts", "温暖阿虎", "Ahu"),
        VoiceOption("zh_male_liufei_uranus_bigtts", "刘飞", "Liufei"),
        VoiceOption("zh_male_taocheng_uranus_bigtts", "小天", "Xiaotian"),
        VoiceOption("zh_male_m191_uranus_bigtts", "云舟", "Yunzhou"),
        VoiceOption("zh_female_gujie_uranus_bigtts", "顾姐", "Gujie"),
        VoiceOption("zh_female_jitangmei_uranus_bigtts", "鸡汤妹妹", "Jitang"),
        VoiceOption("zh_female_mengyatou_uranus_bigtts", "萌丫头", "Mengyatou"),
        VoiceOption("zh_female_wenrouxiaoya_uranus_bigtts", "温柔小雅", "Xiaoya"),
        VoiceOption("zh_female_roumeinvyou_uranus_bigtts", "柔美女友", "Gentle girlfriend"),
        VoiceOption("zh_female_tianmeiyueyue_uranus_bigtts", "甜美悦悦", "Yueyue"),
        VoiceOption("zh_female_qingchezizi_uranus_bigtts", "清澈梓梓", "Zizi"),
        VoiceOption("zh_male_kailangxuezhang_uranus_bigtts", "开朗学长", "Sunny senior"),
        VoiceOption("zh_female_qinqienv_uranus_bigtts", "亲切女声", "Friendly female"),
        VoiceOption("zh_female_zhishuaiyingzi_uranus_bigtts", "直率英子", "Yingzi"),
        VoiceOption("zh_female_nvleishen_uranus_bigtts", "女雷神", "Thunder girl"),
        VoiceOption("zh_female_chanmeinv_uranus_bigtts", "谄媚女声", "Flattering"),
        VoiceOption("zh_male_zhubajie_uranus_bigtts", "猪八戒", "Bajie"),
        VoiceOption("zh_male_tangseng_uranus_bigtts", "唐僧", "Tang Seng"),
        VoiceOption("zh_female_chunribu_uranus_bigtts", "春日部姐姐", "Chunribu"),
        VoiceOption("zh_female_lingling_uranus_bigtts", "玲玲姐姐", "Lingling"),
        VoiceOption("zh_female_linxiao_uranus_bigtts", "林潇", "Linxiao"),
        VoiceOption("zh_female_jiaochuannv_uranus_bigtts", "娇喘女声", "Breathy"),
        VoiceOption("zh_female_gaolengyujie_uranus_bigtts", "高冷御姐", "Aloof lady"),
        VoiceOption("zh_male_baqiqingshu_uranus_bigtts", "霸气青叔", "Bossy uncle"),
        VoiceOption("zh_male_huolixiaoge_uranus_bigtts", "活力小哥", "Lively guy"),
        VoiceOption("zh_male_aojiaobazong_uranus_bigtts", "傲娇霸总", "Tsundere CEO"),
        VoiceOption("zh_female_popo_uranus_bigtts", "婆婆", "Granny"),
        VoiceOption("zh_male_xionger_uranus_bigtts", "熊二", "Xionger"),
        VoiceOption("zh_male_silang_uranus_bigtts", "四郎", "Silang"),
        VoiceOption("zh_male_yizhipiannan_uranus_bigtts", "译制片男", "Dubbed male"),
        VoiceOption("zh_female_tvbnv_uranus_bigtts", "TVB女声", "TVB female"),
        VoiceOption("zh_female_mizai_uranus_bigtts", "黑猫侦探社咪仔", "Mizi"),
        VoiceOption("zh_female_shuangkuaisisi_uranus_bigtts", "爽快思思", "Sisi"),
        VoiceOption("zh_female_tianmeitaozi_uranus_bigtts", "甜美桃子", "Taozi"),
        VoiceOption("ICL_uranus_zh_female_kefunvshengwenhecuishou_tob", "外呼凌凌", "Outbound Lingling"),
        VoiceOption("ICL_uranus_zh_male_lengdanshuli_tob", "冷淡疏离", "Cold"),
        VoiceOption("ICL_uranus_zh_female_wenrounvshen_tob", "温柔女神", "Gentle goddess"),
        VoiceOption("ICL_uranus_zh_female_jiaohannvwang_tob", "娇憨女王", "Naive queen"),
        VoiceOption("ICL_uranus_zh_female_tiaopigongzhu_tob", "调皮公主", "Playful princess"),
        VoiceOption("ICL_uranus_en_male_big_boogie_tob", "Big Boogie", "Big Boogie"),
        VoiceOption("ICL_uranus_zh_female_wenwanshanshan_tob", "温婉珊珊", "Shanshan"),
        VoiceOption("ICL_uranus_zh_male_zhongerqingnian_tob", "中二青年", "Chuunibyou"),
        VoiceOption("ICL_uranus_zh_male_zhangjianxiake_tob", "仗剑侠客", "Swordsman"),
        VoiceOption("ICL_uranus_zh_male_younidashu_tob", "油腻大叔", "Greasy uncle"),
        VoiceOption("ICL_uranus_zh_female_heainainai_tob", "和蔼奶奶", "Kind grandma"),
        VoiceOption("ICL_uranus_zh_female_xiemeinvwang_tob", "邪魅女王", "Sultry queen"),
        VoiceOption("ICL_uranus_zh_female_bingjiaojiejie_tob", "病娇姐姐", "Yandere")
    )

    fun find(id: String): VoiceOption? = options.firstOrNull { it.id == id }

    /** 界面只显示名称，ID 是内部标识。取不到（已下架）返回 null，由调用方决定回落。 */
    fun labelOf(id: String?, en: Boolean): String? =
        id?.let { find(it) }?.let { if (en) it.labelEn else it.labelZh }

    /**
     * 凡是列表外的音色（1.0 老配置、已下架的音色、以前手动粘贴的 ID）一律回落默认值，
     * 否则列表里没有任何一项会被选中，用户会看到"一个都没选"的空状态。
     */
    fun migrate(storedId: String?): String {
        if (storedId.isNullOrBlank()) return DEFAULT_VOICE_ID
        return if (options.any { it.id == storedId }) storedId else DEFAULT_VOICE_ID
    }
}
