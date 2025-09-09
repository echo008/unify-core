package com.unify.ui.components.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
 * iOS平台安全组件实现
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
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(title)
            Text(subtitle)
            Text("Enabled Biometrics: ${enabledBiometrics.joinToString { it.name }}")

            Button(
                onClick = {
                    val result =
                        AuthenticationResult(
                            isSuccess = true,
                            biometricType = BiometricType.FINGERPRINT.toString(),
                        )
                    onAuthResult(result)
                },
            ) {
                Text("Authenticate")
            }

            Button(
                onClick = {
                    val result =
                        AuthenticationResult(
                            isSuccess = false,
                            errorMessage = "Authentication cancelled",
                        )
                    onAuthResult(result)
                },
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
    var isPasswordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        placeholder = { Text(placeholder) },
        trailingIcon = {
            if (enableToggleVisibility) {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                    )
                }
            }
        },
    )

    if (showStrengthIndicator) {
        val isValid = password.length >= 8
        Text("Password strength: ${if (isValid) "Strong" else "Weak"}")
        onValidationResult(isValid, if (isValid) null else "Password too short")
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
    OutlinedTextField(
        value = pinCode,
        onValueChange = { newPin ->
            if (newPin.length <= length && newPin.all { it.isDigit() }) {
                onPinCodeChange(newPin)
                if (newPin.length == length) {
                    onComplete(newPin)
                }
            }
        },
        label = { Text("PIN Code") },
        modifier = modifier.fillMaxWidth(),
        visualTransformation = if (maskInput) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
    )

    if (showKeypad) {
        Text("Virtual keypad available")
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
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Secure Keyboard")
            Text("Type: ${keyboardType.name}")
            Text("Randomized: $randomizeLayout")
            Text("Delete Key: $showDeleteKey")

            Button(
                onClick = { onKeyPressed("1") },
            ) {
                Text("1")
            }

            if (showDeleteKey) {
                Button(
                    onClick = { onKeyPressed("DELETE") },
                ) {
                    Text("Delete")
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
        when {
            password.length >= 12 &&
                password.any {
                    it.isUpperCase()
                } && password.any { it.isLowerCase() } && password.any { it.isDigit() } -> PasswordStrength.STRONG
            password.length >= 8 && password.any { it.isUpperCase() } && password.any { it.isLowerCase() } -> PasswordStrength.GOOD
            password.length >= 6 -> PasswordStrength.FAIR
            else -> PasswordStrength.VERY_WEAK
        }

    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Password Strength Indicator")

            if (showText) {
                Text("Strength: ${strength.name}")
            }

            // 显示强度颜色条
            val color =
                when (strength) {
                    PasswordStrength.VERY_WEAK -> Color.Red
                    PasswordStrength.WEAK -> Color(0xFFE91E63)
                    PasswordStrength.FAIR -> Color(0xFFFF9800)
                    PasswordStrength.GOOD -> Color(0xFFFFEB3B)
                    PasswordStrength.STRONG -> Color.Green
                    PasswordStrength.VERY_STRONG -> Color(0xFF4CAF50)
                }

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .background(color),
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
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(title)
            Text(subtitle)
            Text("Code Length: $codeLength")

            OutlinedTextField(
                value = code,
                onValueChange = { newCode ->
                    if (newCode.length <= codeLength && newCode.all { it.isDigit() }) {
                        code = newCode
                        if (newCode.length == codeLength) {
                            onCodeEntered(newCode)
                        }
                    }
                },
                label = { Text("Verification Code") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )

            if (resendEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onResendCode) {
                    Text("Resend Code ${if (countdown > 0) "($countdown)" else ""}")
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
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Security Settings")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Biometric Authentication")
                Switch(
                    checked = config.enableBiometric,
                    onCheckedChange = { onConfigChange(config.copy(enableBiometric = it)) },
                )
            }

            availableBiometrics.forEach { biometric ->
                Row {
                    Switch(
                        checked = config.enabledBiometrics.contains(biometric),
                        onCheckedChange = { enabled ->
                            val newBiometrics =
                                if (enabled) {
                                    config.enabledBiometrics + biometric
                                } else {
                                    config.enabledBiometrics - biometric
                                }
                            onConfigChange(config.copy(enabledBiometrics = newBiometrics))
                        },
                    )
                    Text(biometric)
                    Button(onClick = { onTestBiometric(biometric) }) {
                        Text("Test")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("PIN Code")
                Switch(
                    checked = config.enablePinCode,
                    onCheckedChange = { onConfigChange(config.copy(enablePinCode = it)) },
                )
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
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text("Secure Storage")
            Text("Encryption: ${if (encryptionEnabled) "Enabled" else "Disabled"}")

            if (showDataPreview) {
                Text("Stored Data:")
                data.forEach { (k, v) ->
                    Text("$k: $v")
                }
            }

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

            Button(
                onClick = {
                    val newData = data.toMutableMap()
                    newData[key] = value
                    onDataChange(newData)
                },
            ) {
                Text("Store Data")
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
    var selectedItems by remember {
        mutableStateOf(emptyList<String>())
    }
    Card(
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(title)
            Text(description, style = MaterialTheme.typography.bodyMedium)

            consentItems.forEach { item ->
                Row {
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
        Text("iOS Security Dashboard", style = MaterialTheme.typography.titleLarge)

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

private fun calculatePasswordStrength(
    password: String,
    minLength: Int,
    requireUppercase: Boolean,
    requireLowercase: Boolean,
    requireNumbers: Boolean,
    requireSymbols: Boolean,
): PasswordStrength {
    var score = 0

    if (password.length >= minLength) score++
    if (!requireUppercase || password.any { it.isUpperCase() }) score++
    if (!requireLowercase || password.any { it.isLowerCase() }) score++
    if (!requireNumbers || password.any { it.isDigit() }) score++
    if (!requireSymbols || password.any { !it.isLetterOrDigit() }) score++

    return when {
        score >= 4 -> PasswordStrength.STRONG
        score >= 3 -> PasswordStrength.GOOD
        score >= 2 -> PasswordStrength.FAIR
        else -> PasswordStrength.VERY_WEAK
    }
}
