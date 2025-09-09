package com.unify.ui.components.security

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.unify.core.types.AuthenticationResult
import com.unify.core.types.BiometricType
import com.unify.core.types.PasswordStrength

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
    enabledBiometrics: Set<BiometricType>,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium)
            Button(
                onClick = { onAuthResult(AuthenticationResult(isSuccess = true, biometricType = enabledBiometrics.firstOrNull()?.name)) },
            ) {
                Text("Authenticate")
            }
            Button(
                onClick = { onAuthResult(AuthenticationResult(isSuccess = false, errorMessage = "User cancelled")) },
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
    onValidationResult: (Boolean, String?) -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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
                trailingIcon =
                    if (enableToggleVisibility) {
                        {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Text(if (passwordVisible) "隐藏" else "显示")
                            }
                        }
                    } else {
                        null
                    },
                modifier = Modifier.fillMaxWidth(),
            )

            if (showStrengthIndicator) {
                val strength =
                    when {
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
    onComplete: (String) -> Unit,
) {
    var pin by remember { mutableStateOf("") }

    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Enter PIN Code")
            Text("Length: $length")

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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
    showDeleteKey: Boolean,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Secure Keyboard")
            if (keyboardType == SecureKeyboardType.NUMERIC || keyboardType == SecureKeyboardType.ALPHANUMERIC) {
                Row {
                    (1..9).forEach { num ->
                        Button(
                            onClick = { onKeyPressed(num.toString()) },
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
    colors: PasswordStrengthColors,
) {
    val strength =
        remember(password) {
            when {
                password.length < 6 -> PasswordStrength.WEAK
                password.length < 10 -> PasswordStrength.FAIR
                else -> PasswordStrength.STRONG
            }
        }

    // Password strength is calculated automatically

    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            if (showText) {
                Text("Password Strength: $strength")
            }

            val strengthColor =
                when (strength) {
                    PasswordStrength.VERY_WEAK -> Color.Red
                    PasswordStrength.WEAK -> Color(0xFFFF9800)
                    PasswordStrength.FAIR -> Color.Yellow
                    PasswordStrength.GOOD -> Color(0xFF4CAF50)
                    PasswordStrength.STRONG -> Color.Green
                    PasswordStrength.VERY_STRONG -> Color(0xFF2E7D32)
                }

            LinearProgressIndicator(
                progress = {
                    when (strength) {
                        PasswordStrength.VERY_WEAK -> 0.1f
                        PasswordStrength.WEAK -> 0.25f
                        PasswordStrength.FAIR -> 0.4f
                        PasswordStrength.GOOD -> 0.6f
                        PasswordStrength.STRONG -> 0.8f
                        PasswordStrength.VERY_STRONG -> 1.0f
                    }
                },
                color = strengthColor,
                modifier = Modifier.fillMaxWidth(),
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
    countdown: Int,
) {
    var code by remember { mutableStateOf("") }

    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(title)
            Text(subtitle)
            Text("Code Length: $codeLength")
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
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )

            if (resendEnabled) {
                Button(
                    onClick = onResendCode,
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
    availableBiometrics: Set<String>,
    onTestBiometric: (String) -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Security Settings")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("生物识别")
                Switch(
                    checked = config.enableBiometric,
                    onCheckedChange = {
                        onConfigChange(config.copy(enableBiometric = it))
                    },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("PIN码")
                Switch(
                    checked = config.enablePinCode,
                    onCheckedChange = {
                        onConfigChange(config.copy(enablePinCode = it))
                    },
                )
            }

            Text("可用生物识别: ${availableBiometrics.joinToString()}")

            availableBiometrics.forEach { biometric ->
                Button(
                    onClick = { onTestBiometric(biometric) },
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
    showDataPreview: Boolean,
) {
    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }

    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Secure Storage")
            Text("Encryption: ${if (encryptionEnabled) "Enabled" else "Disabled"}")

            OutlinedTextField(
                value = key,
                onValueChange = { key = it },
                label = { Text("Key") },
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("Value") },
                modifier = Modifier.fillMaxWidth(),
            )

            Row {
                Button(
                    onClick = {
                        val newData = data.toMutableMap()
                        newData[key] = value
                        onDataChange(newData)
                    },
                ) {
                    Text("Store")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        value = data[key] ?: ""
                    },
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
    consentItems: List<String>,
    onConsentChange: (List<String>) -> Unit,
    modifier: Modifier,
    title: String,
    description: String,
    allowPartialConsent: Boolean,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(title)
            Text(description)

            var selectedItems by remember { mutableStateOf(emptyList<String>()) }

            consentItems.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = selectedItems.contains(item),
                        onCheckedChange = { checked ->
                            selectedItems =
                                if (checked) {
                                    selectedItems + item
                                } else {
                                    selectedItems - item
                                }
                            onConsentChange(selectedItems)
                        },
                    )
                    Text(item, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

@Composable
actual fun UnifySecurityDashboard(
    config: SecurityConfig,
    onConfigChange: (SecurityConfig) -> Unit,
    modifier: Modifier,
    availableBiometrics: Set<String>,
    onTestBiometric: (String) -> Unit,
) {
    Column(modifier = modifier) {
        Text("Android Security Dashboard", style = MaterialTheme.typography.titleLarge)

        Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Security Settings", style = MaterialTheme.typography.titleMedium)

                Row {
                    Switch(
                        checked = config.enableBiometric,
                        onCheckedChange = { onConfigChange(config.copy(enableBiometric = it)) },
                    )
                    Text("Enable Biometric Authentication")
                }

                Row {
                    Switch(
                        checked = config.enablePinCode,
                        onCheckedChange = { onConfigChange(config.copy(enablePinCode = it)) },
                    )
                    Text("Enable PIN Code")
                }

                Text("Max Attempts: ${config.maxAttempts}")
                Text("Lockout Duration: ${config.lockoutDuration / 1000}s")
            }
        }

        if (availableBiometrics.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Available Biometrics", style = MaterialTheme.typography.titleMedium)
                    availableBiometrics.forEach { biometric ->
                        Row {
                            Text(biometric, modifier = Modifier.weight(1f))
                            Button(onClick = { onTestBiometric(biometric) }) {
                                Text("Test")
                            }
                        }
                    }
                }
            }
        }
    }
}
