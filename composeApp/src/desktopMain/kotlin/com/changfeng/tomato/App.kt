package com.changfeng.tomato

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.changfeng.tomato.data.Phase
import com.changfeng.tomato.data.SettingsRepo
import com.changfeng.tomato.timer.NoteLink
import com.changfeng.tomato.timer.TimerState
import com.changfeng.tomato.timer.TomatoEngine
import com.changfeng.tomato.ui.HistoryScreen
import com.changfeng.tomato.ui.HomeScreen
import com.changfeng.tomato.ui.NotesScreen
import com.changfeng.tomato.ui.SelectNoteScreen
import com.changfeng.tomato.ui.SettingsScreen
import com.changfeng.tomato.ui.TimerScreen

private data object RouteHome
private data object RouteSettings
private data object RouteTimer
private data object RouteSelectNote
private data object RouteNotes
private data object RouteHistory

@Composable
fun App(
    engine: TomatoEngine,
    onOpenNotesInWindow: () -> Unit = {},
    onOpenTimerInWindow: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = Color(0xFFE85648),
            secondary = Color(0xFF3587f4),
            onPrimary = Color.White
        ),
    ) {
        val settings = SettingsRepo.settings
        // 时长设置变化后，刷新待开始阶段的剩余时间显示
        LaunchedEffect(settings.focusMinutes, settings.shortBreakMinutes, settings.longBreakMinutes) {
            engine.refreshRemainingIfIdle()
        }

        val backStack = remember { mutableStateListOf<Any>(RouteHome) }

        NavDisplay(
            backStack = backStack,
            modifier = modifier,
        ) { key ->
            when (key) {
                is RouteHome -> NavEntry(key) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.secondary) {
                        HomeScreen(
                            engine = engine,
                            onStart = {
                                if (engine.phase == Phase.Focus && engine.phaseState == TimerState.NotStarted) {
                                    backStack.add(RouteSelectNote)
                                } else {
                                    backStack.add(RouteTimer)
                                }
                            },
                            showNotes = { backStack.add(RouteNotes) },
                            showHistory = { backStack.add(RouteHistory) },
                            showSettings = { backStack.add(RouteSettings) },
                        )
                    }
                }

                is RouteSelectNote -> NavEntry(key) {
                    SelectNoteScreen(
                        onSelect = { lineText: String? ->
                            engine.linkNote(lineText?.let { NoteLink(text = it) })
                            if (engine.phaseState == TimerState.NotStarted) {
                                engine.start()
                            }
                            backStack.removeLastOrNull()
                            backStack.add(RouteTimer)
                        },
                        onBack = backStack::removeLastOrNull,
                    )
                }

                is RouteTimer -> NavEntry(key) {
                    TimerScreen(
                        engine = engine,
                        isInSeparateWindow = false,
                        onDismissRequest = backStack::removeLastOrNull,
                        onOpenInSeparateWindow = {
                            backStack.removeLastOrNull()
                            onOpenTimerInWindow()
                        },
                    )
                }

                is RouteNotes -> NavEntry(key) {
                    NotesScreen(
                        isInSeparateWindow = false,
                        onDismissRequest = backStack::removeLastOrNull,
                        onOpenInSeparateWindow = {
                            backStack.removeLastOrNull()
                            onOpenNotesInWindow()
                        },
                    )
                }

                is RouteHistory -> NavEntry(key) {
                    HistoryScreen(onBack = backStack::removeLastOrNull)
                }

                is RouteSettings -> NavEntry(key) {
                    SettingsScreen(onBack = backStack::removeLastOrNull)
                }

                else -> error("Unknown route: $key")
            }
        }
    }
}
