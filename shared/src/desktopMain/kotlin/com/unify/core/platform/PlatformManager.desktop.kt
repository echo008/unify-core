package com.unify.core.platform

import com.unify.core.types.PlatformType
import com.unify.core.types.DeviceInfo
import java.awt.Toolkit
import java.io.File
import java.net.NetworkInterface
import java.security.MessageDigest

/**
 * Desktop平台管理器实现
 */
class DesktopPlatformManager : BasePlatformManager() {
    
    override fun getPlatformType(): PlatformType = PlatformType.DESKTOP
    
    override fun getPlatformName(): String = "Desktop"
    
    override fun getPlatformVersion(): String = System.getProperty("os.version")
    
    override suspend fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            manufacturer = getSystemManufacturer(),
            model = getSystemModel(),
            systemName = System.getProperty("os.name"),
            systemVersion = System.getProperty("os.version"),
            deviceId = generateDeviceId(),
            isEmulator = false // Desktop环境不是模拟器
        )
    }
    
    override fun hasCapability(capability: String): Boolean {
        return when (capability) {
            "camera" -> hasCamera()
            "gps" -> false // Desktop通常没有GPS
            "bluetooth" -> hasBluetooth()
            "wifi" -> hasWifi()
            "nfc" -> false // Desktop通常没有NFC
            "fingerprint" -> false // Desktop通常没有指纹识别
            "accelerometer" -> false // Desktop没有加速度计
            "gyroscope" -> false // Desktop没有陀螺仪
            "magnetometer" -> false // Desktop没有磁力计
            "microphone" -> hasMicrophone()
            "telephony" -> false // Desktop没有电话功能
            "vibration" -> false // Desktop没有震动功能
            "audio" -> hasAudio()
            "display" -> true // Desktop都有显示器
            "keyboard" -> true // Desktop都有键盘
            "mouse" -> true // Desktop都有鼠标
            "filesystem" -> true // Desktop都有文件系统
            "network" -> hasNetwork()
            "usb" -> true // Desktop都支持USB
            "serial" -> hasSerialPort()
            "parallel" -> hasParallelPort()
            "printer" -> hasPrinter()
            "scanner" -> hasScanner()
            else -> false
        }
    }
    
    override fun getSupportedCapabilities(): List<String> {
        val capabilities = mutableListOf<String>()
        
        if (hasCapability("camera")) capabilities.add("camera")
        if (hasCapability("bluetooth")) capabilities.add("bluetooth")
        if (hasCapability("wifi")) capabilities.add("wifi")
        if (hasCapability("microphone")) capabilities.add("microphone")
        if (hasCapability("audio")) capabilities.add("audio")
        if (hasCapability("display")) capabilities.add("display")
        if (hasCapability("keyboard")) capabilities.add("keyboard")
        if (hasCapability("mouse")) capabilities.add("mouse")
        if (hasCapability("filesystem")) capabilities.add("filesystem")
        if (hasCapability("network")) capabilities.add("network")
        if (hasCapability("usb")) capabilities.add("usb")
        if (hasCapability("serial")) capabilities.add("serial")
        if (hasCapability("parallel")) capabilities.add("parallel")
        if (hasCapability("printer")) capabilities.add("printer")
        if (hasCapability("scanner")) capabilities.add("scanner")
        
        return capabilities
    }
    
    override suspend fun performPlatformInitialization() {
        // Desktop特定初始化
        config["os_name"] = System.getProperty("os.name")
        config["os_version"] = System.getProperty("os.version")
        config["os_arch"] = System.getProperty("os.arch")
        config["java_version"] = System.getProperty("java.version")
        config["java_vendor"] = System.getProperty("java.vendor")
        config["java_home"] = System.getProperty("java.home")
        config["user_name"] = System.getProperty("user.name")
        config["user_home"] = System.getProperty("user.home")
        config["user_dir"] = System.getProperty("user.dir")
        config["file_separator"] = System.getProperty("file.separator")
        config["path_separator"] = System.getProperty("path.separator")
        config["line_separator"] = System.getProperty("line.separator")
        
        // 系统信息
        val runtime = Runtime.getRuntime()
        config["available_processors"] = runtime.availableProcessors().toString()
        config["max_memory"] = runtime.maxMemory().toString()
        config["total_memory"] = runtime.totalMemory().toString()
        config["free_memory"] = runtime.freeMemory().toString()
        
        // 屏幕信息
        try {
            val toolkit = Toolkit.getDefaultToolkit()
            val screenSize = toolkit.screenSize
            config["screen_width"] = screenSize.width.toString()
            config["screen_height"] = screenSize.height.toString()
            config["screen_resolution"] = toolkit.screenResolution.toString()
        } catch (e: Exception) {
            config["screen_error"] = e.message ?: "Unknown screen error"
        }
        
        // 文件系统信息
        val roots = File.listRoots()
        config["file_system_roots"] = roots.joinToString(",") { it.absolutePath }
        
        // 网络接口信息
        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            val interfaces = mutableListOf<String>()
            while (networkInterfaces.hasMoreElements()) {
                val ni = networkInterfaces.nextElement()
                if (ni.isUp && !ni.isLoopback) {
                    interfaces.add(ni.name)
                }
            }
            config["network_interfaces"] = interfaces.joinToString(",")
        } catch (e: Exception) {
            config["network_error"] = e.message ?: "Unknown network error"
        }
        
        // 环境变量
        config["path"] = System.getenv("PATH") ?: ""
        config["temp_dir"] = System.getProperty("java.io.tmpdir")
        
        // JVM信息
        config["jvm_name"] = System.getProperty("java.vm.name")
        config["jvm_version"] = System.getProperty("java.vm.version")
        config["jvm_vendor"] = System.getProperty("java.vm.vendor")
        config["jvm_info"] = System.getProperty("java.vm.info")
    }
    
    override suspend fun performPlatformCleanup() {
        // Desktop特定清理
        config.clear()
    }
    
    private fun getSystemManufacturer(): String {
        return try {
            System.getProperty("java.vm.vendor") ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
    
    private fun getSystemModel(): String {
        val osName = System.getProperty("os.name")
        val osArch = System.getProperty("os.arch")
        return "$osName ($osArch)"
    }
    
    private fun generateDeviceId(): String {
        return try {
            val features = listOf(
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch"),
                System.getProperty("user.name"),
                System.getProperty("java.vm.name"),
                Runtime.getRuntime().availableProcessors().toString()
            )
            
            val input = features.joinToString("|")
            val md = MessageDigest.getInstance("SHA-256")
            val hash = md.digest(input.toByteArray())
            hash.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "desktop-${System.currentTimeMillis()}"
        }
    }
    
    private fun hasCamera(): Boolean {
        return try {
            // 检查是否有摄像头设备
            val osName = System.getProperty("os.name").lowercase()
            when {
                osName.contains("windows") -> {
                    // Windows: 检查设备管理器或注册表
                    File("C:\\Windows\\System32\\drivers").exists()
                }
                osName.contains("mac") -> {
                    // macOS: 检查系统信息
                    true // 大多数Mac都有摄像头
                }
                osName.contains("linux") -> {
                    // Linux: 检查/dev/video*设备
                    File("/dev").listFiles()?.any { it.name.startsWith("video") } ?: false
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun hasBluetooth(): Boolean {
        return try {
            val osName = System.getProperty("os.name").lowercase()
            when {
                osName.contains("windows") -> {
                    // Windows: 检查蓝牙服务
                    true // 假设现代Windows系统都支持蓝牙
                }
                osName.contains("mac") -> {
                    // macOS: 检查蓝牙
                    true // 现代Mac都支持蓝牙
                }
                osName.contains("linux") -> {
                    // Linux: 检查bluetoothd或hci设备
                    File("/sys/class/bluetooth").exists()
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun hasWifi(): Boolean {
        return try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            while (networkInterfaces.hasMoreElements()) {
                val ni = networkInterfaces.nextElement()
                if (ni.name.lowercase().contains("wlan") || 
                    ni.name.lowercase().contains("wifi") ||
                    ni.name.lowercase().contains("wireless")) {
                    return true
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }
    
    private fun hasMicrophone(): Boolean {
        return try {
            // 检查音频系统
            val osName = System.getProperty("os.name").lowercase()
            when {
                osName.contains("windows") -> true // Windows通常有音频输入
                osName.contains("mac") -> true // Mac通常有麦克风
                osName.contains("linux") -> {
                    // Linux: 检查ALSA或PulseAudio
                    File("/proc/asound").exists() || File("/dev/snd").exists()
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun hasAudio(): Boolean {
        return try {
            // 检查音频输出
            val osName = System.getProperty("os.name").lowercase()
            when {
                osName.contains("windows") -> true // Windows通常有音频输出
                osName.contains("mac") -> true // Mac通常有音频输出
                osName.contains("linux") -> {
                    // Linux: 检查ALSA或PulseAudio
                    File("/proc/asound").exists() || File("/dev/snd").exists()
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun hasNetwork(): Boolean {
        return try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            while (networkInterfaces.hasMoreElements()) {
                val ni = networkInterfaces.nextElement()
                if (ni.isUp && !ni.isLoopback) {
                    return true
                }
            }
            false
        } catch (e: Exception) {
            false
        }
    }
    
    private fun hasSerialPort(): Boolean {
        return try {
            val osName = System.getProperty("os.name").lowercase()
            when {
                osName.contains("windows") -> {
                    // Windows: 检查COM端口
                    (1..20).any { File("\\\\.\\COM$it").exists() }
                }
                osName.contains("linux") -> {
                    // Linux: 检查/dev/ttyS*或/dev/ttyUSB*
                    File("/dev").listFiles()?.any { 
                        it.name.startsWith("ttyS") || it.name.startsWith("ttyUSB") 
                    } ?: false
                }
                osName.contains("mac") -> {
                    // macOS: 检查/dev/cu.*或/dev/tty.*
                    File("/dev").listFiles()?.any { 
                        it.name.startsWith("cu.") || it.name.startsWith("tty.") 
                    } ?: false
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun hasParallelPort(): Boolean {
        return try {
            val osName = System.getProperty("os.name").lowercase()
            when {
                osName.contains("windows") -> {
                    // Windows: 检查LPT端口
                    (1..3).any { File("\\\\.\\LPT$it").exists() }
                }
                osName.contains("linux") -> {
                    // Linux: 检查/dev/lp*
                    File("/dev").listFiles()?.any { it.name.startsWith("lp") } ?: false
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun hasPrinter(): Boolean {
        return try {
            // 检查打印服务
            val osName = System.getProperty("os.name").lowercase()
            when {
                osName.contains("windows") -> {
                    // Windows: 检查打印机服务
                    true // 假设Windows系统支持打印
                }
                osName.contains("mac") -> {
                    // macOS: 检查CUPS
                    File("/usr/sbin/cupsd").exists()
                }
                osName.contains("linux") -> {
                    // Linux: 检查CUPS
                    File("/usr/sbin/cupsd").exists() || File("/etc/cups").exists()
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun hasScanner(): Boolean {
        return try {
            // 检查扫描仪支持
            val osName = System.getProperty("os.name").lowercase()
            when {
                osName.contains("linux") -> {
                    // Linux: 检查SANE
                    File("/usr/bin/scanimage").exists() || File("/etc/sane.d").exists()
                }
                else -> false // Windows和macOS的扫描仪检测较复杂
            }
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * 平台管理器创建函数实现
 */
actual fun getCurrentPlatformManager(): PlatformManager = DesktopPlatformManager()

actual fun createAndroidPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("Android平台管理器在Desktop平台不可用")
}

actual fun createIOSPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("iOS平台管理器在Desktop平台不可用")
}

actual fun createWebPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("Web平台管理器在Desktop平台不可用")
}

actual fun createDesktopPlatformManager(): PlatformManager = DesktopPlatformManager()

actual fun createHarmonyOSPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("HarmonyOS平台管理器在Desktop平台不可用")
}

actual fun createMiniProgramPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("MiniProgram平台管理器在Desktop平台不可用")
}

actual fun createWatchPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("Watch平台管理器在Desktop平台不可用")
}

actual fun createTVPlatformManager(): PlatformManager {
    throw UnsupportedOperationException("TV平台管理器在Desktop平台不可用")
}
