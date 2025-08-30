package com.unify.demo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.unify.core.ui.components.*
import com.unify.core.mvi.*
import com.unify.core.performance.UnifyComposeOptimizer.PerformanceTracker
import kotlinx.coroutines.CoroutineScope

/**
 * 用户资料屏幕 - 展示表单处理和数据验证
 */
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val viewModel = remember { ProfileViewModel(scope) }
    
    PerformanceTracker("ProfileScreen") {
        UnifyMVIContainer(
            stateManager = viewModel,
            onEffect = { effect ->
                when (effect) {
                    is ProfileEffect.ShowMessage -> {
                        // 显示消息
                    }
                    is ProfileEffect.ValidationError -> {
                        // 显示验证错误
                    }
                }
            }
        ) { state, isLoading, onIntent ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 顶部栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UnifyButton(
                        onClick = onNavigateBack,
                        text = "← 返回"
                    )
                    Text(
                        text = "用户资料",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.width(80.dp))
                }
                
                // 头像区域
                UnifyCard {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "👤",
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "用户头像",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        UnifyButton(
                            onClick = { onIntent(ProfileIntent.ChangeAvatar) },
                            text = "更换头像"
                        )
                    }
                }
                
                // 基本信息表单
                UnifyCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📝 基本信息",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        UnifyTextField(
                            value = state.profile.name,
                            onValueChange = { onIntent(ProfileIntent.UpdateName(it)) },
                            label = "姓名",
                            isError = state.nameError != null
                        )
                        
                        state.nameError?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        UnifyTextField(
                            value = state.profile.email,
                            onValueChange = { onIntent(ProfileIntent.UpdateEmail(it)) },
                            label = "邮箱",
                            isError = state.emailError != null
                        )
                        
                        state.emailError?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        UnifyTextField(
                            value = state.profile.phone,
                            onValueChange = { onIntent(ProfileIntent.UpdatePhone(it)) },
                            label = "手机号",
                            isError = state.phoneError != null
                        )
                        
                        state.phoneError?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                // 个人简介
                UnifyCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📄 个人简介",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        UnifyTextField(
                            value = state.profile.bio,
                            onValueChange = { onIntent(ProfileIntent.UpdateBio(it)) },
                            placeholder = "介绍一下自己吧...",
                            singleLine = false
                        )
                    }
                }
                
                // 保存按钮
                UnifyButton(
                    onClick = { onIntent(ProfileIntent.SaveProfile) },
                    text = if (isLoading) "保存中..." else "保存资料",
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * 用户资料 MVI 实现
 */
data class ProfileState(
    val profile: UserProfile = UserProfile(),
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null
) : UnifyState

sealed class ProfileIntent : UnifyIntent {
    data class UpdateName(val name: String) : ProfileIntent()
    data class UpdateEmail(val email: String) : ProfileIntent()
    data class UpdatePhone(val phone: String) : ProfileIntent()
    data class UpdateBio(val bio: String) : ProfileIntent()
    object ChangeAvatar : ProfileIntent()
    object SaveProfile : ProfileIntent()
    object LoadProfile : ProfileIntent()
}

sealed class ProfileEffect : UnifyEffect {
    data class ShowMessage(val message: String) : ProfileEffect()
    data class ValidationError(val field: String, val error: String) : ProfileEffect()
}

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val bio: String = "",
    val avatarUrl: String = ""
)

class ProfileViewModel(scope: CoroutineScope) : UnifyViewModel<ProfileIntent, ProfileState, ProfileEffect>(
    initialState = ProfileState(),
    scope = scope
) {
    
    init {
        handleIntent(ProfileIntent.LoadProfile)
    }
    
    override fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.UpdateName -> {
                updateState { state ->
                    state.copy(
                        profile = state.profile.copy(name = intent.name),
                        nameError = validateName(intent.name)
                    )
                }
            }
            
            is ProfileIntent.UpdateEmail -> {
                updateState { state ->
                    state.copy(
                        profile = state.profile.copy(email = intent.email),
                        emailError = validateEmail(intent.email)
                    )
                }
            }
            
            is ProfileIntent.UpdatePhone -> {
                updateState { state ->
                    state.copy(
                        profile = state.profile.copy(phone = intent.phone),
                        phoneError = validatePhone(intent.phone)
                    )
                }
            }
            
            is ProfileIntent.UpdateBio -> {
                updateState { state ->
                    state.copy(
                        profile = state.profile.copy(bio = intent.bio)
                    )
                }
            }
            
            ProfileIntent.ChangeAvatar -> {
                sendEffect(ProfileEffect.ShowMessage("头像更换功能开发中"))
            }
            
            ProfileIntent.SaveProfile -> {
                val currentState = state.value
                if (isValidProfile(currentState)) {
                    handleAsyncIntent(intent) {
                        // 模拟保存
                        kotlinx.coroutines.delay(2000)
                        sendEffect(ProfileEffect.ShowMessage("资料保存成功"))
                    }
                } else {
                    sendEffect(ProfileEffect.ShowMessage("请检查输入信息"))
                }
            }
            
            ProfileIntent.LoadProfile -> {
                handleAsyncIntent(intent) {
                    // 模拟加载用户资料
                    kotlinx.coroutines.delay(1000)
                    val profile = UserProfile(
                        id = "1",
                        name = "张三",
                        email = "zhangsan@example.com",
                        phone = "13800138000",
                        bio = "这是一个示例用户资料"
                    )
                    updateState { state ->
                        state.copy(profile = profile)
                    }
                }
            }
        }
    }
    
    private fun validateName(name: String): String? {
        return when {
            name.isBlank() -> "姓名不能为空"
            name.length < 2 -> "姓名至少需要2个字符"
            name.length > 20 -> "姓名不能超过20个字符"
            else -> null
        }
    }
    
    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "邮箱不能为空"
            !email.contains("@") -> "邮箱格式不正确"
            !email.contains(".") -> "邮箱格式不正确"
            else -> null
        }
    }
    
    private fun validatePhone(phone: String): String? {
        return when {
            phone.isBlank() -> "手机号不能为空"
            phone.length != 11 -> "手机号必须是11位数字"
            !phone.all { it.isDigit() } -> "手机号只能包含数字"
            else -> null
        }
    }
    
    private fun isValidProfile(state: ProfileState): Boolean {
        return state.nameError == null && 
               state.emailError == null && 
               state.phoneError == null &&
               state.profile.name.isNotBlank() &&
               state.profile.email.isNotBlank() &&
               state.profile.phone.isNotBlank()
    }
}
