package com.changfeng.tomato.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.changfeng.tomato.data.SettingsRepo
import com.changfeng.tomato.resources.Res
import com.changfeng.tomato.resources.auto_start_break
import com.changfeng.tomato.resources.auto_start_focus
import com.changfeng.tomato.resources.focus_minutes
import com.changfeng.tomato.resources.long_break_interval
import com.changfeng.tomato.resources.long_break_minutes
import com.changfeng.tomato.resources.notification
import com.changfeng.tomato.resources.settings
import com.changfeng.tomato.resources.short_break_minutes
import com.changfeng.tomato.resources.sound
import org.jetbrains.compose.resources.stringResource

/** 设置页：8 项，修改即持久化。 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings = SettingsRepo.settings

    Column(modifier.fillMaxSize().padding(8.dp).verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(4.dp),
            )
            Text(stringResource(Res.string.settings), style = MaterialTheme.typography.titleMedium)
        }
        HorizontalDivider()

        NumberSettingRow(
            label = stringResource(Res.string.focus_minutes),
            value = settings.focusMinutes,
            onValueChange = { SettingsRepo.update { s -> s.copy(focusMinutes = it) } },
        )
        NumberSettingRow(
            label = stringResource(Res.string.short_break_minutes),
            value = settings.shortBreakMinutes,
            onValueChange = { SettingsRepo.update { s -> s.copy(shortBreakMinutes = it) } },
        )
        NumberSettingRow(
            label = stringResource(Res.string.long_break_minutes),
            value = settings.longBreakMinutes,
            onValueChange = { SettingsRepo.update { s -> s.copy(longBreakMinutes = it) } },
        )
        NumberSettingRow(
            label = stringResource(Res.string.long_break_interval),
            value = settings.longBreakInterval,
            onValueChange = { SettingsRepo.update { s -> s.copy(longBreakInterval = it) } },
        )
        ToggleSettingRow(
            label = stringResource(Res.string.auto_start_break),
            checked = settings.autoStartBreak,
            onCheckedChange = { SettingsRepo.update { s -> s.copy(autoStartBreak = it) } },
        )
        ToggleSettingRow(
            label = stringResource(Res.string.auto_start_focus),
            checked = settings.autoStartFocus,
            onCheckedChange = { SettingsRepo.update { s -> s.copy(autoStartFocus = it) } },
        )
        ToggleSettingRow(
            label = stringResource(Res.string.sound),
            checked = settings.soundEnabled,
            onCheckedChange = { SettingsRepo.update { s -> s.copy(soundEnabled = it) } },
        )
        ToggleSettingRow(
            label = stringResource(Res.string.notification),
            checked = settings.notificationEnabled,
            onCheckedChange = { SettingsRepo.update { s -> s.copy(notificationEnabled = it) } },
        )
    }
}

@Composable
private fun NumberSettingRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember(value) { mutableStateOf(value.toString()) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
    ) {
        Text(label, modifier = Modifier.weight(1f))
        TextField(
            value = text,
            onValueChange = { input ->
                text = input.filter { it in '0'..'9' }.take(3)
                text.toIntOrNull()?.let { onValueChange(it.coerceIn(1, 999)) }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.width(90.dp),
        )
    }
}

@Composable
private fun ToggleSettingRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
