package com.unify.ui.components.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

/**
 * Android安全组件实现
 */
@Composable
actual fun UnifyBiometricAuth(
    onAuthResult: (AuthenticationResult) -> Unit,
    modifier: Modifier,
    title: String,
    subtitle: String,
    negativeButtonText: String,
    enabledBiometrics: Set<BiometricType>
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            Button(
                onClick = { onAuthResult(AuthenticationResult(isSuccess = true)) }
            ) {
                Text("Authenticate")
            }
            Button(
                onClick = { onAuthResult(AuthenticationResult(isSuccess = false, errorMessage = "User cancelled")) }
            ) {
                Text(negativeButtonText)
            }
        }
    }
}

@Composable
actual fun UnifyPasswordField(
    password: String,
    onPasswordChange: (String) -> Unit,
    modifier: Modifier,
    label: String,
    placeholder: String,
    showStrengthIndicator: Boolean,
    enableToggleVisibility: Boolean,
    onValidationResult: (Boolean, String?) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    onPasswordChange(it)
                    onValidationResult(it.length >= 8, if (it.length < 8) "密码至少需要8位" else null)
                },
                label = { Text(label) },
                placeholder = { Text(placeholder) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = if (enableToggleVisibility) {
                    {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Text(if (passwordVisible) "隐藏" else "显示")
                        }
                    }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )
            
            if (showStrengthIndicator) {
                val strength = when {
                    password.length < 6 -> "弱"
                    password.length < 10 -> "中等"
                    else -> "强"
                }
                Text("密码强度: $strength")
            }
        }
    }
}

@Composable
actual fun UnifyPinCodeInput(
    pinCode: String,
    onPinCodeChange: (String) -> Unit,
    modifier: Modifier,
    length: Int,
    maskInput: Boolean,
    showKeypad: Boolean,
    onComplete: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Enter PIN Code")
            Text("Length: ${length}")
            
            OutlinedTextField(
                value = pinCode,
                onValueChange = { newPin ->
                    if (newPin.length <= length) {
                        onPinCodeChange(newPin)
                        if (newPin.length == length) {
                            onComplete(newPin)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (maskInput) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            if (showKeypad) {
                Text("Virtual Keypad Available")
            }
        }
    }
}

@Composable
actual fun UnifySecureKeyboard(
    onKeyPressed: (String) -> Unit,
    modifier: Modifier,
    keyboardType: SecureKeyboardType,
    randomizeLayout: Boolean,
    showDeleteKey: Boolean
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Secure Keyboard")
            if (keyboardType == SecureKeyboardType.NUMERIC || keyboardType == SecureKeyboardType.ALPHANUMERIC) {
                Row {
                    (1..9).forEach { num ->
                        Button(
                            onClick = { onKeyPressed(num.toString()) }
                        ) {
                            Text(num.toString())
                        }
                    }
                }
            }
        }
    }
}

@Composable
actual fun UnifyPasswordStrengthIndicator(
    password: String,
    modifier: Modifier,
    showText: Boolean,
    colors: PasswordStrengthColors
) {
    val strength = remember(password) {
        when {
            password.length < 6 -> PasswordStrength.WEAK
            password.length < 10 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
    }
    
    // Password strength is calculated automatically
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            if (showText) {
                Text("Password Strength: $strength")
            }
            
            val strengthColor = when (strength) {
                PasswordStrength.WEAK -> colors.weakColor
                PasswordStrength.MEDIUM -> colors.mediumColor
                PasswordStrength.STRONG -> colors.strongColor
                PasswordStrength.VERY_STRONG -> colors.veryStrongColor
            }
            
            LinearProgressIndicator(
                progress = when (strength) {
                    PasswordStrength.WEAK -> 0.25f
                    PasswordStrength.MEDIUM -> 0.5f
                    PasswordStrength.STRONG -> 0.75f
                    PasswordStrength.VERY_STRONG -> 1.0f
                },
                color = strengthColor,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
actual fun UnifyTwoFactorAuth(
    onCodeEntered: (String) -> Unit,
    modifier: Modifier,
    codeLength: Int,
    title: String,
    subtitle: String,
    resendEnabled: Boolean,
    onResendCode: () -> Unit,
    countdown: Int
) {
    var code by remember { mutableStateOf("") }
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(title)
            Text(subtitle)
            Text("Code Length: ${codeLength}")
            if (countdown > 0) {
                Text("Countdown: ${countdown}s")
            }
            
            OutlinedTextField(
                value = code,
                onValueChange = { newCode ->
                    if (newCode.length <= codeLength) {
                        code = newCode
                        if (newCode.length == codeLength) {
                            onCodeEntered(newCode)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            if (resendEnabled) {
                Button(
                    onClick = onResendCode
                ) {
                    Text("Resend Code")
                }
            }
        }
    }
}

@Composable
actual fun UnifySecuritySettings(
    config: SecurityConfig,
    onConfigChange: (SecurityConfig) -> Unit,
    modifier: Modifier,
    availableBiometrics: Set<BiometricType>,
    onTestBiometric: (BiometricType) -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Security Settings")
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("生物识别")
                Switch(
                    checked = config.enableBiometric,
                    onCheckedChange = { 
                        onConfigChange(config.copy(enableBiometric = it))
                    }
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("PIN码")
                Switch(
                    checked = config.enablePinCode,
                    onCheckedChange = { 
                        onConfigChange(config.copy(enablePinCode = it))
                    }
                )
            }
            
            Text("可用生物识别: ${availableBiometrics.joinToString()}")
            
            availableBiometrics.forEach { biometric ->
                Button(
                    onClick = { onTestBiometric(biometric) }
                ) {
                    Text("测试 $biometric")
                }
            }
        }
    }
}

@Composable
actual fun UnifySecureStorage(
    data: Map<String, String>,
    onDataChange: (Map<String, String>) -> Unit,
    modifier: Modifier,
    encryptionEnabled: Boolean,
    showDataPreview: Boolean
) {
    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Secure Storage")
            Text("Encryption: ${if (encryptionEnabled) "Enabled" else "Disabled"}")
            
            OutlinedTextField(
                value = key,
                onValueChange = { key = it },
                label = { Text("Key") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("Value") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Row {
                Button(
                    onClick = { 
                        val newData = data.toMutableMap()
                        newData[key] = value
                        onDataChange(newData)
                    }
                ) {
                    Text("Store")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { 
                        value = data[key] ?: ""
                    }
                ) {
                    Text("Retrieve")
                }
            }
            
            if (showDataPreview) {
                Text("Stored Data:")
                data.forEach { (k, v) ->
                    Text("$k: $v")
                }
            }
        }
    }
}

@Composable
actual fun UnifyPrivacyConsent(
    privacyItems: List<PrivacyItem>,
    onConsentChange: (Map<String, Boolean>) -> Unit,
    modifier: Modifier,
    title: String,
    showSelectAll: Boolean
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(title)
            
            if (showSelectAll) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("全选")
                    Switch(
                        checked = false,
                        onCheckedChange = { selectAll ->
                            val newConsents = privacyItems.associate { it.id to selectAll }
                            onConsentChange(newConsents)
                        }
                    )
                }
            }
            
            LazyColumn {
                items(privacyItems) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title)
                            Text(item.description)
                            if (item.required) {
                                Text("必需", color = Color.Red)
                            }
                        }
                        Switch(
                            checked = item.defaultValue,
                            onCheckedChange = { 
                                val newConsents = mapOf(item.id to it)
                                onConsentChange(newConsents)
                            }
                        )
                    }
                }
            }
        }
    }
}
