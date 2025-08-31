package com.unify.core.dynamic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * Compose组件工厂
 * 基于Compose语法创建动态组件
 */
class ComposeComponentFactory(
    private val componentData: ComponentData
) : ComponentFactory {
    
    @Composable
    override fun CreateComponent(
        props: Map<String, Any>,
        children: @Composable () -> Unit
    ) {
        try {
            when (componentData.metadata.name) {
                "DynamicText" -> DynamicTextComponent(props)
                "DynamicButton" -> DynamicButtonComponent(props, children)
                "DynamicCard" -> DynamicCardComponent(props, children)
                "DynamicList" -> DynamicListComponent(props)
                "DynamicForm" -> DynamicFormComponent(props)
                "DynamicChart" -> DynamicChartComponent(props)
                else -> {
                    // 解析并执行动态代码
                    ExecuteDynamicCode(componentData.code, props, children)
                }
            }
        } catch (e: Exception) {
            ErrorComponent(
                error = "组件渲染失败: ${e.message}",
                componentId = componentData.metadata.name
            )
        }
    }
    
    override fun getMetadata(): ComponentMetadata = componentData.metadata
}

/**
 * 原生组件工厂
 * 调用平台原生组件
 */
class NativeComponentFactory(
    private val componentData: ComponentData
) : ComponentFactory {
    
    @Composable
    override fun CreateComponent(
        props: Map<String, Any>,
        children: @Composable () -> Unit
    ) {
        // 根据平台调用相应的原生组件
        when (PlatformManager.getCurrentPlatform()) {
            UnifyPlatform.ANDROID -> AndroidNativeComponent(componentData, props, children)
            UnifyPlatform.IOS -> IOSNativeComponent(componentData, props, children)
            UnifyPlatform.WEB -> WebNativeComponent(componentData, props, children)
            UnifyPlatform.DESKTOP -> DesktopNativeComponent(componentData, props, children)
            else -> {
                // 降级到Compose实现
                ComposeComponentFactory(componentData).CreateComponent(props, children)
            }
        }
    }
    
    override fun getMetadata(): ComponentMetadata = componentData.metadata
}

/**
 * 混合组件工厂
 * 结合Compose和原生组件
 */
class HybridComponentFactory(
    private val componentData: ComponentData
) : ComponentFactory {
    
    @Composable
    override fun CreateComponent(
        props: Map<String, Any>,
        children: @Composable () -> Unit
    ) {
        val useNative = props["useNative"] as? Boolean ?: false
        
        if (useNative && isNativeSupported()) {
            NativeComponentFactory(componentData).CreateComponent(props, children)
        } else {
            ComposeComponentFactory(componentData).CreateComponent(props, children)
        }
    }
    
    override fun getMetadata(): ComponentMetadata = componentData.metadata
    
    private fun isNativeSupported(): Boolean {
        return when (PlatformManager.getCurrentPlatform()) {
            UnifyPlatform.ANDROID, UnifyPlatform.IOS -> true
            else -> false
        }
    }
}

/**
 * 动态文本组件工厂
 */
class DynamicTextComponentFactory : ComponentFactory {
    @Composable
    override fun CreateComponent(
        props: Map<String, Any>,
        children: @Composable () -> Unit
    ) {
        val text = props["text"] as? String ?: ""
        val fontSize = props["fontSize"] as? Float ?: 16f
        val color = props["color"] as? String ?: "#000000"
        val fontWeight = props["fontWeight"] as? String ?: "normal"
        
        Text(
            text = text,
            fontSize = androidx.compose.ui.unit.TextUnit(fontSize, androidx.compose.ui.unit.TextUnitType.Sp),
            color = Color(parseColor(color)),
            fontWeight = parseFontWeight(fontWeight)
        )
    }
    
    override fun getMetadata(): ComponentMetadata = ComponentMetadata(
        name = "DynamicText",
        version = "1.0.0",
        description = "动态文本组件"
    )
}

/**
 * 动态按钮组件工厂
 */
class DynamicButtonComponentFactory : ComponentFactory {
    @Composable
    override fun CreateComponent(
        props: Map<String, Any>,
        children: @Composable () -> Unit
    ) {
        val text = props["text"] as? String ?: "按钮"
        val onClick = props["onClick"] as? (() -> Unit)
        val enabled = props["enabled"] as? Boolean ?: true
        val variant = props["variant"] as? String ?: "filled"
        
        when (variant) {
            "outlined" -> OutlinedButton(
                onClick = { onClick?.invoke() },
                enabled = enabled
            ) {
                Text(text)
            }
            "text" -> TextButton(
                onClick = { onClick?.invoke() },
                enabled = enabled
            ) {
                Text(text)
            }
            else -> Button(
                onClick = { onClick?.invoke() },
                enabled = enabled
            ) {
                Text(text)
            }
        }
    }
    
    override fun getMetadata(): ComponentMetadata = ComponentMetadata(
        name = "DynamicButton",
        version = "1.0.0",
        description = "动态按钮组件"
    )
}

/**
 * 动态图片组件工厂
 */
class DynamicImageComponentFactory : ComponentFactory {
    @Composable
    override fun CreateComponent(
        props: Map<String, Any>,
        children: @Composable () -> Unit
    ) {
        val src = props["src"] as? String ?: ""
        val width = props["width"] as? Float
        val height = props["height"] as? Float
        val contentDescription = props["contentDescription"] as? String
        
        // 使用平台适配的图片加载组件
        PlatformImage(
            src = src,
            contentDescription = contentDescription,
            modifier = Modifier.let { modifier ->
                var result = modifier
                if (width != null) result = result.width(width.dp)
                if (height != null) result = result.height(height.dp)
                result
            }
        )
    }
    
    override fun getMetadata(): ComponentMetadata = ComponentMetadata(
        name = "DynamicImage",
        version = "1.0.0",
        description = "动态图片组件"
    )
}

/**
 * 动态列表组件工厂
 */
class DynamicListComponentFactory : ComponentFactory {
    @Composable
    override fun CreateComponent(
        props: Map<String, Any>,
        children: @Composable () -> Unit
    ) {
        val items = props["items"] as? List<Map<String, Any>> ?: emptyList()
        val itemTemplate = props["itemTemplate"] as? String ?: "default"
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                DynamicListItem(item, itemTemplate)
            }
        }
    }
    
    override fun getMetadata(): ComponentMetadata = ComponentMetadata(
        name = "DynamicList",
        version = "1.0.0",
        description = "动态列表组件"
    )
}

/**
 * 动态表单组件工厂
 */
class DynamicFormComponentFactory : ComponentFactory {
    @Composable
    override fun CreateComponent(
        props: Map<String, Any>,
        children: @Composable () -> Unit
    ) {
        val fields = props["fields"] as? List<Map<String, Any>> ?: emptyList()
        val onSubmit = props["onSubmit"] as? ((Map<String, Any>) -> Unit)
        
        var formData by remember { mutableStateOf(mutableMapOf<String, Any>()) }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            fields.forEach { field ->
                DynamicFormField(
                    field = field,
                    value = formData[field["name"] as? String ?: ""],
                    onValueChange = { value ->
                        val fieldName = field["name"] as? String ?: ""
                        formData[fieldName] = value
                    }
                )
            }
            
            Button(
                onClick = { onSubmit?.invoke(formData) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("提交")
            }
        }
    }
    
    override fun getMetadata(): ComponentMetadata = ComponentMetadata(
        name = "DynamicForm",
        version = "1.0.0",
        description = "动态表单组件"
    )
}

/**
 * 动态文本组件
 */
@Composable
private fun DynamicTextComponent(props: Map<String, Any>) {
    DynamicTextComponentFactory().CreateComponent(props)
}

/**
 * 动态按钮组件
 */
@Composable
private fun DynamicButtonComponent(props: Map<String, Any>, children: @Composable () -> Unit) {
    DynamicButtonComponentFactory().CreateComponent(props, children)
}

/**
 * 动态卡片组件
 */
@Composable
private fun DynamicCardComponent(props: Map<String, Any>, children: @Composable () -> Unit) {
    val elevation = props["elevation"] as? Float ?: 4f
    val padding = props["padding"] as? Float ?: 16f
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp)
    ) {
        Box(
            modifier = Modifier.padding(padding.dp)
        ) {
            children()
        }
    }
}

/**
 * 动态列表组件
 */
@Composable
private fun DynamicListComponent(props: Map<String, Any>) {
    DynamicListComponentFactory().CreateComponent(props)
}

/**
 * 动态表单组件
 */
@Composable
private fun DynamicFormComponent(props: Map<String, Any>) {
    DynamicFormComponentFactory().CreateComponent(props)
}

/**
 * 动态图表组件
 */
@Composable
private fun DynamicChartComponent(props: Map<String, Any>) {
    val chartType = props["type"] as? String ?: "line"
    val data = props["data"] as? List<Map<String, Any>> ?: emptyList()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("${chartType.uppercase()} 图表 (${data.size} 数据点)")
        }
    }
}

/**
 * 动态列表项
 */
@Composable
private fun DynamicListItem(item: Map<String, Any>, template: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            val title = item["title"] as? String ?: ""
            val subtitle = item["subtitle"] as? String ?: ""
            val description = item["description"] as? String ?: ""
            
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * 动态表单字段
 */
@Composable
private fun DynamicFormField(
    field: Map<String, Any>,
    value: Any?,
    onValueChange: (Any) -> Unit
) {
    val fieldType = field["type"] as? String ?: "text"
    val label = field["label"] as? String ?: ""
    val placeholder = field["placeholder"] as? String ?: ""
    val required = field["required"] as? Boolean ?: false
    
    when (fieldType) {
        "text", "email", "password" -> {
            OutlinedTextField(
                value = value as? String ?: "",
                onValueChange = onValueChange,
                label = { Text(label + if (required) " *" else "") },
                placeholder = { Text(placeholder) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        "number" -> {
            OutlinedTextField(
                value = value?.toString() ?: "",
                onValueChange = { newValue ->
                    newValue.toDoubleOrNull()?.let { onValueChange(it) }
                },
                label = { Text(label + if (required) " *" else "") },
                placeholder = { Text(placeholder) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        "switch" -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label + if (required) " *" else "")
                Switch(
                    checked = value as? Boolean ?: false,
                    onCheckedChange = onValueChange
                )
            }
        }
    }
}

/**
 * 执行动态代码
 */
@Composable
private fun ExecuteDynamicCode(
    code: String,
    props: Map<String, Any>,
    children: @Composable () -> Unit
) {
    // 简化的动态代码执行
    // 实际实现需要安全的代码解析和执行引擎
    try {
        when {
            code.contains("Text(") -> {
                val text = extractTextFromCode(code, props)
                Text(text)
            }
            code.contains("Button(") -> {
                val text = extractTextFromCode(code, props)
                Button(onClick = {}) {
                    Text(text)
                }
            }
            else -> {
                ErrorComponent(
                    error = "不支持的动态代码",
                    componentId = "dynamic"
                )
            }
        }
    } catch (e: Exception) {
        ErrorComponent(
            error = "动态代码执行失败: ${e.message}",
            componentId = "dynamic"
        )
    }
}

/**
 * 错误组件
 */
@Composable
private fun ErrorComponent(error: String, componentId: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "组件错误",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = "组件ID: $componentId",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = "错误信息: $error",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

/**
 * 平台图片组件
 */
@Composable
expect fun PlatformImage(
    src: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
)

/**
 * 平台原生组件
 */
@Composable
expect fun AndroidNativeComponent(
    componentData: ComponentData,
    props: Map<String, Any>,
    children: @Composable () -> Unit
)

@Composable
expect fun IOSNativeComponent(
    componentData: ComponentData,
    props: Map<String, Any>,
    children: @Composable () -> Unit
)

@Composable
expect fun WebNativeComponent(
    componentData: ComponentData,
    props: Map<String, Any>,
    children: @Composable () -> Unit
)

@Composable
expect fun DesktopNativeComponent(
    componentData: ComponentData,
    props: Map<String, Any>,
    children: @Composable () -> Unit
)

/**
 * 工具函数
 */
private fun parseColor(colorString: String): Long {
    return try {
        if (colorString.startsWith("#")) {
            colorString.removePrefix("#").toLong(16) or 0xFF000000
        } else {
            0xFF000000
        }
    } catch (e: Exception) {
        0xFF000000
    }
}

private fun parseFontWeight(weightString: String): FontWeight {
    return when (weightString.lowercase()) {
        "bold" -> FontWeight.Bold
        "light" -> FontWeight.Light
        "medium" -> FontWeight.Medium
        "semibold" -> FontWeight.SemiBold
        else -> FontWeight.Normal
    }
}

private fun extractTextFromCode(code: String, props: Map<String, Any>): String {
    // 简化的文本提取逻辑
    val textMatch = Regex("""Text\("([^"]+)"\)""").find(code)
    return textMatch?.groupValues?.get(1) ?: props["text"] as? String ?: "动态文本"
}
