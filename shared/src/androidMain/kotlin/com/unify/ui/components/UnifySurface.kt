package com.unify.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

@Composable
actual fun UnifySurface(
    modifier: Modifier,
    shape: Shape?,
    color: Color,
    contentColor: Color,
    elevation: Dp,
    border: UnifyBorder?,
    content: @Composable () -> Unit
) {
    val borderStroke = border?.let {
        BorderStroke(it.width, it.color)
    }
    
    Surface(
        modifier = modifier,
        shape = shape ?: MaterialTheme.shapes.medium,
        color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.surface,
        contentColor = if (contentColor != Color.Unspecified) contentColor else MaterialTheme.colorScheme.onSurface,
        tonalElevation = elevation,
        border = borderStroke,
        content = content
    )
}

@Composable
actual fun UnifyCard(
    modifier: Modifier,
    elevation: Dp,
    backgroundColor: Color,
    contentColor: Color,
    border: UnifyBorder?,
    content: @Composable () -> Unit
) {
    val borderStroke = border?.let {
        BorderStroke(it.width, it.color)
    }
    
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(
            containerColor = if (backgroundColor != Color.Unspecified) backgroundColor else MaterialTheme.colorScheme.surface,
            contentColor = if (contentColor != Color.Unspecified) contentColor else MaterialTheme.colorScheme.onSurface
        ),
        border = borderStroke,
        content = content
    )
}

@Composable
actual fun UnifyContainer(
    modifier: Modifier,
    backgroundColor: Color,
    padding: PaddingValues,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                if (backgroundColor != Color.Unspecified) backgroundColor else Color.Transparent
            )
            .padding(padding)
    ) {
        content()
    }
}
