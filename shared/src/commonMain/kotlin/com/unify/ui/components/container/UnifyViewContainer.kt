package com.unify.ui.components.container

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Unify跨平台容器组件
 * 支持所有8大平台的统一容器布局
 */

@Composable
fun UnifyContainer(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(16.dp),
    backgroundColor: Color = Color.Transparent,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(padding),
        content = content
    )
}

@Composable
fun UnifyScrollableContainer(
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding),
        content = content
    )
}

@Composable
fun UnifySection(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (actions != null) {
                Row(content = actions)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(content = content)
    }
}

@Composable
fun UnifyGrid(
    items: List<Any>,
    columns: Int = 2,
    modifier: Modifier = Modifier,
    spacing: androidx.compose.ui.unit.Dp = 8.dp,
    itemContent: @Composable (Any) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        items(items.chunked(columns)) { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                rowItems.forEach { item ->
                    Box(modifier = Modifier.weight(1f)) {
                        itemContent(item)
                    }
                }
                // 填充剩余空间
                repeat(columns - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun UnifyHorizontalList(
    items: List<Any>,
    modifier: Modifier = Modifier,
    spacing: androidx.compose.ui.unit.Dp = 8.dp,
    itemContent: @Composable (Any) -> Unit
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(items) { item ->
            itemContent(item)
        }
    }
}

@Composable
fun UnifyVerticalList(
    items: List<Any>,
    modifier: Modifier = Modifier,
    spacing: androidx.compose.ui.unit.Dp = 8.dp,
    itemContent: @Composable (Any) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(items) { item ->
            itemContent(item)
        }
    }
}

@Composable
fun UnifyExpandableContainer(
    title: String,
    modifier: Modifier = Modifier,
    initialExpanded: Boolean = false,
    icon: String = "▼",
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(initialExpanded) }
    
    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                
                Text(
                    text = if (expanded) "▲" else icon,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        if (expanded) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                content = content
            )
        }
    }
}

@Composable
fun UnifyTabContainer(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Int) -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    text = { Text(title) }
                )
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            content(selectedTab)
        }
    }
}

@Composable
fun UnifyCardContainer(
    modifier: Modifier = Modifier,
    elevation: androidx.compose.ui.unit.Dp = 2.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        onClick = onClick ?: {}
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun UnifyFlexContainer(
    modifier: Modifier = Modifier,
    direction: UnifyFlexDirection = UnifyFlexDirection.ROW,
    wrap: Boolean = false,
    justifyContent: UnifyJustifyContent = UnifyJustifyContent.START,
    alignItems: UnifyAlignItems = UnifyAlignItems.START,
    spacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    when (direction) {
        UnifyFlexDirection.ROW -> {
            Row(
                modifier = modifier,
                horizontalArrangement = when (justifyContent) {
                    UnifyJustifyContent.START -> Arrangement.Start
                    UnifyJustifyContent.CENTER -> Arrangement.Center
                    UnifyJustifyContent.END -> Arrangement.End
                    UnifyJustifyContent.SPACE_BETWEEN -> Arrangement.SpaceBetween
                    UnifyJustifyContent.SPACE_AROUND -> Arrangement.SpaceAround
                    UnifyJustifyContent.SPACE_EVENLY -> Arrangement.SpaceEvenly
                },
                verticalAlignment = when (alignItems) {
                    UnifyAlignItems.START -> Alignment.Top
                    UnifyAlignItems.CENTER -> Alignment.CenterVertically
                    UnifyAlignItems.END -> Alignment.Bottom
                }
            ) {
                content()
            }
        }
        UnifyFlexDirection.COLUMN -> {
            Column(
                modifier = modifier,
                verticalArrangement = when (justifyContent) {
                    UnifyJustifyContent.START -> Arrangement.Top
                    UnifyJustifyContent.CENTER -> Arrangement.Center
                    UnifyJustifyContent.END -> Arrangement.Bottom
                    UnifyJustifyContent.SPACE_BETWEEN -> Arrangement.SpaceBetween
                    UnifyJustifyContent.SPACE_AROUND -> Arrangement.SpaceAround
                    UnifyJustifyContent.SPACE_EVENLY -> Arrangement.SpaceEvenly
                },
                horizontalAlignment = when (alignItems) {
                    UnifyAlignItems.START -> Alignment.Start
                    UnifyAlignItems.CENTER -> Alignment.CenterHorizontally
                    UnifyAlignItems.END -> Alignment.End
                }
            ) {
                content()
            }
        }
    }
}

enum class UnifyFlexDirection { ROW, COLUMN }
enum class UnifyJustifyContent { START, CENTER, END, SPACE_BETWEEN, SPACE_AROUND, SPACE_EVENLY }
enum class UnifyAlignItems { START, CENTER, END }

@Composable
fun UnifyResponsiveContainer(
    modifier: Modifier = Modifier,
    smallContent: @Composable () -> Unit,
    mediumContent: (@Composable () -> Unit)? = null,
    largeContent: (@Composable () -> Unit)? = null
) {
    BoxWithConstraints(modifier = modifier) {
        when {
            maxWidth < 600.dp -> smallContent()
            maxWidth < 900.dp -> mediumContent?.invoke() ?: smallContent()
            else -> largeContent?.invoke() ?: mediumContent?.invoke() ?: smallContent()
        }
    }
}
