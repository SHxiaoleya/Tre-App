package com.trea.app.data.music

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MusicScanner(private val context: Context) {

    private val supportedExtensions = setOf(
        "mp3", "flac", "wav", "ogg", "m4a", "aac", "opus"
    )

    private val coversDir: File
        get() = File(context.cacheDir, "album_covers").apply {
            if (!exists()) {
                mkdirs()
            }
        }

    suspend fun scanFolder(treeUri: Uri): List<Song> = withContext(Dispatchers.IO) {
        val rootDoc = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext emptyList()
        val songs = mutableListOf<Song>()
        traverseDirectory(rootDoc, songs)
        songs.sortedBy { it.title.lowercase() }
    }

    private fun traverseDirectory(dir: DocumentFile, result: MutableList<Song>) {
        val files = dir.listFiles()
        for (file in files) {
            if (file.isDirectory) {
                traverseDirectory(file, result)
            } else if (file.isFile) {
                val name = file.name ?: continue
                val ext = name.substringAfterLast('.', "").lowercase()
                if (ext in supportedExtensions) {
                    val song = parseSong(file)
                    if (song != null) {
                        result.add(song)
                    }
                }
            }
        }
    }

    private fun parseSong(file: DocumentFile): Song? {
        val uri = file.uri
        val fileName = file.name ?: "Unknown"
        val nameWithoutExt = fileName.substringBeforeLast('.')

        var title = nameWithoutExt
        var artist = "未知艺术家"
        var durationMs = 0L
        var coverPath: String? = null

        val retriever = MediaMetadataRetriever()
        try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                retriever.setDataSource(pfd.fileDescriptor)

                val metaTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                if (!metaTitle.isNullOrBlank()) {
                    title = metaTitle.trim()
                }

                val metaArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                if (!metaArtist.isNullOrBlank()) {
                    artist = metaArtist.trim()
                }

                val metaDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                if (!metaDuration.isNullOrBlank()) {
                    durationMs = metaDuration.toLongOrNull() ?: 0L
                }

                val pictureBytes = retriever.embeddedPicture
                if (pictureBytes != null && pictureBytes.isNotEmpty()) {
                    val coverFile = File(coversDir, "cover_${uri.hashCode()}.jpg")
                    if (!coverFile.exists()) {
                        FileOutputStream(coverFile).use { fos ->
                            fos.write(pictureBytes)
                            fos.flush()
                        }
                    }
                    coverPath = coverFile.absolutePath
                }
            }
        } catch (_: Exception) {
            // Fallback to defaults on metadata read failure
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }

        return Song(
            id = uri.toString(),
            uriString = uri.toString(),
            fileName = fileName,
            title = title,
            artist = artist,
            durationMs = durationMs,
            coverPath = coverPath
        )
    }
}
