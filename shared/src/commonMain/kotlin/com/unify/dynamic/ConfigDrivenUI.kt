package com.unify.dynamic

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import com.unify.ui.components.UnifyButton
import com.unify.ui.components.UnifyImage
import com.unify.ui.components.UnifyText
import com.unify.ui.components.UnifyView

class ComponentFactory {
    @Composable
    fun CreateComponent(config: ComponentConfig) {
        when (config.type) {
            "text" -> {
                UnifyText(
                    text = config.props["text"] as? String ?: "",
                    modifier = Modifier.applyStyle(config.style)
                )
            }
            "button" -> {
                UnifyButton(
                    onClick = { /* 事件处理从配置获取 */ },
                    modifier = Modifier.applyStyle(config.style)
                ) {
                    config.children.forEach { childConfig ->
                        CreateComponent(childConfig)
                    }
                }
            }
            "image" -> {
                UnifyImage(
                    src = config.props["src"] as? String ?: "",
                    contentDescription = config.props["alt"] as? String,
                    modifier = Modifier.applyStyle(config.style)
                )
            }
            "view" -> {
                UnifyView(
                    modifier = Modifier.applyStyle(config.style)
                ) {
                    config.children.forEach { childConfig ->
                        CreateComponent(childConfig)
                    }
                }
            }
            else -> {
                Text("未知组件: ${'$'}{config.type}")
            }
        }
    }
}

fun Modifier.applyStyle(style: Map<String, String>): Modifier {
    var modifier = this
    style.forEach { (key, value) ->
        modifier = when (key) {
            "width" -> modifier.width(value.toDp())
            "height" -> modifier.height(value.toDp())
            "padding" -> modifier.padding(value.toDp())
            "backgroundColor" -> modifier.background(Color(value.toColorLong()))
            else -> modifier
        }
    }
    return modifier
}

private fun String.toDp(): Dp = this.removeSuffix("dp").toFloatOrNull()?.dp ?: 0.dp
private fun String.toColorLong(): Long = this.removePrefix("#").toLongOrNull(16) ?: 0xFF000000
