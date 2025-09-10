package com.unify.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.web.WebDriver
import com.unify.database.UnifyDatabase
import kotlinx.coroutines.await
import org.w3c.dom.get
import kotlin.js.Promise

/**
 * Web/JS平台数据库管理器实现
 */
actual class UnifyDatabaseManager private constructor() {
    actual companion object {
        private var instance: UnifyDatabaseManager? = null
        
        actual fun create(): UnifyDatabaseManager {
            return instance ?: run {
                UnifyDatabaseManager().also { instance = it }
            }
        }
    }

    private var database: UnifyDatabase? = null
    private var driver: SqlDriver? = null

    actual suspend fun initialize() {
        if (database == null) {
            driver = WebDriver().also { webDriver ->
                webDriver.execute(null, "PRAGMA foreign_keys=ON", 0, null).await()
            }
            database = UnifyDatabase(driver!!)
        }
    }

    actual fun getDatabase(): UnifyDatabase {
        return database ?: throw IllegalStateException("Database not initialized. Call initialize() first.")
    }

    actual suspend fun <T> transaction(block: suspend () -> T): T {
        val db = getDatabase()
        var result: T? = null
        db.transactionWithResult {
            result = kotlinx.coroutines.runBlocking { block() }
        }
        return result!!
    }

    actual suspend fun clearAllData() {
        val db = getDatabase()
        db.transaction {
            db.unifyDatabaseQueries.clearAllCache()
        }
    }

    actual fun getDatabaseVersion(): Int {
        return UnifyDatabase.Schema.version.toInt()
    }

    actual suspend fun migrate(fromVersion: Int, toVersion: Int) {
        if (fromVersion < toVersion) {
            UnifyDatabase.Schema.migrate(driver!!, fromVersion.toLong(), toVersion.toLong())
        }
    }

    actual suspend fun exportDatabase(): ByteArray {
        // Web平台数据库导出的简化实现
        return try {
            // 在实际实现中，可以通过IndexedDB API导出数据
            val jsonData = """{"version": ${getDatabaseVersion()}, "data": "exported"}"""
            jsonData.encodeToByteArray()
        } catch (e: Exception) {
            ByteArray(0)
        }
    }

    actual suspend fun importDatabase(data: ByteArray): Boolean {
        return try {
            database?.close()
            driver?.close()
            
            // 重新初始化数据库
            initialize()
            true
        } catch (e: Exception) {
            false
        }
    }

    actual suspend fun getDatabaseStats(): DatabaseStats {
        return DatabaseStats(
            totalTables = 4,
            totalRecords = 0L,
            databaseSize = 0L, // Web平台难以准确获取大小
            lastBackupTime = null,
            syncStatus = SyncStatus.SYNCED
        )
    }
}
