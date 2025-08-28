package com.unify.platform.desktop

import com.unify.network.*
import com.unify.storage.*
import com.unify.platform.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import java.io.File

/**
 * 桌面平台适配器
 * 支持Windows、macOS、Linux桌面平台
 */

/**
 * 桌面平台类型
 */
enum class DesktopPlatformType {
    WINDOWS,
    MACOS,
    LINUX
}

/**
 * 桌面平台网络服务实现
 */
expect class DesktopNetworkServiceImpl() : UnifyNetworkService {
    override suspend fun <T> get(
        url: String,
        headers: Map<String, String>,
        queryParams: Map<String, String>
    ): NetworkResult<T>
    
    override suspend fun <T> post(
        url: String,
        body: Any?,
        headers: Map<String, String>
    ): NetworkResult<T>
    
    override suspend fun <T> put(
        url: String,
        body: Any?,
        headers: Map<String, String>
    ): NetworkResult<T>
    
    override suspend fun <T> delete(
        url: String,
        headers: Map<String, String>
    ): NetworkResult<T>
    
    override suspend fun uploadFile(
        url: String,
        filePath: String,
        headers: Map<String, String>
    ): NetworkResult<String>
    
    override suspend fun downloadFile(
        url: String,
        destinationPath: String,
        headers: Map<String, String>
    ): NetworkResult<String>
    
    override fun <T> streamRequest(
        url: String,
        headers: Map<String, String>
    ): Flow<NetworkResult<T>>
}

/**
 * 桌面平台Preferences存储实现
 */
expect class DesktopPreferencesStorage() : UnifyStorage {
    override suspend fun getString(key: String, defaultValue: String?): String?
    override suspend fun putString(key: String, value: String)
    override suspend fun getInt(key: String, defaultValue: Int): Int
    override suspend fun putInt(key: String, value: Int)
    override suspend fun getLong(key: String, defaultValue: Long): Long
    override suspend fun putLong(key: String, value: Long)
    override suspend fun getFloat(key: String, defaultValue: Float): Float
    override suspend fun putFloat(key: String, value: Float)
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean
    override suspend fun putBoolean(key: String, value: Boolean)
    override suspend fun <T> getObject(key: String, clazz: Class<T>): T?
    override suspend fun <T> putObject(key: String, value: T)
    override suspend fun remove(key: String)
    override suspend fun clear()
    override suspend fun contains(key: String): Boolean
    override suspend fun getAllKeys(): Set<String>
    override fun <T> observeKey(key: String, clazz: Class<T>): Flow<T?>
}

/**
 * 桌面平台数据库存储实现
 */
expect class DesktopDatabaseStorage() : UnifyStorage {
    override suspend fun getString(key: String, defaultValue: String?): String?
    override suspend fun putString(key: String, value: String)
    override suspend fun getInt(key: String, defaultValue: Int): Int
    override suspend fun putInt(key: String, value: Int)
    override suspend fun getLong(key: String, defaultValue: Long): Long
    override suspend fun putLong(key: String, value: Long)
    override suspend fun getFloat(key: String, defaultValue: Float): Float
    override suspend fun putFloat(key: String, value: Float)
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean
    override suspend fun putBoolean(key: String, value: Boolean)
    override suspend fun <T> getObject(key: String, clazz: Class<T>): T?
    override suspend fun <T> putObject(key: String, value: T)
    override suspend fun remove(key: String)
    override suspend fun clear()
    override suspend fun contains(key: String): Boolean
    override suspend fun getAllKeys(): Set<String>
    override fun <T> observeKey(key: String, clazz: Class<T>): Flow<T?>
}

/**
 * 桌面平台文件系统存储实现
 */
expect class DesktopFileStorage() : UnifyStorage {
    override suspend fun getString(key: String, defaultValue: String?): String?
    override suspend fun putString(key: String, value: String)
    override suspend fun getInt(key: String, defaultValue: Int): Int
    override suspend fun putInt(key: String, value: Int)
    override suspend fun getLong(key: String, defaultValue: Long): Long
    override suspend fun putLong(key: String, value: Long)
    override suspend fun getFloat(key: String, defaultValue: Float): Float
    override suspend fun putFloat(key: String, value: Float)
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean
    override suspend fun putBoolean(key: String, value: Boolean)
    override suspend fun <T> getObject(key: String, clazz: Class<T>): T?
    override suspend fun <T> putObject(key: String, value: T)
    override suspend fun remove(key: String)
    override suspend fun clear()
    override suspend fun contains(key: String): Boolean
    override suspend fun getAllKeys(): Set<String>
    override fun <T> observeKey(key: String, clazz: Class<T>): Flow<T?>
}

/**
 * 桌面平台安全存储实现
 */
expect class DesktopSecureStorage() : UnifyStorage {
    override suspend fun getString(key: String, defaultValue: String?): String?
    override suspend fun putString(key: String, value: String)
    override suspend fun getInt(key: String, defaultValue: Int): Int
    override suspend fun putInt(key: String, value: Int)
    override suspend fun getLong(key: String, defaultValue: Long): Long
    override suspend fun putLong(key: String, value: Long)
    override suspend fun getFloat(key: String, defaultValue: Float): Float
    override suspend fun putFloat(key: String, value: Float)
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean
    override suspend fun putBoolean(key: String, value: Boolean)
    override suspend fun <T> getObject(key: String, clazz: Class<T>): T?
    override suspend fun <T> putObject(key: String, value: T)
    override suspend fun remove(key: String)
    override suspend fun clear()
    override suspend fun contains(key: String): Boolean
    override suspend fun getAllKeys(): Set<String>
    override fun <T> observeKey(key: String, clazz: Class<T>): Flow<T?>
}

/**
 * 桌面平台信息实现
 */
expect class DesktopPlatformInfoImpl() : PlatformInfo {
    override val platformType: PlatformType
    override val platformVersion: String
    override val deviceModel: String
    override val isDebug: Boolean
    
    override fun getScreenSize(): Pair<Int, Int>
    override fun getDeviceId(): String?
    override fun isMobile(): Boolean
    override fun isTablet(): Boolean
    
    fun getDesktopPlatformType(): DesktopPlatformType
    fun getSystemArchitecture(): String
    fun getAvailableMemory(): Long
    fun getCPUInfo(): String
}

/**
 * 桌面平台能力实现
 */
expect class DesktopPlatformCapabilitiesImpl() : PlatformCapabilities {
    override val supportsFileSystem: Boolean
    override val supportsCamera: Boolean
    override val supportsLocation: Boolean
    override val supportsBiometric: Boolean
    override val supportsNotification: Boolean
    override val supportsVibration: Boolean
    override val supportsClipboard: Boolean
    override val supportsShare: Boolean
    override val supportsDeepLink: Boolean
    override val supportsBackgroundTask: Boolean
    
    // 桌面平台特有能力
    val supportsMultiWindow: Boolean
    val supportsFileDialog: Boolean
    val supportsSystemTray: Boolean
    val supportsMenuBar: Boolean
    val supportsKeyboardShortcuts: Boolean
    val supportsDragAndDrop: Boolean
}

/**
 * 桌面平台窗口管理器
 */
expect class DesktopWindowManager {
    fun createWindow(config: WindowConfig): WindowHandle
    fun closeWindow(handle: WindowHandle)
    fun minimizeWindow(handle: WindowHandle)
    fun maximizeWindow(handle: WindowHandle)
    fun restoreWindow(handle: WindowHandle)
    fun setWindowTitle(handle: WindowHandle, title: String)
    fun setWindowSize(handle: WindowHandle, width: Int, height: Int)
    fun setWindowPosition(handle: WindowHandle, x: Int, y: Int)
    fun showWindow(handle: WindowHandle)
    fun hideWindow(handle: WindowHandle)
    fun getActiveWindows(): List<WindowHandle>
}

/**
 * 窗口配置
 */
data class WindowConfig(
    val title: String = "Unify KMP App",
    val width: Int = 800,
    val height: Int = 600,
    val minWidth: Int = 400,
    val minHeight: Int = 300,
    val resizable: Boolean = true,
    val centered: Boolean = true,
    val alwaysOnTop: Boolean = false,
    val showInTaskbar: Boolean = true,
    val icon: String? = null
)

/**
 * 窗口句柄
 */
expect class WindowHandle {
    val id: String
    val isValid: Boolean
    fun focus()
    fun bringToFront()
}

/**
 * 桌面平台文件对话框管理器
 */
expect class DesktopFileDialogManager {
    suspend fun showOpenFileDialog(
        title: String = "选择文件",
        filters: List<FileFilter> = emptyList(),
        multiSelect: Boolean = false
    ): List<String>?
    
    suspend fun showSaveFileDialog(
        title: String = "保存文件",
        defaultFileName: String? = null,
        filters: List<FileFilter> = emptyList()
    ): String?
    
    suspend fun showOpenDirectoryDialog(
        title: String = "选择文件夹"
    ): String?
}

/**
 * 文件过滤器
 */
data class FileFilter(
    val description: String,
    val extensions: List<String>
)

/**
 * 桌面平台系统托盘管理器
 */
expect class DesktopSystemTrayManager {
    fun createTrayIcon(config: TrayIconConfig): TrayIconHandle?
    fun removeTrayIcon(handle: TrayIconHandle)
    fun updateTrayIcon(handle: TrayIconHandle, icon: String)
    fun updateTrayTooltip(handle: TrayIconHandle, tooltip: String)
    fun showTrayNotification(
        handle: TrayIconHandle,
        title: String,
        message: String,
        type: NotificationType = NotificationType.INFO
    )
}

/**
 * 系统托盘图标配置
 */
data class TrayIconConfig(
    val icon: String,
    val tooltip: String = "Unify KMP App",
    val menu: List<TrayMenuItem> = emptyList()
)

/**
 * 系统托盘图标句柄
 */
expect class TrayIconHandle {
    val id: String
    val isValid: Boolean
}

/**
 * 系统托盘菜单项
 */
data class TrayMenuItem(
    val text: String,
    val enabled: Boolean = true,
    val action: () -> Unit
)

/**
 * 通知类型
 */
enum class NotificationType {
    INFO,
    WARNING,
    ERROR
}

/**
 * 桌面平台剪贴板管理器
 */
expect class DesktopClipboardManager {
    suspend fun getText(): String?
    suspend fun setText(text: String)
    suspend fun hasText(): Boolean
    suspend fun getImage(): ByteArray?
    suspend fun setImage(imageData: ByteArray)
    suspend fun hasImage(): Boolean
    suspend fun clear()
}

/**
 * 桌面平台快捷键管理器
 */
expect class DesktopShortcutManager {
    fun registerGlobalShortcut(
        shortcut: KeyboardShortcut,
        action: () -> Unit
    ): Boolean
    
    fun unregisterGlobalShortcut(shortcut: KeyboardShortcut)
    
    fun registerWindowShortcut(
        window: WindowHandle,
        shortcut: KeyboardShortcut,
        action: () -> Unit
    ): Boolean
    
    fun unregisterWindowShortcut(
        window: WindowHandle,
        shortcut: KeyboardShortcut
    )
    
    fun unregisterAllShortcuts()
}

/**
 * 键盘快捷键
 */
data class KeyboardShortcut(
    val key: String,
    val modifiers: Set<KeyModifier> = emptySet()
)

/**
 * 按键修饰符
 */
enum class KeyModifier {
    CTRL,
    ALT,
    SHIFT,
    META // Windows键或Command键
}

/**
 * 桌面平台拖拽管理器
 */
expect class DesktopDragDropManager {
    fun enableFileDrop(
        window: WindowHandle,
        onFilesDropped: (List<String>) -> Unit
    )
    
    fun disableFileDrop(window: WindowHandle)
    
    fun enableTextDrop(
        window: WindowHandle,
        onTextDropped: (String) -> Unit
    )
    
    fun disableTextDrop(window: WindowHandle)
}

/**
 * 桌面平台菜单管理器
 */
expect class DesktopMenuManager {
    fun createMenuBar(items: List<MenuItem>): MenuBarHandle
    fun setWindowMenuBar(window: WindowHandle, menuBar: MenuBarHandle)
    fun removeWindowMenuBar(window: WindowHandle)
    fun createContextMenu(items: List<MenuItem>): ContextMenuHandle
    fun showContextMenu(
        menu: ContextMenuHandle,
        x: Int,
        y: Int
    )
}

/**
 * 菜单项
 */
sealed class MenuItem {
    data class Action(
        val text: String,
        val enabled: Boolean = true,
        val shortcut: KeyboardShortcut? = null,
        val action: () -> Unit
    ) : MenuItem()
    
    data class Submenu(
        val text: String,
        val items: List<MenuItem>
    ) : MenuItem()
    
    object Separator : MenuItem()
}

/**
 * 菜单栏句柄
 */
expect class MenuBarHandle {
    val id: String
    val isValid: Boolean
}

/**
 * 上下文菜单句柄
 */
expect class ContextMenuHandle {
    val id: String
    val isValid: Boolean
}

/**
 * 桌面平台数据库驱动
 */
expect class DesktopDatabaseDriverFactory {
    fun createDriver(): app.cash.sqldelight.db.SqlDriver
}
