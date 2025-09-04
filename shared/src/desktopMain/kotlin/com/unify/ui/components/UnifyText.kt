package com.unify.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Desktop平台统一文本组件
 * 针对桌面端优化的文本显示
 */
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
    style: TextStyle
) {
    // Desktop平台字体缩放因子
    val desktopFontScaleFactor = 1.0f
    
    val adjustedFontSize = if (fontSize != TextUnit.Unspecified) {
        fontSize * desktopFontScaleFactor
    } else {
        14.sp * desktopFontScaleFactor
    }
    
    val desktopTextStyle = style.copy(
        fontSize = adjustedFontSize,
        color = if (color != Color.Unspecified) color else Color(0xFF212121),
        fontWeight = fontWeight ?: FontWeight.Normal,
        textAlign = textAlign ?: TextAlign.Start
    )
    
    Text(
        text = text,
        modifier = modifier,
        style = desktopTextStyle,
        overflow = overflow,
        maxLines = if (maxLines == Int.MAX_VALUE) Int.MAX_VALUE else maxLines
    )
}
