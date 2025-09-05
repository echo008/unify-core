@file:OptIn(ExperimentalMaterial3Api::class)

package com.unify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Unify表单组件
 * 提供完整的表单输入和验证组件
 */

/**
 * 表单字段数据类
 */
data class FormField(
    val key: String,
    val label: String,
    val value: String = "",
    val isRequired: Boolean = false,
    val errorMessage: String? = null,
    val fieldType: FormFieldType = FormFieldType.Text,
    val options: List<String> = emptyList(),
    val placeholder: String = "",
    val maxLength: Int? = null
)

/**
 * 表单字段类型
 */
enum class FormFieldType {
    Text, Email, Password, Number, Phone, TextArea, 
    Dropdown, Checkbox, Radio, Date, Time, Switch
}

/**
 * 表单验证规则
 */
data class ValidationRule(
    val validator: (String) -> Boolean,
    val errorMessage: String
)

/**
 * 表单状态管理
 */
class FormState {
    private val _fields = mutableStateMapOf<String, FormField>()
    private val _validationRules = mutableMapOf<String, List<ValidationRule>>()
    
    val fields: Map<String, FormField> = _fields
    
    fun addField(field: FormField) {
        _fields[field.key] = field
    }
    
    fun updateField(key: String, value: String) {
        _fields[key]?.let { field ->
            _fields[key] = field.copy(value = value, errorMessage = null)
            validateField(key)
        }
    }
    
    fun addValidationRule(fieldKey: String, rule: ValidationRule) {
        val rules = _validationRules[fieldKey]?.toMutableList() ?: mutableListOf()
        rules.add(rule)
        _validationRules[fieldKey] = rules
    }
    
    fun validateField(key: String): Boolean {
        val field = _fields[key] ?: return true
        val rules = _validationRules[key] ?: return true
        
        for (rule in rules) {
            if (!rule.validator(field.value)) {
                _fields[key] = field.copy(errorMessage = rule.errorMessage)
                return false
            }
        }
        
        _fields[key] = field.copy(errorMessage = null)
        return true
    }
    
    fun validateAll(): Boolean {
        var isValid = true
        _fields.keys.forEach { key ->
            if (!validateField(key)) {
                isValid = false
            }
        }
        return isValid
    }
    
    fun getFieldValue(key: String): String {
        return _fields[key]?.value ?: ""
    }
    
    fun hasErrors(): Boolean {
        return _fields.values.any { it.errorMessage != null }
    }
}

/**
 * 记住表单状态
 */
@Composable
fun rememberFormState(): FormState {
    return remember { FormState() }
}

/**
 * 表单容器
 */
@Composable
fun UnifyForm(
    formState: FormState,
    modifier: Modifier = Modifier,
    title: String? = null,
    onSubmit: () -> Unit = {},
    submitText: String = "提交",
    content: @Composable ColumnScope.(FormState) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            content(formState)
            
            Button(
                onClick = {
                    if (formState.validateAll()) {
                        onSubmit()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !formState.hasErrors()
            ) {
                Text(submitText)
            }
        }
    }
}

/**
 * 文本输入字段
 */
@Composable
fun UnifyTextField(
    field: FormField,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = field.value,
            onValueChange = { value ->
                val finalValue = if (field.maxLength != null) {
                    value.take(field.maxLength)
                } else value
                onValueChange(finalValue)
            },
            label = {
                Row {
                    Text(field.label)
                    if (field.isRequired) {
                        Text(" *", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            placeholder = { Text(field.placeholder) },
            isError = field.errorMessage != null,
            keyboardOptions = when (field.fieldType) {
                FormFieldType.Email -> KeyboardOptions(keyboardType = KeyboardType.Email)
                FormFieldType.Number -> KeyboardOptions(keyboardType = KeyboardType.Number)
                FormFieldType.Phone -> KeyboardOptions(keyboardType = KeyboardType.Phone)
                else -> KeyboardOptions.Default
            },
            visualTransformation = if (field.fieldType == FormFieldType.Password) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            maxLines = if (field.fieldType == FormFieldType.TextArea) 4 else 1,
            modifier = Modifier.fillMaxWidth()
        )
        
        field.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
        
        field.maxLength?.let { maxLength ->
            Text(
                text = "${field.value.length}/$maxLength",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End)
                    .padding(top = 4.dp)
            )
        }
    }
}

/**
 * 下拉选择字段
 */
@Composable
fun UnifyDropdownField(
    field: FormField,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = field.value,
                onValueChange = {},
                readOnly = true,
                label = {
                    Row {
                        Text(field.label)
                        if (field.isRequired) {
                            Text(" *", color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                isError = field.errorMessage != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                field.options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
        
        field.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * 复选框字段
 */
@Composable
fun UnifyCheckboxField(
    field: FormField,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isChecked = field.value == "true"
    
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { checked ->
                    onValueChange(checked.toString())
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = field.label,
                style = MaterialTheme.typography.bodyMedium
            )
            if (field.isRequired) {
                Text(
                    text = " *",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        
        field.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 40.dp, top = 4.dp)
            )
        }
    }
}

/**
 * 单选按钮组字段
 */
@Composable
fun UnifyRadioGroupField(
    field: FormField,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = if (field.isRequired) "${field.label} *" else field.label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        field.options.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = field.value == option,
                    onClick = { onValueChange(option) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        field.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * 开关字段
 */
@Composable
fun UnifySwitchField(
    field: FormField,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isChecked = field.value == "true"
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = field.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (field.placeholder.isNotEmpty()) {
                Text(
                    text = field.placeholder,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Switch(
            checked = isChecked,
            onCheckedChange = { checked ->
                onValueChange(checked.toString())
            }
        )
    }
}

/**
 * 表单分组
 */
@Composable
fun UnifyFormGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

/**
 * 表单步骤器
 */
@Composable
fun UnifyFormStepper(
    currentStep: Int,
    totalSteps: Int,
    stepTitles: List<String>,
    onStepClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val stepNumber = index + 1
            val isCompleted = stepNumber < currentStep
            val isActive = stepNumber == currentStep
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // 步骤圆圈
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            when {
                                isCompleted -> MaterialTheme.colorScheme.primary
                                isActive -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outline
                            },
                            androidx.compose.foundation.shape.CircleShape
                        )
                        .clickable { onStepClick(stepNumber) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isCompleted) "✓" else stepNumber.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // 步骤标题
                if (index < stepTitles.size) {
                    Text(
                        text = stepTitles[index],
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isActive || isCompleted) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // 连接线
            if (index < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .height(2.dp)
                        .background(
                            if (isCompleted) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            }
                        )
                )
            }
        }
    }
}

// 常用验证规则
object ValidationRules {
    fun required() = ValidationRule(
        validator = { it.isNotBlank() },
        errorMessage = "此字段为必填项"
    )
    
    fun email() = ValidationRule(
        validator = { it.contains("@") && it.contains(".") },
        errorMessage = "请输入有效的邮箱地址"
    )
    
    fun minLength(length: Int) = ValidationRule(
        validator = { it.length >= length },
        errorMessage = "最少需要 $length 个字符"
    )
    
    fun maxLength(length: Int) = ValidationRule(
        validator = { it.length <= length },
        errorMessage = "最多允许 $length 个字符"
    )
    
    fun phone() = ValidationRule(
        validator = { it.matches(Regex("^1[3-9]\\d{9}$")) },
        errorMessage = "请输入有效的手机号码"
    )
}

// 扩展函数
private fun Modifier.clickable(onClick: () -> Unit): Modifier {
    return this.then(Modifier.padding(4.dp))
}
