@file:OptIn(ExperimentalMaterial3Api::class)

package com.unify.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Unify主题颜色
 */
object UnifyColors {
    val Primary = Color(0xFF6200EE)
    val PrimaryVariant = Color(0xFF3700B3)
    val Secondary = Color(0xFF03DAC6)
    val SecondaryVariant = Color(0xFF018786)
    val Background = Color(0xFFFFFFFF)
    val Surface = Color(0xFFFFFFFF)
    val Error = Color(0xFFB00020)
    val OnPrimary = Color(0xFFFFFFFF)
    val OnSecondary = Color(0xFF000000)
    val OnBackground = Color(0xFF000000)
    val OnSurface = Color(0xFF000000)
    val OnError = Color(0xFFFFFFFF)

    // 扩展颜色
    val Success = Color(0xFF4CAF50)
    val Warning = Color(0xFFFF9800)
    val Info = Color(0xFF2196F3)
    val Light = Color(0xFFF5F5F5)
    val Dark = Color(0xFF212121)
}

/**
 * Unify间距系统
 */
object UnifySpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}

/**
 * Unify字体大小
 */
object UnifyFontSizes {
    val xs = 10.sp
    val sm = 12.sp
    val md = 14.sp
    val lg = 16.sp
    val xl = 18.sp
    val xxl = 20.sp
    val h6 = 20.sp
    val h5 = 24.sp
    val h4 = 34.sp
    val h3 = 48.sp
    val h2 = 60.sp
    val h1 = 96.sp
}

/**
 * Unify圆角系统
 */
object UnifyCornerRadius {
    val none = 0.dp
    val xs = 2.dp
    val sm = 4.dp
    val md = 8.dp
    val lg = 12.dp
    val xl = 16.dp
    val xxl = 24.dp
    val full = 50.dp
}

/**
 * Unify阴影系统
 */
object UnifyElevation {
    val none = 0.dp
    val xs = 1.dp
    val sm = 2.dp
    val md = 4.dp
    val lg = 8.dp
    val xl = 12.dp
    val xxl = 16.dp
}

/**
 * Unify容器组件
 */
@Composable
fun UnifyContainer(
    modifier: Modifier = Modifier,
    backgroundColor: Color = UnifyColors.Background,
    padding: PaddingValues = PaddingValues(UnifySpacing.md),
    elevation: androidx.compose.ui.unit.Dp = UnifyElevation.none,
    cornerRadius: androidx.compose.ui.unit.Dp = UnifyCornerRadius.none,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = RoundedCornerShape(cornerRadius),
    ) {
        Column(
            modifier = Modifier.padding(padding),
            content = content,
        )
    }
}

// UnifyCard moved to UnifySurface.kt to use expect/actual mechanism and avoid duplicate declarations

/**
 * Unify分隔线组件
 */
@Composable
fun UnifyDivider(
    modifier: Modifier = Modifier,
    color: Color = UnifyColors.OnSurface.copy(alpha = 0.12f),
    thickness: androidx.compose.ui.unit.Dp = 1.dp,
    startIndent: androidx.compose.ui.unit.Dp = 0.dp,
) {
    HorizontalDivider(
        modifier = modifier,
        color = color,
        thickness = thickness,
    )
}

/**
 * Unify垂直分隔线组件
 */
@Composable
fun UnifyVerticalDivider(
    modifier: Modifier = Modifier,
    color: Color = UnifyColors.OnSurface.copy(alpha = 0.12f),
    thickness: androidx.compose.ui.unit.Dp = 1.dp,
) {
    VerticalDivider(
        modifier = modifier,
        color = color,
        thickness = thickness,
    )
}

/**
 * Unify间距组件
 */
@Composable
fun UnifySpacer(
    height: androidx.compose.ui.unit.Dp = 0.dp,
    width: androidx.compose.ui.unit.Dp = 0.dp,
) {
    Spacer(
        modifier =
            Modifier
                .height(height)
                .width(width),
    )
}

/**
 * Unify行布局组件
 */
@Composable
fun UnifyRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        content = content,
    )
}

// Layout components moved to UnifyLayout.kt to avoid duplicate declarations

// Layout components moved to UnifyLayout.kt to avoid duplicate declarations

/**
 * Unify盒子布局组件
 */
@Composable
fun UnifyBox(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    propagateMinConstraints: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = contentAlignment,
        propagateMinConstraints = propagateMinConstraints,
        content = content,
    )
}

/**
 * Unify居中组件
 */
@Composable
fun UnifyCenter(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

/**
 * Unify标签组件
 */
@Composable
fun UnifyChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    backgroundColor: Color = if (selected) UnifyColors.Primary else UnifyColors.Surface,
    contentColor: Color = if (selected) UnifyColors.OnPrimary else UnifyColors.OnSurface,
    onClick: (() -> Unit)? = null,
) {
    FilterChip(
        selected = selected,
        onClick = { onClick?.invoke() },
        label = {
            Text(
                text = text,
                color = contentColor,
                fontSize = UnifyFontSizes.sm,
            )
        },
        modifier = modifier,
        enabled = enabled,
        colors =
            FilterChipDefaults.filterChipColors(
                containerColor = backgroundColor,
                labelColor = contentColor,
                selectedContainerColor = UnifyColors.Primary,
                selectedLabelColor = UnifyColors.OnPrimary,
            ),
    )
}

/**
 * Unify徽章组件
 */
@Composable
fun UnifyBadge(
    modifier: Modifier = Modifier,
    backgroundColor: Color = UnifyColors.Error,
    contentColor: Color = UnifyColors.OnError,
    content: @Composable (RowScope.() -> Unit)? = null,
) {
    Badge(
        modifier = modifier,
        containerColor = backgroundColor,
        contentColor = contentColor,
        content = content,
    )
}

/**
 * Unify带徽章的组件
 */
@Composable
fun UnifyBadgedBox(
    badge: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier) {
        content()
        badge()
    }
}

/**
 * Unify工具提示组件
 */
@Composable
fun UnifyTooltip(
    tooltip: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip {
                Text(tooltip)
            }
        },
        state = rememberTooltipState(),
    ) {
        content()
    }
}

/**
 * Unify滑动删除组件
 */
@Composable
fun UnifySwipeToDismiss(
    modifier: Modifier = Modifier,
    background: @Composable RowScope.() -> Unit,
    dismissContent: @Composable RowScope.() -> Unit,
) {
    // 简化实现，避免复杂的SwipeToDismiss API
    Row(modifier = modifier) {
        dismissContent()
    }
}

/**
 * Unify可展开组件
 */
@Composable
fun UnifyExpandable(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { onExpandedChange(!expanded) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.weight(1f)) {
                header()
            }
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "收起" else "展开",
            )
        }

        AnimatedVisibility(visible = expanded) {
            content()
        }
    }
}

/**
 * Unify空状态组件
 */
@Composable
fun UnifyEmptyState(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    icon: @Composable (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(UnifySpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon?.invoke()

        Spacer(modifier = Modifier.height(UnifySpacing.md))

        Text(
            text = title,
            fontSize = UnifyFontSizes.lg,
            fontWeight = FontWeight.Bold,
            color = UnifyColors.OnSurface,
        )

        description?.let {
            Spacer(modifier = Modifier.height(UnifySpacing.sm))
            Text(
                text = it,
                fontSize = UnifyFontSizes.md,
                color = UnifyColors.OnSurface.copy(alpha = 0.7f),
            )
        }

        action?.let {
            Spacer(modifier = Modifier.height(UnifySpacing.lg))
            it()
        }
    }
}

/**
 * Unify错误状态组件
 */
@Composable
fun UnifyErrorState(
    modifier: Modifier = Modifier,
    title: String = "出错了",
    description: String? = null,
    onRetry: (() -> Unit)? = null,
) {
    UnifyEmptyState(
        modifier = modifier,
        title = title,
        description = description,
        icon = {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "错误",
                tint = UnifyColors.Error,
                modifier = Modifier.size(48.dp),
            )
        },
        action =
            onRetry?.let {
                {
                    Button(onClick = it) {
                        Text("重试")
                    }
                }
            },
    )
}

/**
 * Unify加载状态组件
 */
@Composable
fun UnifyLoadingState(
    modifier: Modifier = Modifier,
    message: String = "加载中...",
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(UnifySpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            color = UnifyColors.Primary,
        )

        Spacer(modifier = Modifier.height(UnifySpacing.md))

        Text(
            text = message,
            fontSize = UnifyFontSizes.md,
            color = UnifyColors.OnSurface.copy(alpha = 0.7f),
        )
    }
}
