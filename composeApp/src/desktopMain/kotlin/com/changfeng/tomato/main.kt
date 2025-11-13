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
import com.changfeng.tomato.resources.Res
import com.changfeng.tomato.resources.tomato
import org.jetbrains.compose.resources.painterResource

fun main() = application {
    val tomatoWidowState = rememberWindowState(
        width = 400.dp,
        height = 300.dp,
        position = WindowPosition(Alignment.TopStart)
    )
    var showTomato by remember { mutableStateOf(true) }
    var showNotes by remember { mutableStateOf(false) }
    LaunchedEffect(showTomato, showNotes) {
        if (!showTomato && !showNotes) {
            exitApplication()
        }
    }

    if (showTomato) {
        Window(
            state = tomatoWidowState,
            onCloseRequest = {
                showTomato = false
            },
            title = "tomato",
            alwaysOnTop = true,
            icon = painterResource(Res.drawable.tomato),
        ) {
            App(
                isNotesInSeparateWindow = showNotes,
                onOpenNotesInWindow = {
                    showNotes = true
                })
        }
    }

    if (showNotes) {
        Window(
            state = rememberWindowState(
                width = 400.dp, height = 300.dp, position = WindowPosition(
                    x = tomatoWidowState.position.x,
                    y = tomatoWidowState.position.y + tomatoWidowState.size.height
                )
            ),
            onCloseRequest = {
                showNotes = false
            },
            title = "notes",
            alwaysOnTop = true,
            icon = painterResource(Res.drawable.tomato)
        ) {
            Notes(
                isInSeparateWindow = true,
                onDismissRequest = {
                    showNotes = false
                }, onOpenInSeparateWindow = {
                    showNotes = true
                })
        }
    }
}