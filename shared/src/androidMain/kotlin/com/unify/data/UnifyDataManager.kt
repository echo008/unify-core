package com.unify.data

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Android平台数据管理器实现
 */
actual class UnifyDataManagerImpl actual constructor() : UnifyDataManager {
    
    private lateinit var context: Context
    private lateinit var preferences: SharedPreferences
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var database: UnifyDatabase
    
    companion object {
        private const val PREFS_NAME = "unify_prefs"
        private const val DATASTORE_NAME = "unify_datastore"
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)
    }
    
    fun initialize(context: Context) {
        this.context = context
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        this.dataStore = context.dataStore
        this.database = Room.databaseBuilder(
            context,
            UnifyDatabase::class.java,
            "unify_database"
        ).build()
    }
    
    override suspend fun saveString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }
    
    override suspend fun getString(key: String, defaultValue: String): String {
        return preferences.getString(key, defaultValue) ?: defaultValue
    }
    
    override suspend fun saveInt(key: String, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }
    
    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return preferences.getInt(key, defaultValue)
    }
    
    override suspend fun saveBoolean(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }
    
    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return preferences.getBoolean(key, defaultValue)
    }
    
    override suspend fun saveFloat(key: String, value: Float) {
        preferences.edit().putFloat(key, value).apply()
    }
    
    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return preferences.getFloat(key, defaultValue)
    }
    
    override suspend fun saveLong(key: String, value: Long) {
        preferences.edit().putLong(key, value).apply()
    }
    
    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return preferences.getLong(key, defaultValue)
    }
    
    override suspend fun saveObject(key: String, value: Any) {
        val json = Json.encodeToString(kotlinx.serialization.serializer(), value)
        saveString(key, json)
    }
    
    override suspend fun <T> getObject(key: String, defaultValue: T, serializer: kotlinx.serialization.KSerializer<T>): T {
        val json = getString(key, "")
        return if (json.isNotEmpty()) {
            try {
                Json.decodeFromString(serializer, json)
            } catch (e: Exception) {
                defaultValue
            }
        } else {
            defaultValue
        }
    }
    
    override suspend fun remove(key: String) {
        preferences.edit().remove(key).apply()
    }
    
    override suspend fun clear() {
        preferences.edit().clear().apply()
    }
    
    override suspend fun contains(key: String): Boolean {
        return preferences.contains(key)
    }
    
    override suspend fun getAllKeys(): Set<String> {
        return preferences.all.keys
    }
    
    override fun observeString(key: String, defaultValue: String): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(key)] ?: defaultValue
        }
    }
    
    override fun observeInt(key: String, defaultValue: Int): Flow<Int> {
        return dataStore.data.map { preferences ->
            preferences[intPreferencesKey(key)] ?: defaultValue
        }
    }
    
    override fun observeBoolean(key: String, defaultValue: Boolean): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey(key)] ?: defaultValue
        }
    }
    
    override fun observeFloat(key: String, defaultValue: Float): Flow<Float> {
        return dataStore.data.map { preferences ->
            preferences[floatPreferencesKey(key)] ?: defaultValue
        }
    }
    
    override fun observeLong(key: String, defaultValue: Long): Flow<Long> {
        return dataStore.data.map { preferences ->
            preferences[longPreferencesKey(key)] ?: defaultValue
        }
    }
    
    override suspend fun saveToSecureStorage(key: String, value: String) {
        // 使用Android Keystore进行加密存储
        val encryptedValue = encryptValue(value)
        saveString("secure_$key", encryptedValue)
    }
    
    override suspend fun getFromSecureStorage(key: String, defaultValue: String): String {
        val encryptedValue = getString("secure_$key", "")
        return if (encryptedValue.isNotEmpty()) {
            try {
                decryptValue(encryptedValue)
            } catch (e: Exception) {
                defaultValue
            }
        } else {
            defaultValue
        }
    }
    
    override suspend fun saveFile(fileName: String, data: ByteArray) {
        val file = File(context.filesDir, fileName)
        file.writeBytes(data)
    }
    
    override suspend fun loadFile(fileName: String): ByteArray? {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) {
            file.readBytes()
        } else {
            null
        }
    }
    
    override suspend fun deleteFile(fileName: String): Boolean {
        val file = File(context.filesDir, fileName)
        return file.delete()
    }
    
    override suspend fun fileExists(fileName: String): Boolean {
        val file = File(context.filesDir, fileName)
        return file.exists()
    }
    
    override suspend fun getFileSize(fileName: String): Long {
        val file = File(context.filesDir, fileName)
        return if (file.exists()) file.length() else 0L
    }
    
    override suspend fun listFiles(): List<String> {
        return context.filesDir.listFiles()?.map { it.name } ?: emptyList()
    }
    
    override suspend fun getDatabaseSize(): Long {
        return database.openHelper.readableDatabase.pageSize * 
               database.openHelper.readableDatabase.pageCount
    }
    
    override suspend fun compactDatabase() {
        database.openHelper.writableDatabase.execSQL("VACUUM")
    }
    
    override suspend fun exportData(): String {
        val allData = mutableMapOf<String, Any>()
        preferences.all.forEach { (key, value) ->
            allData[key] = value ?: ""
        }
        return Json.encodeToString(allData)
    }
    
    override suspend fun importData(data: String): Boolean {
        return try {
            val importedData: Map<String, String> = Json.decodeFromString(data)
            val editor = preferences.edit()
            importedData.forEach { (key, value) ->
                editor.putString(key, value)
            }
            editor.apply()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun backup(): String {
        return exportData()
    }
    
    override suspend fun restore(backupData: String): Boolean {
        return importData(backupData)
    }
    
    private fun encryptValue(value: String): String {
        // 实现Android Keystore加密
        // 这里简化实现，实际应用中应使用Android Keystore
        return android.util.Base64.encodeToString(value.toByteArray(), android.util.Base64.DEFAULT)
    }
    
    private fun decryptValue(encryptedValue: String): String {
        // 实现Android Keystore解密
        // 这里简化实现，实际应用中应使用Android Keystore
        return String(android.util.Base64.decode(encryptedValue, android.util.Base64.DEFAULT))
    }
}

/**
 * Android Room数据库定义
 */
@Database(
    entities = [UnifyEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(UnifyTypeConverters::class)
abstract class UnifyDatabase : RoomDatabase() {
    abstract fun unifyDao(): UnifyDao
}

/**
 * 数据实体
 */
@Entity(tableName = "unify_data")
data class UnifyEntity(
    @PrimaryKey val key: String,
    val value: String,
    val type: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 数据访问对象
 */
@Dao
interface UnifyDao {
    @Query("SELECT * FROM unify_data WHERE key = :key")
    suspend fun get(key: String): UnifyEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: UnifyEntity)
    
    @Query("DELETE FROM unify_data WHERE key = :key")
    suspend fun delete(key: String)
    
    @Query("DELETE FROM unify_data")
    suspend fun deleteAll()
    
    @Query("SELECT * FROM unify_data")
    fun observeAll(): Flow<List<UnifyEntity>>
}

/**
 * 类型转换器
 */
class UnifyTypeConverters {
    @TypeConverter
    fun fromString(value: String): List<String> {
        return Json.decodeFromString(value)
    }
    
    @TypeConverter
    fun fromList(list: List<String>): String {
        return Json.encodeToString(list)
    }
}
