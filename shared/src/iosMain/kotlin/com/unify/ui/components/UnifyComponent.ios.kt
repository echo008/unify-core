package com.unify.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle

@Composable
actual fun UnifyView(
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) { content() }
}

@Composable
actual fun UnifyText(
    text: String,
    modifier: Modifier,
    style: TextStyle,
    color: Color
) {
    Text(text = text, modifier = modifier, style = style, color = color)
}

@Composable
actual fun UnifyButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    content: @Composable RowScope.() -> Unit
) {
    Button(onClick = onClick, modifier = modifier, enabled = enabled, content = content)
}

@Composable
actual fun UnifyImage(
    src: String,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale
) {
    // 占位实现
    Box(modifier = modifier)
}

@Composable
actual fun UnifyInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    placeholder: String,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = { Text(placeholder) },
        enabled = enabled
    )
}
