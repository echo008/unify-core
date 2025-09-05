@file:OptIn(ExperimentalMaterial3Api::class)

package com.unify.ui.components.form

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unify.ui.components.input.UnifyTextField
import com.unify.ui.input.UnifyCheckbox
import com.unify.ui.input.UnifyRadioButton
import com.unify.ui.input.UnifySwitch

/**
 * Unify跨平台表单组件
 * 支持所有8大平台的统一表单体验
 */

data class UnifyFormField(
    val id: String,
    val label: String,
    val type: UnifyFormFieldType,
    val required: Boolean = false,
    val placeholder: String = "",
    val options: List<String> = emptyList(),
    val validation: (String) -> String? = { null }
)

enum class UnifyFormFieldType {
    TEXT, EMAIL, PASSWORD, NUMBER, PHONE, 
    MULTILINE, DROPDOWN, RADIO, CHECKBOX, SWITCH, SLIDER
}

data class UnifyFormData(
    val fields: Map<String, Any> = emptyMap()
) {
    fun getString(fieldId: String): String = fields[fieldId] as? String ?: ""
    fun getBoolean(fieldId: String): Boolean = fields[fieldId] as? Boolean ?: false
    fun getFloat(fieldId: String): Float = fields[fieldId] as? Float ?: 0f
    fun getList(fieldId: String): List<String> = fields[fieldId] as? List<String> ?: emptyList()
}

@Composable
fun UnifyForm(
    fields: List<UnifyFormField>,
    formData: UnifyFormData,
    onDataChange: (String, Any) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    submitButton: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        fields.forEach { field ->
            UnifyFormFieldRenderer(
                field = field,
                value = when (field.type) {
                    UnifyFormFieldType.CHECKBOX, 
                    UnifyFormFieldType.SWITCH -> formData.getBoolean(field.id)
                    UnifyFormFieldType.SLIDER -> formData.getFloat(field.id)
                    else -> formData.getString(field.id)
                },
                onValueChange = { value -> onDataChange(field.id, value) }
            )
        }
        
        submitButton?.invoke()
    }
}

@Composable
private fun UnifyFormFieldRenderer(
    field: UnifyFormField,
    value: Any,
    onValueChange: (Any) -> Unit,
    modifier: Modifier = Modifier
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    Column(modifier = modifier) {
        when (field.type) {
            UnifyFormFieldType.TEXT,
            UnifyFormFieldType.EMAIL,
            UnifyFormFieldType.PASSWORD,
            UnifyFormFieldType.NUMBER,
            UnifyFormFieldType.PHONE -> {
                UnifyTextField(
                    value = value as String,
                    onValueChange = { newValue ->
                        errorMessage = field.validation(newValue)
                        onValueChange(newValue)
                    },
                    label = field.label + if (field.required) " *" else "",
                    placeholder = field.placeholder,
                    isError = errorMessage != null,
                    errorMessage = errorMessage,
                    isPassword = field.type == UnifyFormFieldType.PASSWORD,
                    keyboardType = when (field.type) {
                        UnifyFormFieldType.EMAIL -> androidx.compose.ui.text.input.KeyboardType.Email
                        UnifyFormFieldType.NUMBER -> androidx.compose.ui.text.input.KeyboardType.Number
                        UnifyFormFieldType.PHONE -> androidx.compose.ui.text.input.KeyboardType.Phone
                        else -> androidx.compose.ui.text.input.KeyboardType.Text
                    }
                )
            }
            
            UnifyFormFieldType.MULTILINE -> {
                UnifyTextField(
                    value = value as String,
                    onValueChange = { newValue ->
                        errorMessage = field.validation(newValue)
                        onValueChange(newValue)
                    },
                    label = field.label + if (field.required) " *" else "",
                    placeholder = field.placeholder,
                    singleLine = false,
                    minLines = 3,
                    maxLines = 6,
                    isError = errorMessage != null,
                    errorMessage = errorMessage
                )
            }
            
            UnifyFormFieldType.DROPDOWN -> {
                var expanded by remember { mutableStateOf(false) }
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    UnifyTextField(
                        value = value as String,
                        onValueChange = {},
                        label = field.label + if (field.required) " *" else "",
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
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
            }
            
            UnifyFormFieldType.RADIO -> {
                Column {
                    Text(
                        text = field.label + if (field.required) " *" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    field.options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = value == option,
                                onClick = { onValueChange(option) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            UnifyFormFieldType.CHECKBOX -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = value as Boolean,
                        onCheckedChange = { onValueChange(it) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = field.label + if (field.required) " *" else "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            UnifyFormFieldType.SWITCH -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = field.label + if (field.required) " *" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = value as Boolean,
                        onCheckedChange = { onValueChange(it) }
                    )
                }
            }
            
            UnifyFormFieldType.SLIDER -> {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = field.label + if (field.required) " *" else "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "%.1f".format(value as Float),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = value as Float,
                        onValueChange = { onValueChange(it) },
                        valueRange = 0f..100f
                    )
                }
            }
        }
    }
}

@Composable
fun UnifyFormSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        content()
    }
}

@Composable
fun UnifyFormValidator(
    fields: List<UnifyFormField>,
    formData: UnifyFormData,
    onValidationResult: (Boolean, List<String>) -> Unit
) {
    val errors = mutableListOf<String>()
    
    fields.forEach { field ->
        val value = when (field.type) {
            UnifyFormFieldType.CHECKBOX, 
            UnifyFormFieldType.SWITCH -> formData.getBoolean(field.id).toString()
            UnifyFormFieldType.SLIDER -> formData.getFloat(field.id).toString()
            else -> formData.getString(field.id)
        }
        
        // 必填字段验证
        if (field.required && value.isEmpty()) {
            errors.add("${field.label}是必填项")
        }
        
        // 自定义验证
        val validationError = field.validation(value)
        if (validationError != null) {
            errors.add(validationError)
        }
    }
    
    LaunchedEffect(formData) {
        onValidationResult(errors.isEmpty(), errors)
    }
}

@Composable
fun UnifyFormSubmitButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled && !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text)
    }
}
