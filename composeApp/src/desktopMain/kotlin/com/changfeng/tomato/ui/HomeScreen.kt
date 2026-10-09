package com.changfeng.tomato.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.changfeng.tomato.data.Phase
import com.changfeng.tomato.data.RecordsRepo
import com.changfeng.tomato.resources.Res
import com.changfeng.tomato.resources.back_to_timer
import com.changfeng.tomato.resources.start_focus
import com.changfeng.tomato.resources.today_progress
import com.changfeng.tomato.timer.TimerState
import com.changfeng.tomato.timer.TomatoEngine
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(
    engine: TomatoEngine,
    onStart: () -> Unit,
    showNotes: () -> Unit,
    showHistory: () -> Unit,
    showSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val finishedToday = RecordsRepo.finishedToday()
    val canStartNew = engine.phase == Phase.Focus && engine.phaseState == TimerState.NotStarted
    Column(modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(Res.string.today_progress, finishedToday),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.size(16.dp))
                Text(
                    if (canStartNew) stringResource(Res.string.start_focus)
                    else stringResource(Res.string.back_to_timer),
                    fontSize = 28.sp,
                    modifier = Modifier
                        .clickable(onClick = onStart)
                        .padding(8.dp),
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.AutoMirrored.Default.Note,
                contentDescription = "Notes",
                tint = Color.Gray,
                modifier = Modifier
                    .clickable(onClick = showNotes)
                    .rotate(-90f),
            )
            Spacer(Modifier.size(16.dp))
            Icon(
                Icons.Default.History,
                contentDescription = "History",
                tint = Color.Gray,
                modifier = Modifier.clickable(onClick = showHistory),
            )
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.Gray,
                modifier = Modifier.clickable(onClick = showSettings),
            )
        }
    }
}

