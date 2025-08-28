package com.unify.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.unify.sample.UnifySampleApp
import com.unify.ui.theme.UnifyTheme

/**
 * Android平台主Activity
 * 集成Unify KMP框架的示例应用
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            UnifyAndroidApp()
        }
    }
}

@Composable
fun UnifyAndroidApp() {
    UnifyTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize()
            ) { paddingValues ->
                // 使用共享的示例应用
                UnifySampleApp()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UnifyAndroidAppPreview() {
    UnifyAndroidApp()
}
