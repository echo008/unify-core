package com.unify.platform.container

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Container平台特定组件
 * 提供容器化和布局管理功能
 */

/**
 * 响应式容器组件
 */
@Composable
fun UnifyResponsiveContainer(
    modifier: Modifier = Modifier,
    breakpoints: ContainerBreakpoints = ContainerBreakpoints.default(),
    content: @Composable BoxScope.(ContainerSize) -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        val containerSize =
            when {
                maxWidth < breakpoints.small -> ContainerSize.SMALL
                maxWidth < breakpoints.medium -> ContainerSize.MEDIUM
                maxWidth < breakpoints.large -> ContainerSize.LARGE
                else -> ContainerSize.EXTRA_LARGE
            }

        content(containerSize)
    }
}

/**
 * 网格容器组件
 */
@Composable
fun UnifyGridContainer(
    items: List<GridItem>,
    columns: Int = 2,
    modifier: Modifier = Modifier,
    spacing: androidx.compose.ui.unit.Dp = 16.dp,
    onItemClick: ((String) -> Unit)? = null,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing),
        contentPadding = PaddingValues(spacing),
    ) {
        items(items.chunked(columns)) { rowItems ->
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(spacing),
            ) {
                items(rowItems) { item ->
                    UnifyGridItemCard(
                        item = item,
                        onClick = { onItemClick?.invoke(item.id) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // 填充空白项
                repeat(columns - rowItems.size) {
                    item {
                        Spacer(modifier = Modifier.width(100.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun UnifyGridItemCard(
    item: GridItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )

            item.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * 弹性布局容器
 */
@Composable
fun UnifyFlexContainer(
    items: List<FlexItem>,
    direction: FlexDirection = FlexDirection.ROW,
    wrap: FlexWrap = FlexWrap.NO_WRAP,
    justifyContent: JustifyContent = JustifyContent.START,
    alignItems: AlignItems = AlignItems.START,
    modifier: Modifier = Modifier,
    spacing: androidx.compose.ui.unit.Dp = 8.dp,
) {
    when (direction) {
        FlexDirection.ROW -> {
            Row(
                modifier = modifier,
                horizontalArrangement = getRowArrangement(justifyContent, spacing),
                verticalAlignment = getRowAlignment(alignItems),
            ) {
                items.forEach { item ->
                    Box(
                        modifier = Modifier.weight(item.flex),
                    ) {
                        item.content()
                    }
                }
            }
        }
        FlexDirection.COLUMN -> {
            Column(
                modifier = modifier,
                verticalArrangement = getColumnArrangement(justifyContent, spacing),
                horizontalAlignment = getColumnAlignment(alignItems),
            ) {
                items.forEach { item ->
                    Box(
                        modifier = Modifier.weight(item.flex),
                    ) {
                        item.content()
                    }
                }
            }
        }
    }
}

/**
 * 卡片容器组件
 */
@Composable
fun UnifyCardContainer(
    title: String? = null,
    subtitle: String? = null,
    actions: List<CardAction> = emptyList(),
    modifier: Modifier = Modifier,
    elevation: androidx.compose.ui.unit.Dp = 4.dp,
    colors: CardColors = CardDefaults.cardColors(),
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = colors,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            // 标题区域
            if (title != null || subtitle != null || actions.isNotEmpty()) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        title?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        subtitle?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    // 操作按钮
                    if (actions.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            actions.forEach { action ->
                                when (action) {
                                    is CardAction.Icon -> {
                                        IconButton(onClick = action.onClick) {
                                            Icon(
                                                imageVector = action.icon,
                                                contentDescription = action.contentDescription,
                                            )
                                        }
                                    }
                                    is CardAction.Text -> {
                                        TextButton(onClick = action.onClick) {
                                            Text(action.text)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Divider()
            }

            // 内容区域
            Column(
                modifier = Modifier.padding(16.dp),
                content = content,
            )
        }
    }
}

/**
 * 手风琴容器组件
 */
@Composable
fun UnifyAccordionContainer(
    sections: List<AccordionSection>,
    expandedSections: Set<String>,
    onSectionToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
    allowMultiple: Boolean = true,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(sections) { section ->
            UnifyAccordionItem(
                section = section,
                isExpanded = expandedSections.contains(section.id),
                onToggle = {
                    if (allowMultiple) {
                        onSectionToggle(section.id)
                    } else {
                        // 单选模式：关闭其他所有项
                        if (expandedSections.contains(section.id)) {
                            onSectionToggle(section.id)
                        } else {
                            expandedSections.forEach { onSectionToggle(it) }
                            onSectionToggle(section.id)
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun UnifyAccordionItem(
    section: AccordionSection,
    isExpanded: Boolean,
    onToggle: () -> Unit,
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            // 标题栏
            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onToggle() },
                color =
                    if (isExpanded) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = section.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color =
                                if (isExpanded) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                        )

                        section.subtitle?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color =
                                    if (isExpanded) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                            )
                        }
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "收起" else "展开",
                        tint =
                            if (isExpanded) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                    )
                }
            }

            // 内容区域
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Box(
                    modifier = Modifier.padding(16.dp),
                ) {
                    section.content()
                }
            }
        }
    }
}

/**
 * 标签页容器组件
 */
@Composable
fun UnifyTabContainer(
    tabs: List<TabItem>,
    selectedTabId: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    tabPosition: TabPosition = TabPosition.TOP,
) {
    when (tabPosition) {
        TabPosition.TOP -> {
            Column(modifier = modifier) {
                UnifyTabRow(tabs, selectedTabId, onTabSelected)
                UnifyTabContent(tabs, selectedTabId)
            }
        }
        TabPosition.BOTTOM -> {
            Column(modifier = modifier) {
                UnifyTabContent(tabs, selectedTabId, Modifier.weight(1f))
                UnifyTabRow(tabs, selectedTabId, onTabSelected)
            }
        }
        TabPosition.LEFT -> {
            Row(modifier = modifier) {
                UnifyVerticalTabRow(tabs, selectedTabId, onTabSelected)
                UnifyTabContent(tabs, selectedTabId, Modifier.weight(1f))
            }
        }
        TabPosition.RIGHT -> {
            Row(modifier = modifier) {
                UnifyTabContent(tabs, selectedTabId, Modifier.weight(1f))
                UnifyVerticalTabRow(tabs, selectedTabId, onTabSelected)
            }
        }
    }
}

@Composable
private fun UnifyTabRow(
    tabs: List<TabItem>,
    selectedTabId: String,
    onTabSelected: (String) -> Unit,
) {
    TabRow(
        selectedTabIndex = tabs.indexOfFirst { it.id == selectedTabId },
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = tab.id == selectedTabId,
                onClick = { onTabSelected(tab.id) },
                text = { Text(tab.title) },
                icon = tab.icon?.let { { Icon(it, contentDescription = null) } },
            )
        }
    }
}

@Composable
private fun UnifyVerticalTabRow(
    tabs: List<TabItem>,
    selectedTabId: String,
    onTabSelected: (String) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .width(200.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        tabs.forEach { tab ->
            val isSelected = tab.id == selectedTabId

            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onTabSelected(tab.id) },
                color =
                    if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        Color.Transparent
                    },
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    tab.icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint =
                                if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                        )
                    }

                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            if (isSelected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                }
            }
        }
    }
}

@Composable
private fun UnifyTabContent(
    tabs: List<TabItem>,
    selectedTabId: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        tabs.find { it.id == selectedTabId }?.content?.invoke()
    }
}

/**
 * 滚动容器组件
 */
@Composable
fun UnifyScrollContainer(
    modifier: Modifier = Modifier,
    scrollDirection: ScrollDirection = ScrollDirection.VERTICAL,
    showScrollbar: Boolean = true,
    content: @Composable () -> Unit,
) {
    when (scrollDirection) {
        ScrollDirection.VERTICAL -> {
            LazyColumn(
                modifier = modifier,
                content = {
                    item { content() }
                },
            )
        }
        ScrollDirection.HORIZONTAL -> {
            LazyRow(
                modifier = modifier,
                content = {
                    item { content() }
                },
            )
        }
        ScrollDirection.BOTH -> {
            // 双向滚动实现
            Box(modifier = modifier) {
                content()
            }
        }
    }
}

// 辅助函数

private fun getRowArrangement(
    justifyContent: JustifyContent,
    spacing: androidx.compose.ui.unit.Dp,
): Arrangement.Horizontal {
    return when (justifyContent) {
        JustifyContent.START -> Arrangement.spacedBy(spacing)
        JustifyContent.END -> Arrangement.spacedBy(spacing, Alignment.End)
        JustifyContent.CENTER -> Arrangement.spacedBy(spacing, Alignment.CenterHorizontally)
        JustifyContent.SPACE_BETWEEN -> Arrangement.SpaceBetween
        JustifyContent.SPACE_AROUND -> Arrangement.SpaceAround
        JustifyContent.SPACE_EVENLY -> Arrangement.SpaceEvenly
    }
}

private fun getColumnArrangement(
    justifyContent: JustifyContent,
    spacing: androidx.compose.ui.unit.Dp,
): Arrangement.Vertical {
    return when (justifyContent) {
        JustifyContent.START -> Arrangement.spacedBy(spacing)
        JustifyContent.END -> Arrangement.spacedBy(spacing, Alignment.Bottom)
        JustifyContent.CENTER -> Arrangement.spacedBy(spacing, Alignment.CenterVertically)
        JustifyContent.SPACE_BETWEEN -> Arrangement.SpaceBetween
        JustifyContent.SPACE_AROUND -> Arrangement.SpaceAround
        JustifyContent.SPACE_EVENLY -> Arrangement.SpaceEvenly
    }
}

private fun getRowAlignment(alignItems: AlignItems): Alignment.Vertical {
    return when (alignItems) {
        AlignItems.START -> Alignment.Top
        AlignItems.END -> Alignment.Bottom
        AlignItems.CENTER -> Alignment.CenterVertically
        AlignItems.STRETCH -> Alignment.CenterVertically // Compose doesn't have stretch
    }
}

private fun getColumnAlignment(alignItems: AlignItems): Alignment.Horizontal {
    return when (alignItems) {
        AlignItems.START -> Alignment.Start
        AlignItems.END -> Alignment.End
        AlignItems.CENTER -> Alignment.CenterHorizontally
        AlignItems.STRETCH -> Alignment.CenterHorizontally // Compose doesn't have stretch
    }
}

// 数据类和枚举定义

data class ContainerBreakpoints(
    val small: androidx.compose.ui.unit.Dp,
    val medium: androidx.compose.ui.unit.Dp,
    val large: androidx.compose.ui.unit.Dp,
) {
    companion object {
        fun default() =
            ContainerBreakpoints(
                small = 600.dp,
                medium = 960.dp,
                large = 1280.dp,
            )
    }
}

enum class ContainerSize {
    SMALL,
    MEDIUM,
    LARGE,
    EXTRA_LARGE,
}

data class GridItem(
    val id: String,
    val title: String,
    val description: String? = null,
    val icon: ImageVector,
)

data class FlexItem(
    val flex: Float = 1f,
    val content: @Composable () -> Unit,
)

enum class FlexDirection {
    ROW,
    COLUMN,
}

enum class FlexWrap {
    NO_WRAP,
    WRAP,
}

enum class JustifyContent {
    START,
    END,
    CENTER,
    SPACE_BETWEEN,
    SPACE_AROUND,
    SPACE_EVENLY,
}

enum class AlignItems {
    START,
    END,
    CENTER,
    STRETCH,
}

sealed class CardAction {
    data class Icon(
        val icon: ImageVector,
        val onClick: () -> Unit,
        val contentDescription: String? = null,
    ) : CardAction()

    data class Text(
        val text: String,
        val onClick: () -> Unit,
    ) : CardAction()
}

data class AccordionSection(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val content: @Composable () -> Unit,
)

data class TabItem(
    val id: String,
    val title: String,
    val icon: ImageVector? = null,
    val content: @Composable () -> Unit,
)

enum class TabPosition {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT,
}

enum class ScrollDirection {
    VERTICAL,
    HORIZONTAL,
    BOTH,
}
