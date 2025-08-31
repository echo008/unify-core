package com.unify.ui.accessibility

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.ui.LocalUnifyTheme

/**
 * Unify 无障碍支持系统
 * 提供全面的无障碍功能，支持屏幕阅读器、键盘导航、高对比度等
 */

/**
 * 无障碍级别枚举
 */
enum class UnifyAccessibilityLevel {
    AA,     // WCAG 2.1 AA 级别
    AAA     // WCAG 2.1 AAA 级别
}

/**
 * 无障碍配置数据类
 */
data class UnifyAccessibilityConfig(
    val level: UnifyAccessibilityLevel = UnifyAccessibilityLevel.AA,
    val highContrast: Boolean = false,
    val largeText: Boolean = false,
    val reduceMotion: Boolean = false,
    val screenReaderEnabled: Boolean = false,
    val keyboardNavigationEnabled: Boolean = true,
    val focusIndicatorEnabled: Boolean = true,
    val semanticLabelsEnabled: Boolean = true
)

/**
 * 无障碍上下文提供者
 */
val LocalUnifyAccessibility = compositionLocalOf { UnifyAccessibilityConfig() }

/**
 * 无障碍配置提供者组件
 */
@Composable
fun UnifyAccessibilityProvider(
    config: UnifyAccessibilityConfig = UnifyAccessibilityConfig(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalUnifyAccessibility provides config
    ) {
        content()
    }
}

/**
 * 无障碍语义化修饰符
 */
fun Modifier.unifySemantics(
    contentDescription: String? = null,
    role: Role? = null,
    stateDescription: String? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    selected: Boolean? = null,
    toggleableState: ToggleableState? = null,
    enabled: Boolean = true,
    heading: Boolean = false,
    error: String? = null,
    testTag: String? = null
): Modifier = this.semantics {
    contentDescription?.let { this.contentDescription = it }
    role?.let { this.role = it }
    stateDescription?.let { this.stateDescription = it }
    onClick?.let { this.onClick(label = contentDescription) { it(); true } }
    onLongClick?.let { this.onLongClick(label = contentDescription) { it(); true } }
    selected?.let { this.selected = it }
    toggleableState?.let { this.toggleableState = it }
    this.disabled = !enabled
    if (heading) this.heading()
    error?.let { this.error(it) }
    testTag?.let { this.testTag = it }
}

/**
 * 焦点指示器修饰符
 */
@Composable
fun Modifier.unifyFocusIndicator(
    enabled: Boolean = true,
    color: Color = LocalUnifyTheme.current.colors.primary,
    width: Dp = 2.dp
): Modifier {
    val accessibility = LocalUnifyAccessibility.current
    
    return if (accessibility.focusIndicatorEnabled && enabled) {
        var isFocused by remember { mutableStateOf(false) }
        
        this
            .onFocusChanged { isFocused = it.isFocused }
            .then(
                if (isFocused) {
                    Modifier.border(width, color)
                } else {
                    Modifier
                }
            )
    } else {
        this
    }
}

/**
 * 键盘导航修饰符
 */
fun Modifier.unifyKeyboardNavigation(
    enabled: Boolean = true,
    focusRequester: FocusRequester? = null
): Modifier {
    return if (enabled) {
        var modifier = this.focusable()
        focusRequester?.let { modifier = modifier.focusRequester(it) }
        modifier
    } else {
        this
    }
}

/**
 * 高对比度文本组件
 */
@Composable
fun UnifyAccessibleText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    contentDescription: String? = null,
    heading: Boolean = false
) {
    val accessibility = LocalUnifyAccessibility.current
    val theme = LocalUnifyTheme.current
    
    val adjustedColor = if (accessibility.highContrast) {
        if (color == Color.Unspecified) {
            theme.colors.onSurface
        } else {
            enhanceContrast(color, theme.colors.surface)
        }
    } else {
        color
    }
    
    val adjustedFontSize = if (accessibility.largeText && fontSize != androidx.compose.ui.unit.TextUnit.Unspecified) {
        fontSize * 1.3f
    } else {
        fontSize
    }
    
    Text(
        text = text,
        modifier = modifier.unifySemantics(
            contentDescription = contentDescription,
            heading = heading
        ),
        color = adjustedColor,
        fontSize = adjustedFontSize,
        fontWeight = fontWeight
    )
}

/**
 * 无障碍按钮组件
 */
@Composable
fun UnifyAccessibleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    stateDescription: String? = null,
    content: @Composable RowScope.() -> Unit
) {
    val accessibility = LocalUnifyAccessibility.current
    val focusRequester = remember { FocusRequester() }
    
    Button(
        onClick = onClick,
        modifier = modifier
            .unifySemantics(
                contentDescription = contentDescription,
                stateDescription = stateDescription,
                role = Role.Button,
                onClick = onClick,
                enabled = enabled
            )
            .unifyFocusIndicator(enabled = accessibility.focusIndicatorEnabled)
            .unifyKeyboardNavigation(
                enabled = accessibility.keyboardNavigationEnabled,
                focusRequester = focusRequester
            ),
        enabled = enabled,
        content = content
    )
}

/**
 * 无障碍输入框组件
 */
@Composable
fun UnifyAccessibleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: String? = null,
    placeholder: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    contentDescription: String? = null
) {
    val accessibility = LocalUnifyAccessibility.current
    val focusRequester = remember { FocusRequester() }
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .unifySemantics(
                    contentDescription = contentDescription ?: label,
                    role = Role.TextInput,
                    enabled = enabled,
                    error = errorText
                )
                .unifyFocusIndicator(enabled = accessibility.focusIndicatorEnabled)
                .unifyKeyboardNavigation(
                    enabled = accessibility.keyboardNavigationEnabled,
                    focusRequester = focusRequester
                ),
            enabled = enabled,
            readOnly = readOnly,
            label = label?.let { { Text(it) } },
            placeholder = placeholder?.let { { Text(it) } },
            isError = errorText != null
        )
        
        // 辅助文本
        helperText?.let { text ->
            UnifyAccessibleText(
                text = text,
                fontSize = 12.sp,
                color = LocalUnifyTheme.current.colors.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        
        // 错误文本
        errorText?.let { text ->
            UnifyAccessibleText(
                text = text,
                fontSize = 12.sp,
                color = LocalUnifyTheme.current.colors.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                contentDescription = "错误：$text"
            )
        }
    }
}

/**
 * 无障碍复选框组件
 */
@Composable
fun UnifyAccessibleCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
    contentDescription: String? = null
) {
    val accessibility = LocalUnifyAccessibility.current
    val focusRequester = remember { FocusRequester() }
    
    val stateDescription = when {
        checked -> "已选中"
        else -> "未选中"
    }
    
    Row(
        modifier = modifier
            .selectable(
                selected = checked,
                onClick = { onCheckedChange?.invoke(!checked) },
                enabled = enabled,
                role = Role.Checkbox
            )
            .unifySemantics(
                contentDescription = contentDescription ?: label,
                role = Role.Checkbox,
                stateDescription = stateDescription,
                toggleableState = ToggleableState(checked),
                enabled = enabled
            )
            .unifyFocusIndicator(enabled = accessibility.focusIndicatorEnabled)
            .unifyKeyboardNavigation(
                enabled = accessibility.keyboardNavigationEnabled,
                focusRequester = focusRequester
            )
            .padding(vertical = 8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null, // 由父级处理
            enabled = enabled
        )
        
        label?.let { text ->
            Spacer(modifier = Modifier.width(8.dp))
            UnifyAccessibleText(
                text = text,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 无障碍单选按钮组件
 */
@Composable
fun UnifyAccessibleRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
    contentDescription: String? = null
) {
    val accessibility = LocalUnifyAccessibility.current
    val focusRequester = remember { FocusRequester() }
    
    val stateDescription = when {
        selected -> "已选择"
        else -> "未选择"
    }
    
    Row(
        modifier = modifier
            .selectable(
                selected = selected,
                onClick = { onClick?.invoke() },
                enabled = enabled,
                role = Role.RadioButton
            )
            .unifySemantics(
                contentDescription = contentDescription ?: label,
                role = Role.RadioButton,
                stateDescription = stateDescription,
                selected = selected,
                enabled = enabled
            )
            .unifyFocusIndicator(enabled = accessibility.focusIndicatorEnabled)
            .unifyKeyboardNavigation(
                enabled = accessibility.keyboardNavigationEnabled,
                focusRequester = focusRequester
            )
            .padding(vertical = 8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null, // 由父级处理
            enabled = enabled
        )
        
        label?.let { text ->
            Spacer(modifier = Modifier.width(8.dp))
            UnifyAccessibleText(
                text = text,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 无障碍单选按钮组
 */
@Composable
fun UnifyAccessibleRadioGroup(
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    groupLabel: String? = null
) {
    Column(
        modifier = modifier
            .selectableGroup()
            .unifySemantics(
                contentDescription = groupLabel,
                role = Role.RadioButton
            )
    ) {
        groupLabel?.let { label ->
            UnifyAccessibleText(
                text = label,
                fontWeight = FontWeight.Medium,
                heading = true,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        options.forEach { option ->
            UnifyAccessibleRadioButton(
                selected = option == selectedOption,
                onClick = { onOptionSelected(option) },
                enabled = enabled,
                label = option,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * 无障碍开关组件
 */
@Composable
fun UnifyAccessibleSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
    contentDescription: String? = null
) {
    val accessibility = LocalUnifyAccessibility.current
    val focusRequester = remember { FocusRequester() }
    
    val stateDescription = when {
        checked -> "开启"
        else -> "关闭"
    }
    
    Row(
        modifier = modifier
            .selectable(
                selected = checked,
                onClick = { onCheckedChange?.invoke(!checked) },
                enabled = enabled,
                role = Role.Switch
            )
            .unifySemantics(
                contentDescription = contentDescription ?: label,
                role = Role.Switch,
                stateDescription = stateDescription,
                toggleableState = ToggleableState(checked),
                enabled = enabled
            )
            .unifyFocusIndicator(enabled = accessibility.focusIndicatorEnabled)
            .unifyKeyboardNavigation(
                enabled = accessibility.keyboardNavigationEnabled,
                focusRequester = focusRequester
            )
            .padding(vertical = 8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        label?.let { text ->
            UnifyAccessibleText(
                text = text,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Switch(
            checked = checked,
            onCheckedChange = null, // 由父级处理
            enabled = enabled
        )
    }
}

/**
 * 无障碍滑块组件
 */
@Composable
fun UnifyAccessibleSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    label: String? = null,
    contentDescription: String? = null,
    valueDescription: ((Float) -> String)? = null
) {
    val accessibility = LocalUnifyAccessibility.current
    val focusRequester = remember { FocusRequester() }
    
    val stateDescription = valueDescription?.invoke(value) ?: "当前值：${(value * 100).toInt()}%"
    
    Column(modifier = modifier) {
        label?.let { text ->
            UnifyAccessibleText(
                text = text,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .unifySemantics(
                    contentDescription = contentDescription ?: label,
                    role = Role.Slider,
                    stateDescription = stateDescription,
                    enabled = enabled
                )
                .unifyFocusIndicator(enabled = accessibility.focusIndicatorEnabled)
                .unifyKeyboardNavigation(
                    enabled = accessibility.keyboardNavigationEnabled,
                    focusRequester = focusRequester
                ),
            enabled = enabled,
            valueRange = valueRange,
            steps = steps
        )
    }
}

/**
 * 无障碍列表项组件
 */
@Composable
fun UnifyAccessibleListItem(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean = false,
    contentDescription: String? = null,
    stateDescription: String? = null,
    content: @Composable RowScope.() -> Unit
) {
    val accessibility = LocalUnifyAccessibility.current
    val focusRequester = remember { FocusRequester() }
    
    val itemStateDescription = when {
        selected -> "已选中"
        else -> stateDescription
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(enabled = enabled) { onClick() }
                } else {
                    Modifier
                }
            )
            .unifySemantics(
                contentDescription = contentDescription,
                role = Role.Button,
                stateDescription = itemStateDescription,
                onClick = onClick,
                selected = selected,
                enabled = enabled
            )
            .unifyFocusIndicator(enabled = accessibility.focusIndicatorEnabled)
            .unifyKeyboardNavigation(
                enabled = accessibility.keyboardNavigationEnabled,
                focusRequester = focusRequester
            )
            .padding(16.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        content = content
    )
}

/**
 * 对比度增强辅助函数
 */
private fun enhanceContrast(foreground: Color, background: Color): Color {
    // 简化的对比度增强算法
    val contrastRatio = calculateContrastRatio(foreground, background)
    
    return if (contrastRatio < 4.5f) {
        // 如果对比度不足，调整颜色
        if (isLightColor(background)) {
            Color.Black
        } else {
            Color.White
        }
    } else {
        foreground
    }
}

/**
 * 计算对比度比值
 */
private fun calculateContrastRatio(color1: Color, color2: Color): Float {
    val luminance1 = calculateLuminance(color1)
    val luminance2 = calculateLuminance(color2)
    
    val lighter = maxOf(luminance1, luminance2)
    val darker = minOf(luminance1, luminance2)
    
    return (lighter + 0.05f) / (darker + 0.05f)
}

/**
 * 计算颜色亮度
 */
private fun calculateLuminance(color: Color): Float {
    fun adjustColorComponent(component: Float): Float {
        return if (component <= 0.03928f) {
            component / 12.92f
        } else {
            kotlin.math.pow((component + 0.055f) / 1.055f, 2.4f).toFloat()
        }
    }
    
    val r = adjustColorComponent(color.red)
    val g = adjustColorComponent(color.green)
    val b = adjustColorComponent(color.blue)
    
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}

/**
 * 判断是否为浅色
 */
private fun isLightColor(color: Color): Boolean {
    return calculateLuminance(color) > 0.5f
}

/**
 * 无障碍公告组件（用于动态内容变化通知）
 */
@Composable
fun UnifyAccessibilityAnnouncement(
    message: String,
    priority: AnnouncementPriority = AnnouncementPriority.Polite
) {
    val accessibility = LocalUnifyAccessibility.current
    
    if (accessibility.screenReaderEnabled) {
        LaunchedEffect(message) {
            // 这里会在平台特定实现中调用屏幕阅读器API
            // 例如在Android中调用AccessibilityManager.announce()
        }
    }
}

/**
 * 公告优先级枚举
 */
enum class AnnouncementPriority {
    Polite,     // 礼貌模式，等待当前语音结束
    Assertive   // 断言模式，立即打断当前语音
}
