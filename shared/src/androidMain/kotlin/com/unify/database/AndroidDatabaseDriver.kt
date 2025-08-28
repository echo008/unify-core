package com.unify.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.unify.database.UnifyDatabase

/**
 * Android平台SQLDelight数据库驱动实现
 */
actual class UnifyDatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = UnifyDatabase.Schema,
            context = context,
            name = "unify_database.db"
        )
    }
}
