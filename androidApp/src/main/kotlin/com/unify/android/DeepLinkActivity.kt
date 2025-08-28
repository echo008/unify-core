package com.unify.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unify.navigation.UnifyNavigator
import com.unify.navigation.UnifyRoute
import com.unify.ui.theme.UnifyTheme
import org.koin.android.ext.android.inject

/**
 * 深度链接处理Activity
 * 处理外部应用跳转到Unify应用的深度链接
 */
class DeepLinkActivity : ComponentActivity() {
    
    private val navigator: UnifyNavigator by inject()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val deepLinkUri = intent?.data
        
        setContent {
            UnifyTheme {
                DeepLinkHandler(
                    deepLinkUri = deepLinkUri,
                    onNavigateToMain = { navigateToMain() },
                    onNavigateToRoute = { route -> navigateToRoute(route) }
                )
            }
        }
        
        // 处理深度链接
        handleDeepLink(deepLinkUri)
    }
    
    private fun handleDeepLink(uri: Uri?) {
        if (uri == null) {
            navigateToMain()
            return
        }
        
        when (uri.host) {
            "user" -> {
                val userId = uri.pathSegments.getOrNull(0)
                if (userId != null) {
                    navigateToRoute(UnifyRoute(
                        path = "/user/$userId",
                        name = "UserDetail"
                    ))
                } else {
                    navigateToMain()
                }
            }
            "sample" -> {
                navigateToMain()
            }
            else -> {
                navigateToMain()
            }
        }
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    private fun navigateToRoute(route: UnifyRoute) {
        navigator.navigateTo(route)
        navigateToMain()
    }
}

@Composable
fun DeepLinkHandler(
    deepLinkUri: Uri?,
    onNavigateToMain: () -> Unit,
    onNavigateToRoute: (UnifyRoute) -> Unit
) {
    LaunchedEffect(deepLinkUri) {
        // 延迟一下，让用户看到处理界面
        kotlinx.coroutines.delay(1000)
        onNavigateToMain()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "正在处理深度链接...",
            style = MaterialTheme.typography.bodyLarge
        )
        
        deepLinkUri?.let { uri ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = uri.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
