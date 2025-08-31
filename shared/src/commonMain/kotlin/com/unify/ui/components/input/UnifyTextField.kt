package com.unify.ui.components.input

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.LocalUnifyPlatformTheme
import com.unify.ui.components.foundation.UnifyIcon
import com.unify.ui.components.foundation.UnifyIconSize
import com.unify.ui.components.foundation.UnifyText
import com.unify.ui.components.foundation.UnifyTextVariant

/**
 * Unify TextField 组件
 * 支持多平台适配的统一文本输入组件，参考 KuiklyUI 设计规范
 */

/**
 * 文本框变体枚举
 */
enum class UnifyTextFieldVariant {
    FILLED,         // 填充样式
    OUTLINED,       // 轮廓样式
    STANDARD        // 标准样式
}

/**
 * 文本框尺寸枚举
 */
enum class UnifyTextFieldSize {
    SMALL,          // 小尺寸
    MEDIUM,         // 中等尺寸
    LARGE           // 大尺寸
}

/**
 * 文本框状态枚举
 */
enum class UnifyTextFieldState {
    NORMAL,         // 正常
    SUCCESS,        // 成功
    WARNING,        // 警告
    ERROR           // 错误
}

/**
 * 主要 Unify TextField 组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    variant: UnifyTextFieldVariant = UnifyTextFieldVariant.OUTLINED,
    size: UnifyTextFieldSize = UnifyTextFieldSize.MEDIUM,
    state: UnifyTextFieldState = UnifyTextFieldState.NORMAL,
    label: String? = null,
    placeholder: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    prefix: String? = null,
    suffix: String? = null,
    maxLines: Int = 1,
    minLines: Int = 1,
    maxLength: Int? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape? = null,
    colors: TextFieldColors? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val platformTheme = LocalUnifyPlatformTheme.current
    
    // 获取文本框配置
    val textFieldConfig = getTextFieldConfig(variant, size, state, theme)
    val actualShape = shape ?: textFieldConfig.shape
    val actualColors = colors ?: textFieldConfig.colors
    
    // 处理字符限制
    val actualValue = if (maxLength != null && value.length > maxLength) {
        value.take(maxLength)
    } else {
        value
    }
    
    val actualOnValueChange: (String) -> Unit = { newValue ->
        if (maxLength == null || newValue.length <= maxLength) {
            onValueChange(newValue)
        }
    }
    
    Column(
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        }
    ) {
        when (variant) {
            UnifyTextFieldVariant.FILLED -> {
                TextField(
                    value = actualValue,
                    onValueChange = actualOnValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    readOnly = readOnly,
                    textStyle = textFieldConfig.textStyle,
                    label = label?.let { { Text(it) } },
                    placeholder = placeholder?.let { { Text(it) } },
                    leadingIcon = leadingIcon?.let { icon ->
                        {
                            UnifyIcon(
                                imageVector = icon,
                                contentDescription = null,
                                size = UnifyIconSize.SMALL
                            )
                        }
                    },
                    trailingIcon = trailingIcon?.let { icon ->
                        {
                            IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                                UnifyIcon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    size = UnifyIconSize.SMALL
                                )
                            }
                        }
                    },
                    prefix = prefix?.let { { Text(it) } },
                    suffix = suffix?.let { { Text(it) } },
                    supportingText = getSupportingText(helperText, errorText, state),
                    isError = state == UnifyTextFieldState.ERROR,
                    visualTransformation = visualTransformation,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    singleLine = maxLines == 1,
                    maxLines = if (maxLines == 1) 1 else maxLines,
                    minLines = minLines,
                    interactionSource = interactionSource,
                    shape = actualShape,
                    colors = actualColors
                )
            }
            
            UnifyTextFieldVariant.OUTLINED -> {
                OutlinedTextField(
                    value = actualValue,
                    onValueChange = actualOnValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    readOnly = readOnly,
                    textStyle = textFieldConfig.textStyle,
                    label = label?.let { { Text(it) } },
                    placeholder = placeholder?.let { { Text(it) } },
                    leadingIcon = leadingIcon?.let { icon ->
                        {
                            UnifyIcon(
                                imageVector = icon,
                                contentDescription = null,
                                size = UnifyIconSize.SMALL
                            )
                        }
                    },
                    trailingIcon = trailingIcon?.let { icon ->
                        {
                            IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                                UnifyIcon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    size = UnifyIconSize.SMALL
                                )
                            }
                        }
                    },
                    prefix = prefix?.let { { Text(it) } },
                    suffix = suffix?.let { { Text(it) } },
                    supportingText = getSupportingText(helperText, errorText, state),
                    isError = state == UnifyTextFieldState.ERROR,
                    visualTransformation = visualTransformation,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    singleLine = maxLines == 1,
                    maxLines = if (maxLines == 1) 1 else maxLines,
                    minLines = minLines,
                    interactionSource = interactionSource,
                    shape = actualShape,
                    colors = OutlinedTextFieldDefaults.colors()
                )
            }
            
            UnifyTextFieldVariant.STANDARD -> {
                TextField(
                    value = actualValue,
                    onValueChange = actualOnValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    readOnly = readOnly,
                    textStyle = textFieldConfig.textStyle,
                    label = label?.let { { Text(it) } },
                    placeholder = placeholder?.let { { Text(it) } },
                    leadingIcon = leadingIcon?.let { icon ->
                        {
                            UnifyIcon(
                                imageVector = icon,
                                contentDescription = null,
                                size = UnifyIconSize.SMALL
                            )
                        }
                    },
                    trailingIcon = trailingIcon?.let { icon ->
                        {
                            IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                                UnifyIcon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    size = UnifyIconSize.SMALL
                                )
                            }
                        }
                    },
                    prefix = prefix?.let { { Text(it) } },
                    suffix = suffix?.let { { Text(it) } },
                    supportingText = getSupportingText(helperText, errorText, state),
                    isError = state == UnifyTextFieldState.ERROR,
                    visualTransformation = visualTransformation,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    singleLine = maxLines == 1,
                    maxLines = if (maxLines == 1) 1 else maxLines,
                    minLines = minLines,
                    interactionSource = interactionSource,
                    colors = actualColors
                )
            }
        }
        
        // 字符计数器
        if (maxLength != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                UnifyText(
                    text = "${actualValue.length}/$maxLength",
                    variant = UnifyTextVariant.CAPTION,
                    color = if (actualValue.length > maxLength * 0.9) {
                        theme.colors.warning
                    } else {
                        theme.colors.onSurfaceVariant
                    }
                )
            }
        }
    }
}

/**
 * 密码输入框组件
 */
@Composable
fun UnifyPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: UnifyTextFieldVariant = UnifyTextFieldVariant.OUTLINED,
    size: UnifyTextFieldSize = UnifyTextFieldSize.MEDIUM,
    state: UnifyTextFieldState = UnifyTextFieldState.NORMAL,
    label: String? = null,
    placeholder: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    showPasswordToggle: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    contentDescription: String? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }
    
    UnifyTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        variant = variant,
        size = size,
        state = state,
        label = label,
        placeholder = placeholder,
        helperText = helperText,
        errorText = errorText,
        trailingIcon = if (showPasswordToggle) {
            if (passwordVisible) {
                androidx.compose.material.icons.Icons.Filled.VisibilityOff
            } else {
                androidx.compose.material.icons.Icons.Filled.Visibility
            }
        } else null,
        onTrailingIconClick = if (showPasswordToggle) {
            { passwordVisible = !passwordVisible }
        } else null,
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        contentDescription = contentDescription
    )
}

/**
 * 搜索输入框组件
 */
@Composable
fun UnifySearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: UnifyTextFieldVariant = UnifyTextFieldVariant.FILLED,
    placeholder: String? = "搜索...",
    onSearch: ((String) -> Unit)? = null,
    onClear: (() -> Unit)? = null,
    contentDescription: String? = null
) {
    UnifyTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        variant = variant,
        placeholder = placeholder,
        leadingIcon = androidx.compose.material.icons.Icons.Default.Search,
        trailingIcon = if (value.isNotEmpty()) {
            androidx.compose.material.icons.Icons.Default.Clear
        } else null,
        onTrailingIconClick = if (value.isNotEmpty()) {
            {
                onValueChange("")
                onClear?.invoke()
            }
        } else null,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch?.invoke(value) }
        ),
        maxLines = 1,
        contentDescription = contentDescription
    )
}

/**
 * 多行文本输入框组件
 */
@Composable
fun UnifyTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: UnifyTextFieldVariant = UnifyTextFieldVariant.OUTLINED,
    state: UnifyTextFieldState = UnifyTextFieldState.NORMAL,
    label: String? = null,
    placeholder: String? = null,
    helperText: String? = null,
    errorText: String? = null,
    minLines: Int = 3,
    maxLines: Int = 6,
    maxLength: Int? = null,
    contentDescription: String? = null
) {
    UnifyTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        variant = variant,
        state = state,
        label = label,
        placeholder = placeholder,
        helperText = helperText,
        errorText = errorText,
        minLines = minLines,
        maxLines = maxLines,
        maxLength = maxLength,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Sentences
        ),
        contentDescription = contentDescription
    )
}

/**
 * 获取支持文本
 */
@Composable
private fun getSupportingText(
    helperText: String?,
    errorText: String?,
    state: UnifyTextFieldState
): (@Composable () -> Unit)? {
    return when {
        state == UnifyTextFieldState.ERROR && errorText != null -> {
            { Text(errorText) }
        }
        helperText != null -> {
            { Text(helperText) }
        }
        else -> null
    }
}

/**
 * 获取文本框配置
 */
@Composable
private fun getTextFieldConfig(
    variant: UnifyTextFieldVariant,
    size: UnifyTextFieldSize,
    state: UnifyTextFieldState,
    theme: com.unify.ui.theme.UnifyTheme
): TextFieldConfig {
    val textStyle = when (size) {
        UnifyTextFieldSize.SMALL -> theme.typography.bodySmall
        UnifyTextFieldSize.MEDIUM -> theme.typography.bodyMedium
        UnifyTextFieldSize.LARGE -> theme.typography.bodyLarge
    }
    
    val shape = when (size) {
        UnifyTextFieldSize.SMALL -> theme.shapes.small
        UnifyTextFieldSize.MEDIUM -> theme.shapes.medium
        UnifyTextFieldSize.LARGE -> theme.shapes.medium
    }
    
    val colors = when (variant) {
        UnifyTextFieldVariant.FILLED -> TextFieldDefaults.colors()
        UnifyTextFieldVariant.OUTLINED -> OutlinedTextFieldDefaults.colors()
        UnifyTextFieldVariant.STANDARD -> TextFieldDefaults.colors()
    }
    
    return TextFieldConfig(
        textStyle = textStyle,
        shape = shape,
        colors = colors
    )
}

/**
 * 文本框配置数据类
 */
private data class TextFieldConfig(
    val textStyle: TextStyle,
    val shape: Shape,
    val colors: TextFieldColors
)
