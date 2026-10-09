package com.changfeng.tomato.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration

private val markedRegex = Regex("^(\\s*)\\[[xX]]\\s?")
private val unmarkedRegex = Regex("^(\\s*)\\[ ]\\s?")
private val strikeColor = Color(0xFF9E9E9E)

/** 便笺行是否为已完成事项（`[x]` 开头）。 */
fun isLineMarkedDone(line: String): Boolean =
    line.trimStart().startsWith("[x]") || line.trimStart().startsWith("[X]")

/**
 * 便笺文本变换：以 `[x]` 开头的行渲染为划线 + 灰色，编辑能力不受影响。
 */
val ChecklistStrikethroughTransformation = VisualTransformation { annotated ->
    val source = annotated.text
    val builder = AnnotatedString.Builder(source)
    var lineStart = 0
    while (lineStart < source.length) {
        val lineEnd = source.indexOf('\n', lineStart).let { if (it == -1) source.length else it }
        if (isLineMarkedDone(source.substring(lineStart, lineEnd))) {
            builder.addStyle(
                SpanStyle(textDecoration = TextDecoration.LineThrough, color = strikeColor),
                lineStart,
                lineEnd,
            )
        }
        if (lineEnd == source.length) break
        lineStart = lineEnd + 1
    }
    TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
}

/**
 * Alt+Enter：切换光标所在事项行的完成状态。
 * 未标记行 → 行首（缩进后）插入 `[x] `；已标记行 → 移除标记。
 * 返回新文本和调整后的光标位置。
 */
fun toggleLineAtCursor(text: String, cursor: Int): Pair<String, Int> {
    val clamped = cursor.coerceIn(0, text.length)
    val lineStart = when (val idx = text.lastIndexOf('\n', (clamped - 1).coerceAtLeast(0))) {
        -1 -> 0
        else -> idx + 1
    }
    val lineEnd = text.indexOf('\n', lineStart).let { if (it == -1) text.length else it }
    val line = text.substring(lineStart, lineEnd)
    val newLine = when {
        markedRegex.containsMatchIn(line) -> line.replaceFirst(markedRegex, "$1")
        unmarkedRegex.containsMatchIn(line) -> line.replaceFirst("[ ]", "[x]")
        else -> {
            val indent = line.takeWhile { it == ' ' || it == '\t' }
            indent + "[x] " + line.substring(indent.length)
        }
    }
    val newText = text.substring(0, lineStart) + newLine + text.substring(lineEnd)
    val newCursor = (clamped + newLine.length - line.length).coerceIn(0, newText.length)
    return newText to newCursor
}
