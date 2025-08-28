package com.unify.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.Serializable

/**
 * 统一路由系统
 * 基于文档第7章要求实现的路由导航系统
 */

/**
 * 路由定义接口
 */
@Serializable
sealed class UnifyRoute {
    @Serializable
    object Home : UnifyRoute()
    
    @Serializable
    object Login : UnifyRoute()
    
    @Serializable
    data class UserDetail(val userId: String) : UnifyRoute()
    
    @Serializable
    data class UserList(val category: String = "all") : UnifyRoute()
    
    @Serializable
    data class Settings(val section: String = "general") : UnifyRoute()
    
    @Serializable
    data class WebView(val url: String, val title: String = "") : UnifyRoute()
    
    @Serializable
    data class CustomRoute(val path: String, val params: Map<String, String> = emptyMap()) : UnifyRoute()
}

/**
 * 导航事件
 */
sealed class NavigationEvent {
    data class NavigateTo(val route: UnifyRoute, val clearBackStack: Boolean = false) : NavigationEvent()
    object NavigateBack : NavigationEvent()
    data class NavigateUp(val route: UnifyRoute? = null) : NavigationEvent()
    object ClearBackStack : NavigationEvent()
    data class PopToRoute(val route: UnifyRoute, val inclusive: Boolean = false) : NavigationEvent()
}

/**
 * 导航状态
 */
data class NavigationState(
    val currentRoute: UnifyRoute = UnifyRoute.Home,
    val backStack: List<UnifyRoute> = emptyList(),
    val canNavigateBack: Boolean = false
)

/**
 * 统一导航器接口
 */
interface UnifyNavigator {
    val navigationState: NavigationState
    val navigationEvents: SharedFlow<NavigationEvent>
    
    fun navigateTo(route: UnifyRoute, clearBackStack: Boolean = false)
    fun navigateBack()
    fun navigateUp(route: UnifyRoute? = null)
    fun clearBackStack()
    fun popToRoute(route: UnifyRoute, inclusive: Boolean = false)
    fun canNavigateBack(): Boolean
}

/**
 * 统一导航器实现
 */
class UnifyNavigatorImpl : UnifyNavigator {
    private var _navigationState by mutableStateOf(NavigationState())
    override val navigationState: NavigationState get() = _navigationState
    
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    override val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()
    
    override fun navigateTo(route: UnifyRoute, clearBackStack: Boolean) {
        val newBackStack = if (clearBackStack) {
            emptyList()
        } else {
            _navigationState.backStack + _navigationState.currentRoute
        }
        
        _navigationState = _navigationState.copy(
            currentRoute = route,
            backStack = newBackStack,
            canNavigateBack = newBackStack.isNotEmpty()
        )
        
        // 发送导航事件
        _navigationEvents.tryEmit(NavigationEvent.NavigateTo(route, clearBackStack))
    }
    
    override fun navigateBack() {
        if (_navigationState.backStack.isNotEmpty()) {
            val previousRoute = _navigationState.backStack.last()
            val newBackStack = _navigationState.backStack.dropLast(1)
            
            _navigationState = _navigationState.copy(
                currentRoute = previousRoute,
                backStack = newBackStack,
                canNavigateBack = newBackStack.isNotEmpty()
            )
            
            _navigationEvents.tryEmit(NavigationEvent.NavigateBack)
        }
    }
    
    override fun navigateUp(route: UnifyRoute?) {
        val targetRoute = route ?: if (_navigationState.backStack.isNotEmpty()) {
            _navigationState.backStack.last()
        } else {
            UnifyRoute.Home
        }
        
        navigateTo(targetRoute, clearBackStack = true)
        _navigationEvents.tryEmit(NavigationEvent.NavigateUp(route))
    }
    
    override fun clearBackStack() {
        _navigationState = _navigationState.copy(
            backStack = emptyList(),
            canNavigateBack = false
        )
        _navigationEvents.tryEmit(NavigationEvent.ClearBackStack)
    }
    
    override fun popToRoute(route: UnifyRoute, inclusive: Boolean) {
        val routeIndex = _navigationState.backStack.indexOfLast { it == route }
        if (routeIndex != -1) {
            val newBackStack = if (inclusive) {
                _navigationState.backStack.take(routeIndex)
            } else {
                _navigationState.backStack.take(routeIndex + 1)
            }
            
            _navigationState = _navigationState.copy(
                backStack = newBackStack,
                canNavigateBack = newBackStack.isNotEmpty()
            )
        }
        _navigationEvents.tryEmit(NavigationEvent.PopToRoute(route, inclusive))
    }
    
    override fun canNavigateBack(): Boolean = _navigationState.canNavigateBack
}

/**
 * 路由匹配器
 */
class RouteMatchers {
    companion object {
        fun matchRoute(path: String): UnifyRoute? {
            return when {
                path == "/" || path == "/home" -> UnifyRoute.Home
                path == "/login" -> UnifyRoute.Login
                path == "/users" -> UnifyRoute.UserList()
                path.startsWith("/users/") -> {
                    val userId = path.substringAfter("/users/")
                    UnifyRoute.UserDetail(userId)
                }
                path == "/settings" -> UnifyRoute.Settings()
                path.startsWith("/settings/") -> {
                    val section = path.substringAfter("/settings/")
                    UnifyRoute.Settings(section)
                }
                path.startsWith("/webview?") -> {
                    val params = parseQueryParams(path.substringAfter("?"))
                    val url = params["url"] ?: return null
                    val title = params["title"] ?: ""
                    UnifyRoute.WebView(url, title)
                }
                else -> UnifyRoute.CustomRoute(path, parseQueryParams(path))
            }
        }
        
        private fun parseQueryParams(query: String): Map<String, String> {
            if (query.isEmpty()) return emptyMap()
            
            return query.split("&").mapNotNull { param ->
                val parts = param.split("=", limit = 2)
                if (parts.size == 2) {
                    parts[0] to parts[1]
                } else null
            }.toMap()
        }
        
        fun routeToPath(route: UnifyRoute): String {
            return when (route) {
                is UnifyRoute.Home -> "/"
                is UnifyRoute.Login -> "/login"
                is UnifyRoute.UserList -> "/users"
                is UnifyRoute.UserDetail -> "/users/${route.userId}"
                is UnifyRoute.Settings -> if (route.section == "general") "/settings" else "/settings/${route.section}"
                is UnifyRoute.WebView -> "/webview?url=${route.url}&title=${route.title}"
                is UnifyRoute.CustomRoute -> route.path
            }
        }
    }
}

/**
 * 深度链接处理器
 */
class DeepLinkHandler(private val navigator: UnifyNavigator) {
    
    fun handleDeepLink(url: String): Boolean {
        val route = parseDeepLink(url)
        return if (route != null) {
            navigator.navigateTo(route, clearBackStack = true)
            true
        } else {
            false
        }
    }
    
    private fun parseDeepLink(url: String): UnifyRoute? {
        // 支持的深度链接格式:
        // unify://home
        // unify://users/123
        // unify://settings/profile
        // https://app.example.com/users/123
        
        val cleanUrl = when {
            url.startsWith("unify://") -> url.substringAfter("unify://")
            url.startsWith("https://app.example.com/") -> url.substringAfter("https://app.example.com/")
            url.startsWith("http://app.example.com/") -> url.substringAfter("http://app.example.com/")
            else -> return null
        }
        
        return RouteMatchers.matchRoute("/$cleanUrl")
    }
}

/**
 * 导航中间件
 */
interface NavigationMiddleware {
    fun beforeNavigation(from: UnifyRoute, to: UnifyRoute): Boolean
    fun afterNavigation(from: UnifyRoute, to: UnifyRoute)
}

/**
 * 认证导航中间件
 */
class AuthNavigationMiddleware(
    private val isAuthenticated: () -> Boolean
) : NavigationMiddleware {
    
    private val protectedRoutes = setOf(
        UnifyRoute.UserList::class,
        UnifyRoute.UserDetail::class,
        UnifyRoute.Settings::class
    )
    
    override fun beforeNavigation(from: UnifyRoute, to: UnifyRoute): Boolean {
        // 检查是否需要认证
        val requiresAuth = protectedRoutes.any { it.isInstance(to) }
        
        return if (requiresAuth && !isAuthenticated()) {
            // 重定向到登录页面
            false
        } else {
            true
        }
    }
    
    override fun afterNavigation(from: UnifyRoute, to: UnifyRoute) {
        // 导航完成后的处理
    }
}

/**
 * 日志导航中间件
 */
class LoggingNavigationMiddleware : NavigationMiddleware {
    override fun beforeNavigation(from: UnifyRoute, to: UnifyRoute): Boolean {
        println("Navigation: ${from::class.simpleName} -> ${to::class.simpleName}")
        return true
    }
    
    override fun afterNavigation(from: UnifyRoute, to: UnifyRoute) {
        println("Navigation completed: ${to::class.simpleName}")
    }
}

/**
 * 增强导航器（支持中间件）
 */
class EnhancedUnifyNavigator(
    private val baseNavigator: UnifyNavigator,
    private val middlewares: List<NavigationMiddleware> = emptyList()
) : UnifyNavigator by baseNavigator {
    
    override fun navigateTo(route: UnifyRoute, clearBackStack: Boolean) {
        val currentRoute = navigationState.currentRoute
        
        // 执行前置中间件
        val canNavigate = middlewares.all { it.beforeNavigation(currentRoute, route) }
        
        if (canNavigate) {
            baseNavigator.navigateTo(route, clearBackStack)
            
            // 执行后置中间件
            middlewares.forEach { it.afterNavigation(currentRoute, route) }
        } else {
            // 导航被中间件阻止，可能需要重定向到登录页面
            if (route !is UnifyRoute.Login) {
                baseNavigator.navigateTo(UnifyRoute.Login, clearBackStack = true)
            }
        }
    }
}
