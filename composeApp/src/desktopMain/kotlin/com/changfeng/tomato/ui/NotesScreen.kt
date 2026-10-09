package com.changfeng.tomato.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.changfeng.tomato.data.NotesRepo
import com.changfeng.tomato.resources.Res
import com.changfeng.tomato.resources.notes
import com.changfeng.tomato.resources.notes_hint
import org.jetbrains.compose.resources.stringResource

/**
 * 便笺：单个多行 TextField，一行一个事项。
 * Alt+Enter 将光标所在行标记为完成（行首加 `[x]`，渲染划线），再按一次取消。
 * 主窗内显示返回按钮与"打开独立窗口"按钮；独立窗口模式下只保留编辑区。
 */
@Composable
fun NotesScreen(
    isInSeparateWindow: Boolean,
    onDismissRequest: () -> Unit = {},
    onOpenInSeparateWindow: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var value by remember { mutableStateOf(TextFieldValue(NotesRepo.text)) }

    Column(modifier.fillMaxSize().padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isInSeparateWindow) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .clickable(onClick = onDismissRequest)
                        .padding(4.dp),
                )
            }
            Text(stringResource(Res.string.notes), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(Res.string.notes_hint),
                color = Color.Gray,
                style = MaterialTheme.typography.labelSmall,
            )
            if (!isInSeparateWindow) {
                Icon(
                    Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open in separate window",
                    modifier = Modifier
                        .clickable(onClick = onOpenInSeparateWindow)
                        .padding(4.dp),
                )
            }
        }
        HorizontalDivider()
        BasicTextField(
            value = value,
            onValueChange = {
                value = it
                NotesRepo.update(it.text)
            },
            textStyle = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            visualTransformation = ChecklistStrikethroughTransformation,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown &&
                        event.key == Key.Enter &&
                        event.isAltPressed
                    ) {
                        val (newText, newCursor) = toggleLineAtCursor(
                            value.text,
                            value.selection.min,
                        )
                        value = TextFieldValue(newText, TextRange(newCursor))
                        NotesRepo.update(newText)
                        true
                    } else {
                        false
                    }
                },
        )
    }
}
