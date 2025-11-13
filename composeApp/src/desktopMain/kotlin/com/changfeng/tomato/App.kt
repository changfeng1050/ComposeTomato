package com.changfeng.tomato

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.onClick
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.sharp.Done
import androidx.compose.material.icons.sharp.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.changfeng.tomato.resources.Res
import com.changfeng.tomato.resources.cancel
import com.changfeng.tomato.resources.create_tomato_clock
import com.changfeng.tomato.resources.current_timer
import com.changfeng.tomato.resources.discard
import com.changfeng.tomato.resources.discard_tomato
import com.changfeng.tomato.resources.discard_tomato_message
import com.changfeng.tomato.resources.save
import com.changfeng.tomato.resources.timer_duration
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import java.io.File
import java.text.SimpleDateFormat

private const val SettingsFile = "settings.json"
private const val NotesFile = "notes.json"

private data object RouteHome
private data object RouteSettings
private data object RouteTomato
private data object RouteNotes

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App(
    isNotesInSeparateWindow: Boolean = false,
    onOpenNotesInWindow: () -> Unit = {},
) {
    val currentIsNotesInSeparateWindow by rememberUpdatedState(isNotesInSeparateWindow)
    var countdown by remember { mutableStateOf(25 * 60) }
    LaunchedEffect(Unit) {
        countdown = readCountdown() * 60
    }
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = Color(0xFFE85648),
            secondary = Color(0xFF3587f4),
            onPrimary = Color.White
        ),
    ) {
        val backStack = remember { mutableStateListOf<Any>(RouteHome) }

        NavDisplay(
            backStack = backStack,
        ) { key ->
            when (key) {
                is RouteHome -> NavEntry(key) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.secondary) {
                        Home(
                            isNotesInSeparateWindow = currentIsNotesInSeparateWindow,
                            showSettings = {
                                backStack.add(RouteSettings)
                            },
                            showNotes = {
                                backStack.add(RouteNotes)
                            },
                            onStart = {
                                backStack.add(RouteTomato)
                            },
                        )
                    }
                }

                is RouteSettings -> NavEntry(key) {
                    SetCountdownDialog(
                        (countdown / 60).coerceAtLeast(1),
                        onDismissRequest = backStack::removeLastOrNull,
                        onConfirm = {
                            countdown = it * 60
                            saveCountdown(it)
                            backStack.removeLastOrNull()
                        }
                    )
                }

                is RouteTomato -> NavEntry(key) {
                    Tomato(
                        onDismissRequest = backStack::removeLastOrNull,
                        countdown = countdown
                    )
                }

                is RouteNotes -> NavEntry(key) {
                    Notes(
                        isInSeparateWindow = false,
                        onDismissRequest = backStack::removeLastOrNull,
                        onOpenInSeparateWindow = {
                            onOpenNotesInWindow()
                            backStack.removeLastOrNull()
                        }
                    )
                }

                else -> error("Unknown route: $key")
            }
        }
    }
}

@Composable
fun Home(
    isNotesInSeparateWindow: Boolean,
    showSettings: () -> Unit,
    showNotes: () -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(
                stringResource(Res.string.create_tomato_clock),
                modifier = Modifier.clickable(onClick = onStart).padding(4.dp)
            )
        }

        Row {
            if (!isNotesInSeparateWindow) {
                Icon(
                    Icons.AutoMirrored.Default.Note,
                    contentDescription = "Notes",
                    tint = Color.Gray,
                    modifier = Modifier.clickable(onClick = showNotes).rotate(-90f)
                )
            }
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.Gray,
                modifier = Modifier.clickable(onClick = showSettings)
            )
        }
    }
}

@Composable
fun SetCountdownDialog(
    countdown: Int,
    onDismissRequest: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var text by remember { mutableStateOf(countdown.toString()) }
    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = MaterialTheme.shapes.extraSmall,
        confirmButton = {
            TextButton(
                onClick = {
                    text.toIntOrNull()?.coerceAtLeast(1)?.let {
                        onConfirm(it)
                    }
                }, shape = MaterialTheme.shapes.extraSmall
            ) {
                Text(stringResource(Res.string.save))
            }
        },
        title = {
            Text(stringResource(Res.string.current_timer, text))
        },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.timer_duration))
                Spacer(Modifier.size(8.dp))
                TextField(text, onValueChange = {
                    it.filter { it in '0'..'9' }
                        .let {
                            text = it
                        }
                })
            }
        }
    )
}

@Composable
private fun ConfirmDiscardDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        shape = MaterialTheme.shapes.extraSmall,
        title = { Text(stringResource(Res.string.discard_tomato)) },
        text = { Text(stringResource(Res.string.discard_tomato_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm, shape = MaterialTheme.shapes.extraSmall) {
                Text(stringResource(Res.string.discard))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest, shape = MaterialTheme.shapes.extraSmall) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Tomato(
    onDismissRequest: () -> Unit,
    countdown: Int = 25 * 60,
    modifier: Modifier = Modifier
) {
    var state by remember { mutableStateOf(State.NotStarted) }
    var showDiscard by remember { mutableStateOf(false) }
    var remainingTime by remember { mutableStateOf(countdown) }

    LaunchedEffect(state) {
        if (state == State.NotStarted || state == State.Running) {
            remainingTime = countdown
        }
        if (state == State.Running) {
            while (remainingTime > 0) {
                remainingTime--
                delay(1000)
            }
            state = State.Finished
        }
    }

    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimary) {
        Box(
            modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary).onClick {
                when (state) {
                    State.NotStarted -> {
                        state = State.Running
                    }

                    State.Running -> {
                        showDiscard = true
                    }

                    State.Finished -> {
                        state = State.Running
                    }
                }
            }.onClick(matcher = PointerMatcher.mouse(PointerButton.Secondary)) {
                if (state == State.Running) {
                    showDiscard = true
                } else {
                    onDismissRequest()
                }
            },
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { 1 - remainingTime.toFloat() / countdown },
                modifier = Modifier.size(200.dp),
                color = LocalContentColor.current,
                strokeWidth = 6.dp,
                trackColor = Color.Black.copy(0.10f),
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp
            )
            when (state) {
                State.NotStarted -> {
                    Icon(
                        Icons.Sharp.PlayArrow,
                        contentDescription = "Start",
                        modifier = Modifier.size(80.dp)
                    )
                }

                State.Running -> {
                    Text(
                        SimpleDateFormat("mm:ss").format(remainingTime * 1000L),
                        fontSize = 48.sp
                    )
                }

                State.Finished -> {
                    Icon(
                        Icons.Sharp.Done,
                        contentDescription = "Finished",
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

        }
    }

    if (showDiscard) {
        ConfirmDiscardDialog(
            onDismissRequest = { showDiscard = false },
            onConfirm = {
                state = State.NotStarted
                remainingTime = countdown
                showDiscard = false
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun Notes(
    isInSeparateWindow: Boolean,
    onDismissRequest: () -> Unit,
    onOpenInSeparateWindow: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClose by remember { mutableStateOf(false) }
    Box(modifier.fillMaxSize().onPointerEvent(PointerEventType.Enter) {
        showClose = true
    }.onPointerEvent(PointerEventType.Exit) {
        showClose = false
    }
    ) {
        var notes by remember { mutableStateOf(readNotes()) }
        TextField(
            value = notes.text,
            onValueChange = {
                notes = notes.copy(text = it)
                saveNotes(it)
            },
            colors = TextFieldDefaults.colors(
                errorIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxSize()
        )
        if (!isInSeparateWindow) {
            AnimatedVisibility(showClose, modifier = Modifier.align(Alignment.TopEnd)) {
                Row {
                    Icon(
                        Icons.AutoMirrored.Filled.OpenInNew,
                        modifier = Modifier.padding(4.dp).clickable(onClick = onOpenInSeparateWindow),
                        contentDescription = "Open in separate window"
                    )
                    Icon(
                        Icons.Default.Clear,
                        modifier = Modifier.padding(4.dp).clickable(onClick = onDismissRequest),
                        contentDescription = "Delete"
                    )
                }
            }
        }
    }
}


@Serializable
private data class Settings(
    val countdown: Int = 25,
)

@Serializable
private data class Notes(
    val text: String = "",
)

private val json = Json {
    coerceInputValues = true
    isLenient = true
    ignoreUnknownKeys = true
    encodeDefaults = true
    useAlternativeNames = true
    allowSpecialFloatingPointValues = true
    prettyPrint = true
}

private fun readCountdown(): Int {
    val file = File(SettingsFile)
    if (!file.exists()) {
        return 25
    }
    val content = file.readText()
    return try {
        json.decodeFromString<Settings>(content).countdown
    } catch (e: Exception) {
        e.printStackTrace()
        25
    }
}

private fun saveCountdown(countdown: Int) {
    val file = File(SettingsFile)
    file.writeText(json.encodeToString(Settings(countdown)))
}

private fun readNotes(): Notes {
    val file = File(NotesFile)
    if (!file.exists()) {
        return Notes("")
    }
    val content = file.readText()
    return try {
        json.decodeFromString<Notes>(content)
    } catch (e: Exception) {
        e.printStackTrace()
        Notes("")
    }
}

private fun saveNotes(notes: String) {
    val file = File(NotesFile)
    file.writeText(json.encodeToString(Notes(notes)))
}

enum class State {
    NotStarted,
    Running,
    Finished
}