package com.unify.ui.components.platform

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 小程序平台UI适配器实现
 */
@Composable
actual fun UnifyPlatformButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    text: String
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF07C160)
        )
    ) {
        Text(text = text, color = Color.White)
    }
}

@Composable
actual fun UnifyPlatformTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    placeholder: String,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = { Text(placeholder) },
        enabled = enabled,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF07C160)
        )
    )
}

@Composable
actual fun UnifyPlatformSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier,
    enabled: Boolean
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color(0xFF07C160),
            checkedTrackColor = Color(0xFF07C160).copy(alpha = 0.5f)
        )
    )
}

@Composable
actual fun UnifyPlatformSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        valueRange = valueRange,
        enabled = enabled,
        colors = SliderDefaults.colors(
            thumbColor = Color(0xFF07C160),
            activeTrackColor = Color(0xFF07C160)
        )
    )
}

@Composable
actual fun UnifyPlatformProgressBar(
    progress: Float,
    modifier: Modifier,
    color: Color
) {
    LinearProgressIndicator(
        progress = progress,
        modifier = modifier,
        color = color.takeIf { it != Color.Unspecified } ?: Color(0xFF07C160)
    )
}

@Composable
actual fun UnifyPlatformDialog(
    onDismissRequest: () -> Unit,
    title: String,
    content: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)?
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title) },
        text = content,
        confirmButton = confirmButton,
        dismissButton = dismissButton
    )
}

@Composable
actual fun UnifyPlatformCard(
    modifier: Modifier,
    onClick: (() -> Unit)?,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick ?: {},
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        content()
    }
}

@Composable
actual fun UnifyPlatformChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    selected: Boolean
) {
    FilterChip(
        onClick = onClick,
        label = { Text(text) },
        selected = selected,
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFF07C160),
            selectedLabelColor = Color.White
        )
    )
}

/**
 * 小程序平台特有的UI组件和交互方式
 */
object MiniAppUnifyPlatformAdapters {
    
    /**
     * 小程序导航栏
     */
    @Composable
    fun MiniAppNavigationBar(
        title: String,
        backgroundColor: Color = Color(0xFF000000),
        textColor: Color = Color.White,
        onBackClick: (() -> Unit)? = null,
        actions: List<NavigationAction> = emptyList(),
        modifier: Modifier = Modifier
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = backgroundColor
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 返回按钮
                onBackClick?.let { backAction ->
                    IconButton(
                        onClick = backAction,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = textColor
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                // 标题
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )
                
                // 操作按钮
                actions.forEach { action ->
                    IconButton(
                        onClick = action.onClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = action.contentDescription,
                            tint = textColor
                        )
                    }
                }
            }
        }
    }
    
    /**
     * 小程序底部标签栏
     */
    @Composable
    fun MiniAppTabBar(
        tabs: List<TabItem>,
        selectedTab: Int,
        onTabSelected: (Int) -> Unit,
        modifier: Modifier = Modifier
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = { onTabSelected(index) }
                        ) {
                            Icon(
                                imageVector = if (selectedTab == index) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.text,
                                tint = if (selectedTab == index) Color(0xFF07C160) else Color(0xFF999999)
                            )
                        }
                        Text(
                            text = tab.text,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selectedTab == index) Color(0xFF07C160) else Color(0xFF999999)
                        )
                    }
                }
            }
        }
    }
    
    /**
     * 小程序分享面板
     */
    @Composable
    fun MiniAppSharePanel(
        shareOptions: List<ShareOption>,
        onShareOptionSelected: (ShareOption) -> Unit,
        onDismiss: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "分享到",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(shareOptions.size) { index ->
                        val option = shareOptions[index]
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = option.backgroundColor,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable { onShareOptionSelected(option) }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = option.icon,
                                        contentDescription = option.name,
                                        tint = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = option.name,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("取消")
                }
            }
        }
    }
    
    /**
     * 小程序授权弹窗
     */
    @Composable
    fun MiniAppAuthDialog(
        title: String,
        description: String,
        permissions: List<String>,
        onAllow: () -> Unit,
        onDeny: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // 权限列表
                permissions.forEach { permission ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF07C160),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = permission,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDeny,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF666666)
                        )
                    ) {
                        Text("拒绝")
                    }
                    
                    Button(
                        onClick = onAllow,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF07C160)
                        )
                    ) {
                        Text("允许")
                    }
                }
            }
        }
    }
    
    /**
     * 小程序加载指示器
     */
    @Composable
    fun MiniAppLoadingIndicator(
        text: String = "加载中...",
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Color(0xFF07C160),
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666)
                )
            }
        }
    }
    
    /**
     * 小程序支付面板
     */
    @Composable
    fun MiniAppPaymentPanel(
        amount: String,
        paymentMethods: List<PaymentMethod>,
        selectedMethod: PaymentMethod?,
        onMethodSelected: (PaymentMethod) -> Unit,
        onConfirmPayment: () -> Unit,
        onCancel: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // 支付金额
                Text(
                    text = "支付金额",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "¥$amount",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFFF6B35),
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                
                // 支付方式
                Text(
                    text = "选择支付方式",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                paymentMethods.forEach { method ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedMethod == method,
                            onClick = { onMethodSelected(method) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = Color(0xFF07C160)
                            )
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Icon(
                            imageVector = method.icon,
                            contentDescription = method.name,
                            tint = method.iconColor
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = method.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消")
                    }
                    
                    Button(
                        onClick = onConfirmPayment,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF07C160)
                        ),
                        enabled = selectedMethod != null
                    ) {
                        Text("确认支付")
                    }
                }
            }
        }
    }
}

/**
 * 导航操作
 */
data class NavigationAction(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit
)

/**
 * 标签项
 */
data class TabItem(
    val text: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)

/**
 * 分享选项
 */
data class ShareOption(
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val backgroundColor: Color
)

/**
 * 支付方式
 */
data class PaymentMethod(
    val id: String,
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconColor: Color
)
