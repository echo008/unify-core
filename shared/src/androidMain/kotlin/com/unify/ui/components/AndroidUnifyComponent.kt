package com.unify.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import coil.compose.rememberAsyncImagePainter

@Composable
actual fun UnifyView(
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        content()
    }
}

@Composable
actual fun UnifyText(
    text: String,
    modifier: Modifier,
    style: TextStyle,
    color: androidx.compose.ui.graphics.Color
) {
    Text(
        text = text,
        modifier = modifier,
        style = style,
        color = color
    )
}

@Composable
actual fun UnifyButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        content = content
    )
}

@Composable
actual fun UnifyImage(
    src: String,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: androidx.compose.ui.layout.ContentScale
) {
    val painter: Painter = rememberAsyncImagePainter(model = src)
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}

@Composable
actual fun UnifyInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    placeholder: String,
    enabled: Boolean
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = androidx.compose.ui.graphics.Color.Gray
                    )
                }
                innerTextField()
            }
        }
    )
}
