package com.unify.data

import kotlinx.coroutines.flow.Flow

/**
 * 跨平台数据管理器接口
 */
interface UnifyDataManager {
    suspend fun saveString(
        key: String,
        value: String,
    )

    suspend fun getString(
        key: String,
        defaultValue: String = "",
    ): String

    suspend fun saveInt(
        key: String,
        value: Int,
    )

    suspend fun getInt(
        key: String,
        defaultValue: Int = 0,
    ): Int

    suspend fun saveBoolean(
        key: String,
        value: Boolean,
    )

    suspend fun getBoolean(
        key: String,
        defaultValue: Boolean = false,
    ): Boolean

    suspend fun saveFloat(
        key: String,
        value: Float,
    )

    suspend fun getFloat(
        key: String,
        defaultValue: Float = 0f,
    ): Float

    suspend fun saveLong(
        key: String,
        value: Long,
    )

    suspend fun getLong(
        key: String,
        defaultValue: Long = 0L,
    ): Long

    suspend fun saveObject(
        key: String,
        value: Any,
    )

    suspend fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: kotlinx.serialization.KSerializer<T>,
    ): T

    suspend fun remove(key: String)

    suspend fun clear()

    suspend fun contains(key: String): Boolean

    suspend fun getAllKeys(): Set<String>

    fun observeString(
        key: String,
        defaultValue: String = "",
    ): Flow<String>

    fun observeInt(
        key: String,
        defaultValue: Int = 0,
    ): Flow<Int>

    fun observeBoolean(
        key: String,
        defaultValue: Boolean = false,
    ): Flow<Boolean>

    fun observeFloat(
        key: String,
        defaultValue: Float = 0f,
    ): Flow<Float>

    fun observeLong(
        key: String,
        defaultValue: Long = 0L,
    ): Flow<Long>

    suspend fun saveToSecureStorage(
        key: String,
        value: String,
    )

    suspend fun getFromSecureStorage(
        key: String,
        defaultValue: String = "",
    ): String

    suspend fun saveFile(
        fileName: String,
        data: ByteArray,
    )

    suspend fun getFile(fileName: String): ByteArray?

    suspend fun deleteFile(fileName: String): Boolean

    suspend fun fileExists(fileName: String): Boolean
}

/**
 * 数据管理器实现类expect声明
 */
expect class UnifyDataManagerImpl() : UnifyDataManager
