package com.trea.app.data.music

enum class PlayMode {
    SEQUENTIAL,
    SINGLE_LOOP,
    SHUFFLE;

    fun next(): PlayMode {
        return when (this) {
            SEQUENTIAL -> SINGLE_LOOP
            SINGLE_LOOP -> SHUFFLE
            SHUFFLE -> SEQUENTIAL
        }
    }

    fun displayName(): String {
        return when (this) {
            SEQUENTIAL -> "列表循环"
            SINGLE_LOOP -> "单曲循环"
            SHUFFLE -> "随机播放"
        }
    }
}
