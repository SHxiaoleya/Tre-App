package com.trea.app.data.notes

data class NoteItem(
    val id: String,
    val title: String,
    val content: String,
    val lastModified: Long
) {
    val previewText: String
        get() {
            val trimmed = content.trim()
            if (trimmed.isEmpty()) return "无更多内容"
            return if (trimmed.length > 80) trimmed.take(80) + "..." else trimmed
        }
}
