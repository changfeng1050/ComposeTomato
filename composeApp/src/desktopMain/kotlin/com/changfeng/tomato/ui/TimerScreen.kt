package com.changfeng.tomato.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.sharp.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.changfeng.tomato.data.Phase
import com.changfeng.tomato.data.SettingsRepo
import com.changfeng.tomato.resources.Res
import com.changfeng.tomato.resources.cancel
import com.changfeng.tomato.resources.discard
import com.changfeng.tomato.resources.discard_tomato
import com.changfeng.tomato.resources.discard_tomato_message
import com.changfeng.tomato.resources.paused
import com.changfeng.tomato.resources.phase_focus
import com.changfeng.tomato.resources.phase_long_break
import com.changfeng.tomato.resources.phase_short_break
import com.changfeng.tomato.resources.skip_break
import com.changfeng.tomato.timer.TimerState
import com.changfeng.tomato.timer.TomatoEngine
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun phaseLabel(phase: Phase): String = when (phase) {
    Phase.Focus -> stringResource(Res.string.phase_focus)
    Phase.ShortBreak -> stringResource(Res.string.phase_short_break)
    Phase.LongBreak -> stringResource(Res.string.phase_long_break)
}

private fun formatTime(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%02d:%02d".format(m, s)
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun TimerScreen(
    engine: TomatoEngine,
    isInSeparateWindow: Boolean,
    onDismissRequest: () -> Unit,
    onOpenInSeparateWindow: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showDiscard by remember { mutableStateOf(false) }
    val isFocus = engine.phase == Phase.Focus
    val backgroundColor = if (isFocus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Box(
            modifier
                .fillMaxSize()
                .background(backgroundColor)
                .onClick {
                    when (engine.phaseState) {
                        TimerState.NotStarted -> engine.start()
                        TimerState.Running -> if (isFocus) engine.pause()
                        TimerState.Paused -> if (isFocus) engine.resume()
                    }
                }
                .onClick(matcher = PointerMatcher.mouse(PointerButton.Secondary)) {
                    if (isFocus && engine.phaseState != TimerState.NotStarted) {
                        showDiscard = true
                    } else {
                        onDismissRequest()
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    phaseLabel(engine.phase),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = {
                            1f - engine.remainingSeconds.toFloat() / engine.totalSeconds.coerceAtLeast(1)
                        },
                        modifier = Modifier.size(200.dp),
                        color = LocalContentColor.current,
                        strokeWidth = 6.dp,
                        trackColor = Color.Black.copy(0.10f),
                        strokeCap = StrokeCap.Round,
                        gapSize = 0.dp,
                    )
                    when (engine.phaseState) {
                        TimerState.NotStarted -> Icon(
                            Icons.Sharp.PlayArrow,
                            contentDescription = "Start",
                            modifier = Modifier.size(80.dp),
                        )

                        TimerState.Running, TimerState.Paused -> Text(
                            formatTime(engine.remainingSeconds),
                            fontSize = 48.sp,
                        )
                    }
                }
                if (engine.phaseState == TimerState.Paused) {
                    Text(
                        stringResource(Res.string.paused),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                // 本轮循环进度点
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 16.dp),
                ) {
                    val interval = SettingsRepo.settings.longBreakInterval.coerceAtLeast(1)
                    repeat(interval) { index ->
                        Box(
                            Modifier
                                .size(10.dp)
                                .background(
                                    if (index < engine.completedInCycle.coerceAtMost(interval)) Color.White
                                    else Color.White.copy(alpha = 0.25f),
                                    CircleShape,
                                ),
                        )
                        if (index < interval - 1) Spacer(Modifier.size(6.dp))
                    }
                }
            }

            if (!isInSeparateWindow) {
                Icon(
                    Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open in separate window",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clickable(onClick = onOpenInSeparateWindow)
                        .padding(10.dp)
                        .size(18.dp),
                )
            }
            if (!isFocus) {
                Text(
                    stringResource(Res.string.skip_break),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .clickable { engine.skipBreak() }
                        .padding(16.dp),
                )
            }
        }
    }

    if (showDiscard) {
        AlertDialog(
            onDismissRequest = { showDiscard = false },
            shape = MaterialTheme.shapes.extraSmall,
            title = { Text(stringResource(Res.string.discard_tomato)) },
            text = { Text(stringResource(Res.string.discard_tomato_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        engine.discard()
                        showDiscard = false
                    },
                    shape = MaterialTheme.shapes.extraSmall,
                ) {
                    Text(stringResource(Res.string.discard))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDiscard = false },
                    shape = MaterialTheme.shapes.extraSmall,
                ) {
                    Text(stringResource(Res.string.cancel))
                }
            },
        )
    }
}
