---
name: dingwhen
description: 一本会出声的通书：米纸、墨字、一枚朱砂印，时间永远先用眼睛看见、再用耳朵听见。
colors:
  ink: "#1E1A15"
  paper: "#F3E9D6"
  card: "#FBF4E4"
  linen: "#EDE2CB"
  stone: "#6B6152"
  rule: "#DFD2B8"
  vermilion: "#A63A2B"
  vermilion-deep: "#48200F"
  seal-cream: "#F8F0DF"
  moss: "#3F5B43"
typography:
  display:
    fontFamily: "serif"
    fontSize: "56sp"
    fontWeight: 400
    lineHeight: "62sp"
    letterSpacing: "-1.5sp"
    fontFeature: "tnum"
  headline:
    fontFamily: "serif"
    fontSize: "26sp"
    fontWeight: 500
    lineHeight: "32sp"
    letterSpacing: "0sp"
  title:
    fontFamily: "serif"
    fontSize: "20sp"
    fontWeight: 500
    lineHeight: "26sp"
    letterSpacing: "0sp"
  body:
    fontFamily: "sans-serif"
    fontSize: "16sp"
    fontWeight: 400
    lineHeight: "24sp"
    letterSpacing: "0sp"
  label:
    fontFamily: "sans-serif"
    fontSize: "11sp"
    fontWeight: 500
    lineHeight: "14sp"
    letterSpacing: "0.6sp"
rounded:
  xs: "2dp"
  sm: "3dp"
  md: "4dp"
  lg: "6dp"
  xl: "8dp"
spacing:
  page: "20dp"
  gap: "10dp"
  section: "22dp"
  card: "16dp"
  row: "13dp"
components:
  seal-band:
    backgroundColor: "{colors.vermilion}"
    textColor: "{colors.seal-cream}"
    rounded: "{rounded.md}"
  card:
    backgroundColor: "{colors.card}"
    rounded: "{rounded.md}"
  card-muted:
    backgroundColor: "{colors.linen}"
    rounded: "{rounded.md}"
  button-primary:
    backgroundColor: "{colors.vermilion}"
    textColor: "{colors.seal-cream}"
    rounded: "{rounded.sm}"
    height: "52dp"
  chip-segment:
    backgroundColor: "{colors.linen}"
    textColor: "{colors.stone}"
    rounded: "{rounded.sm}"
    height: "38dp"
  chip-segment-selected:
    backgroundColor: "{colors.vermilion}"
    textColor: "{colors.seal-cream}"
    rounded: "{rounded.sm}"
    height: "38dp"
  fab:
    backgroundColor: "{colors.vermilion}"
    textColor: "{colors.seal-cream}"
    rounded: "{rounded.xl}"
    size: "56dp"
  input:
    backgroundColor: "{colors.linen}"
    textColor: "{colors.ink}"
    rounded: "{rounded.md}"
    padding: "12dp 16dp"
---

# Design System: dingwhen

## Overview

**Creative North Star: "通胜"**

dingwhen 是一本会出声的通书：米纸做地，墨字压在上面，朱砂只盖在「下一条」和「新建」上。没有渐变、没有毛玻璃、没有插画；层级由一条条 1dp 栏线、宋体的标题与时间和紧凑的行距建立，像印刷品而不是塑料面板。

世界只借四样东西——字体、配色、密度和一个签名动作（朱砂印块），布局与控件仍是 Android 用户熟悉的那一套：顶部标题、列表行、开关、FAB、系统时间选择器。用户不必学新交互，只需要一眼看见「下一条几点」。

**Key Characteristics:**
- 米纸底 + 墨字 + 朱砂印，红色每屏只出现一处主行动
- 标题、时间、印章用系统 serif（宋体），正文用系统无衬线
- 卡片 4dp 圆角、1dp 栏线、无阴影；交互件用 3dp 直角感圆角
- 列表行内一条 1dp 竖线把「时间列」和「内容列」分开，像通书的双栏

## Colors

一张米纸上只有三种角色：纸、墨、朱砂。

### Primary
- **朱砂 Vermilion** (#A63A2B)：唯一的主行动色。只用于三处——顶部的「下一条」印块、右下角的新建印、以及页面末尾的保存按钮。深色模式压暗为 #48200F，夜里不刺眼。

### Secondary
- **苔绿 Moss** (#3F5B43)：只表示「已完成 / 成功」，不做装饰。

### Neutral
- **米纸 Paper** (#F3E9D6)：页面地面；深色模式 #14110D。
- **卡纸 Card** (#FBF4E4)：卡片与弹层；深色 #1C1812。
- **亚麻 Linen** (#EDE2CB)：输入框、未选中分段器、停用卡片；深色 #2A241B。
- **石灰 Stone** (#6B6152)：副标题、说明文字、日期标签。
- **栏线 Rule** (#DFD2B8)：卡片描边、分节线、行内竖线；深色 #352E24。
- **印底 Seal Cream** (#F8F0DF)：印块上的反白字。

### Named Rules
**The One Seal Rule.** 朱砂是印章，不是主题色：一屏最多一处朱砂主行动。开关、分段器选中态、操作药丸都用墨或栏线，不用朱砂。

**The Ink Rule.** 所有正文与次级信息只用墨与石灰两档；不要引入第三种灰，也不要给文字加色。

## Typography

**Display Font:** 系统 serif（中文为 Noto Serif CJK，拉丁为 Noto Serif）
**Body Font:** 系统 sans-serif（Roboto / Noto Sans CJK）

**Character:** 宋体承担「印出来的」部分——标题、时间、印章；无衬线承担「读的」部分——正文、说明、按钮。两套字体不混排同一行，层级靠这套分工而不是字号堆叠。

### Hierarchy
- **Display**（400，56sp / 62sp，-1.5sp，tnum）：编辑页时间，全应用最大的字；未设置时缩到 32sp 并改用石灰。
- **Headline**（500，26sp / 32sp）：页面标题，serif，左对齐贴 20dp 边距。
- **Title**（500，20sp / 26sp）：卡片主文案，serif；空状态标题。
- **Body**（400，16sp / 24sp）：正文、设置行标题，无衬线。
- **Label**（500，11sp，+0.6sp）：分节标签、印章边框内的小字；全大写般的疏排。

### Named Rules
**The Two Voices Rule.** 同一行文字只能用一种字体：宋体用于「标题 / 时间 / 印章」，无衬线用于「内容 / 说明 / 操作」。不允许在一句话里换字体强调。

**The Tabular Rule.** 时间与计数一律 `tnum`，列表 60dp 的时间列因此能上下对齐。

## Layout

单列，无底部导航。层级只有「列表 ↔ 编辑 / 设置」，靠返回箭头与系统返回手势。

- 页面左右边距 20dp；卡片间距 10dp；分节间距 22dp。
- 列表行：左 18dp、右 12dp、上下 14dp；时间列固定 60dp，后接 14dp 空隙、1dp 竖线、14dp 空隙，然后才是内容。
- 分节标签是一行「文字 + 通到右边的 1dp 细线」，把内容按栏切开。
- 顶部印块横贯整宽（左右 20dp），内部再嵌一圈 1dp 描边，像印章的边框。
- 底部滚动余量 120dp，避免最后一行被 FAB 挡住。

## Elevation & Depth

**平铺，靠栏线分层。** 卡片、印块、输入框全部零阴影；只有 FAB 保留 3dp 抬升（按下 1dp），因为它真的浮在列表上。对话框与时间选择器沿用 Material 3 默认抬升。

### Named Rules
**The No-Shadow Rule.** 想强调一块内容，加栏线或加留白，不加投影。

## Shapes

印刷品不是塑料：圆角小、边线细。

- **8dp**：FAB（印章式方块）。
- **6dp**：对话框。
- **4dp**：卡片、印块、输入框。
- **3dp**：按钮、分段器、小药丸、返回按钮。
- **2dp**：印章内框、状态小标签。

## Components

### Buttons
- **Primary:** 朱砂底、印底色字、3dp 圆角、52dp 高、满宽。
- **FAB:** 56dp 见方、8dp 圆角、朱砂底、白色「+」；是页面唯一悬浮物。
- **Action pill:** 朱砂淡底（浅色为 #F0DFD3）+ 朱砂字，3dp 圆角，用于「去开启」「试听」。

### Cards
- **Corner:** 4dp；**Border:** 1dp 栏线；**Padding:** 16dp（列表行 18/12/14）。
- 停用卡片换成亚麻底色，文字降到石灰，开关置灰。

### Inputs
- 亚麻底、4dp 圆角、无描边；焦点靠光标与键盘表示，不做发光。

### Navigation
- 无 Tab；二级页左上 38dp 返回按钮（3dp 圆角），系统返回手势始终可用。

### Signature Components

**Next Band（顶部印块）.** 横贯的朱砂块，内嵌 1dp 边框：左上「下一条」小字，右上带边框的「今天 / 明天」小签，下面 40sp 宋体时间、内容与周期。它是列表的题眼，红色只在这里出现。

**Ruled Row（列表行）.** 时间列（21sp 宋体等宽数字 + 11sp 石灰日期）｜1dp 竖线｜内容与周期｜墨色开关。竖线是通书双栏的痕迹，也是这一版最容易被认出的细节。

**Section Rule（分节头）.** 11sp 疏排标签后接一条通到右边缘的细线，代替标题下的空白。

## Do's and Don'ts

### Do:
- **Do** 把朱砂留给「下一条」印块、新建印和选中态，一屏一处。
- **Do** 标题与时间用 serif、`tnum`，正文用无衬线。
- **Do** 用 1dp 栏线、4dp 圆角和紧凑行距建立印刷感。
- **Do** 深色模式一一对应：纸变墨、墨变纸，朱砂压暗，角色不变。
- **Do** 保持 Material 3 控件与系统手势，只换字体、配色、密度和一个签名动作。

### Don't:
- **Don't** 用朱砂做开关、进度或普通强调，它一旦泛滥就不再是印。
- **Don't** 给卡片加阴影、加大圆角（>8dp）或加渐变。
- **Don't** 在一行里混用衬线与非衬线。
- **Don't** 增加底部导航或 Tab；层级只用返回。
- **Don't** 写营销腔的界面文案，文案只陈述行为和结果。
