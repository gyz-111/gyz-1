# 晴空天气（QingKong Weather）

全新设计的安卓天气应用（包名 `com.qingkong.weather`），数据来自[和风天气](https://dev.qweather.com)。

## 应用简介

- **实时天气**：动态天气场景动画背景 + 自绘矢量图标
- **24 小时温度曲线**：自绘折线图
- **未来 7 / 15 天预报**：含日出日落、月相、紫外线
- **城市搜索与定位**：内置城市库，支持定位与检索
- **下拉刷新**、天气动态渐变背景

## 当前版本

| 项 | 值 |
|---|---|
| 版本名 | v1.2.3 |
| versionCode | 6 |
| minSdk / targetSdk | 24 / 34 |
| 最低支持 | Android 7.0（API 24）及以上 |

## 下载

- [晴空天气 v1.2.3 APK（直接下载）](https://github.com/gyz-111/gyz-1/raw/main/weatherall/%E6%98%85%E7%A9%BA%E5%A4%A9%E6%B0%94_v1.2.3.apk)
- [仓库中的 APK 目录](https://github.com/gyz-111/gyz-1/tree/main/weatherall)

> 安装前请在系统设置中允许安装未知来源应用。

## 源码说明

本目录为完整安卓工程（Gradle 多模块，单 `:app` 模块），可直接用 Android Studio 打开 `qingkong` 目录构建。

按 [构建说明](./构建说明.txt) 配置本机环境：

- 和风天气 API 密钥与专属域名写在 `app/src/main/java/com/qingkong/weather/WeatherApi.java` 顶部的两个常量中，**请自行替换为自己的密钥**（控制台获取：https://dev.qweather.com）
- 后续升级版本必须在 `app/build.gradle` 中递增 `versionCode` / `versionName` 后用**同一密钥**重新签名，否则无法覆盖安装

## 可用接口

实测可用（其余接口已废弃或受限，勿添加）：

- `/v7/weather/now` 实时天气
- `/v7/weather/24h` 24 小时预报
- `/v7/weather/15d` 15 天预报（含日出日落 / 月相 / 紫外线）

不可用：空气质量 / 生活指数 / 天气预警。

## 构建命令（PowerShell）

```powershell
$env:JAVA_HOME = "D:\android-build-env\jdk\jdk-17.0.20.1+1"
& "D:\android-build-env\gradle\gradle-8.7\bin\gradle.bat" -p "<qingkong工程目录>" assembleRelease
```

产物：`app\build\outputs\apk\release\app-release.apk`
