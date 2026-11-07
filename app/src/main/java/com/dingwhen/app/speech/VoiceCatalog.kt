package com.dingwhen.app.speech

/**
 * 预置音色：全部为豆包语音合成 2.0（`*_uranus_bigtts` / `*_tob`）。
 *
 * 这一版按用户指定的顺序收录 50 个音色，不再按性别分栏——音色合不合适由试听决定，
 * 不由性别决定。名称取自官方音色列表，ID 只在内部使用，界面上不显示。
 *
 * 语种能力与短描述取自官方 2.0 音色表（2026-11 快照）：
 * 团子与 ICL 中文角色为「中文」；Big Boogie 为「美式英语」；
 * 其余 zh_* 官方标注「中文、支持 30+ 语种识别」，这里归为「中英」。
 * 官方能力若有调整，以实测为准修改本表。
 *
 * 2.0 的中文音色官方能力标注统一为「指令遵循」，也就是都能通过 context_texts
 * 接收语气/情感指令。
 *
 * 不同账号开通的范围可能不同，真正的可用性以控制台为准：遇到未开通时换一个再试听即可。
 */

enum class VoiceLanguage { ZH, EN }

data class VoiceOption(
    val id: String,
    val labelZh: String,
    val labelEn: String,
    val languages: Set<VoiceLanguage>,
    /** 一句话描述声音本身，控制在 16 个汉字内；不承载语种信息。 */
    val descriptionZh: String,
    val descriptionEn: String
) {
    val supportsChinese: Boolean get() = VoiceLanguage.ZH in languages
    val supportsEnglish: Boolean get() = VoiceLanguage.EN in languages
}

object VoiceCatalog {

    /** 只支持 2.0，X-Api-Resource-Id 固定值 */
    const val RESOURCE_ID = "seed-tts-2.0"

    const val DEFAULT_VOICE_ID = "zh_female_vv_uranus_bigtts"

    private val BOTH = setOf(VoiceLanguage.ZH, VoiceLanguage.EN)

    private fun bilingual(
        id: String,
        labelZh: String,
        labelEn: String,
        descriptionZh: String,
        descriptionEn: String
    ) = VoiceOption(id, labelZh, labelEn, BOTH, descriptionZh, descriptionEn)

    private fun zhOnly(
        id: String,
        labelZh: String,
        labelEn: String,
        descriptionZh: String,
        descriptionEn: String
    ) = VoiceOption(
        id,
        labelZh,
        labelEn,
        setOf(VoiceLanguage.ZH),
        descriptionZh,
        descriptionEn
    )

    private fun enOnly(
        id: String,
        labelZh: String,
        labelEn: String,
        descriptionZh: String,
        descriptionEn: String
    ) = VoiceOption(
        id,
        labelZh,
        labelEn,
        setOf(VoiceLanguage.EN),
        descriptionZh,
        descriptionEn
    )

    val options: List<VoiceOption> = listOf(
        zhOnly("zh_female_tuanzi_uranus_bigtts", "团子", "Tuanzi", "年轻女声，偏可爱", "Young, cute female"),
        bilingual("zh_female_vv_uranus_bigtts", "Vivi", "Vivi", "自然女声，通用", "Natural, versatile female"),
        bilingual("zh_female_xiaohe_uranus_bigtts", "小何", "Xiaohe", "亲切自然的女声", "Warm, natural female"),
        bilingual("zh_male_wennuanahu_uranus_bigtts", "温暖阿虎", "Ahu", "温暖亲切的男声", "Warm, friendly male"),
        bilingual("zh_male_liufei_uranus_bigtts", "刘飞", "Liufei", "成熟稳重的男声", "Mature, steady male"),
        bilingual("zh_male_taocheng_uranus_bigtts", "小天", "Xiaotian", "亲和自然的男声", "Approachable male"),
        bilingual("zh_male_m191_uranus_bigtts", "云舟", "Yunzhou", "沉稳大气的男声", "Calm, deep male"),
        bilingual("zh_female_gujie_uranus_bigtts", "顾姐", "Gujie", "成熟御姐音", "Mature, confident female"),
        bilingual("zh_female_jitangmei_uranus_bigtts", "鸡汤妹妹", "Jitang", "活泼治愈的女声", "Lively, soothing female"),
        bilingual("zh_female_mengyatou_uranus_bigtts", "萌丫头", "Mengyatou", "软萌少女音", "Soft, cute girl"),
        bilingual("zh_female_wenrouxiaoya_uranus_bigtts", "温柔小雅", "Xiaoya", "温柔细腻的女声", "Gentle, soft female"),
        bilingual("zh_female_roumeinvyou_uranus_bigtts", "柔美女友", "Gentle girlfriend", "轻柔甜美的女声", "Sweet, soft female"),
        bilingual("zh_female_tianmeiyueyue_uranus_bigtts", "甜美悦悦", "Yueyue", "甜美明亮的女声", "Sweet, bright female"),
        bilingual("zh_female_qingchezizi_uranus_bigtts", "清澈梓梓", "Zizi", "清澈干净的女声", "Clear, clean female"),
        bilingual("zh_male_kailangxuezhang_uranus_bigtts", "开朗学长", "Sunny senior", "开朗阳光的男声", "Sunny, cheerful male"),
        bilingual("zh_female_qinqienv_uranus_bigtts", "亲切女声", "Friendly female", "亲切随和的女声", "Friendly, easygoing female"),
        bilingual("zh_female_zhishuaiyingzi_uranus_bigtts", "直率英子", "Yingzi", "直率干练的女声", "Direct, capable female"),
        bilingual("zh_female_nvleishen_uranus_bigtts", "女雷神", "Thunder girl", "有力量感的女声", "Powerful female"),
        bilingual("zh_female_chanmeinv_uranus_bigtts", "谄媚女声", "Flattering", "刻意讨好的女声", "Flattering female"),
        bilingual("zh_male_zhubajie_uranus_bigtts", "猪八戒", "Bajie", "憨厚的角色音", "Comic character voice"),
        bilingual("zh_male_tangseng_uranus_bigtts", "唐僧", "Tang Seng", "温和絮叨的角色音", "Gentle, talkative character"),
        bilingual("zh_female_chunribu_uranus_bigtts", "春日部姐姐", "Chunribu", "元气姐姐音", "Energetic older sister"),
        bilingual("zh_female_lingling_uranus_bigtts", "玲玲姐姐", "Lingling", "温柔姐姐音", "Gentle older sister"),
        bilingual("zh_female_linxiao_uranus_bigtts", "林潇", "Linxiao", "御姐音，偏磁性", "Mature, magnetic female"),
        bilingual("zh_female_jiaochuannv_uranus_bigtts", "娇喘女声", "Breathy", "气声偏重的女声", "Breathy female"),
        bilingual("zh_female_gaolengyujie_uranus_bigtts", "高冷御姐", "Aloof lady", "高冷御姐音", "Aloof, mature female"),
        bilingual("zh_male_baqiqingshu_uranus_bigtts", "霸气青叔", "Bossy uncle", "低沉霸气的男声", "Deep, commanding male"),
        bilingual("zh_male_huolixiaoge_uranus_bigtts", "活力小哥", "Lively guy", "活力轻快的男声", "Lively young male"),
        bilingual("zh_male_aojiaobazong_uranus_bigtts", "傲娇霸总", "Tsundere CEO", "傲娇总裁音", "Tsundere CEO"),
        bilingual("zh_female_popo_uranus_bigtts", "婆婆", "Granny", "年长女声", "Older female"),
        bilingual("zh_male_xionger_uranus_bigtts", "熊二", "Xionger", "憨憨的角色音", "Goofy character"),
        bilingual("zh_male_silang_uranus_bigtts", "四郎", "Silang", "古风贵公子音", "Costume drama noble"),
        bilingual("zh_male_yizhipiannan_uranus_bigtts", "译制片男", "Dubbed male", "译制片旁白腔", "Dubbed narration"),
        bilingual("zh_female_tvbnv_uranus_bigtts", "TVB女声", "TVB female", "港剧女声", "TVB-style female"),
        bilingual("zh_female_mizai_uranus_bigtts", "黑猫侦探社咪仔", "Mizi", "侦探社萌系角色音", "Cute detective character"),
        bilingual("zh_female_shuangkuaisisi_uranus_bigtts", "爽快思思", "Sisi", "爽快干脆的女声", "Crisp, direct female"),
        bilingual("zh_female_tianmeitaozi_uranus_bigtts", "甜美桃子", "Taozi", "甜美可爱的女声", "Sweet, cute female"),
        zhOnly(
            "ICL_uranus_zh_female_kefunvshengwenhecuishou_tob",
            "外呼凌凌",
            "Outbound Lingling",
            "客服外呼女声",
            "Outbound service female"
        ),
        zhOnly(
            "ICL_uranus_zh_male_lengdanshuli_tob",
            "冷淡疏离",
            "Cold",
            "冷淡疏离的男声",
            "Cold, distant male"
        ),
        zhOnly(
            "ICL_uranus_zh_female_wenrounvshen_tob",
            "温柔女神",
            "Gentle goddess",
            "温柔女神音",
            "Gentle goddess"
        ),
        zhOnly(
            "ICL_uranus_zh_female_jiaohannvwang_tob",
            "娇憨女王",
            "Naive queen",
            "娇憨女王音",
            "Naive queen"
        ),
        zhOnly(
            "ICL_uranus_zh_female_tiaopigongzhu_tob",
            "调皮公主",
            "Playful princess",
            "调皮公主音",
            "Playful princess"
        ),
        enOnly(
            "ICL_uranus_en_male_big_boogie_tob",
            "Big Boogie",
            "Big Boogie",
            "美式英语男声",
            "American English male"
        ),
        zhOnly(
            "ICL_uranus_zh_female_wenwanshanshan_tob",
            "温婉珊珊",
            "Shanshan",
            "温婉柔和的女声",
            "Gentle, graceful female"
        ),
        zhOnly(
            "ICL_uranus_zh_male_zhongerqingnian_tob",
            "中二青年",
            "Chuunibyou",
            "中二热血青年音",
            "Chuunibyou youth"
        ),
        zhOnly(
            "ICL_uranus_zh_male_zhangjianxiake_tob",
            "仗剑侠客",
            "Swordsman",
            "古风侠客音",
            "Wuxia swordsman"
        ),
        zhOnly(
            "ICL_uranus_zh_male_younidashu_tob",
            "油腻大叔",
            "Greasy uncle",
            "中年大叔音",
            "Middle-aged uncle"
        ),
        zhOnly(
            "ICL_uranus_zh_female_heainainai_tob",
            "和蔼奶奶",
            "Kind grandma",
            "和蔼奶奶音",
            "Kind grandma"
        ),
        zhOnly(
            "ICL_uranus_zh_female_xiemeinvwang_tob",
            "邪魅女王",
            "Sultry queen",
            "邪魅女王音",
            "Sultry queen"
        ),
        zhOnly(
            "ICL_uranus_zh_female_bingjiaojiejie_tob",
            "病娇姐姐",
            "Yandere",
            "病娇姐姐音",
            "Yandere older sister"
        )
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
