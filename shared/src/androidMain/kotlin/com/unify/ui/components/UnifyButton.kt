package com.unify.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun UnifyButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    contentPadding: PaddingValues
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (backgroundColor != Color.Unspecified) backgroundColor else MaterialTheme.colorScheme.primary,
            contentColor = if (contentColor != Color.Unspecified) contentColor else MaterialTheme.colorScheme.onPrimary
        ),
        contentPadding = contentPadding
    ) {
        Text(text = text)
    }
}

@Composable
actual fun UnifyIconButton(
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        content = content
    )
}

@Composable
actual fun UnifyFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier,
    backgroundColor: Color,
    contentColor: Color,
    content: @Composable () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = if (backgroundColor != Color.Unspecified) backgroundColor else FloatingActionButtonDefaults.containerColor,
        contentColor = if (contentColor != Color.Unspecified) contentColor else MaterialTheme.colorScheme.onPrimaryContainer,
        content = content
    )
}
