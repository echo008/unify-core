package com.unify.ui.components.security

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class KeyboardType { NUMERIC, ALPHANUMERIC, SYMBOLS }
enum class PasswordStrength { WEAK, MEDIUM, STRONG, VERY_STRONG }
enum class AuthMethod { PIN, BIOMETRIC, PASSWORD, TWO_FACTOR }
enum class BiometricType { FINGERPRINT, FACE, VOICE }
enum class BiometricResult { SUCCESS, FAILURE, CANCELLED }
enum class EncryptionLevel { NONE, BASIC, ADVANCED, MILITARY }

data class PasswordRequirement(val type: String, val met: Boolean)
data class SecuritySettings(val enableBiometric: Boolean, val requirePin: Boolean)
data class SecureStorageItem(val id: String, val name: String, val type: String)
data class ConsentItem(val id: String, val title: String, val required: Boolean)
data class SecurityAuditResult(val id: String, val issue: String, val severity: String)
data class AlertAction(val id: String, val label: String, val action: () -> Unit)

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
    // Native平台密码输入组件实现
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
    // Native平台PIN码输入组件实现
}

@Composable
actual fun UnifySecureKeyboard(
    onKeyPressed: (String) -> Unit,
    modifier: Modifier,
    keyboardType: SecureKeyboardType,
    randomizeLayout: Boolean,
    showDeleteKey: Boolean
) {
    // Native平台安全键盘组件实现
}

@Composable
actual fun UnifyPasswordStrengthIndicator(
    password: String,
    modifier: Modifier,
    showText: Boolean,
    colors: PasswordStrengthColors
) {
    // Native平台密码强度指示器组件实现
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
    // Native平台双因素认证组件实现
}

@Composable
actual fun UnifySecuritySettings(
    config: SecurityConfig,
    onConfigChange: (SecurityConfig) -> Unit,
    modifier: Modifier,
    availableBiometrics: Set<String>,
    onTestBiometric: (String) -> Unit
) {
    // Native平台安全设置组件实现
}

@Composable
actual fun UnifySecureStorage(
    data: Map<String, String>,
    onDataChange: (Map<String, String>) -> Unit,
    modifier: Modifier,
    encryptionEnabled: Boolean,
    showDataPreview: Boolean
) {
    // Native平台安全存储组件实现
}

@Composable
actual fun UnifyPrivacyConsent(
    consentItems: List<String>,
    onConsentChange: (List<String>) -> Unit,
    modifier: Modifier,
    title: String,
    description: String,
    allowPartialConsent: Boolean
) {
    // Native平台隐私同意组件实现
}

@Composable
actual fun UnifyBiometricAuth(
    onAuthResult: (Boolean) -> Unit,
    modifier: Modifier,
    title: String,
    subtitle: String,
    negativeButtonText: String,
    enabledBiometrics: Set<String>
) {
    // Native平台生物识别认证组件实现
}

// 删除不匹配的actual实现，这些函数在commonMain中没有对应的expect声明
