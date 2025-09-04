package com.unify.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

@Composable
actual fun UnifyText(
    text: String,
    modifier: Modifier,
    color: Color,
    fontSize: TextUnit,
    fontWeight: FontWeight?,
    textAlign: TextAlign?,
    overflow: TextOverflow,
    maxLines: Int,
    style: TextStyle?
) {
    Text(
        text = text,
        modifier = modifier,
        color = if (color != Color.Unspecified) color else Color.Unspecified,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        style = style ?: MaterialTheme.typography.bodyMedium
    )
}

@Composable
actual fun UnifyHeadlineText(
    text: String,
    modifier: Modifier,
    color: Color,
    textAlign: TextAlign?
) {
    Text(
        text = text,
        modifier = modifier,
        color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurface,
        textAlign = textAlign,
        style = MaterialTheme.typography.headlineMedium
    )
}

@Composable
actual fun UnifyBodyText(
    text: String,
    modifier: Modifier,
    color: Color,
    textAlign: TextAlign?,
    maxLines: Int
) {
    Text(
        text = text,
        modifier = modifier,
        color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurface,
        textAlign = textAlign,
        maxLines = maxLines,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
actual fun UnifyLabelText(
    text: String,
    modifier: Modifier,
    color: Color,
    textAlign: TextAlign?
) {
    Text(
        text = text,
        modifier = modifier,
        color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = textAlign,
        style = MaterialTheme.typography.labelMedium
    )
}
