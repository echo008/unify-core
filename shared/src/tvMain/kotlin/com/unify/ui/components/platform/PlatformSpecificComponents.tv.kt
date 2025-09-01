package com.unify.ui.components.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * TV平台特定组件实现
 * 适配Android TV、tvOS、HarmonyOS TV的大屏幕和遥控器交互
 */

@Composable
actual fun PlatformSpecificButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp)
            .focusable()
            .onFocusChanged { isFocused = it.isFocused },
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFocused) Color(0xFF1976D2) else Color(0xFF2196F3),
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isFocused) 8.dp else 4.dp
        )
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
actual fun PlatformSpecificCard(
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF1E1E1E)) // TV深色背景
            .border(
                width = if (isFocused) 3.dp else 1.dp,
                color = if (isFocused) Color(0xFF2196F3) else Color(0xFF333333),
                shape = RoundedCornerShape(12.dp)
            )
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .padding(20.dp),
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
    var isFocused by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontSize = 16.sp,
                color = Color(0xFFB3B3B3),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2D2D2D))
                .border(
                    width = if (isFocused) 3.dp else 1.dp,
                    color = if (isFocused) Color(0xFF2196F3) else Color(0xFF555555),
                    shape = RoundedCornerShape(8.dp)
                )
                .focusable()
                .onFocusChanged { isFocused = it.isFocused }
                .clickable { /* 触发虚拟键盘 */ }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = if (value.isNotEmpty()) value else placeholder,
                fontSize = 16.sp,
                color = if (value.isNotEmpty()) Color.White else Color(0xFF777777)
            )
        }
    }
}

@Composable
actual fun PlatformSpecificList(
    items: List<String>,
    onItemClick: (String) -> Unit,
    modifier: Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(items) { item ->
            var isFocused by remember { mutableStateOf(false) }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isFocused) Color(0xFF2D2D2D) else Color(0xFF1E1E1E)
                    )
                    .border(
                        width = if (isFocused) 2.dp else 0.dp,
                        color = Color(0xFF2196F3),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .focusable()
                    .onFocusChanged { isFocused = it.isFocused }
                    .clickable { onItemClick(item) }
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item,
                    fontSize = 18.sp,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "▶",
                    fontSize = 16.sp,
                    color = if (isFocused) Color(0xFF2196F3) else Color(0xFF777777)
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
    Card(
        modifier = modifier
            .fillMaxWidth(0.6f)
            .padding(32.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = content,
                fontSize = 18.sp,
                color = Color(0xFFB3B3B3),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                var cancelFocused by remember { mutableStateOf(false) }
                var confirmFocused by remember { mutableStateOf(false) }
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .focusable()
                        .onFocusChanged { cancelFocused = it.isFocused },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (cancelFocused) Color(0xFF555555) else Color(0xFF333333)
                    )
                ) {
                    Text("取消", fontSize = 16.sp)
                }
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .focusable()
                        .onFocusChanged { confirmFocused = it.isFocused },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (confirmFocused) Color(0xFF1976D2) else Color(0xFF2196F3)
                    )
                ) {
                    Text("确认", fontSize = 16.sp)
                }
            }
        }
    }
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
            .background(Color(0xFF1E1E1E))
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEachIndexed { index, item ->
            var isFocused by remember { mutableStateOf(false) }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .focusable()
                    .onFocusChanged { isFocused = it.isFocused }
                    .clickable { onItemSelected(index) }
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                selectedIndex == index -> Color(0xFF2196F3)
                                isFocused -> Color(0xFF333333)
                                else -> Color.Transparent
                            }
                        )
                        .border(
                            width = if (isFocused) 2.dp else 0.dp,
                            color = Color(0xFF2196F3),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.icon ?: "•",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    color = if (selectedIndex == index || isFocused) Color.White else Color(0xFF777777)
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
                color = Color(0xFF2196F3),
                strokeWidth = 4.dp,
                modifier = Modifier.size(48.dp)
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
    var isFocused by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .size(width = 64.dp, height = 32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (checked) Color(0xFF2196F3) else Color(0xFF333333)
            )
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = Color(0xFF2196F3),
                shape = RoundedCornerShape(16.dp)
            )
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .clickable { onCheckedChange(!checked) },
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(4.dp)
        )
    }
}

@Composable
actual fun PlatformSpecificSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "%.1f".format(value),
            fontSize = 16.sp,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFF2196F3),
                activeTrackColor = Color(0xFF2196F3),
                inactiveTrackColor = Color(0xFF333333)
            )
        )
    }
}

@Composable
actual fun PlatformSpecificImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF2D2D2D))
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) Color(0xFF2196F3) else Color(0xFF333333),
                shape = RoundedCornerShape(8.dp)
            )
            .focusable()
            .onFocusChanged { isFocused = it.isFocused },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "图片",
            fontSize = 16.sp,
            color = Color(0xFF777777)
        )
    }
}
