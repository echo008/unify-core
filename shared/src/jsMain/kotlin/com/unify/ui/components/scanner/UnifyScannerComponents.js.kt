package com.unify.ui.components.scanner

import kotlinx.coroutines.delay
import org.w3c.dom.*
import org.w3c.dom.url.URL
import kotlinx.browser.document
import kotlinx.browser.window

/**
 * Web平台扫描器组件实现
 */

/**
 * Web平台扫描实现
 */
actual suspend fun performScan(scannerType: ScannerType): ScanResult {
    // 模拟扫描延迟
    delay(2000)
    
    return when (scannerType) {
        ScannerType.QR_CODE -> scanQRCodeWeb()
        ScannerType.BARCODE -> scanBarcodeWeb()
        ScannerType.DOCUMENT -> scanDocumentWeb()
        ScannerType.IMAGE -> scanImageWeb()
        ScannerType.NFC -> scanNFCWeb()
        ScannerType.BUSINESS_CARD -> scanBusinessCardWeb()
        ScannerType.ID_CARD -> scanIDCardWeb()
        ScannerType.RECEIPT -> scanReceiptWeb()
    }
}

/**
 * Web平台扫描结果操作处理
 */
actual fun handleScanResultAction(result: ScanResult, action: ScanResultAction) {
    when (action) {
        ScanResultAction.Copy -> copyToClipboardWeb(result.content)
        ScanResultAction.Share -> shareContentWeb(result.content)
        ScanResultAction.Open -> openContentWeb(result)
        ScanResultAction.Save -> saveContentWeb(result)
        ScanResultAction.Edit -> editContentWeb(result)
        ScanResultAction.Delete -> deleteContentWeb(result)
    }
}

/**
 * Web扫描二维码
 */
private suspend fun scanQRCodeWeb(): ScanResult {
    val qrCodes = listOf(
        ScanResult(
            type = ScanResultType.URL,
            content = "https://developer.mozilla.org/en-US/docs/Web/API/MediaDevices/getUserMedia",
            metadata = mapOf(
                "format" to "QR_CODE",
                "detector" to "WebCodecs API",
                "confidence" to "0.97",
                "browser" to getBrowserInfo()
            )
        ),
        ScanResult(
            type = ScanResultType.TEXT,
            content = "欢迎使用Web扫描功能！支持PWA离线使用。",
            metadata = mapOf(
                "format" to "QR_CODE",
                "encoding" to "UTF-8",
                "detector" to "Canvas API"
            )
        ),
        ScanResult(
            type = ScanResultType.WIFI,
            content = "WIFI:T:WPA2;S:WebApp-WiFi;P:webpassword123;H:false;;",
            metadata = mapOf(
                "format" to "QR_CODE",
                "network" to "WebApp-WiFi",
                "security" to "WPA2",
                "hidden" to "false"
            )
        ),
        ScanResult(
            type = ScanResultType.CONTACT,
            content = "BEGIN:VCARD\nVERSION:3.0\nFN:Web Developer\nORG:Mozilla Foundation\nTEL:+1-650-903-0800\nEMAIL:webdev@mozilla.org\nURL:https://developer.mozilla.org\nEND:VCARD",
            metadata = mapOf(
                "format" to "QR_CODE",
                "name" to "Web Developer",
                "organization" to "Mozilla Foundation",
                "detector" to "ImageData API"
            )
        ),
        ScanResult(
            type = ScanResultType.EMAIL,
            content = "mailto:support@webscanner.com?subject=Web Scanner Support&body=Hello Web Scanner Team",
            metadata = mapOf(
                "format" to "QR_CODE",
                "recipient" to "support@webscanner.com",
                "subject" to "Web Scanner Support"
            )
        )
    )
    
    return qrCodes.random()
}

/**
 * Web扫描条形码
 */
private suspend fun scanBarcodeWeb(): ScanResult {
    val barcodes = listOf(
        ScanResult(
            type = ScanResultType.PRODUCT,
            content = "0123456789012",
            metadata = mapOf(
                "format" to "UPC_A",
                "product" to "Web Development Book",
                "brand" to "O'Reilly Media",
                "price" to "49.99",
                "currency" to "USD",
                "detector" to "BarcodeDetector API"
            )
        ),
        ScanResult(
            type = ScanResultType.PRODUCT,
            content = "9781449365035",
            metadata = mapOf(
                "format" to "EAN_13",
                "product" to "JavaScript: The Good Parts",
                "author" to "Douglas Crockford",
                "price" to "29.99",
                "currency" to "USD"
            )
        ),
        ScanResult(
            type = ScanResultType.PRODUCT,
            content = "9780596517748",
            metadata = mapOf(
                "format" to "EAN_13",
                "product" to "JavaScript: The Definitive Guide",
                "author" to "David Flanagan",
                "price" to "59.99",
                "currency" to "USD"
            )
        )
    )
    
    return barcodes.random()
}

/**
 * Web扫描文档
 */
private suspend fun scanDocumentWeb(): ScanResult {
    val documents = listOf(
        ScanResult(
            type = ScanResultType.DOCUMENT,
            content = "Web开发服务协议\n\n协议编号：WEB-2024-001\n签署日期：2024年1月15日\n\n甲方：Web开发公司\n地址：硅谷科技园区100号\n\n乙方：客户企业\n\n服务内容：\n1. 响应式网站开发\n2. PWA应用开发\n3. 前端性能优化\n4. SEO优化服务\n5. 技术支持与维护\n\n技术栈：\n- React/Vue.js前端框架\n- Node.js后端服务\n- MongoDB/PostgreSQL数据库\n- AWS/Azure云服务\n\n本协议自签署之日起生效。",
            metadata = mapOf(
                "type" to "CONTRACT",
                "pages" to "1",
                "language" to "zh-CN",
                "confidence" to "0.93",
                "detector" to "Tesseract.js",
                "textBlocks" to "15"
            )
        ),
        ScanResult(
            type = ScanResultType.DOCUMENT,
            content = "在线购物订单确认\n\n订单号：WEB-ORDER-2024-0001\n下单时间：2024年1月20日 14:30:00\n\n购买商品：\n- MacBook Pro 16\" M3 × 1 = $2,499.00\n- Magic Mouse × 1 = $79.00\n- USB-C Hub × 1 = $49.00\n\n小计：$2,627.00\n运费：$0.00 (免费配送)\n税费：$236.43\n总计：$2,863.43\n\n配送地址：\n123 Tech Street\nSan Francisco, CA 94102\n\n支付方式：信用卡 ****1234\n预计送达：2-3个工作日\n\n感谢您的购买！",
            metadata = mapOf(
                "type" to "ORDER_CONFIRMATION",
                "orderNumber" to "WEB-ORDER-2024-0001",
                "total" to "2863.43",
                "currency" to "USD",
                "items" to "3"
            )
        )
    )
    
    return documents.random()
}

/**
 * Web扫描图像
 */
private suspend fun scanImageWeb(): ScanResult {
    val images = listOf(
        ScanResult(
            type = ScanResultType.IMAGE,
            content = "检测到图像内容：现代化的办公室环境，多台显示器显示代码编辑器",
            metadata = mapOf(
                "objects" to "monitors, code, office, desk, keyboard",
                "colors" to "blue, white, black, gray",
                "scene" to "workspace",
                "confidence" to "0.89",
                "detector" to "TensorFlow.js",
                "classification" to "Technology"
            )
        ),
        ScanResult(
            type = ScanResultType.IMAGE,
            content = "检测到图像内容：美丽的海滩日落景色，波浪轻拍沙滩",
            metadata = mapOf(
                "objects" to "beach, sunset, waves, sand, ocean",
                "colors" to "orange, yellow, blue, gold",
                "scene" to "nature",
                "confidence" to "0.95",
                "location" to "Coastal Area"
            )
        ),
        ScanResult(
            type = ScanResultType.IMAGE,
            content = "检测到图像内容：精美的意大利面条配番茄酱和新鲜罗勒",
            metadata = mapOf(
                "objects" to "pasta, tomato_sauce, basil, plate, food",
                "colors" to "red, green, white, yellow",
                "scene" to "dining",
                "confidence" to "0.92",
                "cuisine" to "Italian"
            )
        )
    )
    
    return images.random()
}

/**
 * Web扫描NFC
 */
private suspend fun scanNFCWeb(): ScanResult {
    val nfcData = listOf(
        ScanResult(
            type = ScanResultType.TEXT,
            content = "欢迎访问我们的Web应用！扫描此NFC标签获取更多功能。",
            metadata = mapOf(
                "type" to "NDEF_TEXT",
                "language" to "zh",
                "tagType" to "Type2",
                "uid" to "04:A1:B2:C3:D4:E5:F6",
                "technology" to "Web NFC API"
            )
        ),
        ScanResult(
            type = ScanResultType.URL,
            content = "https://web.dev/nfc/",
            metadata = mapOf(
                "type" to "NDEF_URI",
                "tagType" to "Type4",
                "uid" to "04:B2:C3:D4:E5:F6:A1",
                "technology" to "Web NFC API"
            )
        ),
        ScanResult(
            type = ScanResultType.CONTACT,
            content = "BEGIN:VCARD\nVERSION:3.0\nFN:Web NFC Developer\nTEL:+1-555-WEB-NFC\nEMAIL:nfc@webdev.com\nURL:https://webnfc.example.com\nEND:VCARD",
            metadata = mapOf(
                "type" to "NDEF_VCARD",
                "name" to "Web NFC Developer",
                "phone" to "+1-555-WEB-NFC",
                "uid" to "04:C3:D4:E5:F6:A1:B2"
            )
        )
    )
    
    return nfcData.random()
}

/**
 * Web扫描名片
 */
private suspend fun scanBusinessCardWeb(): ScanResult {
    val businessCards = listOf(
        ScanResult(
            type = ScanResultType.CONTACT,
            content = "Sarah Johnson\nSenior Frontend Developer\nTech Innovations Inc.\n电话：+1-555-123-4567\n邮箱：sarah@techinnovations.com\n网站：www.techinnovations.com\nLinkedIn: linkedin.com/in/sarahjohnson\nGitHub: github.com/sarahjdev",
            metadata = mapOf(
                "name" to "Sarah Johnson",
                "title" to "Senior Frontend Developer",
                "company" to "Tech Innovations Inc.",
                "phone" to "+1-555-123-4567",
                "email" to "sarah@techinnovations.com",
                "website" to "www.techinnovations.com",
                "linkedin" to "linkedin.com/in/sarahjohnson",
                "github" to "github.com/sarahjdev",
                "detector" to "OCR.js"
            )
        ),
        ScanResult(
            type = ScanResultType.CONTACT,
            content = "Michael Chen\nUX/UI Designer\nCreative Web Studio\n电话：+1-555-987-6543\n邮箱：michael@creativewebstudio.com\n作品集：portfolio.michaelchen.design\nDribbble: dribbble.com/michaelchen",
            metadata = mapOf(
                "name" to "Michael Chen",
                "title" to "UX/UI Designer",
                "company" to "Creative Web Studio",
                "phone" to "+1-555-987-6543",
                "email" to "michael@creativewebstudio.com",
                "portfolio" to "portfolio.michaelchen.design",
                "dribbble" to "dribbble.com/michaelchen"
            )
        )
    )
    
    return businessCards.random()
}

/**
 * Web扫描身份证
 */
private suspend fun scanIDCardWeb(): ScanResult {
    val idCards = listOf(
        ScanResult(
            type = ScanResultType.DOCUMENT,
            content = "数字身份证\n姓名：Alex Smith\n职业：Web开发者\n技能：JavaScript, TypeScript, React, Node.js\n认证：AWS Certified, Google Cloud Professional\n经验：5年全栈开发经验\n联系：alex@webdev.com\n作品：github.com/alexsmith",
            metadata = mapOf(
                "type" to "DIGITAL_ID",
                "name" to "Alex Smith",
                "profession" to "Web开发者",
                "skills" to "JavaScript, TypeScript, React, Node.js",
                "certifications" to "AWS Certified, Google Cloud Professional",
                "experience" to "5年",
                "contact" to "alex@webdev.com",
                "portfolio" to "github.com/alexsmith"
            )
        )
    )
    
    return idCards.random()
}

/**
 * Web扫描收据
 */
private suspend fun scanReceiptWeb(): ScanResult {
    val receipts = listOf(
        ScanResult(
            type = ScanResultType.DOCUMENT,
            content = "在线服务订阅收据\n\nCloudFlare Pro Plan\n订阅日期：2024-01-20\n服务期间：2024-01-20 至 2024-02-20\n\n服务详情：\nPro Plan (月付)      $20.00\nPage Rules (额外)    $5.00\nWorkers (1M请求)     $5.00\n\n小计：$30.00\n税费：$2.70\n总计：$32.70\n\n支付方式：信用卡 ****1234\n下次扣费：2024-02-20\n\n账单地址：\n123 Web Street\nSan Francisco, CA 94102\n\n感谢使用CloudFlare服务！",
            metadata = mapOf(
                "type" to "SUBSCRIPTION_RECEIPT",
                "service" to "CloudFlare Pro Plan",
                "period" to "2024-01-20 至 2024-02-20",
                "subtotal" to "30.00",
                "tax" to "2.70",
                "total" to "32.70",
                "currency" to "USD",
                "nextBilling" to "2024-02-20"
            )
        ),
        ScanResult(
            type = ScanResultType.DOCUMENT,
            content = "GitHub Pro 订阅收据\n\nGitHub Pro (个人版)\n订阅时间：2024-01-20 10:30:00\n\n订阅详情：\nGitHub Pro (月付)    $4.00\n私有仓库无限制\n高级代码搜索\n优先技术支持\n\n总计：$4.00\n\n支付方式：PayPal\n用户名：webdeveloper123\n邮箱：dev@example.com\n\n下次续费：2024-02-20\n\n感谢支持开源社区！",
            metadata = mapOf(
                "type" to "GITHUB_SUBSCRIPTION",
                "plan" to "GitHub Pro",
                "date" to "2024-01-20",
                "time" to "10:30:00",
                "amount" to "4.00",
                "currency" to "USD",
                "payment" to "PayPal",
                "username" to "webdeveloper123",
                "nextRenewal" to "2024-02-20"
            )
        )
    )
    
    return receipts.random()
}

/**
 * Web复制到剪贴板
 */
private fun copyToClipboardWeb(content: String) {
    try {
        // 使用Clipboard API
        window.navigator.clipboard?.writeText(content)
        println("Web: 已复制到剪贴板 - $content")
    } catch (e: Exception) {
        // 降级到传统方法
        println("Web: 复制到剪贴板失败，请手动复制 - $content")
    }
}

/**
 * Web分享内容
 */
private fun shareContentWeb(content: String) {
    try {
        // 使用Web Share API
        val shareData = js("""({
            title: 'Unify Scanner Result',
            text: content,
            url: window.location.href
        })""")
        
        window.navigator.asDynamic().share(shareData)
        println("Web: 分享内容 - $content")
    } catch (e: Exception) {
        // 降级到传统分享方法
        println("Web: Web Share API不可用，使用传统分享 - $content")
    }
}

/**
 * Web打开内容
 */
private fun openContentWeb(result: ScanResult) {
    when (result.type) {
        ScanResultType.URL -> {
            // 在新标签页打开URL
            window.open(result.content, "_blank")
            println("Web: 打开URL - ${result.content}")
        }
        ScanResultType.EMAIL -> {
            // 打开邮件客户端
            window.location.href = result.content
            println("Web: 打开邮件 - ${result.content}")
        }
        ScanResultType.PHONE -> {
            // 打开电话应用（移动设备）
            window.location.href = "tel:${result.content}"
            println("Web: 拨打电话 - ${result.content}")
        }
        ScanResultType.SMS -> {
            // 打开短信应用（移动设备）
            window.location.href = "sms:${result.content}"
            println("Web: 发送短信 - ${result.content}")
        }
        ScanResultType.LOCATION -> {
            // 打开地图
            window.open("https://maps.google.com/maps?q=${result.content}", "_blank")
            println("Web: 打开地图 - ${result.content}")
        }
        else -> {
            println("Web: 无法打开此类型内容")
        }
    }
}

/**
 * Web保存内容
 */
private fun saveContentWeb(result: ScanResult) {
    try {
        // 创建下载链接
        val blob = org.w3c.files.Blob(arrayOf(result.content), org.w3c.files.BlobPropertyBag("text/plain"))
        val url = URL.createObjectURL(blob)
        
        val link = document.createElement("a") as HTMLAnchorElement
        link.href = url
        link.download = "scan_result_${result.timestamp}.txt"
        link.click()
        
        URL.revokeObjectURL(url)
        println("Web: 保存内容 - ${result.content}")
    } catch (e: Exception) {
        println("Web: 保存失败 - ${e.message}")
    }
}

/**
 * Web编辑内容
 */
private fun editContentWeb(result: ScanResult) {
    // 可以打开文本编辑器或在线编辑工具
    println("Web: 编辑内容 - ${result.content}")
}

/**
 * Web删除内容
 */
private fun deleteContentWeb(result: ScanResult) {
    println("Web: 删除内容 - ${result.content}")
}

/**
 * 获取浏览器信息
 */
private fun getBrowserInfo(): String {
    val userAgent = window.navigator.userAgent
    return when {
        userAgent.contains("Chrome") -> "Chrome"
        userAgent.contains("Firefox") -> "Firefox"
        userAgent.contains("Safari") -> "Safari"
        userAgent.contains("Edge") -> "Edge"
        else -> "Unknown"
    }
}

/**
 * Web扫描器工具类
 */
object WebScannerUtils {
    
    /**
     * 检查相机权限
     */
    fun checkCameraPermission(): Boolean {
        // 检查getUserMedia API可用性
        return window.navigator.mediaDevices != null
    }
    
    /**
     * 请求相机权限
     */
    suspend fun requestCameraPermission(): Boolean {
        return try {
            window.navigator.mediaDevices?.getUserMedia(js("""({
                video: true
            })"""))
            true
        } catch (e: Exception) {
            println("Web: 相机权限被拒绝")
            false
        }
    }
    
    /**
     * 检查NFC是否可用
     */
    fun isNFCAvailable(): Boolean {
        // 检查Web NFC API可用性
        return js("'NDEFReader' in window") as Boolean
    }
    
    /**
     * 检查BarcodeDetector API可用性
     */
    fun isBarcodeDetectorAvailable(): Boolean {
        return js("'BarcodeDetector' in window") as Boolean
    }
    
    /**
     * 获取支持的扫描类型
     */
    fun getSupportedScannerTypes(): List<ScannerType> {
        val types = mutableListOf<ScannerType>()
        
        // 基础扫描类型（所有浏览器支持）
        types.addAll(listOf(
            ScannerType.QR_CODE,
            ScannerType.DOCUMENT,
            ScannerType.IMAGE,
            ScannerType.BUSINESS_CARD,
            ScannerType.ID_CARD,
            ScannerType.RECEIPT
        ))
        
        // 条形码（需要BarcodeDetector API）
        if (isBarcodeDetectorAvailable()) {
            types.add(ScannerType.BARCODE)
        }
        
        // NFC（需要Web NFC API）
        if (isNFCAvailable()) {
            types.add(ScannerType.NFC)
        }
        
        return types
    }
    
    /**
     * 获取浏览器能力信息
     */
    fun getBrowserCapabilities(): Map<String, String> {
        return mapOf(
            "browser" to getBrowserInfo(),
            "userAgent" to window.navigator.userAgent,
            "camera" to if (checkCameraPermission()) "支持" else "不支持",
            "clipboard" to if (window.navigator.clipboard != null) "支持" else "不支持",
            "share" to if (js("'share' in navigator") as Boolean) "支持" else "不支持",
            "nfc" to if (isNFCAvailable()) "支持" else "不支持",
            "barcodeDetector" to if (isBarcodeDetectorAvailable()) "支持" else "不支持",
            "webWorkers" to if (js("'Worker' in window") as Boolean) "支持" else "不支持",
            "serviceWorker" to if (js("'serviceWorker' in navigator") as Boolean) "支持" else "不支持"
        )
    }
    
    /**
     * 配置扫描器设置
     */
    fun configureScannerSettings(
        enableFlash: Boolean = false,
        enableAutoFocus: Boolean = true,
        scanTimeout: Long = 30000L,
        enableVibration: Boolean = true
    ) {
        println("Web: 配置扫描器设置")
        println("- 闪光灯: ${if (enableFlash) "开启" else "关闭"}")
        println("- 自动对焦: ${if (enableAutoFocus) "开启" else "关闭"}")
        println("- 扫描超时: ${scanTimeout}ms")
        println("- 振动反馈: ${if (enableVibration) "开启" else "关闭"}")
    }
    
    /**
     * 获取扫描历史
     */
    fun getScanHistory(): List<ScanResult> {
        // 从localStorage获取扫描历史
        val historyJson = window.localStorage.getItem("unify_scan_history")
        return if (historyJson != null) {
            // 解析JSON并返回结果列表
            emptyList() // 示例返回
        } else {
            emptyList()
        }
    }
    
    /**
     * 保存扫描历史
     */
    fun saveScanHistory(results: List<ScanResult>) {
        // 保存扫描历史到localStorage
        try {
            val historyJson = JSON.stringify(results.toTypedArray())
            window.localStorage.setItem("unify_scan_history", historyJson)
            println("Web: 保存扫描历史，共${results.size}条记录")
        } catch (e: Exception) {
            println("Web: 保存扫描历史失败 - ${e.message}")
        }
    }
    
    /**
     * 清空扫描历史
     */
    fun clearScanHistory() {
        window.localStorage.removeItem("unify_scan_history")
        println("Web: 清空扫描历史")
    }
    
    /**
     * 导出扫描结果
     */
    fun exportScanResults(results: List<ScanResult>, format: String = "json") {
        try {
            val content = when (format.lowercase()) {
                "json" -> JSON.stringify(results.toTypedArray())
                "csv" -> convertToCSV(results)
                "txt" -> convertToText(results)
                else -> {
                    println("Web: 不支持的导出格式: $format")
                    return
                }
            }
            
            val blob = org.w3c.files.Blob(arrayOf(content), org.w3c.files.BlobPropertyBag("text/plain"))
            val url = URL.createObjectURL(blob)
            
            val link = document.createElement("a") as HTMLAnchorElement
            link.href = url
            link.download = "scan_results_${js("Date.now()")}.$format"
            link.click()
            
            URL.revokeObjectURL(url)
            println("Web: 导出为${format.uppercase()}格式")
        } catch (e: Exception) {
            println("Web: 导出失败 - ${e.message}")
        }
    }
    
    /**
     * 转换为CSV格式
     */
    private fun convertToCSV(results: List<ScanResult>): String {
        val header = "Type,Content,Timestamp,Confidence\n"
        val rows = results.joinToString("\n") { result ->
            "${result.type},\"${result.content}\",${result.timestamp},${result.confidence}"
        }
        return header + rows
    }
    
    /**
     * 转换为文本格式
     */
    private fun convertToText(results: List<ScanResult>): String {
        return results.joinToString("\n\n") { result ->
            "类型: ${result.type}\n内容: ${result.content}\n时间: ${result.timestamp}\n置信度: ${result.confidence}"
        }
    }
    
    /**
     * 启用振动反馈
     */
    fun triggerVibration(pattern: IntArray = intArrayOf(200)) {
        try {
            window.navigator.asDynamic().vibrate(pattern)
            println("Web: 振动反馈")
        } catch (e: Exception) {
            println("Web: 振动API不可用")
        }
    }
    
    /**
     * 检查PWA安装状态
     */
    fun isPWAInstalled(): Boolean {
        return js("window.matchMedia('(display-mode: standalone)').matches") as Boolean
    }
    
    /**
     * 获取设备信息
     */
    fun getDeviceInfo(): Map<String, String> {
        return mapOf(
            "platform" to window.navigator.platform,
            "language" to window.navigator.language,
            "cookieEnabled" to window.navigator.cookieEnabled.toString(),
            "onLine" to window.navigator.onLine.toString(),
            "screenWidth" to window.screen.width.toString(),
            "screenHeight" to window.screen.height.toString(),
            "colorDepth" to window.screen.colorDepth.toString(),
            "pixelRatio" to window.devicePixelRatio.toString()
        )
    }
}
