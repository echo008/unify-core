package com.unify.ui.components.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.unify.core.types.AuthenticationResult
import com.unify.core.types.BiometricType

/**
 * Desktop平台生物识别认证组件
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = {
                        onAuthResult(
                            AuthenticationResult(
                                isSuccess = true,
                                biometricType = BiometricType.FINGERPRINT.name,
                                errorMessage = null,
                            ),
                        )
                    },
                ) {
                    Text("模拟成功")
                }

                OutlinedButton(
                    onClick = {
                        onAuthResult(
                            AuthenticationResult(
                                isSuccess = false,
                                biometricType = null,
                                errorMessage = "用户取消",
                            ),
                        )
                    },
                ) {
                    Text(negativeButtonText)
                }
            }
        }
    }
}

/**
 * Desktop平台密码输入组件
 */
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

    Column(modifier = modifier) {
        OutlinedTextField(
            value = password,
            onValueChange = {
                onPasswordChange(it)
                onValidationResult(it.length >= 6, if (it.length < 6) "密码长度至少6位" else null)
            },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon =
                if (enableToggleVisibility) {
                    {
                        TextButton(
                            onClick = { isPasswordVisible = !isPasswordVisible },
                        ) {
                            Text(if (isPasswordVisible) "隐藏" else "显示")
                        }
                    }
                } else {
                    null
                },
            modifier = Modifier.fillMaxWidth(),
        )

        if (showStrengthIndicator) {
            UnifyPasswordStrengthIndicator(
                password = password,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/**
 * Desktop平台PIN码输入组件
 */
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
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OutlinedTextField(
            value = pinCode,
            onValueChange = { newValue ->
                if (newValue.length <= length && newValue.all { it.isDigit() }) {
                    onPinCodeChange(newValue)
                    if (newValue.length == length) {
                        onComplete(newValue)
                    }
                }
            },
            label = { Text("PIN码") },
            visualTransformation = if (maskInput) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.fillMaxWidth(),
        )

        if (showKeypad) {
            Spacer(modifier = Modifier.height(16.dp))
            UnifySecureKeyboard(
                onKeyPressed = { key ->
                    when (key) {
                        "DELETE" -> {
                            if (pinCode.isNotEmpty()) {
                                onPinCodeChange(pinCode.dropLast(1))
                            }
                        }
                        else -> {
                            if (pinCode.length < length) {
                                onPinCodeChange(pinCode + key)
                            }
                        }
                    }
                },
                keyboardType = SecureKeyboardType.NUMERIC,
            )
        }
    }
}

/**
 * Desktop平台安全键盘组件
 */
@Composable
actual fun UnifySecureKeyboard(
    onKeyPressed: (String) -> Unit,
    modifier: Modifier,
    keyboardType: SecureKeyboardType,
    randomizeLayout: Boolean,
    showDeleteKey: Boolean,
) {
    val keys =
        when (keyboardType) {
            SecureKeyboardType.NUMERIC -> listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
            SecureKeyboardType.ALPHANUMERIC -> ('A'..'Z').map { it.toString() } + ('0'..'9').map { it.toString() }
            SecureKeyboardType.ALPHABETIC -> ('A'..'Z').map { it.toString() }
        }

    val displayKeys = if (randomizeLayout) keys.shuffled() else keys

    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(8.dp),
        ) {
            displayKeys.chunked(3).forEach { rowKeys ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    rowKeys.forEach { key ->
                        Button(
                            onClick = { onKeyPressed(key) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(key)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (showDeleteKey) {
                Button(
                    onClick = { onKeyPressed("DELETE") },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("删除")
                }
            }
        }
    }
}

/**
 * Desktop平台密码强度指示器组件
 */
@Composable
actual fun UnifyPasswordStrengthIndicator(
    password: String,
    modifier: Modifier,
    showText: Boolean,
    colors: PasswordStrengthColors,
) {
    val strength = calculatePasswordStrength(password)
    val strengthText =
        when (strength) {
            0 -> "很弱"
            1 -> "弱"
            2 -> "中等"
            3 -> "强"
            else -> "很强"
        }

    val strengthColor =
        when (strength) {
            0 -> colors.weakColor
            1 -> colors.weakColor
            2 -> colors.mediumColor
            3 -> colors.strongColor
            else -> colors.veryStrongColor
        }

    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress = (strength + 1) / 5f,
            color = strengthColor,
            modifier = Modifier.fillMaxWidth(),
        )

        if (showText) {
            Text(
                text = "密码强度: $strengthText",
                style = MaterialTheme.typography.bodySmall,
                color = strengthColor,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

/**
 * Desktop平台两步验证组件
 */
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

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(16.dp))

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
                label = { Text("验证码") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onResendCode,
                enabled = resendEnabled && countdown == 0,
            ) {
                Text(
                    if (countdown > 0) {
                        "重发验证码 (${countdown}s)"
                    } else {
                        "重发验证码"
                    },
                )
            }
        }
    }
}

/**
 * Desktop平台安全仪表板组件
 */
@Composable
actual fun UnifySecurityDashboard(
    config: SecurityConfig,
    onConfigChange: (SecurityConfig) -> Unit,
    modifier: Modifier,
    availableBiometrics: Set<String>,
    onTestBiometric: (String) -> Unit,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "安全设置",
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("启用生物识别")
                Switch(
                    checked = config.enableBiometric,
                    onCheckedChange = {
                        onConfigChange(config.copy(enableBiometric = it))
                    },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("启用PIN码")
                Switch(
                    checked = config.enablePinCode,
                    onCheckedChange = {
                        onConfigChange(config.copy(enablePinCode = it))
                    },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("强密码要求")
                Switch(
                    checked = config.requireStrongPassword,
                    onCheckedChange = {
                        onConfigChange(config.copy(requireStrongPassword = it))
                    },
                )
            }
        }
    }
}

/**
 * Desktop平台安全设置组件
 */
@Composable
actual fun UnifySecuritySettings(
    config: SecurityConfig,
    onConfigChange: (SecurityConfig) -> Unit,
    modifier: Modifier,
    availableBiometrics: Set<String>,
    onTestBiometric: (String) -> Unit,
) {
    UnifySecurityDashboard(
        config = config,
        onConfigChange = onConfigChange,
        modifier = modifier,
        availableBiometrics = availableBiometrics,
        onTestBiometric = onTestBiometric,
    )
}

/**
 * Desktop平台安全存储组件
 */
@Composable
actual fun UnifySecureStorage(
    data: Map<String, String>,
    onDataChange: (Map<String, String>) -> Unit,
    modifier: Modifier,
    encryptionEnabled: Boolean,
    showDataPreview: Boolean,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "安全存储",
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("存储项目数量: ${data.size}")

            if (showDataPreview && data.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                data.entries.take(3).forEach { (key, value) ->
                    Text(
                        text = "$key: ${if (encryptionEnabled) "***" else value}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

/**
 * Desktop平台隐私同意组件
 */
@Composable
actual fun UnifyPrivacyConsent(
    consentItems: List<String>,
    onConsentChange: (List<String>) -> Unit,
    modifier: Modifier,
    title: String,
    description: String,
    allowPartialConsent: Boolean,
) {
    var selectedItems by remember { mutableStateOf(emptySet<String>()) }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(16.dp))

            consentItems.forEach { item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
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
                            onConsentChange(selectedItems.toList())
                        },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(item)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

/**
 * 计算密码强度
 */
private fun calculatePasswordStrength(password: String): Int {
    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++
    return score
}
