package com.unify.ui.components.security

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.unify.core.types.AuthenticationResult
import com.unify.core.types.BiometricType

/**
 * Unify跨平台安全组件
 * 支持生物识别、密码验证、安全输入等功能
 */

// 使用统一的安全相关类型定义，避免重复声明
// 注意：由于存在重复声明问题，暂时使用String类型替代复杂类型

data class SecurityConfig(
    val enableBiometric: Boolean = true,
    val enablePinCode: Boolean = true,
    val maxAttempts: Int = 3,
    val lockoutDuration: Long = 300000L, // 5 minutes
    val requireStrongPassword: Boolean = true,
    val enabledBiometrics: Set<String> = setOf("FINGERPRINT", "FACE_ID"),
)

@Composable
expect fun UnifyBiometricAuth(
    onAuthResult: (AuthenticationResult) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "生物识别验证",
    subtitle: String = "请使用指纹或面部识别",
    negativeButtonText: String = "取消",
    enabledBiometrics: Set<BiometricType> = setOf(BiometricType.FINGERPRINT, BiometricType.FACE_ID),
)

@Composable
expect fun UnifyPasswordField(
    password: String,
    onPasswordChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "密码",
    placeholder: String = "请输入密码",
    showStrengthIndicator: Boolean = true,
    enableToggleVisibility: Boolean = true,
    onValidationResult: (Boolean, String?) -> Unit = { _, _ -> },
)

@Composable
expect fun UnifyPinCodeInput(
    pinCode: String,
    onPinCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    maskInput: Boolean = true,
    showKeypad: Boolean = true,
    onComplete: (String) -> Unit = {},
)

@Composable
expect fun UnifySecureKeyboard(
    onKeyPressed: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: SecureKeyboardType = SecureKeyboardType.NUMERIC,
    randomizeLayout: Boolean = true,
    showDeleteKey: Boolean = true,
)

enum class SecureKeyboardType {
    NUMERIC,
    ALPHANUMERIC,
    ALPHABETIC,
}

@Composable
expect fun UnifyPasswordStrengthIndicator(
    password: String,
    modifier: Modifier = Modifier,
    showText: Boolean = true,
    colors: PasswordStrengthColors = PasswordStrengthDefaults.colors(),
)

data class PasswordStrengthColors(
    val weakColor: Color,
    val mediumColor: Color,
    val strongColor: Color,
    val veryStrongColor: Color,
)

object PasswordStrengthDefaults {
    @Composable
    fun colors(
        weakColor: Color = Color.Red,
        mediumColor: Color = Color.Yellow,
        strongColor: Color = Color.Green,
        veryStrongColor: Color = Color.Blue,
    ): PasswordStrengthColors =
        PasswordStrengthColors(
            weakColor = weakColor,
            mediumColor = mediumColor,
            strongColor = strongColor,
            veryStrongColor = veryStrongColor,
        )
}

@Composable
expect fun UnifyTwoFactorAuth(
    onCodeEntered: (String) -> Unit,
    modifier: Modifier = Modifier,
    codeLength: Int = 6,
    title: String = "两步验证",
    subtitle: String = "请输入验证码",
    resendEnabled: Boolean = true,
    onResendCode: () -> Unit = {},
    countdown: Int = 0,
)

@Composable
expect fun UnifySecurityDashboard(
    config: SecurityConfig,
    onConfigChange: (SecurityConfig) -> Unit,
    modifier: Modifier = Modifier,
    availableBiometrics: Set<String> = emptySet(),
    onTestBiometric: (String) -> Unit = {},
)

@Composable
expect fun UnifySecuritySettings(
    config: SecurityConfig,
    onConfigChange: (SecurityConfig) -> Unit,
    modifier: Modifier = Modifier,
    availableBiometrics: Set<String> = emptySet(),
    onTestBiometric: (String) -> Unit = {},
)

@Composable
expect fun UnifySecureStorage(
    data: Map<String, String>,
    onDataChange: (Map<String, String>) -> Unit,
    modifier: Modifier = Modifier,
    encryptionEnabled: Boolean = true,
    showDataPreview: Boolean = false,
)

@Composable
expect fun UnifyPrivacyConsent(
    consentItems: List<String>,
    onConsentChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "隐私协议",
    description: String = "请阅读并同意以下条款",
    allowPartialConsent: Boolean = false,
)
