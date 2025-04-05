package com.changfeng.tomato

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.changfeng.tomato.resources.Res
import com.changfeng.tomato.resources.tomato
import org.jetbrains.compose.resources.painterResource

fun main() = application {
    Window(
        state = rememberWindowState(width = 400.dp, height = 300.dp),
        onCloseRequest = ::exitApplication,
        title = "tomato",
        alwaysOnTop = true,
        icon = painterResource(Res.drawable.tomato),
    ) {
        App()
    }
}