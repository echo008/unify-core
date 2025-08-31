package com.unify.ui.components.security

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import com.unify.ui.components.foundation.*

/**
 * 安全验证类型
 */
enum class UnifySecurityVerificationType {
    PASSWORD,       // 密码
    PIN,           // PIN码
    PATTERN,       // 图案
    BIOMETRIC,     // 生物识别
    TWO_FACTOR,    // 双因子认证
    CAPTCHA        // 验证码
}

/**
 * 安全等级
 */
enum class UnifySecurityLevel {
    LOW,           // 低
    MEDIUM,        // 中
    HIGH,          // 高
    CRITICAL       // 关键
}

/**
 * 密码强度检查组件
 */
@Composable
fun UnifyPasswordStrengthChecker(
    password: String,
    modifier: Modifier = Modifier,
    onStrengthChange: ((UnifySecurityLevel) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    val strength = calculatePasswordStrength(password)
    
    LaunchedEffect(strength) {
        onStrengthChange?.invoke(strength)
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = getSecurityLevelColor(strength),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                UnifyText(
                    text = "密码强度: ${getSecurityLevelName(strength)}",
                    variant = UnifyTextVariant.BODY_MEDIUM,
                    fontWeight = FontWeight.Medium,
                    color = getSecurityLevelColor(strength)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 强度进度条
            LinearProgressIndicator(
                progress = when (strength) {
                    UnifySecurityLevel.LOW -> 0.25f
                    UnifySecurityLevel.MEDIUM -> 0.5f
                    UnifySecurityLevel.HIGH -> 0.75f
                    UnifySecurityLevel.CRITICAL -> 1f
                },
                modifier = Modifier.fillMaxWidth(),
                color = getSecurityLevelColor(strength),
                trackColor = theme.colors.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 密码要求检查
            val requirements = getPasswordRequirements(password)
            requirements.forEach { (requirement, met) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (met) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (met) Color.Green else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    UnifyText(
                        text = requirement,
                        variant = UnifyTextVariant.BODY_SMALL,
                        color = if (met) Color.Green else theme.colors.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 安全验证组件
 */
@Composable
fun UnifySecurityVerification(
    verificationType: UnifySecurityVerificationType,
    modifier: Modifier = Modifier,
    onVerificationSuccess: (() -> Unit)? = null,
    onVerificationFailed: ((String) -> Unit)? = null,
    contentDescription: String? = null
) {
    val theme = LocalUnifyTheme.current
    var isVerifying by remember { mutableStateOf(false) }
    var inputValue by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription?.let { this.contentDescription = it }
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = getVerificationIcon(verificationType),
                contentDescription = null,
                tint = theme.colors.primary,
                modifier = Modifier.size(48.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            UnifyText(
                text = getVerificationTitle(verificationType),
                variant = UnifyTextVariant.H6,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (verificationType) {
                UnifySecurityVerificationType.PASSWORD -> {
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { inputValue = it },
                        label = { UnifyText(text = "密码") },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "隐藏密码" else "显示密码"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                UnifySecurityVerificationType.PIN -> {
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) inputValue = it },
                        label = { UnifyText(text = "PIN码") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                UnifySecurityVerificationType.BIOMETRIC -> {
                    Button(
                        onClick = {
                            isVerifying = true
                            // 触发生物识别
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = theme.colors.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        UnifyText(
                            text = if (isVerifying) "验证中..." else "开始生物识别",
                            color = theme.colors.onPrimary
                        )
                    }
                }
                
                else -> {
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { inputValue = it },
                        label = { UnifyText(text = "验证码") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            if (verificationType != UnifySecurityVerificationType.BIOMETRIC) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        if (inputValue.isNotBlank()) {
                            isVerifying = true
                        }
                    },
                    enabled = inputValue.isNotBlank() && !isVerifying,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = theme.colors.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    UnifyText(
                        text = if (isVerifying) "验证中..." else "验证",
                        color = theme.colors.onPrimary
                    )
                }
            }
        }
    }
    
    LaunchedEffect(isVerifying) {
        if (isVerifying) {
            kotlinx.coroutines.delay(2000)
            val success = (0..1).random() == 1
            if (success) {
                onVerificationSuccess?.invoke()
            } else {
                onVerificationFailed?.invoke("验证失败，请重试")
            }
            isVerifying = false
        }
    }
}

// 辅助函数
private fun calculatePasswordStrength(password: String): UnifySecurityLevel {
    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++
    
    return when (score) {
        0, 1 -> UnifySecurityLevel.LOW
        2, 3 -> UnifySecurityLevel.MEDIUM
        4 -> UnifySecurityLevel.HIGH
        else -> UnifySecurityLevel.CRITICAL
    }
}

private fun getSecurityLevelColor(level: UnifySecurityLevel): Color {
    return when (level) {
        UnifySecurityLevel.LOW -> Color.Red
        UnifySecurityLevel.MEDIUM -> Color.Orange
        UnifySecurityLevel.HIGH -> Color.Green
        UnifySecurityLevel.CRITICAL -> Color.Blue
    }
}

private fun getSecurityLevelName(level: UnifySecurityLevel): String {
    return when (level) {
        UnifySecurityLevel.LOW -> "弱"
        UnifySecurityLevel.MEDIUM -> "中等"
        UnifySecurityLevel.HIGH -> "强"
        UnifySecurityLevel.CRITICAL -> "极强"
    }
}

private fun getVerificationIcon(type: UnifySecurityVerificationType): ImageVector {
    return when (type) {
        UnifySecurityVerificationType.PASSWORD -> Icons.Default.Lock
        UnifySecurityVerificationType.PIN -> Icons.Default.Pin
        UnifySecurityVerificationType.PATTERN -> Icons.Default.Pattern
        UnifySecurityVerificationType.BIOMETRIC -> Icons.Default.Fingerprint
        UnifySecurityVerificationType.TWO_FACTOR -> Icons.Default.Security
        UnifySecurityVerificationType.CAPTCHA -> Icons.Default.Quiz
    }
}

private fun getVerificationTitle(type: UnifySecurityVerificationType): String {
    return when (type) {
        UnifySecurityVerificationType.PASSWORD -> "密码验证"
        UnifySecurityVerificationType.PIN -> "PIN码验证"
        UnifySecurityVerificationType.PATTERN -> "图案验证"
        UnifySecurityVerificationType.BIOMETRIC -> "生物识别验证"
        UnifySecurityVerificationType.TWO_FACTOR -> "双因子验证"
        UnifySecurityVerificationType.CAPTCHA -> "验证码验证"
    }
}

private fun getPasswordRequirements(password: String): List<Pair<String, Boolean>> {
    return listOf(
        "至少8个字符" to (password.length >= 8),
        "包含大写字母" to password.any { it.isUpperCase() },
        "包含小写字母" to password.any { it.isLowerCase() },
        "包含数字" to password.any { it.isDigit() },
        "包含特殊字符" to password.any { !it.isLetterOrDigit() }
    )
}
