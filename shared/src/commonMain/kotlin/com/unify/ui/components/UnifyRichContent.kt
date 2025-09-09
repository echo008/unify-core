package com.unify.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Unify富文本内容组件
 * 支持多种富文本格式和交互功能
 */

/**
 * 富文本编辑器
 */
@Composable
fun UnifyRichTextEditor(
    content: String,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "请输入内容...",
    maxLines: Int = Int.MAX_VALUE,
) {
    var text by remember { mutableStateOf(content) }

    LaunchedEffect(content) {
        text = content
    }

    Column(modifier = modifier) {
        // 工具栏
        UnifyRichTextToolbar(
            onFormatClick = { format ->
                // 处理格式化操作
                text = applyFormat(text, format)
                onContentChange(text)
            },
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 文本编辑区域
        OutlinedTextField(
            value = text,
            onValueChange = { newText ->
                text = newText
                onContentChange(newText)
            },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = maxLines,
            textStyle = MaterialTheme.typography.bodyMedium,
        )
    }
}

/**
 * 富文本工具栏
 */
@Composable
private fun UnifyRichTextToolbar(
    onFormatClick: (RichTextFormat) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 粗体按钮
        IconButton(onClick = { onFormatClick(RichTextFormat.Bold) }) {
            Text("B", fontWeight = FontWeight.Bold)
        }

        // 斜体按钮
        IconButton(onClick = { onFormatClick(RichTextFormat.Italic) }) {
            Text("I", fontStyle = FontStyle.Italic)
        }

        // 下划线按钮
        IconButton(onClick = { onFormatClick(RichTextFormat.Underline) }) {
            Text("U", textDecoration = TextDecoration.Underline)
        }

        // 标题按钮
        IconButton(onClick = { onFormatClick(RichTextFormat.Header) }) {
            Text("H", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        // 列表按钮
        IconButton(onClick = { onFormatClick(RichTextFormat.List) }) {
            Text("•")
        }

        // 链接按钮
        IconButton(onClick = { onFormatClick(RichTextFormat.Link) }) {
            Text("🔗")
        }
    }
}

/**
 * 富文本显示器
 */
@Composable
fun UnifyRichTextViewer(
    content: String,
    modifier: Modifier = Modifier,
) {
    val parsedContent = parseRichText(content)

    SelectionContainer {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(parsedContent) { element ->
                when (element) {
                    is RichTextElement.Header -> {
                        Text(
                            text = element.text,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    is RichTextElement.Paragraph -> {
                        Text(
                            text = element.annotatedString,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    is RichTextElement.ListItem -> {
                        Row {
                            Text("• ", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = element.text,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    is RichTextElement.CodeBlock -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                ),
                        ) {
                            Text(
                                text = element.code,
                                modifier = Modifier.padding(12.dp),
                                style =
                                    MaterialTheme.typography.bodySmall.copy(
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    ),
                            )
                        }
                    }
                    is RichTextElement.Quote -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                ),
                        ) {
                            Text(
                                text = element.text,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Markdown渲染器
 */
@Composable
fun UnifyMarkdownRenderer(
    markdown: String,
    modifier: Modifier = Modifier,
) {
    val elements = parseMarkdown(markdown)

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(elements) { element ->
            when (element) {
                is MarkdownElement.H1 -> {
                    Text(
                        text = element.text,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
                is MarkdownElement.H2 -> {
                    Text(
                        text = element.text,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                is MarkdownElement.H3 -> {
                    Text(
                        text = element.text,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                is MarkdownElement.Paragraph -> {
                    Text(
                        text = element.text,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                is MarkdownElement.Code -> {
                    Card(
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                    ) {
                        Text(
                            text = element.code,
                            modifier = Modifier.padding(12.dp),
                            style =
                                MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                ),
                        )
                    }
                }
                is MarkdownElement.ListElement -> {
                    Column {
                        element.items.forEach { item: String ->
                            Row {
                                Text("• ")
                                Text(item)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 代码高亮显示器
 */
@Composable
fun UnifyCodeHighlighter(
    code: String,
    language: String = "kotlin",
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = Color(0xFF2B2B2B),
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            // 语言标签
            Text(
                text = language.uppercase(),
                color = Color(0xFF6C7B7F),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // 代码内容
            SelectionContainer {
                Text(
                    text = highlightCode(code, language),
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        ),
                    color = Color.White,
                )
            }
        }
    }
}

/**
 * 表格组件
 */
@Composable
fun UnifyTable(
    headers: List<String>,
    rows: List<List<String>>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column {
            // 表头
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
            ) {
                headers.forEach { header ->
                    Text(
                        text = header,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            HorizontalDivider()

            // 表格行
            rows.forEach { row ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                ) {
                    row.forEach { cell ->
                        Text(
                            text = cell,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

// 数据类和枚举
enum class RichTextFormat {
    Bold,
    Italic,
    Underline,
    Header,
    List,
    Link,
}

sealed class RichTextElement {
    data class Header(val text: String, val level: Int = 1) : RichTextElement()

    data class Paragraph(val annotatedString: androidx.compose.ui.text.AnnotatedString) : RichTextElement()

    data class ListItem(val text: String) : RichTextElement()

    data class CodeBlock(val code: String, val language: String = "") : RichTextElement()

    data class Quote(val text: String) : RichTextElement()
}

sealed class MarkdownElement {
    data class H1(val text: String) : MarkdownElement()

    data class H2(val text: String) : MarkdownElement()

    data class H3(val text: String) : MarkdownElement()

    data class Paragraph(val text: String) : MarkdownElement()

    data class Code(val code: String) : MarkdownElement()

    data class ListElement(val items: List<String>) : MarkdownElement()
}

// 辅助函数
private fun applyFormat(
    text: String,
    format: RichTextFormat,
): String {
    return when (format) {
        RichTextFormat.Bold -> "**$text**"
        RichTextFormat.Italic -> "*$text*"
        RichTextFormat.Underline -> "<u>$text</u>"
        RichTextFormat.Header -> "# $text"
        RichTextFormat.List -> "• $text"
        RichTextFormat.Link -> "[$text](url)"
    }
}

private fun parseRichText(content: String): List<RichTextElement> {
    val elements = mutableListOf<RichTextElement>()
    val lines = content.split("\n")

    for (line in lines) {
        when {
            line.startsWith("# ") -> {
                elements.add(RichTextElement.Header(line.substring(2)))
            }
            line.startsWith("• ") -> {
                elements.add(RichTextElement.ListItem(line.substring(2)))
            }
            line.startsWith("```") -> {
                // 简化的代码块处理
                elements.add(RichTextElement.CodeBlock(line.substring(3)))
            }
            line.startsWith("> ") -> {
                elements.add(RichTextElement.Quote(line.substring(2)))
            }
            else -> {
                val annotatedString =
                    buildAnnotatedString {
                        var currentText = line

                        // 处理粗体
                        while (currentText.contains("**")) {
                            val start = currentText.indexOf("**")
                            val end = currentText.indexOf("**", start + 2)
                            if (end != -1) {
                                append(currentText.substring(0, start))
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(currentText.substring(start + 2, end))
                                }
                                currentText = currentText.substring(end + 2)
                            } else {
                                break
                            }
                        }
                        append(currentText)
                    }
                elements.add(RichTextElement.Paragraph(annotatedString))
            }
        }
    }

    return elements
}

private fun parseMarkdown(markdown: String): List<MarkdownElement> {
    val elements = mutableListOf<MarkdownElement>()
    val lines = markdown.split("\n")

    for (line in lines) {
        when {
            line.startsWith("# ") -> elements.add(MarkdownElement.H1(line.substring(2)))
            line.startsWith("## ") -> elements.add(MarkdownElement.H2(line.substring(3)))
            line.startsWith("### ") -> elements.add(MarkdownElement.H3(line.substring(4)))
            line.startsWith("```") -> elements.add(MarkdownElement.Code(line.substring(3)))
            line.startsWith("- ") || line.startsWith("* ") -> {
                // 简化的列表处理
                elements.add(MarkdownElement.ListElement(listOf(line.substring(2))))
            }
            line.isNotBlank() -> elements.add(MarkdownElement.Paragraph(line))
        }
    }

    return elements
}

private fun highlightCode(
    code: String,
    language: String,
): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        // 简化的语法高亮
        val keywords =
            when (language.lowercase()) {
                "kotlin" -> listOf("fun", "val", "var", "class", "interface", "object", "if", "else", "when", "for", "while")
                "java" -> listOf("public", "private", "protected", "class", "interface", "if", "else", "for", "while")
                else -> emptyList()
            }

        var currentText = code
        for (keyword in keywords) {
            currentText = currentText.replace(keyword, "**$keyword**")
        }

        append(currentText)
    }
}
