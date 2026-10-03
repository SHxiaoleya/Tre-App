package com.trea.app.data.notes

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class NoteRepository(private val context: Context) {

    private val notesDir: File
        get() = File(context.filesDir, "notes").apply {
            if (!exists()) {
                mkdirs()
            }
        }

    private val _notesFlow = MutableStateFlow<List<NoteItem>>(emptyList())
    val notesFlow: StateFlow<List<NoteItem>> = _notesFlow.asStateFlow()

    suspend fun refreshNotes() = withContext(Dispatchers.IO) {
        val files = notesDir.listFiles { file ->
            file.isFile && file.name.endsWith(".txt")
        } ?: emptyArray()

        val list = files.mapNotNull { file ->
            try {
                val fullText = file.readText()
                val id = file.nameWithoutExtension
                val lines = fullText.lines()
                val title = if (lines.isNotEmpty()) lines.first().trim() else "无标题"
                val content = if (lines.size > 1) {
                    lines.drop(1).joinToString("\n").trimStart('\n')
                } else {
                    ""
                }
                NoteItem(
                    id = id,
                    title = title.ifEmpty { "无标题" },
                    content = content,
                    lastModified = file.lastModified()
                )
            } catch (_: Exception) {
                null
            }
        }.sortedByDescending { it.lastModified }

        _notesFlow.value = list
    }

    suspend fun saveNote(id: String?, title: String, content: String): NoteItem = withContext(Dispatchers.IO) {
        val noteId = id?.ifEmpty { null } ?: ("note_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().take(6))
        val targetFile = File(notesDir, "$noteId.txt")

        val cleanTitle = title.trim().ifEmpty { "无标题" }
        val cleanContent = content.trim()

        val textToSave = buildString {
            append(cleanTitle)
            append("\n\n")
            append(cleanContent)
        }

        targetFile.writeText(textToSave)
        refreshNotes()

        NoteItem(
            id = noteId,
            title = cleanTitle,
            content = cleanContent,
            lastModified = targetFile.lastModified()
        )
    }

    suspend fun deleteNote(id: String): Boolean = withContext(Dispatchers.IO) {
        val file = File(notesDir, "$id.txt")
        val deleted = if (file.exists()) file.delete() else false
        refreshNotes()
        deleted
    }

    suspend fun getNoteById(id: String): NoteItem? = withContext(Dispatchers.IO) {
        val file = File(notesDir, "$id.txt")
        if (!file.exists()) return@withContext null
        try {
            val lines = file.readText().lines()
            val title = if (lines.isNotEmpty()) lines.first().trim() else "无标题"
            val content = if (lines.size > 1) {
                lines.drop(1).joinToString("\n").trimStart('\n')
            } else {
                ""
            }
            NoteItem(
                id = id,
                title = title.ifEmpty { "无标题" },
                content = content,
                lastModified = file.lastModified()
            )
        } catch (_: Exception) {
            null
        }
    }
}
