# dingwhen

到点就叫你的提醒 App。个人自用，Android 8.0（API 26）及以上，无网络权限。

## 能做什么

- 手动建提醒：选时间 + 周期（仅一次 / 每天 / 工作日 / 每周几 / 每月几号），写一句话
- 到点发系统通知：锁屏可见，响系统默认提示音
- 通知按钮自己配：打开指定 App / 稍后 10 分钟 / 知道了；不配就一个都不显示
- 一次性提醒响过自动停用，周期提醒自动排下一次
- 重启、换时区、改系统时间后自动恢复调度；关机期间错过的会补一条说明
- 浅色 / 深色 / 跟随系统，中英文切换

## 下载

到 [Releases](https://github.com/rmrfnoise/dingwhen/releases) 下载最新的 `dingwhen-x.y.apk`，点开安装（系统提示时允许「安装未知来源应用」）。

## 第一次用

1. 打开后先在「设置」里把 **通知权限** 和 **精确闹钟** 打开，否则到点收不到。
2. 建议再开 **电池优化白名单**；小米 / 华为 / OPPO / vivo 还要在系统的「自启动管理」里放行。
3. 回到列表，点右下角「新建提醒」。

## 自己编译

需要 JDK 17 和 Android SDK 35：

```bash
./gradlew assembleDebug     # 调试包
./gradlew assembleRelease   # 正式包，需在根目录放 keystore.properties
```

`keystore.properties` 四个字段：`storeFile` / `storePassword` / `keyAlias` / `keyPassword`。
推一个 `v*` 标签会触发 GitHub Actions 构建正式包并挂到 Release。
