package com.unify.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.core.providers.*
import com.unify.core.ui.UnifyUIManager
import com.unify.ui.components.*

/**
 * Unify UI 核心入口
 * 提供统一的UI组件库和主题系统
 */

/**
 * Unify应用程序根组件
 */
@Composable
fun UnifyApp(
    theme: UnifyTheme = UnifyTheme.Default,
    configuration: UnifyConfiguration = UnifyConfiguration.Default,
    content: @Composable () -> Unit
) {
    val uiManager = currentUIManager()
    
    MaterialTheme(
        colorScheme = theme.colorScheme.toMaterialColorScheme(),
        typography = theme.typography.toMaterialTypography(),
        shapes = theme.shapes.toMaterialShapes()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}

/**
 * Unify主题提供器
 */
@Composable
fun UnifyThemeProvider(
    theme: UnifyTheme = UnifyTheme.Default,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalUnifyTheme provides theme
    ) {
        content()
    }
}

/**
 * Unify屏幕组件
 */
@Composable
fun UnifyScreen(
    modifier: Modifier = Modifier,
    title: String? = null,
    subtitle: String? = null,
    backgroundColor: Color = currentUnifyTheme().colorScheme.background.toColor(),
    padding: PaddingValues = PaddingValues(16.dp),
    topBar: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar ?: {
            if (title != null) {
                UnifyTopAppBar(
                    title = title,
                    subtitle = subtitle
                )
            }
        },
        bottomBar = bottomBar ?: {},
        floatingActionButton = floatingActionButton ?: {},
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(padding)
        ) {
            content(paddingValues)
        }
    }
}

/**
 * Unify顶部应用栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifyTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit) = {},
    backgroundColor: Color = currentUnifyTheme().colorScheme.primary.toColor(),
    contentColor: Color = currentUnifyTheme().colorScheme.onPrimary.toColor()
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    color = contentColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
                subtitle?.let {
                    Text(
                        text = it,
                        color = contentColor.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
        },
        modifier = modifier,
        navigationIcon = navigationIcon ?: {},
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor,
            titleContentColor = contentColor,
            navigationIconContentColor = contentColor,
            actionIconContentColor = contentColor
        )
    )
}

/**
 * Unify底部导航栏
 */
@Composable
fun UnifyBottomNavigation(
    modifier: Modifier = Modifier,
    backgroundColor: Color = currentUnifyTheme().colorScheme.surface.toColor(),
    contentColor: Color = currentUnifyTheme().colorScheme.primary.toColor(),
    content: @Composable RowScope.() -> Unit
) {
    NavigationBar(
        modifier = modifier,
        containerColor = backgroundColor,
        contentColor = contentColor,
        content = content
    )
}

/**
 * Unify底部导航项
 */
@Composable
fun RowScope.UnifyBottomNavigationItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    alwaysShowLabel: Boolean = true
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = icon,
        modifier = modifier,
        enabled = enabled,
        label = label,
        alwaysShowLabel = alwaysShowLabel
    )
}

/**
 * Unify侧边抽屉
 */
@Composable
fun UnifyDrawer(
    modifier: Modifier = Modifier,
    backgroundColor: Color = currentUnifyTheme().colorScheme.surface.toColor(),
    contentColor: Color = currentUnifyTheme().colorScheme.onSurface.toColor(),
    content: @Composable ColumnScope.() -> Unit
) {
    ModalDrawerSheet(
        modifier = modifier,
        drawerContainerColor = backgroundColor,
        drawerContentColor = contentColor,
        content = content
    )
}

/**
 * Unify抽屉项
 */
@Composable
fun UnifyDrawerItem(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    selected: Boolean = false,
    enabled: Boolean = true
) {
    NavigationDrawerItem(
        label = { Text(label) },
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = currentUnifyTheme().colorScheme.primary.toColor().copy(alpha = 0.12f),
            selectedTextColor = currentUnifyTheme().colorScheme.primary.toColor(),
            selectedIconColor = currentUnifyTheme().colorScheme.primary.toColor()
        )
    )
}

/**
 * Unify标签页
 */
@Composable
fun UnifyTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = currentUnifyTheme().colorScheme.surface.toColor(),
    contentColor: Color = currentUnifyTheme().colorScheme.primary.toColor(),
    indicator: @Composable (tabPositions: List<TabPosition>) -> Unit = { tabPositions ->
        if (selectedTabIndex < tabPositions.size) {
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                color = contentColor
            )
        }
    },
    divider: @Composable () -> Unit = {
        HorizontalDivider(color = currentUnifyTheme().colorScheme.onSurface.toColor().copy(alpha = 0.12f))
    },
    tabs: @Composable () -> Unit
) {
    TabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = backgroundColor,
        contentColor = contentColor,
        indicator = indicator,
        divider = divider,
        tabs = tabs
    )
}

/**
 * Unify标签项
 */
@Composable
fun UnifyTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    selectedContentColor: Color = currentUnifyTheme().colorScheme.primary.toColor(),
    unselectedContentColor: Color = currentUnifyTheme().colorScheme.onSurface.toColor().copy(alpha = 0.6f)
) {
    Tab(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        text = text,
        icon = icon,
        selectedContentColor = selectedContentColor,
        unselectedContentColor = unselectedContentColor
    )
}

/**
 * Unify滚动标签页
 */
@Composable
fun UnifyScrollableTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = currentUnifyTheme().colorScheme.surface.toColor(),
    contentColor: Color = currentUnifyTheme().colorScheme.primary.toColor(),
    edgePadding: androidx.compose.ui.unit.Dp = 52.dp,
    indicator: @Composable (tabPositions: List<TabPosition>) -> Unit = { tabPositions ->
        if (selectedTabIndex < tabPositions.size) {
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                color = contentColor
            )
        }
    },
    divider: @Composable () -> Unit = {
        HorizontalDivider(color = currentUnifyTheme().colorScheme.onSurface.toColor().copy(alpha = 0.12f))
    },
    tabs: @Composable () -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = backgroundColor,
        contentColor = contentColor,
        edgePadding = edgePadding,
        indicator = indicator,
        divider = divider,
        tabs = tabs
    )
}

/**
 * Unify分页器
 */
@Composable
fun UnifyPager(
    pageCount: Int,
    modifier: Modifier = Modifier,
    state: PagerState = rememberPagerState { pageCount },
    contentPadding: PaddingValues = PaddingValues(0.dp),
    pageSize: PageSize = PageSize.Fill,
    beyondBoundsPageCount: Int = 0,
    pageSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    flingBehavior: TargetedFlingBehavior = PagerDefaults.flingBehavior(state = state),
    userScrollEnabled: Boolean = true,
    reverseLayout: Boolean = false,
    key: ((index: Int) -> Any)? = null,
    pageNestedScrollConnection: NestedScrollConnection = PagerDefaults.pageNestedScrollConnection(
        Orientation.Horizontal
    ),
    pageContent: @Composable PagerScope.(page: Int) -> Unit
) {
    HorizontalPager(
        pageCount = pageCount,
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        pageSize = pageSize,
        beyondBoundsPageCount = beyondBoundsPageCount,
        pageSpacing = pageSpacing,
        verticalAlignment = verticalAlignment,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
        reverseLayout = reverseLayout,
        key = key,
        pageNestedScrollConnection = pageNestedScrollConnection,
        pageContent = pageContent
    )
}

/**
 * Unify垂直分页器
 */
@Composable
fun UnifyVerticalPager(
    pageCount: Int,
    modifier: Modifier = Modifier,
    state: PagerState = rememberPagerState { pageCount },
    contentPadding: PaddingValues = PaddingValues(0.dp),
    pageSize: PageSize = PageSize.Fill,
    beyondBoundsPageCount: Int = 0,
    pageSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    flingBehavior: TargetedFlingBehavior = PagerDefaults.flingBehavior(state = state),
    userScrollEnabled: Boolean = true,
    reverseLayout: Boolean = false,
    key: ((index: Int) -> Any)? = null,
    pageNestedScrollConnection: NestedScrollConnection = PagerDefaults.pageNestedScrollConnection(
        Orientation.Vertical
    ),
    pageContent: @Composable PagerScope.(page: Int) -> Unit
) {
    VerticalPager(
        pageCount = pageCount,
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        pageSize = pageSize,
        beyondBoundsPageCount = beyondBoundsPageCount,
        pageSpacing = pageSpacing,
        horizontalAlignment = horizontalAlignment,
        flingBehavior = flingBehavior,
        userScrollEnabled = userScrollEnabled,
        reverseLayout = reverseLayout,
        key = key,
        pageNestedScrollConnection = pageNestedScrollConnection,
        pageContent = pageContent
    )
}

/**
 * Unify分页指示器
 */
@Composable
fun UnifyPagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    pageCount: Int = pagerState.pageCount,
    activeColor: Color = currentUnifyTheme().colorScheme.primary.toColor(),
    inactiveColor: Color = activeColor.copy(alpha = 0.3f),
    indicatorWidth: androidx.compose.ui.unit.Dp = 8.dp,
    indicatorHeight: androidx.compose.ui.unit.Dp = 8.dp,
    spacing: androidx.compose.ui.unit.Dp = 8.dp
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val color = if (pagerState.currentPage == index) activeColor else inactiveColor
            Box(
                modifier = Modifier
                    .size(width = indicatorWidth, height = indicatorHeight)
                    .background(color, CircleShape)
            )
        }
    }
}

// 扩展函数：将Unify颜色转换为Material颜色
private fun Long.toColor(): Color = Color(this)

private fun UnifyColorScheme.toMaterialColorScheme(): ColorScheme {
    return lightColorScheme(
        primary = Color(primary),
        onPrimary = Color(onPrimary),
        primaryContainer = Color(primaryVariant),
        onPrimaryContainer = Color(onPrimary),
        secondary = Color(secondary),
        onSecondary = Color(onSecondary),
        secondaryContainer = Color(secondaryVariant),
        onSecondaryContainer = Color(onSecondary),
        tertiary = Color(secondary),
        onTertiary = Color(onSecondary),
        tertiaryContainer = Color(secondaryVariant),
        onTertiaryContainer = Color(onSecondary),
        error = Color(error),
        onError = Color(onError),
        errorContainer = Color(error).copy(alpha = 0.12f),
        onErrorContainer = Color(error),
        background = Color(background),
        onBackground = Color(onBackground),
        surface = Color(surface),
        onSurface = Color(onSurface),
        surfaceVariant = Color(surface),
        onSurfaceVariant = Color(onSurface),
        outline = Color(onSurface).copy(alpha = 0.12f),
        outlineVariant = Color(onSurface).copy(alpha = 0.06f),
        scrim = Color.Black,
        inverseSurface = Color(onSurface),
        inverseOnSurface = Color(surface),
        inversePrimary = Color(primary).copy(alpha = 0.8f),
        surfaceDim = Color(surface).copy(alpha = 0.87f),
        surfaceBright = Color(surface),
        surfaceContainerLowest = Color(surface),
        surfaceContainerLow = Color(surface),
        surfaceContainer = Color(surface),
        surfaceContainerHigh = Color(surface),
        surfaceContainerHighest = Color(surface)
    )
}

private fun UnifyTypography.toMaterialTypography(): Typography {
    return Typography(
        displayLarge = androidx.compose.ui.text.TextStyle(
            fontSize = h1.fontSize.sp,
            lineHeight = h1.lineHeight.sp,
            fontWeight = FontWeight(h1.fontWeight),
            letterSpacing = h1.letterSpacing.sp
        ),
        displayMedium = androidx.compose.ui.text.TextStyle(
            fontSize = h2.fontSize.sp,
            lineHeight = h2.lineHeight.sp,
            fontWeight = FontWeight(h2.fontWeight),
            letterSpacing = h2.letterSpacing.sp
        ),
        displaySmall = androidx.compose.ui.text.TextStyle(
            fontSize = h3.fontSize.sp,
            lineHeight = h3.lineHeight.sp,
            fontWeight = FontWeight(h3.fontWeight),
            letterSpacing = h3.letterSpacing.sp
        ),
        headlineLarge = androidx.compose.ui.text.TextStyle(
            fontSize = h4.fontSize.sp,
            lineHeight = h4.lineHeight.sp,
            fontWeight = FontWeight(h4.fontWeight),
            letterSpacing = h4.letterSpacing.sp
        ),
        headlineMedium = androidx.compose.ui.text.TextStyle(
            fontSize = h5.fontSize.sp,
            lineHeight = h5.lineHeight.sp,
            fontWeight = FontWeight(h5.fontWeight),
            letterSpacing = h5.letterSpacing.sp
        ),
        headlineSmall = androidx.compose.ui.text.TextStyle(
            fontSize = h6.fontSize.sp,
            lineHeight = h6.lineHeight.sp,
            fontWeight = FontWeight(h6.fontWeight),
            letterSpacing = h6.letterSpacing.sp
        ),
        titleLarge = androidx.compose.ui.text.TextStyle(
            fontSize = subtitle1.fontSize.sp,
            lineHeight = subtitle1.lineHeight.sp,
            fontWeight = FontWeight(subtitle1.fontWeight),
            letterSpacing = subtitle1.letterSpacing.sp
        ),
        titleMedium = androidx.compose.ui.text.TextStyle(
            fontSize = subtitle2.fontSize.sp,
            lineHeight = subtitle2.lineHeight.sp,
            fontWeight = FontWeight(subtitle2.fontWeight),
            letterSpacing = subtitle2.letterSpacing.sp
        ),
        bodyLarge = androidx.compose.ui.text.TextStyle(
            fontSize = body1.fontSize.sp,
            lineHeight = body1.lineHeight.sp,
            fontWeight = FontWeight(body1.fontWeight),
            letterSpacing = body1.letterSpacing.sp
        ),
        bodyMedium = androidx.compose.ui.text.TextStyle(
            fontSize = body2.fontSize.sp,
            lineHeight = body2.lineHeight.sp,
            fontWeight = FontWeight(body2.fontWeight),
            letterSpacing = body2.letterSpacing.sp
        ),
        labelLarge = androidx.compose.ui.text.TextStyle(
            fontSize = button.fontSize.sp,
            lineHeight = button.lineHeight.sp,
            fontWeight = FontWeight(button.fontWeight),
            letterSpacing = button.letterSpacing.sp
        ),
        labelMedium = androidx.compose.ui.text.TextStyle(
            fontSize = caption.fontSize.sp,
            lineHeight = caption.lineHeight.sp,
            fontWeight = FontWeight(caption.fontWeight),
            letterSpacing = caption.letterSpacing.sp
        ),
        labelSmall = androidx.compose.ui.text.TextStyle(
            fontSize = overline.fontSize.sp,
            lineHeight = overline.lineHeight.sp,
            fontWeight = FontWeight(overline.fontWeight),
            letterSpacing = overline.letterSpacing.sp
        )
    )
}

private fun UnifyShapes.toMaterialShapes(): Shapes {
    return Shapes(
        extraSmall = RoundedCornerShape(small.dp),
        small = RoundedCornerShape(small.dp),
        medium = RoundedCornerShape(medium.dp),
        large = RoundedCornerShape(large.dp),
        extraLarge = RoundedCornerShape(large.dp)
    )
}
