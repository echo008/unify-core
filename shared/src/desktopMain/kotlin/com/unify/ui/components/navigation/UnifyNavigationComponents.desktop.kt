package com.unify.ui.components.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Desktop平台导航组件actual实现
 */

@Composable
actual fun UnifyNavigationHost(
    startDestination: String,
    modifier: Modifier,
    route: String?,
    builder: NavigationGraphBuilder.() -> Unit,
) {
    var currentDestination by remember { mutableStateOf(startDestination) }
    val graphBuilder = NavigationGraphBuilder()
    builder(graphBuilder)

    Box(modifier = modifier.fillMaxSize()) {
        Text(
            text = "导航主机 - 当前页面: $currentDestination",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Composable
actual fun UnifyNavController(): NavController {
    return remember { DesktopNavController() }
}

private class DesktopNavController : NavController {
    private var _currentDestination: NavDestination? = null
    override val currentDestination: NavDestination? get() = _currentDestination

    private val backStack = mutableListOf<String>()

    override fun navigate(route: String) {
        _currentDestination?.route?.let { current ->
            backStack.add(current)
        }
        _currentDestination = DesktopNavDestination(route)
    }

    override fun navigateUp(): Boolean = popBackStack()

    override fun popBackStack(): Boolean {
        return if (backStack.isNotEmpty()) {
            val previousRoute = backStack.removeLastOrNull()
            _currentDestination = previousRoute?.let { DesktopNavDestination(it) }
            true
        } else {
            false
        }
    }

    override fun popBackStack(
        route: String,
        inclusive: Boolean,
    ): Boolean {
        val index = backStack.lastIndexOf(route)
        return if (index >= 0) {
            if (inclusive) {
                backStack.subList(index, backStack.size).clear()
            } else {
                backStack.subList(index + 1, backStack.size).clear()
            }
            _currentDestination =
                if (backStack.isNotEmpty()) {
                    DesktopNavDestination(backStack.last())
                } else {
                    null
                }
            true
        } else {
            false
        }
    }
}

private class DesktopNavDestination(override val route: String?) : NavDestination {
    override val id: String = route ?: "unknown"
}

@Composable
actual fun UnifyBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    // Desktop平台的返回处理实现
    LaunchedEffect(enabled) {
        if (enabled) {
            // 在实际应用中，这里可以注册键盘监听器来处理ESC键或其他返回操作
        }
    }
}

@Composable
actual fun UnifyNavigationSuite(
    navigationSuiteItems: NavigationSuiteScope.() -> Unit,
    modifier: Modifier,
    layoutType: NavigationSuiteType,
    colors: NavigationSuiteColors,
    content: @Composable () -> Unit,
) {
    // 简化实现，直接显示内容而不处理导航项
    // 在Desktop平台，我们提供一个基本的导航容器

    when (layoutType) {
        NavigationSuiteType.NavigationBar -> {
            Column(modifier = modifier) {
                Box(modifier = Modifier.weight(1f)) {
                    content()
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                colors.navigationBarContainerColor.takeIf { it != Color.Unspecified }
                                    ?: MaterialTheme.colorScheme.surface,
                        ),
                ) {
                    Text(
                        text = "导航栏 - Desktop模拟",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
        NavigationSuiteType.NavigationRail -> {
            Row(modifier = modifier) {
                Card(
                    modifier = Modifier.fillMaxHeight(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                colors.navigationRailContainerColor.takeIf { it != Color.Unspecified }
                                    ?: MaterialTheme.colorScheme.surface,
                        ),
                ) {
                    Text(
                        text = "导航栏",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    content()
                }
            }
        }
        NavigationSuiteType.NavigationDrawer -> {
            Row(modifier = modifier) {
                Card(
                    modifier = Modifier.width(280.dp).fillMaxHeight(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                colors.navigationDrawerContainerColor.takeIf { it != Color.Unspecified }
                                    ?: MaterialTheme.colorScheme.surface,
                        ),
                ) {
                    Text(
                        text = "导航抽屉 - Desktop模拟",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    content()
                }
            }
        }
        NavigationSuiteType.Auto -> {
            // 自动选择布局类型，这里默认使用NavigationBar
            Column(modifier = modifier) {
                Box(modifier = Modifier.weight(1f)) {
                    content()
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                ) {
                    Text(
                        text = "自动导航套件 - Desktop模拟",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
