package com.unify.examples

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.ui.state.*
import com.unify.ui.theme.UnifyTheme
import com.unify.i18n.UnifyI18n
import com.unify.i18n.Locale
import com.unify.i18n.rememberI18nString
import kotlinx.coroutines.flow.StateFlow

/**
 * Hello World 示例应用
 * 展示 Unify KMP 框架的核心功能
 */

// 状态定义
data class HelloWorldState(
    val message: String = "Hello, Unify KMP!",
    val counter: Int = 0,
    val currentLanguage: Locale = Locale.CHINESE,
    val isLoading: Boolean = false
) : State

// 意图定义
sealed class HelloWorldIntent : Intent {
    object IncrementCounter : HelloWorldIntent()
    object DecrementCounter : HelloWorldIntent()
    data class ChangeLanguage(val locale: Locale) : HelloWorldIntent()
    object ResetCounter : HelloWorldIntent()
}

// 副作用定义
sealed class HelloWorldEffect : Effect {
    data class ShowToast(val message: String) : HelloWorldEffect()
    object CounterResetComplete : HelloWorldEffect()
}

// Reducer实现
class HelloWorldReducer : Reducer<HelloWorldState, HelloWorldIntent> {
    override fun reduce(state: HelloWorldState, intent: HelloWorldIntent): HelloWorldState {
        return when (intent) {
            is HelloWorldIntent.IncrementCounter -> {
                state.copy(counter = state.counter + 1)
            }
            is HelloWorldIntent.DecrementCounter -> {
                state.copy(counter = maxOf(0, state.counter - 1))
            }
            is HelloWorldIntent.ChangeLanguage -> {
                state.copy(currentLanguage = intent.locale)
            }
            is HelloWorldIntent.ResetCounter -> {
                state.copy(counter = 0)
            }
        }
    }
}

// 中间件实现
class HelloWorldMiddleware : Middleware<HelloWorldState, HelloWorldIntent, HelloWorldEffect> {
    override suspend fun process(
        state: HelloWorldState,
        intent: HelloWorldIntent,
        sendEffect: suspend (HelloWorldEffect) -> Unit
    ) {
        when (intent) {
            is HelloWorldIntent.IncrementCounter -> {
                if (state.counter + 1 == 10) {
                    sendEffect(HelloWorldEffect.ShowToast("恭喜达到10次点击！"))
                }
            }
            is HelloWorldIntent.ResetCounter -> {
                sendEffect(HelloWorldEffect.CounterResetComplete)
            }
            is HelloWorldIntent.ChangeLanguage -> {
                UnifyI18n.setLocale(intent.locale)
            }
            else -> { /* 其他意图不需要处理副作用 */ }
        }
    }
}

// ViewModel实现
class HelloWorldViewModel : UnifyViewModel<HelloWorldState, HelloWorldIntent, HelloWorldEffect>() {
    
    override fun createInitialState(): HelloWorldState = HelloWorldState()
    
    override fun createReducer(): Reducer<HelloWorldState, HelloWorldIntent> = HelloWorldReducer()
    
    override fun createMiddleware(): List<Middleware<HelloWorldState, HelloWorldIntent, HelloWorldEffect>> {
        return listOf(HelloWorldMiddleware())
    }
    
    init {
        // 初始化国际化系统
        UnifyI18n.initialize(Locale.CHINESE)
    }
}

/**
 * Hello World 主界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelloWorldScreen(
    viewModel: HelloWorldViewModel = remember { HelloWorldViewModel() }
) {
    val state by viewModel.stateFlow.collectAsState()
    
    // 处理副作用
    LaunchedEffect(viewModel) {
        viewModel.effectFlow.collect { effect ->
            when (effect) {
                is HelloWorldEffect.ShowToast -> {
                    println("Toast: ${effect.message}")
                }
                is HelloWorldEffect.CounterResetComplete -> {
                    println("计数器已重置")
                }
            }
        }
    }
    
    UnifyTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                
                // 标题
                Text(
                    text = rememberI18nString("app.name"),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // 欢迎消息
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = rememberI18nString("hello.welcome"),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = rememberI18nString("hello.description"),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // 计数器部分
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = rememberI18nString("counter.title"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 计数显示
                        Text(
                            text = state.counter.toString(),
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // 按钮行
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 减少按钮
                            Button(
                                onClick = { 
                                    viewModel.sendIntent(HelloWorldIntent.DecrementCounter)
                                },
                                enabled = state.counter > 0,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text("-", fontSize = 20.sp)
                            }
                            
                            // 增加按钮
                            Button(
                                onClick = { 
                                    viewModel.sendIntent(HelloWorldIntent.IncrementCounter)
                                }
                            ) {
                                Text("+", fontSize = 20.sp)
                            }
                            
                            // 重置按钮
                            OutlinedButton(
                                onClick = { 
                                    viewModel.sendIntent(HelloWorldIntent.ResetCounter)
                                },
                                enabled = state.counter > 0
                            ) {
                                Text(rememberI18nString("common.reset"))
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // 语言切换部分
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = rememberI18nString("language.title"),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 语言选择按钮
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            LanguageButton(
                                text = "中文",
                                isSelected = state.currentLanguage == Locale.CHINESE,
                                onClick = { 
                                    viewModel.sendIntent(HelloWorldIntent.ChangeLanguage(Locale.CHINESE))
                                }
                            )
                            
                            LanguageButton(
                                text = "English",
                                isSelected = state.currentLanguage == Locale.ENGLISH,
                                onClick = { 
                                    viewModel.sendIntent(HelloWorldIntent.ChangeLanguage(Locale.ENGLISH))
                                }
                            )
                            
                            LanguageButton(
                                text = "日本語",
                                isSelected = state.currentLanguage == Locale.JAPANESE,
                                onClick = { 
                                    viewModel.sendIntent(HelloWorldIntent.ChangeLanguage(Locale.JAPANESE))
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // 框架信息
                Text(
                    text = rememberI18nString("framework.info"),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun LanguageButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    if (isSelected) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text, fontSize = 12.sp)
        }
    } else {
        OutlinedButton(onClick = onClick) {
            Text(text, fontSize = 12.sp)
        }
    }
}

/**
 * 添加Hello World相关的翻译
 */
fun initializeHelloWorldTranslations() {
    // 中文翻译
    UnifyI18n.addTranslations(Locale.CHINESE, mapOf(
        "hello.welcome" to "欢迎使用 Unify KMP！",
        "hello.description" to "这是一个跨平台开发框架示例",
        "counter.title" to "计数器演示",
        "language.title" to "语言切换",
        "framework.info" to "基于 Kotlin Multiplatform 构建",
        "common.reset" to "重置"
    ))
    
    // 英文翻译
    UnifyI18n.addTranslations(Locale.ENGLISH, mapOf(
        "hello.welcome" to "Welcome to Unify KMP!",
        "hello.description" to "This is a cross-platform development framework example",
        "counter.title" to "Counter Demo",
        "language.title" to "Language Switch",
        "framework.info" to "Built with Kotlin Multiplatform",
        "common.reset" to "Reset"
    ))
    
    // 日文翻译
    UnifyI18n.addTranslations(Locale.JAPANESE, mapOf(
        "hello.welcome" to "Unify KMP へようこそ！",
        "hello.description" to "これはクロスプラットフォーム開発フレームワークの例です",
        "counter.title" to "カウンターデモ",
        "language.title" to "言語切り替え",
        "framework.info" to "Kotlin Multiplatform で構築",
        "common.reset" to "リセット"
    ))
}
