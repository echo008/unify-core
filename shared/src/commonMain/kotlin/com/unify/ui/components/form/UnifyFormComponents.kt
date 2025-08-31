package com.unify.ui.components.form

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.components.foundation.*
import kotlinx.coroutines.delay

/**
 * Unify 表单组件
 * 对应微信小程序的 form、label、input、textarea、picker-view 等表单组件
 */

/**
 * 表单验证规则
 */
data class UnifyFormRule(
    val required: Boolean = false,
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val pattern: Regex? = null,
    val validator: ((String) -> String?)? = null,
    val message: String = ""
)

/**
 * 表单字段状态
 */
data class UnifyFormFieldState(
    val value: String = "",
    val error: String? = null,
    val isValid: Boolean = true,
    val isTouched: Boolean = false,
    val isFocused: Boolean = false
)

/**
 * 表单状态管理
 */
class UnifyFormState {
    private val _fields = mutableStateMapOf<String, UnifyFormFieldState>()
    val fields: Map<String, UnifyFormFieldState> = _fields
    
    private val _rules = mutableMapOf<String, List<UnifyFormRule>>()
    
    fun setFieldValue(name: String, value: String) {
        val currentState = _fields[name] ?: UnifyFormFieldState()
        val newState = currentState.copy(
            value = value,
            isTouched = true
        )
        _fields[name] = validateField(name, newState)
    }
    
    fun setFieldFocus(name: String, focused: Boolean) {
        val currentState = _fields[name] ?: UnifyFormFieldState()
        _fields[name] = currentState.copy(isFocused = focused)
    }
    
    fun setFieldRules(name: String, rules: List<UnifyFormRule>) {
        _rules[name] = rules
    }
    
    private fun validateField(name: String, state: UnifyFormFieldState): UnifyFormFieldState {
        val rules = _rules[name] ?: return state
        
        for (rule in rules) {
            // 必填验证
            if (rule.required && state.value.isBlank()) {
                return state.copy(
                    error = rule.message.ifEmpty { "此字段为必填项" },
                    isValid = false
                )
            }
            
            // 长度验证
            rule.minLength?.let { min ->
                if (state.value.length < min) {
                    return state.copy(
                        error = rule.message.ifEmpty { "最少需要${min}个字符" },
                        isValid = false
                    )
                }
            }
            
            rule.maxLength?.let { max ->
                if (state.value.length > max) {
                    return state.copy(
                        error = rule.message.ifEmpty { "最多允许${max}个字符" },
                        isValid = false
                    )
                }
            }
            
            // 正则验证
            rule.pattern?.let { pattern ->
                if (!pattern.matches(state.value)) {
                    return state.copy(
                        error = rule.message.ifEmpty { "格式不正确" },
                        isValid = false
                    )
                }
            }
            
            // 自定义验证
            rule.validator?.let { validator ->
                val error = validator(state.value)
                if (error != null) {
                    return state.copy(
                        error = error,
                        isValid = false
                    )
                }
            }
        }
        
        return state.copy(error = null, isValid = true)
    }
    
    fun validateAll(): Boolean {
        var isValid = true
        _fields.keys.forEach { name ->
            val currentState = _fields[name] ?: UnifyFormFieldState()
            val validatedState = validateField(name, currentState.copy(isTouched = true))
            _fields[name] = validatedState
            if (!validatedState.isValid) {
                isValid = false
            }
        }
        return isValid
    }
    
    fun reset() {
        _fields.clear()
    }
    
    fun getValues(): Map<String, String> {
        return _fields.mapValues { it.value.value }
    }
}

/**
 * 表单组件
 */
@Composable
fun UnifyForm(
    modifier: Modifier = Modifier,
    formState: UnifyFormState = remember { UnifyFormState() },
    onSubmit: ((values: Map<String, String>) -> Unit)? = null,
    onReset: (() -> Unit)? = null,
    reportSubmit: Boolean = false,
    reportSubmitTimeout: Long = 0L,
    contentDescription: String? = null,
    content: @Composable UnifyFormScope.() -> Unit
) {
    val scope = remember(formState) { UnifyFormScope(formState) }
    
    Column(
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        }
    ) {
        scope.content()
    }
}

/**
 * 表单作用域
 */
class UnifyFormScope(val formState: UnifyFormState) {
    
    @Composable
    fun UnifyFormField(
        name: String,
        label: String? = null,
        rules: List<UnifyFormRule> = emptyList(),
        modifier: Modifier = Modifier,
        content: @Composable (UnifyFormFieldState, (String) -> Unit) -> Unit
    ) {
        LaunchedEffect(name, rules) {
            formState.setFieldRules(name, rules)
        }
        
        val fieldState = formState.fields[name] ?: UnifyFormFieldState()
        
        Column(modifier = modifier) {
            label?.let {
                UnifyLabel(
                    text = it,
                    required = rules.any { rule -> rule.required },
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            
            content(fieldState) { value ->
                formState.setFieldValue(name, value)
            }
            
            if (fieldState.error != null && fieldState.isTouched) {
                UnifyText(
                    text = fieldState.error,
                    variant = UnifyTextVariant.CAPTION,
                    color = LocalUnifyTheme.current.colors.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
    
    @Composable
    fun UnifySubmitButton(
        text: String = "提交",
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        onClick: (() -> Unit)? = null
    ) {
        UnifyButton(
            text = text,
            onClick = {
                if (formState.validateAll()) {
                    onClick?.invoke()
                }
            },
            enabled = enabled,
            modifier = modifier
        )
    }
    
    @Composable
    fun UnifyResetButton(
        text: String = "重置",
        modifier: Modifier = Modifier,
        onClick: (() -> Unit)? = null
    ) {
        UnifyButton(
            text = text,
            variant = UnifyButtonVariant.OUTLINED,
            onClick = {
                formState.reset()
                onClick?.invoke()
            },
            modifier = modifier
        )
    }
}

/**
 * 标签组件
 */
@Composable
fun UnifyLabel(
    text: String,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    htmlFor: String? = null,
    color: Color = LocalUnifyTheme.current.colors.onSurface,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    contentDescription: String? = null
) {
    Row(
        modifier = modifier.semantics {
            contentDescription?.let { 
                this.contentDescription = it 
            }
        },
        verticalAlignment = Alignment.CenterVertically
    ) {
        UnifyText(
            text = text,
            color = color,
            fontSize = fontSize,
            fontWeight = fontWeight
        )
        
        if (required) {
            UnifyText(
                text = " *",
                color = LocalUnifyTheme.current.colors.error,
                fontSize = fontSize
            )
        }
    }
}

/**
 * 输入框类型
 */
enum class UnifyInputType {
    TEXT,           // 文本
    NUMBER,         // 数字
    IDCARD,         // 身份证
    DIGIT,          // 带小数点数字
    SAFE_PASSWORD,  // 密码安全输入
    NICKNAME        // 昵称
}

/**
 * 输入框确认类型
 */
enum class UnifyInputConfirmType {
    SEND,    // 发送
    SEARCH,  // 搜索
    NEXT,    // 下一个
    GO,      // 前往
    DONE     // 完成
}

/**
 * 增强输入框组件
 */
@Composable
fun UnifyInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    type: UnifyInputType = UnifyInputType.TEXT,
    password: Boolean = false,
    placeholder: String = "",
    placeholderStyle: TextStyle? = null,
    disabled: Boolean = false,
    maxLength: Int = Int.MAX_VALUE,
    cursorSpacing: Dp = 0.dp,
    autoFocus: Boolean = false,
    focus: Boolean = false,
    confirmType: UnifyInputConfirmType = UnifyInputConfirmType.DONE,
    confirmHold: Boolean = false,
    cursor: Dp = 2.dp,
    selectionStart: Int = -1,
    selectionEnd: Int = -1,
    adjustPosition: Boolean = true,
    holdKeyboard: Boolean = false,
    safePasswordCertPath: String = "",
    safePasswordLength: Int = 0,
    safePasswordTimeStamp: Long = 0L,
    safePasswordNonce: String = "",
    safePasswordSalt: String = "",
    safePasswordCustomHash: String = "",
    randomNumber: Boolean = false,
    controlled: Boolean = false,
    alwaysEmbed: Boolean = false,
    onInput: ((String) -> Unit)? = null,
    onFocus: (() -> Unit)? = null,
    onBlur: (() -> Unit)? = null,
    onConfirm: ((String) -> Unit)? = null,
    onKeyboardHeightChange: ((height: Dp) -> Unit)? = null,
    onNicknameReview: ((pass: Boolean) -> Unit)? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isFocused by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(!password) }
    
    // 自动聚焦
    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            focusRequester.requestFocus()
        }
    }
    
    // 手动聚焦控制
    LaunchedEffect(focus) {
        if (focus) {
            focusRequester.requestFocus()
        }
    }
    
    val keyboardType = when (type) {
        UnifyInputType.NUMBER -> KeyboardType.Number
        UnifyInputType.DIGIT -> KeyboardType.Decimal
        UnifyInputType.TEXT, UnifyInputType.NICKNAME -> KeyboardType.Text
        UnifyInputType.IDCARD -> KeyboardType.Text
        UnifyInputType.SAFE_PASSWORD -> KeyboardType.Password
    }
    
    val imeAction = when (confirmType) {
        UnifyInputConfirmType.SEND -> ImeAction.Send
        UnifyInputConfirmType.SEARCH -> ImeAction.Search
        UnifyInputConfirmType.NEXT -> ImeAction.Next
        UnifyInputConfirmType.GO -> ImeAction.Go
        UnifyInputConfirmType.DONE -> ImeAction.Done
    }
    
    val visualTransformation = if (password && !isPasswordVisible) {
        PasswordVisualTransformation()
    } else {
        VisualTransformation.None
    }
    
    val borderColor by animateColorAsState(
        targetValue = when {
            !disabled && isFocused -> theme.colors.primary
            disabled -> theme.colors.outline.copy(alpha = 0.5f)
            else -> theme.colors.outline
        },
        animationSpec = tween(200),
        label = "border_color"
    )
    
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val filteredValue = if (newValue.length <= maxLength) newValue else value
            onValueChange(filteredValue)
            onInput?.invoke(filteredValue)
        },
        modifier = modifier
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                if (focusState.isFocused) {
                    onFocus?.invoke()
                } else {
                    onBlur?.invoke()
                }
            }
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            },
        enabled = !disabled,
        readOnly = false,
        textStyle = LocalTextStyle.current.copy(
            color = if (disabled) theme.colors.onSurface.copy(alpha = 0.6f) else theme.colors.onSurface
        ),
        placeholder = {
            if (placeholder.isNotEmpty()) {
                UnifyText(
                    text = placeholder,
                    color = theme.colors.onSurface.copy(alpha = 0.6f),
                    variant = UnifyTextVariant.BODY_MEDIUM
                )
            }
        },
        leadingIcon = leadingIcon?.let { icon ->
            {
                UnifyIcon(
                    icon = icon,
                    size = UnifyIconSize.MEDIUM,
                    tint = theme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        trailingIcon = {
            Row {
                if (password) {
                    IconButton(
                        onClick = { isPasswordVisible = !isPasswordVisible }
                    ) {
                        UnifyIcon(
                            icon = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            size = UnifyIconSize.MEDIUM,
                            tint = theme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                trailingIcon?.let { icon ->
                    IconButton(
                        onClick = { onTrailingIconClick?.invoke() }
                    ) {
                        UnifyIcon(
                            icon = icon,
                            size = UnifyIconSize.MEDIUM,
                            tint = theme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        },
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onSend = { onConfirm?.invoke(value) },
            onSearch = { onConfirm?.invoke(value) },
            onNext = { onConfirm?.invoke(value) },
            onGo = { onConfirm?.invoke(value) },
            onDone = { 
                onConfirm?.invoke(value)
                if (!holdKeyboard) {
                    keyboardController?.hide()
                }
            }
        ),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = borderColor,
            unfocusedBorderColor = borderColor,
            disabledBorderColor = borderColor,
            cursorColor = theme.colors.primary
        )
    )
}

/**
 * 多行文本输入框组件
 */
@Composable
fun UnifyTextarea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    disabled: Boolean = false,
    maxLength: Int = Int.MAX_VALUE,
    autoFocus: Boolean = false,
    focus: Boolean = false,
    autoHeight: Boolean = false,
    fixed: Boolean = false,
    cursorSpacing: Dp = 0.dp,
    cursor: Dp = 2.dp,
    showConfirmBar: Boolean = true,
    selectionStart: Int = -1,
    selectionEnd: Int = -1,
    adjustPosition: Boolean = true,
    holdKeyboard: Boolean = false,
    disableDefaultPadding: Boolean = false,
    confirmType: UnifyInputConfirmType = UnifyInputConfirmType.DONE,
    confirmHold: Boolean = false,
    onInput: ((String) -> Unit)? = null,
    onFocus: (() -> Unit)? = null,
    onBlur: (() -> Unit)? = null,
    onConfirm: ((String) -> Unit)? = null,
    onLineChange: ((height: Dp) -> Unit)? = null,
    onKeyboardHeightChange: ((height: Dp) -> Unit)? = null,
    minRows: Int = 3,
    maxRows: Int = 6,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    var isFocused by remember { mutableStateOf(false) }
    
    // 自动聚焦
    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            focusRequester.requestFocus()
        }
    }
    
    // 手动聚焦控制
    LaunchedEffect(focus) {
        if (focus) {
            focusRequester.requestFocus()
        }
    }
    
    val imeAction = when (confirmType) {
        UnifyInputConfirmType.SEND -> ImeAction.Send
        UnifyInputConfirmType.SEARCH -> ImeAction.Search
        UnifyInputConfirmType.NEXT -> ImeAction.Next
        UnifyInputConfirmType.GO -> ImeAction.Go
        UnifyInputConfirmType.DONE -> ImeAction.Done
    }
    
    val borderColor by animateColorAsState(
        targetValue = when {
            !disabled && isFocused -> theme.colors.primary
            disabled -> theme.colors.outline.copy(alpha = 0.5f)
            else -> theme.colors.outline
        },
        animationSpec = tween(200),
        label = "border_color"
    )
    
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                val filteredValue = if (newValue.length <= maxLength) newValue else value
                onValueChange(filteredValue)
                onInput?.invoke(filteredValue)
            },
            modifier = modifier
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                    if (focusState.isFocused) {
                        onFocus?.invoke()
                    } else {
                        onBlur?.invoke()
                    }
                }
                .semantics {
                    contentDescription?.let { 
                        this.contentDescription = it 
                    }
                },
            enabled = !disabled,
            textStyle = LocalTextStyle.current.copy(
                color = if (disabled) theme.colors.onSurface.copy(alpha = 0.6f) else theme.colors.onSurface
            ),
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    UnifyText(
                        text = placeholder,
                        color = theme.colors.onSurface.copy(alpha = 0.6f),
                        variant = UnifyTextVariant.BODY_MEDIUM
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onSend = { onConfirm?.invoke(value) },
                onSearch = { onConfirm?.invoke(value) },
                onNext = { onConfirm?.invoke(value) },
                onGo = { onConfirm?.invoke(value) },
                onDone = { 
                    onConfirm?.invoke(value)
                    if (!holdKeyboard) {
                        keyboardController?.hide()
                    }
                }
            ),
            singleLine = false,
            minLines = minRows,
            maxLines = if (autoHeight) Int.MAX_VALUE else maxRows,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = borderColor,
                unfocusedBorderColor = borderColor,
                disabledBorderColor = borderColor,
                cursorColor = theme.colors.primary
            )
        )
        
        // 字符计数
        if (maxLength < Int.MAX_VALUE) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                UnifyText(
                    text = "${value.length}/$maxLength",
                    variant = UnifyTextVariant.CAPTION,
                    color = if (value.length >= maxLength) theme.colors.error else theme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * 选择器视图数据项
 */
data class UnifyPickerViewItem(
    val value: String,
    val label: String
)

/**
 * 选择器视图组件
 */
@Composable
fun UnifyPickerView(
    value: List<Int>,
    onSelectionChange: (List<Int>) -> Unit,
    modifier: Modifier = Modifier,
    indicatorStyle: String = "",
    maskStyle: String = "",
    maskClass: String = "",
    columns: List<List<UnifyPickerViewItem>> = emptyList(),
    immediateChange: Boolean = false,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    
    Row(
        modifier = modifier
            .height(200.dp)
            .fillMaxWidth()
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            }
    ) {
        columns.forEachIndexed { columnIndex, items ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                // 选择器背景
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            color = theme.colors.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                )
                
                // 选项列表
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items) { item ->
                        val isSelected = value.getOrNull(columnIndex) == items.indexOf(item)
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clickable {
                                    val newValue = value.toMutableList()
                                    if (columnIndex < newValue.size) {
                                        newValue[columnIndex] = items.indexOf(item)
                                    } else {
                                        while (newValue.size <= columnIndex) {
                                            newValue.add(0)
                                        }
                                        newValue[columnIndex] = items.indexOf(item)
                                    }
                                    onSelectionChange(newValue)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            UnifyText(
                                text = item.label,
                                variant = if (isSelected) UnifyTextVariant.BODY_LARGE else UnifyTextVariant.BODY_MEDIUM,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) theme.colors.primary else theme.colors.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
