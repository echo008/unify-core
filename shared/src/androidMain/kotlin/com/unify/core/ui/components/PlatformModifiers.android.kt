package com.unify.core.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp

/**
 * Android平台特定的Modifier扩展
 * 提供Android原生的交互效果和行为
 */

/**
 * Android平台点击效果
 */
@Composable
actual fun Modifier.platformClickable(
    enabled: Boolean,
    onClick: () -> Unit,
): Modifier {
    val haptic = LocalHapticFeedback.current

    return this.clickable(
        enabled = enabled,
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = {
            if (enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
        },
    )
}

/**
 * Android平台长按效果
 */
@Composable
actual fun Modifier.platformLongClickable(
    enabled: Boolean,
    onLongClick: () -> Unit,
): Modifier {
    val haptic = LocalHapticFeedback.current

    return this.pointerInput(enabled) {
        if (enabled) {
            detectTapGestures(
                onLongPress = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
            )
        }
    }
}

/**
 * Android平台阴影效果
 */
@Composable
actual fun Modifier.platformShadow(
    elevation: Dp,
    shape: Shape,
    clip: Boolean,
): Modifier {
    return this
        .let { if (clip) it.clip(shape) else it }
        .then(
            background(
                MaterialTheme.colorScheme.surface,
                shape,
            ),
        )
}

/**
 * Android平台圆角效果
 */
@Composable
actual fun Modifier.platformRoundedCorners(radius: Dp): Modifier {
    return this.clip(RoundedCornerShape(radius))
}

/**
 * Android平台边框效果
 */
@Composable
actual fun Modifier.platformBorder(
    width: Dp,
    color: Color,
    shape: Shape,
): Modifier {
    return this.then(
        border(
            width = width,
            color = color,
            shape = shape,
        ),
    )
}

/**
 * Android平台内边距
 */
@Composable
actual fun Modifier.platformPadding(
    horizontal: Dp,
    vertical: Dp,
): Modifier {
    return this.padding(horizontal = horizontal, vertical = vertical)
}

/**
 * Android平台尺寸
 */
@Composable
actual fun Modifier.platformSize(
    width: Dp,
    height: Dp,
): Modifier {
    return this.size(width = width, height = height)
}

/**
 * Android平台触摸反馈
 */
@Composable
actual fun Modifier.platformTouchFeedback(enabled: Boolean): Modifier {
    val haptic = LocalHapticFeedback.current

    return this.pointerInput(enabled) {
        if (enabled) {
            detectTapGestures(
                onPress = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                },
            )
        }
    }
}

/**
 * Android平台可访问性支持
 */
@Composable
actual fun Modifier.platformAccessibility(
    contentDescription: String?,
    role: String?,
): Modifier {
    return this.then(
        semantics {
            contentDescription?.let { desc ->
                this.contentDescription = desc
            }
            role?.let { r ->
                when (r) {
                    "button" -> this.role = Role.Button
                    "checkbox" -> this.role = Role.Checkbox
                    "switch" -> this.role = Role.Switch
                    "radiobutton" -> this.role = Role.RadioButton
                    "tab" -> this.role = Role.Tab
                    "image" -> this.role = Role.Image
                }
            }
        },
    )
}

/**
 * Android平台滚动行为
 */
@Composable
actual fun Modifier.platformScrollable(enabled: Boolean): Modifier {
    return if (enabled) {
        this.then(
            verticalScroll(
                rememberScrollState(),
            ),
        )
    } else {
        this
    }
}

/**
 * Android平台动画效果
 */
@Composable
actual fun Modifier.platformAnimated(enabled: Boolean): Modifier {
    return if (enabled) {
        this.then(
            animateContentSize(
                animationSpec =
                    spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
            ),
        )
    } else {
        this
    }
}
