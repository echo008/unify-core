package com.unify.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.RowScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import org.jetbrains.compose.web.attributes.disabled
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Img
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.P
import org.jetbrains.compose.web.dom.Text

private object DummyRowScope : RowScope

@Composable
actual fun UnifyView(
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    Div { content() }
}

@Composable
actual fun UnifyText(
    text: String,
    modifier: Modifier,
    style: TextStyle,
    color: Color
) {
    P { Text(text) }
}

@Composable
actual fun UnifyButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    content: @Composable RowScope.() -> Unit
) {
    Button(attrs = {
        if (!enabled) disabled()
        onClick { onClick() }
    }) {
        content(DummyRowScope)
    }
}

@Composable
actual fun UnifyImage(
    src: String,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale
) {
    Img(src = src, attrs = { if (contentDescription != null) attr("alt", contentDescription) })
}

@Composable
actual fun UnifyInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    placeholder: String,
    enabled: Boolean
) {
    Input(value) {
        if (placeholder.isNotEmpty()) placeholder(placeholder)
        if (!enabled) disabled()
        onInput { ev -> onValueChange(ev.value) }
    }
}
