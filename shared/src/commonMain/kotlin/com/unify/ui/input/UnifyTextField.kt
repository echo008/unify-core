package com.unify.ui.input

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.drawBehind

/**
 * Unify文本输入框组件
 * 支持多种样式和验证功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    errorText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    style: TextFieldStyle = TextFieldStyle.OUTLINED
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        when (style) {
            TextFieldStyle.OUTLINED -> {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused },
                    enabled = enabled,
                    readOnly = readOnly,
                    label = label?.let { { Text(it) } },
                    placeholder = placeholder?.let { { Text(it) } },
                    leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
                    trailingIcon = trailingIcon?.let { 
                        {
                            IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                                Icon(it, contentDescription = null)
                            }
                        }
                    },
                    isError = isError,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    singleLine = singleLine,
                    maxLines = maxLines,
                    minLines = minLines,
                    visualTransformation = visualTransformation,
                    interactionSource = interactionSource,
                    shape = RoundedCornerShape(12.dp)
                )
            }
            TextFieldStyle.FILLED -> {
                TextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused },
                    enabled = enabled,
                    readOnly = readOnly,
                    label = label?.let { { Text(it) } },
                    placeholder = placeholder?.let { { Text(it) } },
                    leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
                    trailingIcon = trailingIcon?.let { 
                        {
                            IconButton(onClick = { onTrailingIconClick?.invoke() }) {
                                Icon(it, contentDescription = null)
                            }
                        }
                    },
                    isError = isError,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    singleLine = singleLine,
                    maxLines = maxLines,
                    minLines = minLines,
                    visualTransformation = visualTransformation,
                    interactionSource = interactionSource,
                    shape = RoundedCornerShape(12.dp, 12.dp, 0.dp, 0.dp)
                )
            }
        }
        
        // 支持文本或错误文本
        val displayText = if (isError && errorText != null) errorText else supportingText
        displayText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

/**
 * 密码输入框
 */
@Composable
fun UnifyPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "密码",
    placeholder: String = "请输入密码",
    enabled: Boolean = true,
    isError: Boolean = false,
    errorText: String? = null,
    showPasswordStrength: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var passwordVisible by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        UnifyTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            placeholder = placeholder,
            enabled = enabled,
            isError = isError,
            errorText = errorText,
            leadingIcon = Icons.Default.Lock,
            trailingIcon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            onTrailingIconClick = { passwordVisible = !passwordVisible },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            keyboardActions = keyboardActions
        )
        
        // 密码强度指示器
        if (showPasswordStrength && value.isNotEmpty()) {
            UnifyPasswordStrengthIndicator(password = value)
        }
    }
}

/**
 * 搜索输入框
 */
@Composable
fun UnifySearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索...",
    enabled: Boolean = true,
    onSearch: ((String) -> Unit)? = null,
    onClear: (() -> Unit)? = null
) {
    UnifyTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        enabled = enabled,
        leadingIcon = Icons.Default.Search,
        trailingIcon = if (value.isNotEmpty()) Icons.Default.Clear else null,
        onTrailingIconClick = {
            onValueChange("")
            onClear?.invoke()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch?.invoke(value) }
        ),
        singleLine = true
    )
}

/**
 * 数字输入框
 */
@Composable
fun UnifyNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorText: String? = null,
    allowDecimal: Boolean = true,
    allowNegative: Boolean = true,
    maxValue: Double? = null,
    minValue: Double? = null
) {
    UnifyTextField(
        value = value,
        onValueChange = { newValue ->
            val filteredValue = filterNumericInput(
                newValue, 
                allowDecimal, 
                allowNegative, 
                maxValue, 
                minValue
            )
            onValueChange(filteredValue)
        },
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        enabled = enabled,
        isError = isError,
        errorText = errorText,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (allowDecimal) KeyboardType.Decimal else KeyboardType.Number
        ),
        singleLine = true
    )
}

/**
 * 邮箱输入框
 */
@Composable
fun UnifyEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "邮箱",
    placeholder: String = "请输入邮箱地址",
    enabled: Boolean = true,
    autoValidate: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val isValidEmail = remember(value) { 
        if (autoValidate && value.isNotEmpty()) {
            isValidEmailAddress(value)
        } else true
    }
    
    UnifyTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        enabled = enabled,
        leadingIcon = Icons.Default.Email,
        isError = !isValidEmail,
        errorText = if (!isValidEmail) "请输入有效的邮箱地址" else null,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        keyboardActions = keyboardActions,
        singleLine = true
    )
}

/**
 * 电话号码输入框
 */
@Composable
fun UnifyPhoneField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "手机号",
    placeholder: String = "请输入手机号码",
    enabled: Boolean = true,
    countryCode: String = "+86",
    autoValidate: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val isValidPhone = remember(value) {
        if (autoValidate && value.isNotEmpty()) {
            isValidPhoneNumber(value, countryCode)
        } else true
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 国家代码
        OutlinedTextField(
            value = countryCode,
            onValueChange = { },
            modifier = Modifier.width(80.dp),
            enabled = false,
            singleLine = true,
            textStyle = TextStyle(textAlign = TextAlign.Center)
        )
        
        // 电话号码
        UnifyTextField(
            value = value,
            onValueChange = { newValue ->
                val filteredValue = newValue.filter { it.isDigit() }
                onValueChange(filteredValue)
            },
            modifier = Modifier.weight(1f),
            label = label,
            placeholder = placeholder,
            enabled = enabled,
            leadingIcon = Icons.Default.Phone,
            isError = !isValidPhone,
            errorText = if (!isValidPhone) "请输入有效的手机号码" else null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            keyboardActions = keyboardActions,
            singleLine = true
        )
    }
}

/**
 * 多行文本输入框
 */
@Composable
fun UnifyTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    errorText: String? = null,
    minLines: Int = 3,
    maxLines: Int = 6,
    maxLength: Int? = null,
    showCharacterCount: Boolean = true
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        UnifyTextField(
            value = value,
            onValueChange = { newValue ->
                val filteredValue = if (maxLength != null && newValue.length > maxLength) {
                    newValue.take(maxLength)
                } else newValue
                onValueChange(filteredValue)
            },
            label = label,
            placeholder = placeholder,
            enabled = enabled,
            isError = isError,
            errorText = errorText,
            singleLine = false,
            minLines = minLines,
            maxLines = maxLines,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Default
            )
        )
        
        // 字符计数
        if (showCharacterCount && maxLength != null) {
            Text(
                text = "${value.length}/$maxLength",
                style = MaterialTheme.typography.bodySmall,
                color = if (value.length >= maxLength) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

/**
 * 密码强度指示器
 */
@Composable
private fun UnifyPasswordStrengthIndicator(
    password: String,
    modifier: Modifier = Modifier
) {
    val strength = calculatePasswordStrength(password)
    val strengthColor = when (strength.level) {
        PasswordStrength.WEAK -> Color(0xFFF44336)
        PasswordStrength.MEDIUM -> Color(0xFFFF9800)
        PasswordStrength.STRONG -> Color(0xFF4CAF50)
    }
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 强度条
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .drawBehind {
                            drawRect(
                                color = if (index < strength.score) strengthColor else Color.Gray.copy(alpha = 0.3f)
                            )
                        }
                )
            }
        }
        
        // 强度文本
        Text(
            text = "密码强度: ${strength.level.text}",
            style = MaterialTheme.typography.bodySmall,
            color = strengthColor
        )
    }
}

// 辅助函数

private fun filterNumericInput(
    input: String,
    allowDecimal: Boolean,
    allowNegative: Boolean,
    maxValue: Double?,
    minValue: Double?
): String {
    var filtered = input.filter { char ->
        char.isDigit() || 
        (allowDecimal && char == '.' && input.count { it == '.' } <= 1) ||
        (allowNegative && char == '-' && input.indexOf('-') == 0)
    }
    
    // 验证数值范围
    if (filtered.isNotEmpty() && filtered != "-" && filtered != ".") {
        try {
            val value = filtered.toDouble()
            if (maxValue != null && value > maxValue) {
                filtered = maxValue.toString()
            }
            if (minValue != null && value < minValue) {
                filtered = minValue.toString()
            }
        } catch (e: NumberFormatException) {
            // 忽略无效数字
        }
    }
    
    return filtered
}

private fun isValidEmailAddress(email: String): Boolean {
    val emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    return email.matches(emailPattern.toRegex())
}

private fun isValidPhoneNumber(phone: String, countryCode: String): Boolean {
    return when (countryCode) {
        "+86" -> phone.length == 11 && phone.startsWith("1")
        "+1" -> phone.length == 10
        else -> phone.length >= 7 && phone.length <= 15
    }
}

private fun calculatePasswordStrength(password: String): PasswordStrengthResult {
    var score = 0
    
    if (password.length >= 8) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++
    
    val level = when {
        score >= 4 -> PasswordStrength.STRONG
        score >= 2 -> PasswordStrength.MEDIUM
        else -> PasswordStrength.WEAK
    }
    
    return PasswordStrengthResult(
        score = minOf(score, 3),
        level = level
    )
}

// 数据类和枚举

enum class TextFieldStyle {
    OUTLINED,
    FILLED
}

enum class PasswordStrength(val text: String) {
    WEAK("弱"),
    MEDIUM("中"),
    STRONG("强")
}

data class PasswordStrengthResult(
    val score: Int,
    val level: PasswordStrength
)
