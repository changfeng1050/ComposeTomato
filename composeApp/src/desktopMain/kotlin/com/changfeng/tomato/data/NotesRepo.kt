package com.changfeng.tomato.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable
import java.io.File

/**
 * 便笺：单条多行文本，一行一个事项。
 * 以 `[ ]` / `[x]` 开头的行是清单行（事项），`[x]` 表示已完成。
 */
object NotesRepo {

    private val file = File("notes.json")

    var text by mutableStateOf(load())
        private set

    fun update(newText: String) {
        text = newText
        persist()
    }

    @Serializable
    private data class NoteData(val text: String = "")

    /** 旧「多条便笺」格式，仅用于迁移：合并为单条文本。 */
    @Serializable
    private data class OldNote(val text: String = "", val createdAt: Long = 0)

    @Serializable
    private data class OldNoteList(val notes: List<OldNote> = emptyList())

    private fun load(): String {
        if (!file.exists()) return ""
        val content = file.readText()
        return try {
            if (content.contains("\"notes\"")) {
                // 迁移旧多条格式：按创建顺序合并所有便笺文本
                appJson.decodeFromString<OldNoteList>(content).notes
                    .sortedBy { it.createdAt }
                    .map { it.text.trim() }
                    .filter { it.isNotEmpty() }
                    .joinToString("\n")
                    .also { persist(it) }
            } else {
                appJson.decodeFromString<NoteData>(content).text
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun persist(newText: String = text) {
        try {
            file.writeText(appJson.encodeToString(NoteData(newText)))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
