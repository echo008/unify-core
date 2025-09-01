package com.unify.ui.components.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Web平台特定组件实现
 * 遵循现代Web设计规范，支持响应式布局
 */

@Composable
actual fun PlatformSpecificButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF0066CC), // Web蓝色
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
actual fun PlatformSpecificCard(
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color(0xFFE0E0E0),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp),
        content = content
    )
}

@Composable
actual fun PlatformSpecificTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier
) {
    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333),
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = if (placeholder.isNotEmpty()) { 
                { Text(placeholder, color = Color(0xFF999999)) } 
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0066CC),
                unfocusedBorderColor = Color(0xFFCCCCCC),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )
    }
}

@Composable
actual fun PlatformSpecificList(
    items: List<String>,
    onItemClick: (String) -> Unit,
    modifier: Modifier
) {
    LazyColumn(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = Color(0xFFE0E0E0),
                shape = RoundedCornerShape(8.dp)
            )
            .background(Color.White),
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        items(items) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(item) }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .background(Color.Transparent),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item,
                    fontSize = 14.sp,
                    color = Color(0xFF333333),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "→",
                    fontSize = 14.sp,
                    color = Color(0xFF999999)
                )
            }
        }
    }
}

@Composable
actual fun PlatformSpecificDialog(
    title: String,
    content: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333)
            )
        },
        text = {
            Text(
                text = content,
                fontSize = 14.sp,
                color = Color(0xFF666666)
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "确认",
                    fontSize = 14.sp,
                    color = Color(0xFF0066CC),
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "取消",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            }
        },
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        containerColor = Color.White
    )
}

@Composable
actual fun PlatformSpecificNavigationBar(
    items: List<NavigationItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .border(
                width = 1.dp,
                color = Color(0xFFE0E0E0)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEachIndexed { index, item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onItemSelected(index) }
                    .padding(8.dp)
            ) {
                Text(
                    text = item.icon ?: "•",
                    fontSize = 20.sp,
                    color = if (selectedIndex == index) Color(0xFF0066CC) else Color(0xFF999999)
                )
                Text(
                    text = item.title,
                    fontSize = 12.sp,
                    color = if (selectedIndex == index) Color(0xFF0066CC) else Color(0xFF999999)
                )
            }
        }
    }
}

@Composable
actual fun PlatformSpecificLoadingIndicator(
    isLoading: Boolean,
    modifier: Modifier
) {
    if (isLoading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = Color(0xFF0066CC),
                strokeWidth = 3.dp
            )
        }
    }
}

@Composable
actual fun PlatformSpecificSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = Color(0xFF0066CC),
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = Color(0xFFCCCCCC)
        )
    )
}

@Composable
actual fun PlatformSpecificSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = valueRange,
        modifier = modifier,
        colors = SliderDefaults.colors(
            thumbColor = Color(0xFF0066CC),
            activeTrackColor = Color(0xFF0066CC),
            inactiveTrackColor = Color(0xFFE0E0E0)
        )
    )
}

@Composable
actual fun PlatformSpecificImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier
) {
    // Web平台使用简化的图片加载实现
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFFF5F5F5))
            .border(
                width = 1.dp,
                color = Color(0xFFE0E0E0),
                shape = RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "图片",
            fontSize = 12.sp,
            color = Color(0xFF999999)
        )
    }
}
