package com.changfeng.tomato.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class Settings(
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val longBreakInterval: Int = 4,
    val autoStartBreak: Boolean = false,
    val autoStartFocus: Boolean = false,
    val soundEnabled: Boolean = true,
    val notificationEnabled: Boolean = true,
    val windowX: Float? = null,
    val windowY: Float? = null,
    val windowWidth: Float? = null,
    val windowHeight: Float? = null,
)

/** 旧版 settings.json 结构，仅用于迁移。 */
@Serializable
private data class LegacySettings(val countdown: Int = 25)

object SettingsRepo {

    private val file = File("settings.json")

    var settings by mutableStateOf(load())
        private set

    fun update(transform: (Settings) -> Settings) {
        settings = transform(settings)
        persist()
    }

    private fun load(): Settings {
        if (!file.exists()) return Settings()
        val content = file.readText()
        return try {
            appJson.decodeFromString<Settings>(content)
        } catch (e: Exception) {
            // 迁移旧版 {"countdown": n}
            try {
                Settings(focusMinutes = appJson.decodeFromString<LegacySettings>(content).countdown)
            } catch (e2: Exception) {
                e2.printStackTrace()
                Settings()
            }
        }
    }

    private fun persist() {
        try {
            file.writeText(appJson.encodeToString(settings))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
