package com.unify.ui.components

import androidx.compose.material3.*
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

/**
 * Desktop平台Text组件actual实现
 */

@Composable
actual fun UnifyText(
    text: String,
    modifier: Modifier,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight?,
    textAlign: TextAlign?,
    overflow: TextOverflow,
    maxLines: Int,
    style: TextStyle?,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        style = style ?: LocalTextStyle.current,
    )
}

@Composable
actual fun UnifyHeadlineText(
    text: String,
    modifier: Modifier,
    color: Color,
    textAlign: TextAlign?,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        style = MaterialTheme.typography.headlineLarge,
    )
}

@Composable
actual fun UnifyBodyText(
    text: String,
    modifier: Modifier,
    color: Color,
    textAlign: TextAlign?,
    maxLines: Int,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        style = MaterialTheme.typography.bodyLarge,
    )
}

@Composable
actual fun UnifyLabelText(
    text: String,
    modifier: Modifier,
    color: Color,
    textAlign: TextAlign?,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        textAlign = textAlign,
        style = MaterialTheme.typography.labelLarge,
    )
}
