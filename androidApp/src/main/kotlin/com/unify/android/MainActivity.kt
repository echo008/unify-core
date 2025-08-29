package com.unify.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.unify.android.di.AndroidDI
import com.unify.examples.HelloWorldScreen
import com.unify.examples.initializeHelloWorldTranslations
import com.unify.ui.theme.UnifyTheme

/**
 * Android平台主Activity
 * 集成Unify KMP框架的示例应用
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化依赖注入
        AndroidDI.initialize(this)
        
        // 初始化Hello World翻译
        initializeHelloWorldTranslations()
        
        setContent {
            UnifyTheme {
                HelloWorldScreen()
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    UnifyTheme {
        HelloWorldScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun UnifyAndroidAppPreview() {
    UnifyTheme {
        HelloWorldScreen()
    }
}
