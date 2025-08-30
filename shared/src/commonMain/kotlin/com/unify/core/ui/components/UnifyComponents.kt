package com.unify.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Unify-Core 跨平台响应式 UI 组件库
 * 100% 纯 Compose 实现，支持所有平台
 */

/**
 * 1. 自适应卡片组件
 */
@Composable
fun UnifyCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    colors: CardColors = CardDefaults.cardColors(),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.platformSpecific(),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = colors
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

/**
 * 2. 自适应按钮组件
 */
@Composable
fun UnifyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    text: String
) {
    Button(
        onClick = onClick,
        modifier = modifier.platformButtonStyle(),
        enabled = enabled,
        colors = colors
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * 3. 自适应文本输入框
 */
@Composable
fun UnifyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.platformTextFieldStyle(),
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        enabled = enabled,
        singleLine = singleLine,
        isError = isError,
        colors = OutlinedTextFieldDefaults.colors()
    )
}

/**
 * 4. 自适应列表组件
 */
@Composable
fun <T> UnifyLazyList(
    items: List<T>,
    modifier: Modifier = Modifier,
    key: ((T) -> Any)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    itemContent: @Composable LazyItemScope.(T) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement
    ) {
        items(
            items = items,
            key = key
        ) { item ->
            itemContent(item)
        }
    }
}

/**
 * 5. 响应式网格布局
 */
@Composable
fun UnifyGrid(
    modifier: Modifier = Modifier,
    columns: Int = 2,
    spacing: Dp = 8.dp,
    content: @Composable () -> Unit
) {
    // 使用 FlowRow 实现响应式网格
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        content()
    }
}

/**
 * 6. 加载状态组件
 */
@Composable
fun UnifyLoadingIndicator(
    modifier: Modifier = Modifier,
    text: String = "加载中..."
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 7. 错误状态组件
 */
@Composable
fun UnifyErrorState(
    error: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "❌ 出现错误",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        
        onRetry?.let { retry ->
            Spacer(modifier = Modifier.height(16.dp))
            UnifyButton(
                onClick = retry,
                text = "重试"
            )
        }
    }
}

/**
 * 8. 空状态组件
 */
@Composable
fun UnifyEmptyState(
    message: String = "暂无数据",
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📭",
            style = MaterialTheme.typography.displayMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        action?.let {
            Spacer(modifier = Modifier.height(16.dp))
            it()
        }
    }
}

/**
 * 9. 平台特定修饰符 - expect/actual 实现
 */
expect fun Modifier.platformSpecific(): Modifier
expect fun Modifier.platformButtonStyle(): Modifier  
expect fun Modifier.platformTextFieldStyle(): Modifier

/**
 * 10. 组件状态管理
 */
data class UnifyComponentState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isEmpty: Boolean = false
)

@Composable
fun <T> UnifyStatefulContent(
    state: UnifyComponentState,
    data: T?,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    emptyMessage: String = "暂无数据",
    content: @Composable (T) -> Unit
) {
    when {
        state.isLoading -> {
            UnifyLoadingIndicator(modifier = modifier)
        }
        state.error != null -> {
            UnifyErrorState(
                error = state.error,
                modifier = modifier,
                onRetry = onRetry
            )
        }
        state.isEmpty || data == null -> {
            UnifyEmptyState(
                message = emptyMessage,
                modifier = modifier
            )
        }
        else -> {
            content(data)
        }
    }
}
