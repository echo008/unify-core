package com.unify.demo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unify.ui.components.container.UnifySection
import com.unify.ui.components.input.UnifySwitchWithLabel

/**
 * Unify用户配置文件演示界面
 * 展示跨平台用户配置功能
 */

data class UserProfile(
    val name: String,
    val email: String,
    val avatar: String,
    val joinDate: String,
    val level: Int,
    val points: Int
)

data class UserPreference(
    val id: String,
    val title: String,
    val description: String,
    val enabled: Boolean
)

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier
) {
    var userProfile by remember { 
        mutableStateOf(
            UserProfile(
                name = "张三",
                email = "zhangsan@example.com",
                avatar = "👤",
                joinDate = "2023年1月",
                level = 15,
                points = 2580
            )
        )
    }
    
    var preferences by remember { mutableStateOf(getDefaultPreferences()) }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ProfileHeader(profile = userProfile)
        }
        
        item {
            ProfileStats(profile = userProfile)
        }
        
        item {
            UnifySection(
                title = "个人设置",
                subtitle = "自定义您的应用体验"
            ) {
                preferences.forEach { preference ->
                    UnifySwitchWithLabel(
                        checked = preference.enabled,
                        onCheckedChange = { enabled ->
                            preferences = preferences.map { pref ->
                                if (pref.id == preference.id) {
                                    pref.copy(enabled = enabled)
                                } else pref
                            }
                        },
                        label = preference.title,
                        description = preference.description,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
        
        item {
            ProfileActions()
        }
    }
}

@Composable
private fun ProfileHeader(
    profile: UserProfile,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 头像
            Surface(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.avatar,
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 用户信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = profile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                
                Text(
                    text = "加入时间: ${profile.joinDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ProfileStats(
    profile: UserProfile,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "统计信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = "等级",
                    value = "Lv.${profile.level}",
                    color = Color(0xFF4CAF50)
                )
                
                StatItem(
                    label = "积分",
                    value = "${profile.points}",
                    color = Color(0xFF2196F3)
                )
                
                StatItem(
                    label = "徽章",
                    value = "12",
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileActions(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "账户操作",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { /* 编辑资料 */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("编辑资料")
                }
                
                OutlinedButton(
                    onClick = { /* 更改密码 */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("更改密码")
                }
                
                OutlinedButton(
                    onClick = { /* 隐私设置 */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("隐私设置")
                }
                
                OutlinedButton(
                    onClick = { /* 数据导出 */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("数据导出")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { /* 退出登录 */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("退出登录")
                }
            }
        }
    }
}

private fun getDefaultPreferences(): List<UserPreference> {
    return listOf(
        UserPreference(
            id = "notifications",
            title = "推送通知",
            description = "接收应用推送消息",
            enabled = true
        ),
        UserPreference(
            id = "dark_mode",
            title = "深色模式",
            description = "使用深色主题界面",
            enabled = false
        ),
        UserPreference(
            id = "auto_sync",
            title = "自动同步",
            description = "自动同步数据到云端",
            enabled = true
        ),
        UserPreference(
            id = "location",
            title = "位置服务",
            description = "允许获取位置信息",
            enabled = false
        ),
        UserPreference(
            id = "analytics",
            title = "数据分析",
            description = "帮助改进应用体验",
            enabled = true
        ),
        UserPreference(
            id = "biometric",
            title = "生物识别",
            description = "使用指纹或面部识别",
            enabled = true
        ),
        UserPreference(
            id = "offline_mode",
            title = "离线模式",
            description = "无网络时继续使用",
            enabled = false
        ),
        UserPreference(
            id = "high_quality",
            title = "高质量模式",
            description = "使用更高质量的内容",
            enabled = false
        )
    )
}
