package com.unify.harmony

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unify.core.UnifyCore

/**
 * HarmonyOS平台的Unify应用入口
 * 参考KuiklyUI的实现方案，使用Compose + Kotlin Native架构
 */
@Composable
fun HarmonyUnifyApp() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Unify-Core for HarmonyOS",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "基于KuiklyUI方案的跨平台实现",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // 展示平台信息
                PlatformInfoCard()
            }
        }
    }
}

@Composable
private fun PlatformInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "平台信息",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "平台: HarmonyOS",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "架构: Kotlin Native + Compose",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "渲染: 原生控件映射",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
