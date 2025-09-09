package com.unify.ui.components.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

// 根据commonMain expect声明，只保留匹配的actual实现

@Composable
actual fun UnifyNavigationHost(
    startDestination: String,
    modifier: Modifier,
    route: String?,
    builder: NavigationGraphBuilder.() -> Unit,
) {
    // Native平台导航宿主组件实现
}

@Composable
actual fun UnifyNavController(): NavController {
    // Native平台导航控制器实现
    return object : NavController {
        override fun navigate(route: String) {}

        override fun navigateUp(): Boolean = true

        override fun popBackStack(): Boolean = true

        override fun popBackStack(
            route: String,
            inclusive: Boolean,
        ): Boolean = true

        override val currentDestination: NavDestination? = null
    }
}

@Composable
actual fun UnifyBackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    // Native平台返回处理器实现
}

@Composable
actual fun UnifyNavigationSuite(
    navigationSuiteItems: NavigationSuiteScope.() -> Unit,
    modifier: Modifier,
    layoutType: NavigationSuiteType,
    colors: NavigationSuiteColors,
    content: @Composable () -> Unit,
) {
    // Native平台导航套件组件实现
}
