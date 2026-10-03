# TreA - 极简原生 Android 个人日常工具

**TreA** 是一款基于 **Kotlin**、**Jetpack Compose** 和 **Material 3** 打造的极简、轻量、高品质现代原生 Android 个人日常工具应用。

项目坚持“原生、极简、无冗余”的原则，完全离线运行、无需联网、无广告、无账号体系，遵循 Android 最佳架构设计与现代 API 规范。

---

## 一、功能特性

### 1. 🕒 主页 / 时钟 (Home)
- **实时数字时钟**：大字号极简数字时钟，精确显示小时与分钟（带秒数支持），清晰展示中文公历与星期（如“2026年10月1日 星期四”）。
- **动态问候语**：根据当前时间自动切换问候（05:00–11:59 早上好，12:00–17:59 下午好，18:00–22:59 晚上好，23:00–04:59 夜深了）。
- **个性化称呼**：问候语称呼支持在“设置”中随心自定义（默认“树小乐”），修改即时生效并持久化。
- **随机一言**：内置精选中文人生格言与哲思小句，进入主页随机展示；提供轻量“换一句”按钮随心刷新。

### 2. 🎵 音乐播放器 (Music)
- **本地目录扫描**：通过 Android Storage Access Framework (SAF) 文件夹选择器选择音乐目录，持久化读取权限，无需粗暴申请全局文件权限。
- **全格式兼容**：自动扫描并解析 `MP3`、`FLAC`、`WAV`、`OGG`、`M4A`、`AAC`、`OPUS` 音频文件。
- **元数据与封面提取**：自动读取歌曲名、歌手、时长及内嵌专辑封面，无封面时提供极简优雅默认唱片图形。
- **完整播放控制**：播放/暂停、上一首、下一首、拖动进度条跳转（mm:ss 毫秒精准度）。
- **三大播放模式**：顺序播放（列表循环）、单曲循环、随机播放一键切换。
- **后台与锁屏播放**：基于现代 **Android Media3 (ExoPlayer + MediaSessionService)** 架构实现，锁屏控制、通知栏媒体控制器支持，切换页面或离开 App 音乐不断连。

### 3. 📒 简单账本 (Ledger)
- **顶部财务看板**：实时汇总总收入、总支出及当前结余（结余 = 收入 - 支出），人民币符号（¥）格式化展示。
- **记一笔**：点击浮动按钮即可快速记账，支持金额输入验证、支出/收入类型切换、简单备注填写，自动记录系统精确时间戳。
- **时间倒序列表**：清晰的收入（绿）与支出（红）语义化视觉标签，清晰明了。
- **安全删除**：内置原生确认对话框，防止误触删单。
- **Room 持久化**：本地 SQLite 数据库安全持久化。

### 4. 📝 简单笔记 (Notes)
- **纯文本 TXT 存储**：每篇笔记独立以 `.txt` 纯文本格式保存在应用内部沙盒（`notes/` 目录），第一行为标题，其余为正文，格式纯粹通透。
- **极简编辑流程**：新建、快速查看、编辑保存，无冗余 Markdown、富文本、标签干扰。
- **笔记列表**：展示笔记标题、简明正文内容预览、最后修改时间。
- **安全删除**：带确认对话框的删除操作，同步清理底层 TXT 文件。

### 5. ⚙️ 设置 (Settings)
- **个性化**：
  - 称呼自定义：修改问候语中使用的昵称。
  - 主题切换：**动态取色（Material 3 Dynamic Color，默认）**、跟随系统、浅色模式、深色模式。
- **音乐**：
  - 显示当前选定的音乐路径；
  - 提供“选择文件夹/重新选择”入口。
- **关于**：
  - 应用信息：TreA v1.0.0，开发者介绍，Apache 2.0 开源许可声明，“Made for Android”。

---

## 二、技术栈与架构

- **语言**：Kotlin 2.0 (with Official Compose Compiler plugin)
- **UI 框架**：Jetpack Compose + Material 3 (Material You Design System)
- **设计规范**：Material 3 Dynamic Color（基于系统壁纸自动生成配色方案，同时适配浅色/深色）
- **架构模式**：MVVM (Model-View-ViewModel) + Repository 架构
- **导航组件**：Jetpack Navigation Compose (配合 Material 3 NavigationBar)
- **多媒体播放**：AndroidX Media3 (ExoPlayer, MediaSession, SessionToken)
- **存储方案**：
  - **DataStore Preferences**：设置项与用户偏好（称呼、主题、音乐路径）
  - **Room Database**：账本收支数据
  - **Internal Storage (TXT)**：笔记文本文件
- **权限规范**：最小权限原则（SAF 细粒度文件夹授权，Android 13+ POST_NOTIFICATIONS）

---

## 三、项目目录结构

```
TreA/
├── app/
│   ├── build.gradle.kts           # App 模块依赖与编译配置
│   ├── proguard-rules.pro         # 混淆保护规则
│   └── src/main/
│       ├── AndroidManifest.xml    # 前台播放服务、权限与入口配置
│       ├── java/com/trea/app/
│       │   ├── TreAApplication.kt # 全局 Application 与依赖管理
│       │   ├── MainActivity.kt    # 主入口 Activity（Edge-to-Edge、主题初始化）
│       │   ├── data/
│       │   │   ├── ledger/        # Room 实体、DAO、数据库、仓库
│       │   │   ├── music/         # 音乐模型、扫描器、MediaSession 服务、播放控制器
│       │   │   ├── notes/         # TXT 笔记实体与文件存储仓库
│       │   │   └── preferences/   # DataStore 用户配置与主题模式
│       │   ├── navigation/        # 底部导航与页面路由管理
│       │   ├── theme/             # Material 3 Dynamic Color 配色方案与排版
│       │   └── ui/
│       │       ├── home/          # 主页时钟、问候与随机一言
│       │       ├── ledger/        # 账本汇总与收支管理
│       │       ├── music/         # 音乐播放器界面与播放列表
│       │       ├── notes/         # 笔记列表与全屏文本编辑器
│       │       └── settings/      # 设置页面（称呼、主题、目录、关于）
│       └── res/
│           ├── drawable/          # 几何“T”自适应图标前景、背景、单色层与矢量图
│           ├── mipmap-anydpi-v26/ # 自适应图标 XML
│           ├── values/            # 字符串、基础颜色、样式
│           └── xml/               # 备份与数据提取安全规则
├── gradle/
│   ├── libs.versions.toml         # 统一依赖版本编目 (Version Catalog)
│   └── wrapper/                   # Gradle 包装器配置
├── build.gradle.kts               # 根项目构建脚本
├── settings.gradle.kts            # 模块与仓库配置
└── gradle.properties              # JVM 与 AndroidX 配置
```

---

## 四、编译与运行指南

### 1. 开发环境要求
- **Android Studio**：Android Studio Hedgehog (2023.1.1) 或更高版本（推荐 Iguana / Jellyfish / Koala / Ladybug）
- **JDK**：Java Development Kit 17
- **Android SDK**：
  - `compileSdk`: 35 (Android 15)
  - `minSdk`: 26 (Android 8.0)
  - `targetSdk`: 35 (Android 15)

### 2. 导入与运行
1. 打开 Android Studio，点击 **Open**，选择 `TreA` 文件夹。
2. 等待 Gradle Sync 完成（Gradle 将自动下载所需依赖）。
3. 连接搭载 Android 8.0 及以上版本的手机或启动 Android 模拟器（推荐搭载 Android 12+ 的设备以体验 Dynamic Color 动态取色）。
4. 点击工具栏的 **Run 'app'** 按钮即可编译安装。

### 3. 命令行构建
```bash
cd TreA
# 编译 Debug APK
./gradlew assembleDebug

# 产物位置：app/build/outputs/apk/debug/app-debug.apk
```
