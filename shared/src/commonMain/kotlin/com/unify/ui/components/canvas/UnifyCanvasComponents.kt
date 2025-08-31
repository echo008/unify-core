package com.unify.ui.components.canvas

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import com.unify.ui.LocalUnifyTheme
import kotlin.math.*

/**
 * Unify 画布组件
 * 对应微信小程序的 canvas 组件及绘图功能
 */

/**
 * 画布绘制命令
 */
sealed class UnifyCanvasCommand {
    // 基础绘制命令
    data class MoveTo(val x: Float, val y: Float) : UnifyCanvasCommand()
    data class LineTo(val x: Float, val y: Float) : UnifyCanvasCommand()
    data class Arc(val x: Float, val y: Float, val radius: Float, val startAngle: Float, val endAngle: Float, val anticlockwise: Boolean = false) : UnifyCanvasCommand()
    data class ArcTo(val x1: Float, val y1: Float, val x2: Float, val y2: Float, val radius: Float) : UnifyCanvasCommand()
    data class BezierCurveTo(val cp1x: Float, val cp1y: Float, val cp2x: Float, val cp2y: Float, val x: Float, val y: Float) : UnifyCanvasCommand()
    data class QuadraticCurveTo(val cpx: Float, val cpy: Float, val x: Float, val y: Float) : UnifyCanvasCommand()
    data class Rect(val x: Float, val y: Float, val width: Float, val height: Float) : UnifyCanvasCommand()
    object ClosePath : UnifyCanvasCommand()
    
    // 样式命令
    data class SetStrokeStyle(val color: Color) : UnifyCanvasCommand()
    data class SetFillStyle(val color: Color) : UnifyCanvasCommand()
    data class SetLineWidth(val width: Float) : UnifyCanvasCommand()
    data class SetLineCap(val cap: StrokeCap) : UnifyCanvasCommand()
    data class SetLineJoin(val join: StrokeJoin) : UnifyCanvasCommand()
    data class SetLineDash(val segments: List<Float>, val offset: Float = 0f) : UnifyCanvasCommand()
    data class SetGlobalAlpha(val alpha: Float) : UnifyCanvasCommand()
    data class SetShadow(val offsetX: Float, val offsetY: Float, val blur: Float, val color: Color) : UnifyCanvasCommand()
    
    // 绘制命令
    object Stroke : UnifyCanvasCommand()
    object Fill : UnifyCanvasCommand()
    data class FillRect(val x: Float, val y: Float, val width: Float, val height: Float) : UnifyCanvasCommand()
    data class StrokeRect(val x: Float, val y: Float, val width: Float, val height: Float) : UnifyCanvasCommand()
    data class ClearRect(val x: Float, val y: Float, val width: Float, val height: Float) : UnifyCanvasCommand()
    
    // 文本命令
    data class FillText(val text: String, val x: Float, val y: Float, val maxWidth: Float? = null) : UnifyCanvasCommand()
    data class StrokeText(val text: String, val x: Float, val y: Float, val maxWidth: Float? = null) : UnifyCanvasCommand()
    data class SetFont(val font: String) : UnifyCanvasCommand()
    data class SetTextAlign(val align: String) : UnifyCanvasCommand() // left, center, right
    data class SetTextBaseline(val baseline: String) : UnifyCanvasCommand() // top, middle, bottom
    
    // 图像命令
    data class DrawImage(val imageResource: String, val dx: Float, val dy: Float) : UnifyCanvasCommand()
    data class DrawImageScaled(val imageResource: String, val dx: Float, val dy: Float, val dWidth: Float, val dHeight: Float) : UnifyCanvasCommand()
    data class DrawImageCropped(val imageResource: String, val sx: Float, val sy: Float, val sWidth: Float, val sHeight: Float, val dx: Float, val dy: Float, val dWidth: Float, val dHeight: Float) : UnifyCanvasCommand()
    
    // 变换命令
    data class Scale(val x: Float, val y: Float) : UnifyCanvasCommand()
    data class Rotate(val angle: Float) : UnifyCanvasCommand()
    data class Translate(val x: Float, val y: Float) : UnifyCanvasCommand()
    data class Transform(val a: Float, val b: Float, val c: Float, val d: Float, val e: Float, val f: Float) : UnifyCanvasCommand()
    data class SetTransform(val a: Float, val b: Float, val c: Float, val d: Float, val e: Float, val f: Float) : UnifyCanvasCommand()
    
    // 状态命令
    object Save : UnifyCanvasCommand()
    object Restore : UnifyCanvasCommand()
    
    // 路径命令
    object BeginPath : UnifyCanvasCommand()
    data class Clip(val path: Path? = null) : UnifyCanvasCommand()
    
    // 渐变命令
    data class CreateLinearGradient(val x0: Float, val y0: Float, val x1: Float, val y1: Float, val colorStops: List<Pair<Float, Color>>) : UnifyCanvasCommand()
    data class CreateRadialGradient(val x0: Float, val y0: Float, val r0: Float, val x1: Float, val y1: Float, val r1: Float, val colorStops: List<Pair<Float, Color>>) : UnifyCanvasCommand()
    data class CreateCircularGradient(val x: Float, val y: Float, val colorStops: List<Pair<Float, Color>>) : UnifyCanvasCommand()
    
    // 图案命令
    data class CreatePattern(val imageResource: String, val repetition: String) : UnifyCanvasCommand() // repeat, repeat-x, repeat-y, no-repeat
}

/**
 * 画布状态
 */
data class UnifyCanvasState(
    val strokeStyle: Color = Color.Black,
    val fillStyle: Color = Color.Black,
    val lineWidth: Float = 1f,
    val lineCap: StrokeCap = StrokeCap.Butt,
    val lineJoin: StrokeJoin = StrokeJoin.Miter,
    val lineDashOffset: Float = 0f,
    val globalAlpha: Float = 1f,
    val font: String = "10px sans-serif",
    val textAlign: String = "left",
    val textBaseline: String = "top",
    val shadowOffsetX: Float = 0f,
    val shadowOffsetY: Float = 0f,
    val shadowBlur: Float = 0f,
    val shadowColor: Color = Color.Transparent,
    val transform: Matrix = Matrix()
)

/**
 * 画布绘制上下文
 */
class UnifyCanvasContext {
    private val commands = mutableListOf<UnifyCanvasCommand>()
    private val stateStack = mutableListOf<UnifyCanvasState>()
    private var currentState = UnifyCanvasState()
    private var currentPath = Path()
    
    // 路径方法
    fun moveTo(x: Float, y: Float) {
        commands.add(UnifyCanvasCommand.MoveTo(x, y))
        currentPath.moveTo(x, y)
    }
    
    fun lineTo(x: Float, y: Float) {
        commands.add(UnifyCanvasCommand.LineTo(x, y))
        currentPath.lineTo(x, y)
    }
    
    fun arc(x: Float, y: Float, radius: Float, startAngle: Float, endAngle: Float, anticlockwise: Boolean = false) {
        commands.add(UnifyCanvasCommand.Arc(x, y, radius, startAngle, endAngle, anticlockwise))
        currentPath.addArc(Rect(x - radius, y - radius, x + radius, y + radius), startAngle * 180f / PI.toFloat(), (endAngle - startAngle) * 180f / PI.toFloat())
    }
    
    fun arcTo(x1: Float, y1: Float, x2: Float, y2: Float, radius: Float) {
        commands.add(UnifyCanvasCommand.ArcTo(x1, y1, x2, y2, radius))
    }
    
    fun bezierCurveTo(cp1x: Float, cp1y: Float, cp2x: Float, cp2y: Float, x: Float, y: Float) {
        commands.add(UnifyCanvasCommand.BezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y))
        currentPath.cubicTo(cp1x, cp1y, cp2x, cp2y, x, y)
    }
    
    fun quadraticCurveTo(cpx: Float, cpy: Float, x: Float, y: Float) {
        commands.add(UnifyCanvasCommand.QuadraticCurveTo(cpx, cpy, x, y))
        currentPath.quadraticBezierTo(cpx, cpy, x, y)
    }
    
    fun rect(x: Float, y: Float, width: Float, height: Float) {
        commands.add(UnifyCanvasCommand.Rect(x, y, width, height))
        currentPath.addRect(Rect(x, y, x + width, y + height))
    }
    
    fun closePath() {
        commands.add(UnifyCanvasCommand.ClosePath)
        currentPath.close()
    }
    
    fun beginPath() {
        commands.add(UnifyCanvasCommand.BeginPath)
        currentPath = Path()
    }
    
    // 样式方法
    fun setStrokeStyle(color: Color) {
        commands.add(UnifyCanvasCommand.SetStrokeStyle(color))
        currentState = currentState.copy(strokeStyle = color)
    }
    
    fun setFillStyle(color: Color) {
        commands.add(UnifyCanvasCommand.SetFillStyle(color))
        currentState = currentState.copy(fillStyle = color)
    }
    
    fun setLineWidth(width: Float) {
        commands.add(UnifyCanvasCommand.SetLineWidth(width))
        currentState = currentState.copy(lineWidth = width)
    }
    
    fun setLineCap(cap: StrokeCap) {
        commands.add(UnifyCanvasCommand.SetLineCap(cap))
        currentState = currentState.copy(lineCap = cap)
    }
    
    fun setLineJoin(join: StrokeJoin) {
        commands.add(UnifyCanvasCommand.SetLineJoin(join))
        currentState = currentState.copy(lineJoin = join)
    }
    
    fun setLineDash(segments: List<Float>, offset: Float = 0f) {
        commands.add(UnifyCanvasCommand.SetLineDash(segments, offset))
        currentState = currentState.copy(lineDashOffset = offset)
    }
    
    fun setGlobalAlpha(alpha: Float) {
        commands.add(UnifyCanvasCommand.SetGlobalAlpha(alpha))
        currentState = currentState.copy(globalAlpha = alpha)
    }
    
    fun setShadow(offsetX: Float, offsetY: Float, blur: Float, color: Color) {
        commands.add(UnifyCanvasCommand.SetShadow(offsetX, offsetY, blur, color))
        currentState = currentState.copy(
            shadowOffsetX = offsetX,
            shadowOffsetY = offsetY,
            shadowBlur = blur,
            shadowColor = color
        )
    }
    
    // 绘制方法
    fun stroke() {
        commands.add(UnifyCanvasCommand.Stroke)
    }
    
    fun fill() {
        commands.add(UnifyCanvasCommand.Fill)
    }
    
    fun fillRect(x: Float, y: Float, width: Float, height: Float) {
        commands.add(UnifyCanvasCommand.FillRect(x, y, width, height))
    }
    
    fun strokeRect(x: Float, y: Float, width: Float, height: Float) {
        commands.add(UnifyCanvasCommand.StrokeRect(x, y, width, height))
    }
    
    fun clearRect(x: Float, y: Float, width: Float, height: Float) {
        commands.add(UnifyCanvasCommand.ClearRect(x, y, width, height))
    }
    
    // 文本方法
    fun fillText(text: String, x: Float, y: Float, maxWidth: Float? = null) {
        commands.add(UnifyCanvasCommand.FillText(text, x, y, maxWidth))
    }
    
    fun strokeText(text: String, x: Float, y: Float, maxWidth: Float? = null) {
        commands.add(UnifyCanvasCommand.StrokeText(text, x, y, maxWidth))
    }
    
    fun setFont(font: String) {
        commands.add(UnifyCanvasCommand.SetFont(font))
        currentState = currentState.copy(font = font)
    }
    
    fun setTextAlign(align: String) {
        commands.add(UnifyCanvasCommand.SetTextAlign(align))
        currentState = currentState.copy(textAlign = align)
    }
    
    fun setTextBaseline(baseline: String) {
        commands.add(UnifyCanvasCommand.SetTextBaseline(baseline))
        currentState = currentState.copy(textBaseline = baseline)
    }
    
    // 图像方法
    fun drawImage(imageResource: String, dx: Float, dy: Float) {
        commands.add(UnifyCanvasCommand.DrawImage(imageResource, dx, dy))
    }
    
    fun drawImage(imageResource: String, dx: Float, dy: Float, dWidth: Float, dHeight: Float) {
        commands.add(UnifyCanvasCommand.DrawImageScaled(imageResource, dx, dy, dWidth, dHeight))
    }
    
    fun drawImage(imageResource: String, sx: Float, sy: Float, sWidth: Float, sHeight: Float, dx: Float, dy: Float, dWidth: Float, dHeight: Float) {
        commands.add(UnifyCanvasCommand.DrawImageCropped(imageResource, sx, sy, sWidth, sHeight, dx, dy, dWidth, dHeight))
    }
    
    // 变换方法
    fun scale(x: Float, y: Float) {
        commands.add(UnifyCanvasCommand.Scale(x, y))
    }
    
    fun rotate(angle: Float) {
        commands.add(UnifyCanvasCommand.Rotate(angle))
    }
    
    fun translate(x: Float, y: Float) {
        commands.add(UnifyCanvasCommand.Translate(x, y))
    }
    
    fun transform(a: Float, b: Float, c: Float, d: Float, e: Float, f: Float) {
        commands.add(UnifyCanvasCommand.Transform(a, b, c, d, e, f))
    }
    
    fun setTransform(a: Float, b: Float, c: Float, d: Float, e: Float, f: Float) {
        commands.add(UnifyCanvasCommand.SetTransform(a, b, c, d, e, f))
    }
    
    // 状态方法
    fun save() {
        commands.add(UnifyCanvasCommand.Save)
        stateStack.add(currentState.copy())
    }
    
    fun restore() {
        commands.add(UnifyCanvasCommand.Restore)
        if (stateStack.isNotEmpty()) {
            currentState = stateStack.removeLastOrNull() ?: currentState
        }
    }
    
    // 裁剪方法
    fun clip(path: Path? = null) {
        commands.add(UnifyCanvasCommand.Clip(path ?: currentPath))
    }
    
    // 渐变方法
    fun createLinearGradient(x0: Float, y0: Float, x1: Float, y1: Float, colorStops: List<Pair<Float, Color>>) {
        commands.add(UnifyCanvasCommand.CreateLinearGradient(x0, y0, x1, y1, colorStops))
    }
    
    fun createRadialGradient(x0: Float, y0: Float, r0: Float, x1: Float, y1: Float, r1: Float, colorStops: List<Pair<Float, Color>>) {
        commands.add(UnifyCanvasCommand.CreateRadialGradient(x0, y0, r0, x1, y1, r1, colorStops))
    }
    
    fun createCircularGradient(x: Float, y: Float, colorStops: List<Pair<Float, Color>>) {
        commands.add(UnifyCanvasCommand.CreateCircularGradient(x, y, colorStops))
    }
    
    // 图案方法
    fun createPattern(imageResource: String, repetition: String) {
        commands.add(UnifyCanvasCommand.CreatePattern(imageResource, repetition))
    }
    
    // 获取命令列表
    fun getCommands(): List<UnifyCanvasCommand> = commands.toList()
    
    // 清除所有命令
    fun clear() {
        commands.clear()
        currentPath = Path()
        currentState = UnifyCanvasState()
        stateStack.clear()
    }
    
    // 测量文本
    fun measureText(text: String): Float {
        // 简化实现，实际应该根据字体计算文本宽度
        return text.length * 8f
    }
}

/**
 * 画布组件
 */
@Composable
fun UnifyCanvas(
    modifier: Modifier = Modifier,
    canvasId: String = "",
    type: String = "2d", // 2d, webgl
    disableScroll: Boolean = false,
    onTouchStart: ((touches: List<Pair<Float, Float>>) -> Unit)? = null,
    onTouchMove: ((touches: List<Pair<Float, Float>>) -> Unit)? = null,
    onTouchEnd: ((touches: List<Pair<Float, Float>>) -> Unit)? = null,
    onTouchCancel: ((touches: List<Pair<Float, Float>>) -> Unit)? = null,
    onLongTap: ((x: Float, y: Float) -> Unit)? = null,
    onError: ((error: String) -> Unit)? = null,
    contentDescription: String? = null,
    onDraw: (UnifyCanvasContext) -> Unit
) {
    val context = remember { UnifyCanvasContext() }
    
    // 执行绘制回调
    LaunchedEffect(onDraw) {
        context.clear()
        onDraw(context)
    }
    
    Canvas(
        modifier = modifier
            .semantics {
                contentDescription?.let { 
                    this.contentDescription = it 
                }
            }
    ) {
        // 执行绘制命令
        executeDrawCommands(context.getCommands(), this)
    }
}

/**
 * 执行绘制命令
 */
private fun executeDrawCommands(commands: List<UnifyCanvasCommand>, drawScope: DrawScope) {
    var currentPath = Path()
    var strokeStyle = Color.Black
    var fillStyle = Color.Black
    var lineWidth = 1f
    var lineCap = StrokeCap.Butt
    var lineJoin = StrokeJoin.Miter
    var globalAlpha = 1f
    var shadowOffsetX = 0f
    var shadowOffsetY = 0f
    var shadowBlur = 0f
    var shadowColor = Color.Transparent
    
    with(drawScope) {
        commands.forEach { command ->
            when (command) {
                is UnifyCanvasCommand.MoveTo -> {
                    currentPath.moveTo(command.x, command.y)
                }
                is UnifyCanvasCommand.LineTo -> {
                    currentPath.lineTo(command.x, command.y)
                }
                is UnifyCanvasCommand.Arc -> {
                    val rect = Rect(
                        command.x - command.radius,
                        command.y - command.radius,
                        command.x + command.radius,
                        command.y + command.radius
                    )
                    currentPath.addArc(
                        rect,
                        command.startAngle * 180f / PI.toFloat(),
                        (command.endAngle - command.startAngle) * 180f / PI.toFloat()
                    )
                }
                is UnifyCanvasCommand.BezierCurveTo -> {
                    currentPath.cubicTo(
                        command.cp1x, command.cp1y,
                        command.cp2x, command.cp2y,
                        command.x, command.y
                    )
                }
                is UnifyCanvasCommand.QuadraticCurveTo -> {
                    currentPath.quadraticBezierTo(command.cpx, command.cpy, command.x, command.y)
                }
                is UnifyCanvasCommand.Rect -> {
                    currentPath.addRect(Rect(command.x, command.y, command.x + command.width, command.y + command.height))
                }
                is UnifyCanvasCommand.ClosePath -> {
                    currentPath.close()
                }
                is UnifyCanvasCommand.BeginPath -> {
                    currentPath = Path()
                }
                is UnifyCanvasCommand.SetStrokeStyle -> {
                    strokeStyle = command.color
                }
                is UnifyCanvasCommand.SetFillStyle -> {
                    fillStyle = command.color
                }
                is UnifyCanvasCommand.SetLineWidth -> {
                    lineWidth = command.width
                }
                is UnifyCanvasCommand.SetLineCap -> {
                    lineCap = command.cap
                }
                is UnifyCanvasCommand.SetLineJoin -> {
                    lineJoin = command.join
                }
                is UnifyCanvasCommand.SetGlobalAlpha -> {
                    globalAlpha = command.alpha
                }
                is UnifyCanvasCommand.SetShadow -> {
                    shadowOffsetX = command.offsetX
                    shadowOffsetY = command.offsetY
                    shadowBlur = command.blur
                    shadowColor = command.color
                }
                is UnifyCanvasCommand.Stroke -> {
                    drawPath(
                        path = currentPath,
                        color = strokeStyle.copy(alpha = strokeStyle.alpha * globalAlpha),
                        style = Stroke(
                            width = lineWidth,
                            cap = lineCap,
                            join = lineJoin
                        )
                    )
                }
                is UnifyCanvasCommand.Fill -> {
                    drawPath(
                        path = currentPath,
                        color = fillStyle.copy(alpha = fillStyle.alpha * globalAlpha)
                    )
                }
                is UnifyCanvasCommand.FillRect -> {
                    drawRect(
                        color = fillStyle.copy(alpha = fillStyle.alpha * globalAlpha),
                        topLeft = Offset(command.x, command.y),
                        size = Size(command.width, command.height)
                    )
                }
                is UnifyCanvasCommand.StrokeRect -> {
                    drawRect(
                        color = strokeStyle.copy(alpha = strokeStyle.alpha * globalAlpha),
                        topLeft = Offset(command.x, command.y),
                        size = Size(command.width, command.height),
                        style = Stroke(
                            width = lineWidth,
                            cap = lineCap,
                            join = lineJoin
                        )
                    )
                }
                is UnifyCanvasCommand.ClearRect -> {
                    // 清除指定区域（简化实现）
                    drawRect(
                        color = Color.Transparent,
                        topLeft = Offset(command.x, command.y),
                        size = Size(command.width, command.height),
                        blendMode = BlendMode.Clear
                    )
                }
                is UnifyCanvasCommand.FillText -> {
                    // 简化的文本绘制实现
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = fillStyle.copy(alpha = fillStyle.alpha * globalAlpha).toArgb()
                            textSize = 14f * density
                            isAntiAlias = true
                        }
                        drawText(command.text, command.x, command.y, paint)
                    }
                }
                is UnifyCanvasCommand.Scale -> {
                    scale(command.x, command.y)
                }
                is UnifyCanvasCommand.Rotate -> {
                    rotate(command.angle * 180f / PI.toFloat())
                }
                is UnifyCanvasCommand.Translate -> {
                    translate(command.x, command.y)
                }
                // 其他命令的实现...
                else -> {
                    // 暂未实现的命令
                }
            }
        }
    }
}

/**
 * 画布工具类
 */
object UnifyCanvasUtils {
    
    /**
     * 创建画布上下文
     */
    fun createContext(canvasId: String, type: String = "2d"): UnifyCanvasContext {
        return UnifyCanvasContext()
    }
    
    /**
     * 将画布内容导出为图片
     */
    fun canvasToTempFilePath(canvasId: String, x: Float = 0f, y: Float = 0f, width: Float? = null, height: Float? = null, destWidth: Float? = null, destHeight: Float? = null, fileType: String = "png", quality: Float = 1f): String {
        // 实际实现中应该将画布内容保存为临时文件
        return "/tmp/canvas_${canvasId}_${System.currentTimeMillis()}.${fileType}"
    }
    
    /**
     * 将画布内容保存到相册
     */
    fun canvasPutImageData(canvasId: String, data: ByteArray, x: Float, y: Float, width: Float, height: Float) {
        // 实际实现中应该将图像数据放置到画布上
    }
    
    /**
     * 获取画布上指定区域的图像数据
     */
    fun canvasGetImageData(canvasId: String, x: Float, y: Float, width: Float, height: Float): ByteArray {
        // 实际实现中应该返回指定区域的图像数据
        return ByteArray(0)
    }
}
