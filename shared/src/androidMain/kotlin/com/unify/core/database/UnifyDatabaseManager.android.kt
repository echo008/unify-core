package com.unify.core.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.unify.database.UnifyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android平台数据库管理器实现
 */
actual class UnifyDatabaseManager private constructor() {
    actual companion object {
        private var instance: UnifyDatabaseManager? = null
        private lateinit var context: Context
        
        fun initialize(context: Context) {
            this.context = context.applicationContext
        }
        
        actual fun create(): UnifyDatabaseManager {
            return instance ?: synchronized(this) {
                instance ?: UnifyDatabaseManager().also { instance = it }
            }
        }
    }

    private var database: UnifyDatabase? = null
    private var driver: SqlDriver? = null

    actual suspend fun initialize() {
        withContext(Dispatchers.IO) {
            if (database == null) {
                driver = AndroidSqliteDriver(
                    schema = UnifyDatabase.Schema,
                    context = context,
                    name = "unify_database.db"
                )
                database = UnifyDatabase(driver!!)
            }
        }
    }

    actual fun getDatabase(): UnifyDatabase {
        return database ?: throw IllegalStateException("Database not initialized. Call initialize() first.")
    }

    actual suspend fun <T> transaction(block: suspend () -> T): T {
        return withContext(Dispatchers.IO) {
            val db = getDatabase()
            var result: T? = null
            db.transactionWithResult {
                result = kotlinx.coroutines.runBlocking { block() }
            }
            result!!
        }
    }

    actual suspend fun clearAllData() {
        withContext(Dispatchers.IO) {
            val db = getDatabase()
            db.transaction {
                db.unifyDatabaseQueries.clearAllCache()
                // 清空其他表的数据但保留结构
                db.unifyDatabaseQueries.transaction {
                    afterCommit {
                        // 重置自增ID
                    }
                }
            }
        }
    }

    actual fun getDatabaseVersion(): Int {
        return UnifyDatabase.Schema.version.toInt()
    }

    actual suspend fun migrate(fromVersion: Int, toVersion: Int) {
        withContext(Dispatchers.IO) {
            // 数据库迁移逻辑
            if (fromVersion < toVersion) {
                // 执行迁移脚本
                UnifyDatabase.Schema.migrate(driver!!, fromVersion.toLong(), toVersion.toLong())
            }
        }
    }

    actual suspend fun exportDatabase(): ByteArray {
        return withContext(Dispatchers.IO) {
            // 导出数据库文件
            val dbFile = context.getDatabasePath("unify_database.db")
            if (dbFile.exists()) {
                dbFile.readBytes()
            } else {
                ByteArray(0)
            }
        }
    }

    actual suspend fun importDatabase(data: ByteArray): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val dbFile = context.getDatabasePath("unify_database.db")
                // 关闭数据库连接
                driver?.close()
                
                dbFile.writeBytes(data)
                
                // 重新初始化数据库
                initialize()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    actual suspend fun getDatabaseStats(): DatabaseStats {
        return withContext(Dispatchers.IO) {
            val dbFile = context.getDatabasePath("unify_database.db")
            val size = if (dbFile.exists()) dbFile.length() else 0L
            
            DatabaseStats(
                totalTables = 4, // User, AppConfig, Cache, SyncRecord
                totalRecords = 0L, // 可以通过查询获取实际数量
                databaseSize = size,
                lastBackupTime = null,
                syncStatus = SyncStatus.SYNCED
            )
        }
    }
}
