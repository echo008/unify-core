package com.unify.core.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.unify.database.UnifyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Desktop平台数据库管理器实现
 */
actual class UnifyDatabaseManager private constructor() {
    actual companion object {
        private var instance: UnifyDatabaseManager? = null
        
        actual fun create(): UnifyDatabaseManager {
            return instance ?: synchronized(this) {
                instance ?: UnifyDatabaseManager().also { instance = it }
            }
        }
    }

    private var database: UnifyDatabase? = null
    private var driver: SqlDriver? = null
    private val databasePath = "${System.getProperty("user.home")}/.unify/unify_database.db"

    actual suspend fun initialize() {
        withContext(Dispatchers.IO) {
            if (database == null) {
                // 确保目录存在
                File(databasePath).parentFile?.mkdirs()
                
                driver = NativeSqliteDriver(
                    schema = UnifyDatabase.Schema,
                    name = databasePath
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
            }
        }
    }

    actual fun getDatabaseVersion(): Int {
        return UnifyDatabase.Schema.version.toInt()
    }

    actual suspend fun migrate(fromVersion: Int, toVersion: Int) {
        withContext(Dispatchers.IO) {
            if (fromVersion < toVersion) {
                UnifyDatabase.Schema.migrate(driver!!, fromVersion.toLong(), toVersion.toLong())
            }
        }
    }

    actual suspend fun exportDatabase(): ByteArray {
        return withContext(Dispatchers.IO) {
            try {
                val dbFile = File(databasePath)
                if (dbFile.exists()) {
                    dbFile.readBytes()
                } else {
                    ByteArray(0)
                }
            } catch (e: Exception) {
                ByteArray(0)
            }
        }
    }

    actual suspend fun importDatabase(data: ByteArray): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                database?.close()
                driver?.close()
                
                val dbFile = File(databasePath)
                dbFile.writeBytes(data)
                
                initialize()
                true
            } catch (e: Exception) {
                false
            }
        }
    }

    actual suspend fun getDatabaseStats(): DatabaseStats {
        return withContext(Dispatchers.IO) {
            val dbFile = File(databasePath)
            val size = if (dbFile.exists()) dbFile.length() else 0L
            
            DatabaseStats(
                totalTables = 4,
                totalRecords = 0L,
                databaseSize = size,
                lastBackupTime = null,
                syncStatus = SyncStatus.SYNCED
            )
        }
    }
}
