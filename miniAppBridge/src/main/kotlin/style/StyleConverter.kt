package style

class StyleConverter {
    private val propertyMap = mapOf(
        "width" to "width",
        "height" to "height",
        "padding" to "padding",
        "margin" to "margin",
        "backgroundColor" to "background-color",
        "color" to "color",
        "fontSize" to "font-size",
        "fontWeight" to "font-weight",
        "textAlign" to "text-align",
        "borderRadius" to "border-radius",
        "borderWidth" to "border-width",
        "borderColor" to "border-color",
        "display" to "display",
        "flexDirection" to "flex-direction",
        "justifyContent" to "justify-content",
        "alignItems" to "align-items",
        "position" to "position",
        "top" to "top",
        "left" to "left",
        "right" to "right",
        "bottom" to "bottom",
        "zIndex" to "z-index",
        "opacity" to "opacity",
        "overflow" to "overflow"
    )

    fun convertProperty(key: String, value: Any): String {
        val cssProperty = propertyMap[key] ?: key
        val cssValue = convertValue(key, value)
        return "$cssProperty: $cssValue"
    }

    private fun convertValue(key: String, value: Any): String = when (key) {
        "width", "height", "padding", "margin" -> convertSizeValue(value)
        "fontSize" -> convertFontSize(value)
        "color", "backgroundColor", "borderColor" -> convertColor(value)
        "fontWeight" -> convertFontWeight(value)
        else -> value.toString()
    }

    private fun convertSizeValue(value: Any): String = when (value) {
        is Number -> "${value}rpx"
        is String -> if (value.endsWith("dp") || value.endsWith("px")) {
            "${value.removeSuffix("dp").removeSuffix("px")}rpx"
        } else value
        else -> value.toString()
    }

    private fun convertFontSize(value: Any): String = when (value) {
        is Number -> "${value}rpx"
        is String -> when {
            value.endsWith("sp") -> "${value.removeSuffix("sp")}rpx"
            value.endsWith("dp") -> "${value.removeSuffix("dp")}rpx"
            else -> value
        }
        else -> value.toString()
    }

    private fun convertColor(value: Any): String = when (value) {
        is String -> if (value.startsWith("#")) value else "#$value"
        else -> value.toString()
    }

    private fun convertFontWeight(value: Any): String = when (value.toString().lowercase()) {
        "bold" -> "bold"
        "normal" -> "normal"
        "light" -> "300"
        "medium" -> "500"
        "semibold" -> "600"
        else -> value.toString()
    }
}
