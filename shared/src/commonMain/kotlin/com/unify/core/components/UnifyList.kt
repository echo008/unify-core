package com.unify.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Unify统一列表组件
 * 100% Kotlin Compose语法实现
 */
data class UnifyListItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val description: String? = null,
    val leadingIcon: ImageVector? = null,
    val trailingIcon: ImageVector? = null,
    val onClick: (() -> Unit)? = null,
    val enabled: Boolean = true
)

@Composable
fun <T> UnifyLazyList(
    items: List<T>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    userScrollEnabled: Boolean = true,
    showDividers: Boolean = false,
    dividerColor: Color = MaterialTheme.colorScheme.outline,
    dividerThickness: Dp = 1.dp,
    itemContent: @Composable (item: T, index: Int) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        userScrollEnabled = userScrollEnabled
    ) {
        items(items) { item ->
            val index = items.indexOf(item)
            itemContent(item, index)
            
            if (showDividers && index < items.size - 1) {
                HorizontalDivider(
                    color = dividerColor,
                    thickness = dividerThickness,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun <T> UnifyLazyRow(
    items: List<T>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal = if (!reverseLayout) Arrangement.Start else Arrangement.End,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    userScrollEnabled: Boolean = true,
    itemContent: @Composable (item: T, index: Int) -> Unit
) {
    LazyRow(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = reverseLayout,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        userScrollEnabled = userScrollEnabled
    ) {
        items(items) { item ->
            val index = items.indexOf(item)
            itemContent(item, index)
        }
    }
}

@Composable
fun UnifyListItemComponent(
    item: UnifyListItem,
    modifier: Modifier = Modifier,
    colors: androidx.compose.material3.ListItemColors = ListItemDefaults.colors(),
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp
) {
    val onClick = if (item.enabled && item.onClick != null) {
        item.onClick
    } else null
    
    if (onClick != null) {
        UnifyClickableSurface(
            onClick = onClick,
            modifier = modifier,
            enabled = item.enabled,
            shape = RoundedCornerShape(8.dp),
            tonalElevation = tonalElevation,
            shadowElevation = shadowElevation
        ) {
            ListItemContent(item = item, colors = colors)
        }
    } else {
        UnifySurface(
            modifier = modifier,
            shape = RoundedCornerShape(8.dp),
            tonalElevation = tonalElevation,
            shadowElevation = shadowElevation
        ) {
            ListItemContent(item = item, colors = colors)
        }
    }
}

@Composable
private fun ListItemContent(
    item: UnifyListItem,
    colors: androidx.compose.material3.ListItemColors
) {
    ListItem(
        headlineContent = {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = item.subtitle?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        overlineContent = item.description?.let {
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        leadingContent = item.leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        trailingContent = item.trailingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        colors = colors
    )
}

@Composable
fun UnifyEmptyState(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionButton: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (actionButton != null) {
                Spacer(modifier = Modifier.height(16.dp))
                actionButton()
            }
        }
    }
}
