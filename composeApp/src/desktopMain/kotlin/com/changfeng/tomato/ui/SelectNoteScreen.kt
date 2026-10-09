package com.changfeng.tomato.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.changfeng.tomato.data.NotesRepo
import com.changfeng.tomato.resources.Res
import com.changfeng.tomato.resources.no_note
import com.changfeng.tomato.resources.no_notes_hint
import com.changfeng.tomato.resources.select_note
import org.jetbrains.compose.resources.stringResource

/** 开始专注前选择要关联的便笺事项（一行一个事项）；选中后由调用方启动计时。 */
@Composable
fun SelectNoteScreen(
    onSelect: (String?) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val lines = NotesRepo.text
        .split('\n')
        .map { it.trimEnd() }
        .filter { it.isNotBlank() }

    Column(modifier.fillMaxSize().padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(4.dp),
            )
            Text(stringResource(Res.string.select_note), style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.size(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelect(null) }
                .padding(8.dp),
        ) {
            Icon(
                Icons.Default.LinkOff,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp),
            )
            Text(
                stringResource(Res.string.no_note),
                modifier = Modifier.padding(start = 8.dp),
                color = Color.Gray,
            )
        }
        HorizontalDivider()
        if (lines.isEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Note,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    stringResource(Res.string.no_notes_hint),
                    modifier = Modifier.padding(start = 8.dp),
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        LazyColumn {
            items(lines) { line ->
                val marked = isLineMarkedDone(line)
                Text(
                    line,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (marked) TextDecoration.LineThrough else null,
                    color = if (marked) Color.Gray else Color.Unspecified,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(line) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                )
            }
        }
    }
}
