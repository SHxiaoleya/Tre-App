package com.trea.app.data.preferences

enum class ThemeMode {
    DYNAMIC,
    SYSTEM,
    LIGHT,
    DARK;

    fun displayName(): String {
        return when (this) {
            DYNAMIC -> "动态取色（默认）"
            SYSTEM -> "跟随系统"
            LIGHT -> "浅色模式"
            DARK -> "深色模式"
        }
    }
}
