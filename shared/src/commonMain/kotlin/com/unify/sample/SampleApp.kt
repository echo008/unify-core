package com.unify.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.unify.navigation.UnifyNavigation
import com.unify.navigation.UnifyRoute
import com.unify.sample.ui.UserDetailScreen
import com.unify.sample.ui.UserListScreen
import com.unify.ui.components.UnifyView
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

// Sample App Routes
sealed class SampleRoute : UnifyRoute {
    object UserList : SampleRoute() {
        override val path: String = "/users"
    }
    
    data class UserDetail(val userId: Long) : SampleRoute() {
        override val path: String = "/users/$userId"
    }
}

@Composable
fun SampleApp() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            UnifyView(
                modifier = Modifier.fillMaxSize()
            ) {
                SampleNavigation()
            }
        }
    }
}

@Composable
private fun SampleNavigation() {
    val navigation: UnifyNavigation = koinInject()
    var currentRoute by remember { mutableStateOf<SampleRoute>(SampleRoute.UserList) }
    
    when (currentRoute) {
        is SampleRoute.UserList -> {
            UserListScreen(
                onNavigateToDetail = { userId ->
                    currentRoute = SampleRoute.UserDetail(userId)
                }
            )
        }
        
        is SampleRoute.UserDetail -> {
            UserDetailScreen(
                userId = currentRoute.userId,
                onNavigateBack = {
                    currentRoute = SampleRoute.UserList
                }
            )
        }
    }
}

// Sample App with Koin DI Setup
@Composable
fun SampleAppWithDI() {
    KoinApplication(
        application = {
            modules(
                com.unify.di.commonModule,
                com.unify.sample.di.sampleModule
            )
        }
    ) {
        SampleApp()
    }
}
