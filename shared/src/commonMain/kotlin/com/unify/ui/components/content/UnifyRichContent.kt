package com.unify.ui.components.content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Unify跨平台富文本内容组件
 * 支持所有8大平台的统一富文本显示
 */

data class UnifyRichContentBlock(
    val type: UnifyContentType,
    val content: String,
    val style: UnifyContentStyle = UnifyContentStyle(),
    val metadata: Map<String, Any> = emptyMap()
)

enum class UnifyContentType {
    HEADING1, HEADING2, HEADING3, 
    PARAGRAPH, QUOTE, CODE, 
    LIST_ITEM, NUMBERED_LIST_ITEM,
    DIVIDER, IMAGE, LINK
}

data class UnifyContentStyle(
    val textColor: Color? = null,
    val backgroundColor: Color? = null,
    val fontWeight: FontWeight? = null,
    val textAlign: TextAlign? = null
)

@Composable
fun UnifyRichContent(
    blocks: List<UnifyRichContentBlock>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(blocks) { block ->
            UnifyContentBlockRenderer(block = block)
        }
    }
}

@Composable
private fun UnifyContentBlockRenderer(
    block: UnifyRichContentBlock,
    modifier: Modifier = Modifier
) {
    when (block.type) {
        UnifyContentType.HEADING1 -> {
            Text(
                text = block.content,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = block.style.fontWeight ?: FontWeight.Bold,
                color = block.style.textColor ?: MaterialTheme.colorScheme.onSurface,
                textAlign = block.style.textAlign ?: TextAlign.Start,
                modifier = modifier.fillMaxWidth()
            )
        }
        
        UnifyContentType.HEADING2 -> {
            Text(
                text = block.content,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = block.style.fontWeight ?: FontWeight.Bold,
                color = block.style.textColor ?: MaterialTheme.colorScheme.onSurface,
                textAlign = block.style.textAlign ?: TextAlign.Start,
                modifier = modifier.fillMaxWidth()
            )
        }
        
        UnifyContentType.HEADING3 -> {
            Text(
                text = block.content,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = block.style.fontWeight ?: FontWeight.Bold,
                color = block.style.textColor ?: MaterialTheme.colorScheme.onSurface,
                textAlign = block.style.textAlign ?: TextAlign.Start,
                modifier = modifier.fillMaxWidth()
            )
        }
        
        UnifyContentType.PARAGRAPH -> {
            Text(
                text = block.content,
                style = MaterialTheme.typography.bodyLarge,
                color = block.style.textColor ?: MaterialTheme.colorScheme.onSurface,
                textAlign = block.style.textAlign ?: TextAlign.Start,
                modifier = modifier.fillMaxWidth()
            )
        }
        
        UnifyContentType.QUOTE -> {
            Card(
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = block.style.backgroundColor 
                        ?: MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "❝",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = block.content,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = block.style.textColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        UnifyContentType.CODE -> {
            Card(
                modifier = modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E1E1E)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = block.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF00FF00),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        UnifyContentType.LIST_ITEM -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp, top = 2.dp)
                )
                Text(
                    text = block.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = block.style.textColor ?: MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        UnifyContentType.NUMBERED_LIST_ITEM -> {
            val number = block.metadata["number"] as? Int ?: 1
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "$number.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(end = 8.dp, top = 2.dp)
                )
                Text(
                    text = block.content,
                    style = MaterialTheme.typography.bodyLarge,
                    color = block.style.textColor ?: MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        UnifyContentType.DIVIDER -> {
            HorizontalDivider(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outline
            )
        }
        
        UnifyContentType.IMAGE -> {
            Card(
                modifier = modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
                    // 图片占位符
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🖼️ ${block.content}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    val caption = block.metadata["caption"] as? String
                    if (caption != null) {
                        Text(
                            text = caption,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        )
                    }
                }
            }
        }
        
        UnifyContentType.LINK -> {
            TextButton(
                onClick = {
                    // 处理链接点击
                },
                modifier = modifier
            ) {
                Text(
                    text = "🔗 ${block.content}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun UnifyArticle(
    title: String,
    author: String? = null,
    publishDate: String? = null,
    content: List<UnifyRichContentBlock>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (author != null || publishDate != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (author != null) {
                            Text(
                                text = "作者: $author",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (publishDate != null) {
                            Text(
                                text = publishDate,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        
        items(content) { block ->
            UnifyContentBlockRenderer(block = block)
        }
    }
}

@Composable
fun UnifyMarkdownRenderer(
    markdown: String,
    modifier: Modifier = Modifier
) {
    val blocks = remember(markdown) {
        parseMarkdownToBlocks(markdown)
    }
    
    UnifyRichContent(
        blocks = blocks,
        modifier = modifier
    )
}

private fun parseMarkdownToBlocks(markdown: String): List<UnifyRichContentBlock> {
    val blocks = mutableListOf<UnifyRichContentBlock>()
    val lines = markdown.split("\n")
    
    lines.forEach { line ->
        when {
            line.startsWith("# ") -> {
                blocks.add(
                    UnifyRichContentBlock(
                        type = UnifyContentType.HEADING1,
                        content = line.substring(2)
                    )
                )
            }
            line.startsWith("## ") -> {
                blocks.add(
                    UnifyRichContentBlock(
                        type = UnifyContentType.HEADING2,
                        content = line.substring(3)
                    )
                )
            }
            line.startsWith("### ") -> {
                blocks.add(
                    UnifyRichContentBlock(
                        type = UnifyContentType.HEADING3,
                        content = line.substring(4)
                    )
                )
            }
            line.startsWith("> ") -> {
                blocks.add(
                    UnifyRichContentBlock(
                        type = UnifyContentType.QUOTE,
                        content = line.substring(2)
                    )
                )
            }
            line.startsWith("- ") -> {
                blocks.add(
                    UnifyRichContentBlock(
                        type = UnifyContentType.LIST_ITEM,
                        content = line.substring(2)
                    )
                )
            }
            line.startsWith("```") -> {
                // 代码块处理（简化版）
                blocks.add(
                    UnifyRichContentBlock(
                        type = UnifyContentType.CODE,
                        content = "代码块"
                    )
                )
            }
            line == "---" -> {
                blocks.add(
                    UnifyRichContentBlock(
                        type = UnifyContentType.DIVIDER,
                        content = ""
                    )
                )
            }
            line.isNotBlank() -> {
                blocks.add(
                    UnifyRichContentBlock(
                        type = UnifyContentType.PARAGRAPH,
                        content = line
                    )
                )
            }
        }
    }
    
    return blocks
}
