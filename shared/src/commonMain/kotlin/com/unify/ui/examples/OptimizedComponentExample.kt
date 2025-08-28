package com.unify.ui.examples

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.unify.ui.components.UnifyButton
import com.unify.ui.components.UnifyCard
import com.unify.ui.components.UnifyDivider
import com.unify.ui.components.UnifyLoadingIndicator
import com.unify.ui.components.UnifyTextField
import com.unify.ui.performance.PerformanceMonitor
import com.unify.ui.theme.UnifyTheme
import kotlinx.coroutines.delay

/**
 * 优化组件示例 - 展示如何使用新的Compose优化系统
 */
@Composable
fun OptimizedComponentExample() {
    PerformanceMonitor(componentName = "OptimizedComponentExample") {
        UnifyTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 标题
                    Text(
                        text = "优化组件示例",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 输入表单示例
                    OptimizedFormExample()
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 按钮组示例
                    OptimizedButtonGroupExample()
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 卡片列表示例
                    OptimizedCardListExample()
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // 加载状态示例
                    OptimizedLoadingExample()
                }
            }
        }
    }
}

/**
 * 优化表单示例
 */
@Composable
fun OptimizedFormExample() {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "用户信息表单",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        UnifyTextField(
            value = name,
            onValueChange = { name = it },
            label = "姓名",
            placeholder = "请输入您的姓名",
            leadingIcon = Icons.Default.AccountCircle
        )
        
        UnifyTextField(
            value = email,
            onValueChange = { email = it },
            label = "邮箱",
            placeholder = "请输入您的邮箱",
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
            leadingIcon = Icons.Default.Email
        )
        
        UnifyTextField(
            value = phone,
            onValueChange = { phone = it },
            label = "手机号",
            placeholder = "请输入您的手机号",
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone,
            leadingIcon = Icons.Default.Phone
        )
        
        UnifyButton(
            text = "提交信息",
            onClick = { /* 处理表单提交 */ },
            modifier = Modifier.fillMaxWidth(),
            variant = com.unify.ui.components.ButtonVariant.Primary,
            size = com.unify.ui.components.ButtonSize.Large
        )
    }
}

/**
 * 优化按钮组示例
 */
@Composable
fun OptimizedButtonGroupExample() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "按钮变体示例",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UnifyButton(
                text = "主要按钮",
                onClick = {},
                modifier = Modifier.weight(1f),
                variant = com.unify.ui.components.ButtonVariant.Primary
            )
            
            UnifyButton(
                text = "次要按钮",
                onClick = {},
                modifier = Modifier.weight(1f),
                variant = com.unify.ui.components.ButtonVariant.Secondary
            )
        }
        
        UnifyButton(
            text = "轮廓按钮",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            variant = com.unify.ui.components.ButtonVariant.Outlined
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UnifyButton(
                text = "小按钮",
                onClick = {},
                modifier = Modifier.weight(1f),
                size = com.unify.ui.components.ButtonSize.Small
            )
            
            UnifyButton(
                text = "中按钮",
                onClick = {},
                modifier = Modifier.weight(1f),
                size = com.unify.ui.components.ButtonSize.Medium
            )
            
            UnifyButton(
                text = "大按钮",
                onClick = {},
                modifier = Modifier.weight(1f),
                size = com.unify.ui.components.ButtonSize.Large
            )
        }
        
        UnifyButton(
            text = "带图标按钮",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Default.Search,
            iconPosition = com.unify.ui.components.IconPosition.Start
        )
    }
}

/**
 * 优化卡片列表示例
 */
@Composable
fun OptimizedCardListExample() {
    val items = remember {
        listOf(
            CardItem("项目一", "这是第一个项目的描述", Icons.Default.AccountCircle),
            CardItem("项目二", "这是第二个项目的描述", Icons.Default.Email),
            CardItem("项目三", "这是第三个项目的描述", Icons.Default.Phone),
            CardItem("项目四", "这是第四个项目的描述", Icons.Default.Search)
        )
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "卡片列表示例",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        items.forEach { item ->
            UnifyCard(
                onClick = { /* 处理卡片点击 */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    UnifyDivider()
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    UnifyButton(
                        text = "查看详情",
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        size = com.unify.ui.components.ButtonSize.Small,
                        variant = com.unify.ui.components.ButtonVariant.Outlined
                    )
                }
            }
        }
    }
}

/**
 * 优化加载示例
 */
@Composable
fun OptimizedLoadingExample() {
    var isLoading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "加载状态示例",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            UnifyLoadingIndicator(
                size = 64.dp,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "加载中... ${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            UnifyButton(
                text = "开始加载",
                onClick = {
                    isLoading = true
                    progress = 0f
                },
                variant = com.unify.ui.components.ButtonVariant.Primary
            )
        }
    }
    
    // 模拟加载进度
    LaunchedEffect(isLoading) {
        if (isLoading) {
            while (progress < 1f) {
                delay(100)
                progress += 0.1f
                if (progress >= 1f) {
                    isLoading = false
                    progress = 0f
                }
            }
        }
    }
}

/**
 * 卡片项数据类
 */
data class CardItem(
    val title: String,
    val description: String,
    val icon: ImageVector
)