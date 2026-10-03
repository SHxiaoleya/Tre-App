package com.trea.app.data.music

data class Song(
    val id: String,
    val uriString: String,
    val fileName: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val coverPath: String? = null
) {
    val formattedDuration: String
        get() = formatDuration(durationMs)

    companion object {
        fun formatDuration(ms: Long): String {
            if (ms <= 0) return "00:00"
            val totalSeconds = ms / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }
    }
}
