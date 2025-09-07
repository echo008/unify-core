package com.unify.ui.components.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
actual fun UnifyBiometricAuth(
    onAuthResult: (AuthenticationResult) -> Unit,
    modifier: Modifier,
    title: String,
    subtitle: String,
    negativeButtonText: String,
    enabledBiometrics: Set<BiometricType>
) {
    Column(modifier = modifier) {
        Text("JS Biometric Auth")
        Text(title)
        Text(subtitle)
        Button(onClick = { onAuthResult(AuthenticationResult(isSuccess = true)) }) {
            Text("Simulate Success")
        }
        Button(onClick = { onAuthResult(AuthenticationResult(isSuccess = false, errorMessage = "User cancelled")) }) {
            Text(negativeButtonText)
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
    
    Column {
        OutlinedTextField(
            value = password,
            onValueChange = { 
                onPasswordChange(it)
                onValidationResult(it.length >= 8, if (it.length < 8) "密码至少需要8位" else null)
            },
            modifier = modifier,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = if (enableToggleVisibility) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(if (passwordVisible) "Hide" else "Show")
                    }
                }
            } else null
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
    Column(modifier = modifier) {
        Text("JS PIN Code Input")
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = if (maskInput) PasswordVisualTransformation() else VisualTransformation.None,
            placeholder = { Text("Enter $length digit PIN") }
        )
        if (showKeypad) {
            Text("Virtual keypad enabled")
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
    Column(modifier = modifier) {
        Text("JS Secure Keyboard")
        Text("Type: $keyboardType")
        if (randomizeLayout) {
            Text("Randomized layout")
        }
        when (keyboardType) {
            SecureKeyboardType.NUMERIC -> {
                Row {
                    (1..9).forEach { number ->
                        Button(onClick = { onKeyPressed(number.toString()) }) {
                            Text(number.toString())
                        }
                    }
                }
            }
            SecureKeyboardType.ALPHANUMERIC -> {
                Text("Alphanumeric keyboard")
            }
            SecureKeyboardType.ALPHABETIC -> {
                Text("Alphabetic keyboard")
            }
        }
        if (showDeleteKey) {
            Button(onClick = { onKeyPressed("DELETE") }) {
                Text("Delete")
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
    Column(modifier = modifier) {
        Text("JS Password Strength")
        val strength = when {
            password.length < 4 -> 0.25f
            password.length < 8 -> 0.5f
            password.length < 12 -> 0.75f
            else -> 1f
        }
        val strengthColor = when {
            strength <= 0.25f -> colors.weakColor
            strength <= 0.5f -> colors.mediumColor
            strength <= 0.75f -> colors.strongColor
            else -> colors.veryStrongColor
        }
        LinearProgressIndicator(
            progress = strength,
            color = strengthColor,
            modifier = Modifier.fillMaxWidth()
        )
        if (showText) {
            val strengthText = when {
                strength <= 0.25f -> "Weak"
                strength <= 0.5f -> "Medium"
                strength <= 0.75f -> "Strong"
                else -> "Very Strong"
            }
            Text(strengthText, color = strengthColor)
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
    
    Column(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium)
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
            label = { Text("验证码") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text("输入${codeLength}位验证码") }
        )
        Row {
            Button(
                onClick = { onResendCode() },
                enabled = resendEnabled && countdown <= 0
            ) {
                Text(if (countdown > 0) "重发 (${countdown}s)" else "重发验证码")
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
    Column(modifier = modifier) {
        Text("JS Security Settings")
        Switch(
            checked = config.enableBiometric,
            onCheckedChange = { onConfigChange(config.copy(enableBiometric = it)) }
        )
        Text("Biometric Authentication")
        
        availableBiometrics.forEach { biometric ->
            Row {
                Text(biometric.toString())
                Button(onClick = { onTestBiometric(biometric) }) {
                    Text("Test")
                }
            }
        }
        
        Switch(
            checked = config.enablePinCode,
            onCheckedChange = { onConfigChange(config.copy(enablePinCode = it)) }
        )
        Text("Two Factor Authentication")
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
    var newKey by remember { mutableStateOf("") }
    var newValue by remember { mutableStateOf("") }
    
    Column(modifier = modifier) {
        Text("JS Secure Storage")
        if (encryptionEnabled) {
            Text("Encryption: Enabled")
        }
        
        if (showDataPreview) {
            Text("Stored data (${data.size} items):")
            data.forEach { (key, value) ->
                Text("$key: ${value.take(20)}...")
            }
        }
        
        OutlinedTextField(
            value = newKey,
            onValueChange = { newKey = it },
            label = { Text("Key") }
        )
        OutlinedTextField(
            value = newValue,
            onValueChange = { newValue = it },
            label = { Text("Value") }
        )
        Button(onClick = { 
            if (newKey.isNotEmpty()) {
                onDataChange(data + (newKey to newValue))
                newKey = ""
                newValue = ""
            }
        }) {
            Text("Store")
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
    var consentStates by remember { 
        mutableStateOf(privacyItems.associate { it.id to it.defaultValue })
    }
    
    Column(modifier = modifier) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        
        if (showSelectAll) {
            val allSelected = consentStates.values.all { it }
            Row {
                Switch(
                    checked = allSelected,
                    onCheckedChange = { selectAll ->
                        val newStates = privacyItems.associate { 
                            it.id to (if (it.required) true else selectAll)
                        }
                        consentStates = newStates
                        onConsentChange(newStates)
                    }
                )
                Text("Select All")
            }
        }
        
        privacyItems.forEach { item ->
            Row {
                Switch(
                    checked = consentStates[item.id] ?: item.defaultValue,
                    onCheckedChange = { granted ->
                        val newStates = consentStates + (item.id to granted)
                        consentStates = newStates
                        onConsentChange(newStates)
                    },
                    enabled = !item.required
                )
                Column {
                    Text(item.title + if (item.required) " (Required)" else "")
                    Text(item.description, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
