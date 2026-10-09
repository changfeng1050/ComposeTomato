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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.sharp.Done
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.changfeng.tomato.data.RecordStatus
import com.changfeng.tomato.data.RecordsRepo
import com.changfeng.tomato.data.TomatoRecord
import com.changfeng.tomato.resources.Res
import com.changfeng.tomato.resources.history
import com.changfeng.tomato.resources.no_records
import org.jetbrains.compose.resources.stringResource
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd EEEE")

/** 番茄记录历史：按日期分组倒序，含完成与放弃的专注。 */
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val records = RecordsRepo.records.sortedByDescending { it.endEpochMs }
    val zone = ZoneId.systemDefault()
    val grouped = records.groupBy { Instant.ofEpochMilli(it.endEpochMs).atZone(zone).toLocalDate() }

    Column(modifier.fillMaxSize().padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(4.dp),
            )
            Text(stringResource(Res.string.history), style = MaterialTheme.typography.titleMedium)
        }
        if (grouped.isEmpty()) {
            Text(
                stringResource(Res.string.no_records),
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        LazyColumn {
            grouped.forEach { (date, dayRecords) ->
                item(key = "header-$date") {
                    Text(
                        date.format(dateFormatter),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    HorizontalDivider()
                }
                items(dayRecords, key = { "record-${it.startEpochMs}" }) { record ->
                    RecordRow(record)
                }
            }
        }
    }
}

@Composable
private fun RecordRow(record: TomatoRecord, modifier: Modifier = Modifier) {
    val zone = ZoneId.systemDefault()
    val start = Instant.ofEpochMilli(record.startEpochMs).atZone(zone).format(timeFormatter)
    val end = Instant.ofEpochMilli(record.endEpochMs).atZone(zone).format(timeFormatter)
    val discarded = record.status == RecordStatus.Discarded
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
    ) {
        Icon(
            if (discarded) Icons.Default.Close else Icons.Sharp.Done,
            contentDescription = record.status.name,
            tint = if (discarded) Color(0xFF9E9E9E) else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
        )
        Text(
            "$start – $end",
            fontSize = 14.sp,
            color = if (discarded) Color.Gray else Color.Unspecified,
            modifier = Modifier.padding(start = 8.dp),
        )
        Spacer(Modifier.weight(1f))
        record.noteSnapshot?.takeIf { it.isNotBlank() }?.let { snapshot ->
            Text(
                snapshot.split('\n').first(),
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp).weight(2f, fill = false),
            )
        }
    }
}
