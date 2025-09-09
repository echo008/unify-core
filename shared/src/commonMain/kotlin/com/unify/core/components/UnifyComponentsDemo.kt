package com.unify.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Unify基础组件演示应用
 * 展示所有基础UI组件的使用方法
 */
@Composable
fun UnifyComponentsDemo() {
    var textFieldValue by remember { mutableStateOf("") }
    var selectedNavItem by remember { mutableStateOf("home") }
    var showDialog by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    UnifyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        spacing = UnifySpacing.LARGE,
    ) {
        // 标题
        Text(
            text = "🚀 Unify UI 组件演示",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        // 按钮演示
        UnifySection(title = "按钮组件") {
            UnifyRow(
                spacing = UnifySpacing.SMALL,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                UnifyButton(
                    onClick = { },
                    text = "填充按钮",
                    type = UnifyButtonType.FILLED,
                )
                UnifyButton(
                    onClick = { },
                    text = "轮廓按钮",
                    type = UnifyButtonType.OUTLINED,
                )
                UnifyButton(
                    onClick = { },
                    text = "文本按钮",
                    type = UnifyButtonType.TEXT,
                )
            }

            UnifyRow(
                spacing = UnifySpacing.SMALL,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                UnifyButton(
                    onClick = { },
                    text = "带图标",
                    icon = Icons.Default.Add,
                    type = UnifyButtonType.FILLED,
                )
                UnifyButton(
                    onClick = { },
                    text = "加载中",
                    loading = true,
                    type = UnifyButtonType.OUTLINED,
                )
            }
        }

        // 文本输入演示
        UnifySection(title = "文本输入组件") {
            UnifyTextField(
                value = textFieldValue,
                onValueChange = { textFieldValue = it },
                label = "标签",
                placeholder = "请输入内容",
                leadingIcon = {
                    androidx.compose.material3.Icon(
                        Icons.Default.Email,
                        contentDescription = null,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )

            UnifyTextField(
                value = "",
                onValueChange = { },
                label = "密码输入",
                placeholder = "请输入密码",
                isPassword = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // 卡片演示
        UnifySection(title = "卡片组件") {
            UnifyCard(
                type = UnifyCardType.FILLED,
                onClick = { },
            ) {
                UnifyColumn(
                    modifier = Modifier.padding(16.dp),
                    spacing = UnifySpacing.SMALL,
                ) {
                    Text(
                        text = "填充卡片",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "这是一个可点击的填充卡片示例",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            UnifyCard(type = UnifyCardType.OUTLINED) {
                UnifyColumn(
                    modifier = Modifier.padding(16.dp),
                    spacing = UnifySpacing.SMALL,
                ) {
                    Text(
                        text = "轮廓卡片",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "这是一个轮廓卡片示例",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // 列表演示
        UnifySection(title = "列表组件") {
            val listItems =
                listOf(
                    UnifyListItem(
                        id = "1",
                        title = "列表项 1",
                        subtitle = "这是副标题",
                        leadingIcon = Icons.Default.Person,
                        trailingIcon = Icons.Default.Edit,
                        onClick = { },
                    ),
                    UnifyListItem(
                        id = "2",
                        title = "列表项 2",
                        subtitle = "这是另一个副标题",
                        leadingIcon = Icons.Default.Settings,
                        onClick = { },
                    ),
                    UnifyListItem(
                        id = "3",
                        title = "禁用的列表项",
                        subtitle = "这个项目被禁用了",
                        leadingIcon = Icons.Default.Delete,
                        enabled = false,
                    ),
                )

            UnifyCard {
                UnifyColumn {
                    listItems.forEachIndexed { index, item ->
                        UnifyListItemComponent(item = item)
                        if (index < listItems.size - 1) {
                            UnifyDivider()
                        }
                    }
                }
            }
        }

        // 导航演示
        UnifySection(title = "导航组件") {
            val navItems =
                listOf(
                    UnifyNavigationItem(
                        id = "home",
                        label = "首页",
                        icon = Icons.Default.Home,
                    ),
                    UnifyNavigationItem(
                        id = "search",
                        label = "搜索",
                        icon = Icons.Default.Search,
                        badgeCount = 3,
                    ),
                    UnifyNavigationItem(
                        id = "profile",
                        label = "个人",
                        icon = Icons.Default.Person,
                    ),
                )

            UnifyBottomNavigation(
                items = navItems,
                selectedItemId = selectedNavItem,
                onItemSelected = { selectedNavItem = it },
            )
        }

        // 反馈组件演示
        UnifySection(title = "反馈组件") {
            UnifyFeedbackBanner(
                message = "这是一个成功消息",
                type = UnifyFeedbackType.SUCCESS,
                icon = Icons.Default.Check,
            )

            UnifyFeedbackBanner(
                message = "这是一个警告消息",
                type = UnifyFeedbackType.WARNING,
                icon = Icons.Default.Warning,
            )

            UnifyFeedbackBanner(
                message = "这是一个错误消息",
                type = UnifyFeedbackType.ERROR,
                icon = Icons.Default.Error,
                onDismiss = { },
            )

            UnifyProgressIndicator(
                progress = 0.7f,
                isLinear = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // 对话框演示
        UnifySection(title = "对话框组件") {
            UnifyRow(
                spacing = UnifySpacing.SMALL,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                UnifyButton(
                    onClick = { showDialog = true },
                    text = "显示提示框",
                    type = UnifyButtonType.OUTLINED,
                )
                UnifyButton(
                    onClick = { showConfirmDialog = true },
                    text = "显示确认框",
                    type = UnifyButtonType.OUTLINED,
                )
            }
        }

        // 布局演示
        UnifySection(title = "布局组件") {
            UnifyCard {
                UnifyColumn(
                    modifier = Modifier.padding(16.dp),
                    spacing = UnifySpacing.MEDIUM,
                ) {
                    Text(
                        text = "网格布局示例",
                        style = MaterialTheme.typography.titleMedium,
                    )

                    UnifyRow(
                        spacing = UnifySpacing.SMALL,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        repeat(3) { index ->
                            UnifyCard(
                                type = UnifyCardType.OUTLINED,
                                modifier = Modifier.weight(1f),
                            ) {
                                UnifyBox(
                                    modifier = Modifier.padding(16.dp),
                                    contentAlignment = androidx.compose.ui.Alignment.Center,
                                ) {
                                    Text("项目 ${index + 1}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 对话框
    if (showDialog) {
        UnifyAlertDialog(
            onDismissRequest = { showDialog = false },
            title = "提示",
            message = "这是一个提示对话框示例",
            icon = Icons.Default.Info,
        )
    }

    if (showConfirmDialog) {
        UnifyConfirmationDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = "确认操作",
            message = "您确定要执行此操作吗？",
            onConfirm = { showConfirmDialog = false },
            icon = Icons.Default.Warning,
        )
    }
}
