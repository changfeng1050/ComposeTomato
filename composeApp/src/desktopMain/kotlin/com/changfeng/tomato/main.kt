package com.changfeng.tomato

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.changfeng.tomato.data.Phase
import com.changfeng.tomato.data.RecordStatus
import com.changfeng.tomato.data.RecordsRepo
import com.changfeng.tomato.data.SettingsRepo
import com.changfeng.tomato.data.TomatoRecord
import com.changfeng.tomato.platform.SoundPlayer
import com.changfeng.tomato.platform.TrayController
import com.changfeng.tomato.platform.tomatoTrayImage
import com.changfeng.tomato.resources.Res
import com.changfeng.tomato.resources.app_name
import com.changfeng.tomato.resources.notify_break_finished
import com.changfeng.tomato.resources.notify_focus_finished
import com.changfeng.tomato.resources.tomato
import com.changfeng.tomato.resources.tray_exit
import com.changfeng.tomato.resources.tray_notes
import com.changfeng.tomato.resources.tray_show_hide
import com.changfeng.tomato.timer.NoteLink
import com.changfeng.tomato.timer.TomatoEngine
import com.changfeng.tomato.ui.NotesScreen
import com.changfeng.tomato.ui.TimerScreen
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** 引擎事件 → 留档 + 提示音 + 托盘通知。 */
private object EngineListener : TomatoEngine.Listener {
    var tray: TrayController? = null
    var title: String = "Tomato"
    var focusDoneMessage: String = ""
    var breakDoneMessage: String = ""

    override fun onFocusFinished(startedAtMs: Long, endedAtMs: Long, note: NoteLink?) {
        RecordsRepo.add(
            TomatoRecord(
                startEpochMs = startedAtMs,
                endEpochMs = endedAtMs,
                phase = Phase.Focus,
                status = RecordStatus.Finished,
                noteId = note?.noteId,
                noteSnapshot = note?.text,
            )
        )
    }

    override fun onFocusDiscarded(startedAtMs: Long, endedAtMs: Long, note: NoteLink?) {
        RecordsRepo.add(
            TomatoRecord(
                startEpochMs = startedAtMs,
                endEpochMs = endedAtMs,
                phase = Phase.Focus,
                status = RecordStatus.Discarded,
                noteId = note?.noteId,
                noteSnapshot = note?.text,
            )
        )
    }

    override fun onPhaseCompleted(phase: Phase) {
        if (SettingsRepo.settings.soundEnabled) {
            SoundPlayer.play()
        }
        if (SettingsRepo.settings.notificationEnabled) {
            val message = if (phase == Phase.Focus) focusDoneMessage else breakDoneMessage
            tray?.notify(title, message)
        }
    }
}

fun main() = application {
    val notifyTitle = stringResource(Res.string.app_name)
    val notifyFocusDone = stringResource(Res.string.notify_focus_finished)
    val notifyBreakDone = stringResource(Res.string.notify_break_finished)
    val trayShowHide = stringResource(Res.string.tray_show_hide)
    val trayNotes = stringResource(Res.string.tray_notes)
    val trayExit = stringResource(Res.string.tray_exit)

    LaunchedEffect(Unit) {
        try {
            SoundPlayer.load(Res.readBytes("files/ding.wav"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val saved = SettingsRepo.settings
    val tomatoWindowState = rememberWindowState(
        width = (saved.windowWidth ?: 400f).dp,
        height = (saved.windowHeight ?: 300f).dp,
        position = if (saved.windowX != null && saved.windowY != null) {
            WindowPosition(saved.windowX.dp, saved.windowY.dp)
        } else {
            WindowPosition(Alignment.TopStart)
        },
    )

    var showTomato by remember { mutableStateOf(true) }
    var showNotes by remember { mutableStateOf(false) }
    var showTimerWindow by remember { mutableStateOf(false) }

    fun saveWindowState() {
        val position = tomatoWindowState.position
        val (x, y) = when (position) {
            is WindowPosition.Absolute -> position.x.value to position.y.value
            else -> null to null
        }
        SettingsRepo.update {
            it.copy(
                windowX = x,
                windowY = y,
                windowWidth = tomatoWindowState.size.width.value,
                windowHeight = tomatoWindowState.size.height.value,
            )
        }
    }

    val engine = remember {
        TomatoEngine(
            settingsProvider = { SettingsRepo.settings },
            listener = EngineListener,
        )
    }

    val tray = remember {
        TrayController(
            image = tomatoTrayImage(),
            onToggleMainWindow = { showTomato = !showTomato },
            onShowNotes = { showNotes = true },
            onExit = {
                // 退出时若专注仍在进行，按"放弃"留档（见 CONTEXT.md「放弃」）
                engine.discard()
                saveWindowState()
                EngineListener.tray?.remove()
                exitApplication()
            },
        )
    }
    LaunchedEffect(tray) {
        EngineListener.tray = tray
        EngineListener.title = notifyTitle
        EngineListener.focusDoneMessage = notifyFocusDone
        EngineListener.breakDoneMessage = notifyBreakDone
        tray.install(
            TrayController.Labels(
                tooltip = notifyTitle,
                showHide = trayShowHide,
                notes = trayNotes,
                exit = trayExit,
            )
        )
    }

    if (showTomato) {
        Window(
            state = tomatoWindowState,
            onCloseRequest = {
                // 关闭窗口 = 隐藏到托盘，计时继续；托盘不可用时才真正退出
                saveWindowState()
                if (tray.isInstalled) {
                    showTomato = false
                } else {
                    engine.discard()
                    exitApplication()
                }
            },
            title = "tomato",
            alwaysOnTop = true,
            icon = painterResource(Res.drawable.tomato),
        ) {
            App(
                engine = engine,
                onOpenNotesInWindow = { showNotes = true },
                onOpenTimerInWindow = { showTimerWindow = true },
            )
        }
    }

    if (showTimerWindow) {
        Window(
            state = rememberWindowState(
                width = 400.dp,
                height = 300.dp,
                position = WindowPosition(
                    x = ((tomatoWindowState.position as? WindowPosition.Absolute)?.x ?: 0.dp) +
                            tomatoWindowState.size.width,
                    y = (tomatoWindowState.position as? WindowPosition.Absolute)?.y ?: 0.dp,
                ),
            ),
            onCloseRequest = {
                showTimerWindow = false
            },
            title = "tomato",
            alwaysOnTop = true,
            icon = painterResource(Res.drawable.tomato),
        ) {
            // 独立时钟窗与主窗共享同一个 engine，计时状态实时一致
            TimerScreen(
                engine = engine,
                isInSeparateWindow = true,
                onDismissRequest = { showTimerWindow = false },
            )
        }
    }

    if (showNotes) {
        Window(
            state = rememberWindowState(
                width = 400.dp,
                height = 400.dp,
                position = WindowPosition(
                    x = (tomatoWindowState.position as? WindowPosition.Absolute)?.x ?: 0.dp,
                    y = ((tomatoWindowState.position as? WindowPosition.Absolute)?.y ?: 0.dp) +
                            tomatoWindowState.size.height,
                ),
            ),
            onCloseRequest = {
                showNotes = false
            },
            title = "notes",
            alwaysOnTop = true,
            icon = painterResource(Res.drawable.tomato),
        ) {
            NotesScreen(
                isInSeparateWindow = true,
            )
        }
    }
}
