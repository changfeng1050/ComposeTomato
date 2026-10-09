package com.changfeng.tomato.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/** 计时阶段。 */
enum class Phase { Focus, ShortBreak, LongBreak }

/** 番茄记录状态：完成（跑满）或放弃（主动中止）。 */
enum class RecordStatus { Finished, Discarded }

/**
 * 番茄记录：一次专注的留档。
 * 休息阶段不留档（不计入统计，见 CONTEXT.md「短休息/长休息」）。
 */
@Serializable
data class TomatoRecord(
    val startEpochMs: Long,
    val endEpochMs: Long,
    val phase: Phase,
    val status: RecordStatus,
    val noteId: Long? = null,
    val noteSnapshot: String? = null,
)

@Serializable
private data class RecordList(val records: List<TomatoRecord> = emptyList())

object RecordsRepo {

    private val file = File("records.json")

    var records by mutableStateOf(load())
        private set

    fun add(record: TomatoRecord) {
        records = records + record
        persist()
    }

    /** 今日（本地自然日）完成的番茄数。 */
    fun finishedToday(): Int {
        val today = LocalDate.now()
        return records.count {
            it.phase == Phase.Focus &&
                    it.status == RecordStatus.Finished &&
                    toLocalDate(it.endEpochMs) == today
        }
    }

    private fun toLocalDate(epochMs: Long): LocalDate =
        Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()).toLocalDate()

    private fun load(): List<TomatoRecord> {
        if (!file.exists()) return emptyList()
        return try {
            appJson.decodeFromString<RecordList>(file.readText()).records
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun persist() {
        try {
            file.writeText(appJson.encodeToString(RecordList(records)))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
