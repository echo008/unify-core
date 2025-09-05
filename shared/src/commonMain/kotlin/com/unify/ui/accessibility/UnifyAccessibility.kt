package com.unify.ui.accessibility

import androidx.compose.foundation.layout.*
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import kotlin.math.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Unify无障碍访问支持
 * 提供跨平台的无障碍功能和WCAG 2.1标准实现
 */

/**
 * 无障碍配置
 */
@Stable
data class AccessibilityConfig(
    val enableScreenReader: Boolean = true,
    val enableHighContrast: Boolean = false,
    val enableLargeText: Boolean = false,
    val enableReducedMotion: Boolean = false,
    val enableFocusIndicator: Boolean = true,
    val minimumTouchTargetSize: androidx.compose.ui.unit.Dp = 48.dp,
    val colorContrastRatio: Float = 4.5f
)

/**
 * 无障碍状态
 */
@Stable
class AccessibilityState {
    var isScreenReaderEnabled by mutableStateOf(false)
    var isHighContrastEnabled by mutableStateOf(false)
    var isLargeTextEnabled by mutableStateOf(false)
    var isReducedMotionEnabled by mutableStateOf(false)
    var currentFontScale by mutableStateOf(1.0f)
    var currentColorContrast by mutableStateOf(1.0f)
}

/**
 * 无障碍管理器
 */
class UnifyAccessibilityManager {
    private val _state = mutableStateOf(AccessibilityState())
    val state: State<AccessibilityState> = _state
    
    private val _config = mutableStateOf(AccessibilityConfig())
    val config: State<AccessibilityConfig> = _config
    
    /**
     * 更新无障碍配置
     */
    fun updateConfig(config: AccessibilityConfig) {
        _config.value = config
        applyConfig(config)
    }
    
    /**
     * 应用配置
     */
    private fun applyConfig(config: AccessibilityConfig) {
        _state.value = _state.value.apply {
            isScreenReaderEnabled = config.enableScreenReader
            isHighContrastEnabled = config.enableHighContrast
            isLargeTextEnabled = config.enableLargeText
            isReducedMotionEnabled = config.enableReducedMotion
            currentFontScale = if (config.enableLargeText) 1.3f else 1.0f
            currentColorContrast = if (config.enableHighContrast) 2.0f else 1.0f
        }
    }
    
    /**
     * 检查颜色对比度
     */
    fun checkColorContrast(foreground: Color, background: Color): Float {
        val foregroundLuminance = calculateLuminance(foreground)
        val backgroundLuminance = calculateLuminance(background)
        
        val lighter = maxOf(foregroundLuminance, backgroundLuminance)
        val darker = minOf(foregroundLuminance, backgroundLuminance)
        
        return (lighter + 0.05f) / (darker + 0.05f)
    }
    
    /**
     * 计算颜色亮度
     */
    private fun calculateLuminance(color: Color): Float {
        // 使用简化的亮度计算，避免pow函数依赖
        val r = if (color.red <= 0.03928f) color.red / 12.92f else ((color.red + 0.055f) / 1.055f).let { it * it * it }
        val g = if (color.green <= 0.03928f) color.green / 12.92f else ((color.green + 0.055f) / 1.055f).let { it * it * it }
        val b = if (color.blue <= 0.03928f) color.blue / 12.92f else ((color.blue + 0.055f) / 1.055f).let { it * it * it }
        
        return 0.2126f * r + 0.7152f * g + 0.0722f * b
    }
    
    /**
     * 获取建议的颜色
     */
    fun getSuggestedColor(originalColor: Color, backgroundColor: Color, targetContrast: Float = 4.5f): Color {
        val currentContrast = checkColorContrast(originalColor, backgroundColor)
        if (currentContrast >= targetContrast) {
            return originalColor
        }
        
        // 简化的颜色调整逻辑
        val backgroundLuminance = calculateLuminance(backgroundColor)
        return if (backgroundLuminance > 0.5f) {
            // 浅色背景，使用深色文字
            originalColor.copy(
                red = originalColor.red * 0.7f,
                green = originalColor.green * 0.7f,
                blue = originalColor.blue * 0.7f
            )
        } else {
            // 深色背景，使用浅色文字
            originalColor.copy(
                red = minOf(1.0f, originalColor.red * 1.3f),
                green = minOf(1.0f, originalColor.green * 1.3f),
                blue = minOf(1.0f, originalColor.blue * 1.3f)
            )
        }
    }
}

/**
 * 无障碍提供器
 */
val LocalAccessibilityManager = staticCompositionLocalOf<UnifyAccessibilityManager> {
    error("AccessibilityManager not provided")
}

/**
 * 无障碍提供器组件
 */
@Composable
fun AccessibilityProvider(
    manager: UnifyAccessibilityManager = remember { UnifyAccessibilityManager() },
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAccessibilityManager provides manager
    ) {
        content()
    }
}

/**
 * 获取当前无障碍管理器
 */
@Composable
fun currentAccessibilityManager(): UnifyAccessibilityManager = LocalAccessibilityManager.current

/**
 * 无障碍文本组件
 */
@Composable
fun AccessibleText(
    text: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    role: Role? = null,
    fontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE
) {
    val accessibilityManager = currentAccessibilityManager()
    val state = accessibilityManager.state.value
    val config = accessibilityManager.config.value
    
    val adjustedFontSize = fontSize * state.currentFontScale
    val adjustedColor = if (color != Color.Unspecified && config.enableHighContrast) {
        accessibilityManager.getSuggestedColor(color, MaterialTheme.colorScheme.background)
    } else color
    
    Text(
        text = text,
        modifier = modifier.semantics {
            contentDescription?.let { this.contentDescription = it }
            role?.let { this.role = it }
            if (config.enableScreenReader) {
                this.text = AnnotatedString(text)
            }
        },
        fontSize = adjustedFontSize,
        fontWeight = fontWeight,
        color = adjustedColor,
        maxLines = maxLines
    )
}

/**
 * 无障碍按钮组件
 */
@Composable
fun AccessibleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    content: @Composable RowScope.() -> Unit
) {
    val accessibilityManager = currentAccessibilityManager()
    val config = accessibilityManager.config.value
    
    Button(
        onClick = onClick,
        modifier = modifier
            .sizeIn(minWidth = config.minimumTouchTargetSize, minHeight = config.minimumTouchTargetSize)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
                this.role = Role.Button
                if (enabled) {
                    this.onClick(label = contentDescription) { onClick(); true }
                }
            },
        enabled = enabled,
        content = content
    )
}

/**
 * 无障碍图标按钮组件
 */
@Composable
fun AccessibleIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String,
    content: @Composable () -> Unit
) {
    val accessibilityManager = currentAccessibilityManager()
    val config = accessibilityManager.config.value
    
    IconButton(
        onClick = onClick,
        modifier = modifier
            .sizeIn(minWidth = config.minimumTouchTargetSize, minHeight = config.minimumTouchTargetSize)
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
                if (enabled) {
                    this.onClick(label = contentDescription) { onClick(); true }
                }
            },
        enabled = enabled,
        content = content
    )
}

/**
 * 无障碍输入框组件
 */
@Composable
fun AccessibleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    contentDescription: String? = null,
    errorMessage: String? = null
) {
    val accessibilityManager = currentAccessibilityManager()
    val config = accessibilityManager.config.value
    
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .sizeIn(minHeight = config.minimumTouchTargetSize)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
                this.role = Role.Button
                if (isError && errorMessage != null) {
                    this.error(errorMessage)
                }
                if (config.enableScreenReader) {
                    this.text = AnnotatedString(value)
                }
            },
        enabled = enabled,
        readOnly = readOnly,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        supportingText = supportingText,
        isError = isError,
        singleLine = singleLine,
        maxLines = maxLines
    )
}

/**
 * 无障碍复选框组件
 */
@Composable
fun AccessibleCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null
) {
    val accessibilityManager = currentAccessibilityManager()
    val config = accessibilityManager.config.value
    
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier
            .sizeIn(minWidth = config.minimumTouchTargetSize, minHeight = config.minimumTouchTargetSize)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
                this.role = Role.Checkbox
                this.toggleableState = ToggleableState(checked)
                if (enabled && onCheckedChange != null) {
                    this.onClick(label = contentDescription) { 
                        onCheckedChange(!checked)
                        true 
                    }
                }
            },
        enabled = enabled
    )
}

/**
 * 无障碍单选按钮组件
 */
@Composable
fun AccessibleRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null
) {
    val accessibilityManager = currentAccessibilityManager()
    val config = accessibilityManager.config.value
    
    RadioButton(
        selected = selected,
        onClick = onClick,
        modifier = modifier
            .sizeIn(minWidth = config.minimumTouchTargetSize, minHeight = config.minimumTouchTargetSize)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
                this.role = Role.RadioButton
                this.toggleableState = ToggleableState(selected)
                if (enabled && onClick != null) {
                    this.onClick(label = contentDescription) { 
                        onClick()
                        true 
                    }
                }
            },
        enabled = enabled
    )
}

/**
 * 无障碍开关组件
 */
@Composable
fun AccessibleSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null
) {
    val accessibilityManager = currentAccessibilityManager()
    val config = accessibilityManager.config.value
    
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier
            .sizeIn(minWidth = config.minimumTouchTargetSize, minHeight = config.minimumTouchTargetSize)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
                this.role = Role.Switch
                this.toggleableState = ToggleableState(checked)
                if (enabled && onCheckedChange != null) {
                    this.onClick(label = contentDescription) { 
                        onCheckedChange(!checked)
                        true 
                    }
                }
            },
        enabled = enabled
    )
}

/**
 * 无障碍滑块组件
 */
@Composable
fun AccessibleSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    contentDescription: String? = null,
    valueDescription: String? = null
) {
    val accessibilityManager = currentAccessibilityManager()
    val config = accessibilityManager.config.value
    
    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .sizeIn(minHeight = config.minimumTouchTargetSize)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
                this.role = Role.Button
                this.setProgress(label = valueDescription) { targetValue ->
                    val newValue = valueRange.start + (valueRange.endInclusive - valueRange.start) * targetValue
                    onValueChange(newValue.coerceIn(valueRange))
                    true
                }
            },
        enabled = enabled,
        valueRange = valueRange,
        steps = steps
    )
}

/**
 * 无障碍图片组件
 */
@Composable
fun AccessibleImage(
    painter: androidx.compose.ui.graphics.painter.Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: androidx.compose.ui.layout.ContentScale = androidx.compose.ui.layout.ContentScale.Fit,
    alpha: Float = 1.0f,
    colorFilter: androidx.compose.ui.graphics.ColorFilter? = null
) {
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it
                this.role = Role.Image
            }
        },
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter
    )
}

/**
 * 无障碍列表项组件
 */
@Composable
fun AccessibleListItem(
    modifier: Modifier = Modifier,
    headlineContent: @Composable () -> Unit,
    overlineContent: @Composable (() -> Unit)? = null,
    supportingContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(),
    tonalElevation: androidx.compose.ui.unit.Dp = ListItemDefaults.Elevation,
    shadowElevation: androidx.compose.ui.unit.Dp = ListItemDefaults.Elevation,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null
) {
    val accessibilityManager = currentAccessibilityManager()
    val config = accessibilityManager.config.value
    
    ListItem(
        headlineContent = headlineContent,
        modifier = modifier
            .sizeIn(minHeight = config.minimumTouchTargetSize)
            .semantics {
                contentDescription?.let { this.contentDescription = it }
                this.role = Role.Button
                if (onClick != null) {
                    this.onClick(label = contentDescription) { 
                        onClick()
                        true 
                    }
                }
            }
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else Modifier
            ),
        overlineContent = overlineContent,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        colors = colors,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation
    )
}

/**
 * 无障碍焦点指示器
 */
@Composable
fun AccessibilityFocusIndicator(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val accessibilityManager = currentAccessibilityManager()
    val config = accessibilityManager.config.value
    
    if (config.enableFocusIndicator) {
        Box(
            modifier = modifier
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(2.dp)
        ) {
            content()
        }
    } else {
        content()
    }
}

/**
 * 无障碍公告组件
 */
@Composable
fun AccessibilityAnnouncement(
    message: String,
    priority: AccessibilityAnnouncementPriority = AccessibilityAnnouncementPriority.NORMAL
) {
    val accessibilityManager = currentAccessibilityManager()
    val config = accessibilityManager.config.value
    
    if (config.enableScreenReader) {
        LaunchedEffect(message) {
            // 在实际实现中，这里会调用平台特定的屏幕阅读器API
            // 例如Android的AccessibilityManager.announce()
        }
    }
}

/**
 * 无障碍公告优先级
 */
enum class AccessibilityAnnouncementPriority {
    LOW,
    NORMAL,
    HIGH,
    URGENT
}

/**
 * 无障碍实用工具
 */
object AccessibilityUtils {
    /**
     * 检查是否满足WCAG AA标准
     */
    fun meetsWCAGAA(contrastRatio: Float): Boolean = contrastRatio >= 4.5f
    
    /**
     * 检查是否满足WCAG AAA标准
     */
    fun meetsWCAGAAA(contrastRatio: Float): Boolean = contrastRatio >= 7.0f
    
    /**
     * 获取建议的最小触摸目标大小
     */
    fun getMinimumTouchTargetSize(): androidx.compose.ui.unit.Dp = 48.dp
    
    /**
     * 检查文本大小是否足够大
     */
    fun isTextSizeAccessible(fontSize: androidx.compose.ui.unit.TextUnit): Boolean {
        return fontSize.value >= 14f // 最小14sp
    }
    
    /**
     * 生成无障碍内容描述
     */
    fun generateContentDescription(
        type: String,
        label: String? = null,
        state: String? = null,
        position: String? = null
    ): String {
        val parts = mutableListOf<String>()
        parts.add(type)
        label?.let { parts.add(it) }
        state?.let { parts.add(it) }
        position?.let { parts.add(it) }
        return parts.joinToString(", ")
    }
}
