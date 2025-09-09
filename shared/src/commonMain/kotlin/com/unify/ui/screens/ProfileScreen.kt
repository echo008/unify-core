package com.unify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unify.core.platform.getCurrentTimeMillis

/**
 * 用户档案界面
 * 显示用户信息、统计数据和操作选项
 */
@Composable
fun ProfileScreen() {
    var userProfile by remember { mutableStateOf(generateUserProfile()) }

    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ProfileHeader(userProfile = userProfile)
        }

        item {
            ProfileStats(stats = userProfile.stats)
        }

        item {
            Text(
                text = "账户设置",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }

        items(getProfileMenuItems()) { menuItem ->
            ProfileMenuItem(
                item = menuItem,
                onClick = { /* 处理点击事件 */ },
            )
        }

        item {
            Text(
                text = "最近活动",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }

        items(userProfile.recentActivities) { activity ->
            ActivityItem(activity = activity)
        }
    }
}

@Composable
private fun ProfileHeader(userProfile: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 头像占位符
            Surface(
                modifier =
                    Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "用户头像",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = userProfile.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = userProfile.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color =
                    when (userProfile.membershipLevel) {
                        MembershipLevel.PREMIUM -> Color(0xFFFFD700)
                        MembershipLevel.STANDARD -> Color(0xFFC0C0C0)
                        MembershipLevel.BASIC -> Color(0xFFCD7F32)
                    },
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    text = userProfile.membershipLevel.displayName,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Black,
                )
            }
        }
    }
}

@Composable
private fun ProfileStats(stats: UserStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "统计信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(
                    label = "总使用时长",
                    value = "${stats.totalUsageHours}小时",
                )
                StatItem(
                    label = "完成任务",
                    value = "${stats.completedTasks}",
                )
                StatItem(
                    label = "获得积分",
                    value = "${stats.earnedPoints}",
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(
                    label = "连续签到",
                    value = "${stats.streakDays}天",
                )
                StatItem(
                    label = "好友数量",
                    value = "${stats.friendsCount}",
                )
                StatItem(
                    label = "成就数量",
                    value = "${stats.achievementsCount}",
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProfileMenuItem(
    item: ProfileMenuItemData,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                if (item.subtitle.isNotEmpty()) {
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "前往",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ActivityItem(activity: UserActivity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = activity.icon,
                        contentDescription = activity.title,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                text = formatActivityTime(activity.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun getProfileMenuItems(): List<ProfileMenuItemData> {
    return listOf(
        ProfileMenuItemData(
            icon = Icons.Default.Settings,
            title = "账户设置",
            subtitle = "个人信息、密码、隐私设置",
        ),
        ProfileMenuItemData(
            icon = Icons.Default.Notifications,
            title = "通知设置",
            subtitle = "推送通知、邮件提醒",
        ),
        ProfileMenuItemData(
            icon = Icons.Default.Security,
            title = "安全中心",
            subtitle = "登录记录、设备管理",
        ),
        ProfileMenuItemData(
            icon = Icons.Default.Payment,
            title = "支付管理",
            subtitle = "支付方式、账单记录",
        ),
        ProfileMenuItemData(
            icon = Icons.AutoMirrored.Filled.Help,
            title = "帮助中心",
            subtitle = "常见问题、联系客服",
        ),
        ProfileMenuItemData(
            icon = Icons.Default.Info,
            title = "关于应用",
            subtitle = "版本信息、用户协议",
        ),
    )
}

private fun generateUserProfile(): UserProfile {
    return UserProfile(
        name = "张三",
        email = "zhangsan@example.com",
        membershipLevel = MembershipLevel.PREMIUM,
        stats =
            UserStats(
                totalUsageHours = 156,
                completedTasks = 89,
                earnedPoints = 2340,
                streakDays = 15,
                friendsCount = 42,
                achievementsCount = 18,
            ),
        recentActivities =
            listOf(
                UserActivity(
                    icon = Icons.Default.CheckCircle,
                    title = "完成任务",
                    description = "完成了数据同步任务",
                    timestamp = getCurrentTimeMillis() - 3600000,
                ),
                UserActivity(
                    icon = Icons.Default.Star,
                    title = "获得成就",
                    description = "解锁了连续使用7天成就",
                    timestamp = getCurrentTimeMillis() - 7200000,
                ),
                UserActivity(
                    icon = Icons.Default.Person,
                    title = "更新资料",
                    description = "更新了个人头像",
                    timestamp = getCurrentTimeMillis() - 86400000,
                ),
            ),
    )
}

private fun formatActivityTime(timestamp: Long): String {
    val now = getCurrentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 3600000 -> "${diff / 60000}分钟前"
        diff < 86400000 -> "${diff / 3600000}小时前"
        else -> "${diff / 86400000}天前"
    }
}

data class UserProfile(
    val name: String,
    val email: String,
    val membershipLevel: MembershipLevel,
    val stats: UserStats,
    val recentActivities: List<UserActivity>,
)

data class UserStats(
    val totalUsageHours: Int,
    val completedTasks: Int,
    val earnedPoints: Int,
    val streakDays: Int,
    val friendsCount: Int,
    val achievementsCount: Int,
)

data class UserActivity(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val timestamp: Long,
)

data class ProfileMenuItemData(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
)

enum class MembershipLevel(val displayName: String) {
    BASIC("基础会员"),
    STANDARD("标准会员"),
    PREMIUM("高级会员"),
}
