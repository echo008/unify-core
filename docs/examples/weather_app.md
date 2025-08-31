# 天气应用示例

## 📱 项目概述

天气应用是一个功能丰富的跨平台应用，展示了网络请求、JSON解析、位置服务、错误处理和复杂UI设计等高级功能。

## 🎯 功能特性

- ✅ **实时天气** - 获取当前天气信息
- ✅ **天气预报** - 7天天气预报
- ✅ **位置服务** - 自动获取当前位置
- ✅ **城市搜索** - 搜索全球城市天气
- ✅ **收藏城市** - 保存常用城市
- ✅ **离线缓存** - 缓存天气数据
- ✅ **美观UI** - 天气主题界面设计

## 🏗️ 项目结构

```
weather-app/
├── shared/
│   └── src/
│       └── commonMain/
│           ├── kotlin/
│           │   ├── WeatherApp.kt
│           │   ├── WeatherViewModel.kt
│           │   ├── WeatherRepository.kt
│           │   ├── api/
│           │   │   └── WeatherApiService.kt
│           │   └── models/
│           │       ├── Weather.kt
│           │       └── City.kt
│           └── resources/
│               └── weather_icons/
├── androidApp/
├── iosApp/
├── webApp/
└── desktopApp/
```

## 💻 核心实现

### Weather.kt - 数据模型

```kotlin
package com.unify.weather.models

import kotlinx.serialization.Serializable

@Serializable
data class WeatherData(
    val location: String,
    val country: String,
    val temperature: Double,
    val feelsLike: Double,
    val description: String,
    val humidity: Int,
    val windSpeed: Double,
    val windDirection: Int,
    val pressure: Double,
    val visibility: Double,
    val uvIndex: Double,
    val icon: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class WeatherForecast(
    val date: String,
    val maxTemp: Double,
    val minTemp: Double,
    val description: String,
    val icon: String,
    val humidity: Int,
    val windSpeed: Double
)

@Serializable
data class City(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val isFavorite: Boolean = false
)

enum class WeatherCondition {
    SUNNY, CLOUDY, RAINY, SNOWY, STORMY, FOGGY
}
```

### WeatherApp.kt - 主应用组件

```kotlin
package com.unify.weather

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherApp() {
    val viewModel = remember { WeatherViewModel() }
    val uiState by viewModel.uiState.collectAsState()
    var showCitySearch by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        viewModel.loadCurrentLocationWeather()
    }
    
    MaterialTheme {
        Scaffold(
            topBar = {
                WeatherTopBar(
                    currentCity = uiState.currentWeather?.location ?: "未知位置",
                    onSearchClick = { showCitySearch = true },
                    onLocationClick = { viewModel.loadCurrentLocationWeather() }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    uiState.isLoading -> {
                        LoadingScreen()
                    }
                    
                    uiState.error != null -> {
                        ErrorScreen(
                            error = uiState.error,
                            onRetry = { viewModel.loadCurrentLocationWeather() }
                        )
                    }
                    
                    uiState.currentWeather != null -> {
                        WeatherContent(
                            weather = uiState.currentWeather,
                            forecast = uiState.forecast,
                            favoriteCities = uiState.favoriteCities,
                            onCityClick = { city -> viewModel.loadCityWeather(city) },
                            onFavoriteToggle = { city -> viewModel.toggleFavoriteCity(city) }
                        )
                    }
                }
                
                if (showCitySearch) {
                    CitySearchDialog(
                        onDismiss = { showCitySearch = false },
                        onCitySelected = { city ->
                            viewModel.loadCityWeather(city)
                            showCitySearch = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherTopBar(
    currentCity: String,
    onSearchClick: () -> Unit,
    onLocationClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "天气",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = currentCity,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = "搜索城市")
            }
            IconButton(onClick = onLocationClick) {
                Icon(Icons.Default.LocationOn, contentDescription = "当前位置")
            }
        }
    )
}

@Composable
fun WeatherContent(
    weather: WeatherData,
    forecast: List<WeatherForecast>,
    favoriteCities: List<City>,
    onCityClick: (City) -> Unit,
    onFavoriteToggle: (City) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CurrentWeatherCard(weather = weather)
        }
        
        item {
            WeatherDetailsCard(weather = weather)
        }
        
        if (forecast.isNotEmpty()) {
            item {
                WeatherForecastCard(forecast = forecast)
            }
        }
        
        if (favoriteCities.isNotEmpty()) {
            item {
                FavoriteCitiesCard(
                    cities = favoriteCities,
                    onCityClick = onCityClick,
                    onFavoriteToggle = onFavoriteToggle
                )
            }
        }
    }
}

@Composable
fun CurrentWeatherCard(weather: WeatherData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 天气图标
            WeatherIcon(
                icon = weather.icon,
                modifier = Modifier.size(80.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 温度
            Text(
                text = "${weather.temperature.toInt()}°",
                fontSize = 64.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            // 天气描述
            Text(
                text = weather.description,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            // 体感温度
            Text(
                text = "体感 ${weather.feelsLike.toInt()}°",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun WeatherDetailsCard(weather: WeatherData) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "详细信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherDetailItem(
                    icon = Icons.Default.Water,
                    label = "湿度",
                    value = "${weather.humidity}%"
                )
                
                WeatherDetailItem(
                    icon = Icons.Default.Air,
                    label = "风速",
                    value = "${weather.windSpeed} km/h"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherDetailItem(
                    icon = Icons.Default.Compress,
                    label = "气压",
                    value = "${weather.pressure.toInt()} hPa"
                )
                
                WeatherDetailItem(
                    icon = Icons.Default.Visibility,
                    label = "能见度",
                    value = "${weather.visibility} km"
                )
            }
        }
    }
}

@Composable
fun WeatherDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WeatherForecastCard(forecast: List<WeatherForecast>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "7天预报",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(forecast) { day ->
                    ForecastItem(forecast = day)
                }
            }
        }
    }
}

@Composable
fun ForecastItem(forecast: WeatherForecast) {
    Card(
        modifier = Modifier.width(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = forecast.date,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            WeatherIcon(
                icon = forecast.icon,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${forecast.maxTemp.toInt()}°",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "${forecast.minTemp.toInt()}°",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

### WeatherViewModel.kt - 状态管理

```kotlin
package com.unify.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WeatherUiState(
    val currentWeather: WeatherData? = null,
    val forecast: List<WeatherForecast> = emptyList(),
    val favoriteCities: List<City> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class WeatherViewModel : ViewModel() {
    private val repository = WeatherRepository()
    private val locationManager = LocationManager()
    
    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()
    
    init {
        loadFavoriteCities()
    }
    
    fun loadCurrentLocationWeather() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // 获取当前位置
                val location = locationManager.getCurrentLocation()
                if (location != null) {
                    loadWeatherByCoordinates(location.latitude, location.longitude)
                } else {
                    // 如果无法获取位置，使用默认城市
                    loadCityWeather(City("北京", "中国", 39.9042, 116.4074))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "获取位置失败: ${e.message}"
                )
            }
        }
    }
    
    fun loadCityWeather(city: City) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                loadWeatherByCoordinates(city.latitude, city.longitude)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "获取天气失败: ${e.message}"
                )
            }
        }
    }
    
    private suspend fun loadWeatherByCoordinates(lat: Double, lon: Double) {
        try {
            val weather = repository.getCurrentWeather(lat, lon)
            val forecast = repository.getWeatherForecast(lat, lon)
            
            _uiState.value = _uiState.value.copy(
                currentWeather = weather,
                forecast = forecast,
                isLoading = false,
                error = null
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "加载天气数据失败: ${e.message}"
            )
        }
    }
    
    fun toggleFavoriteCity(city: City) {
        viewModelScope.launch {
            try {
                repository.toggleFavoriteCity(city)
                loadFavoriteCities()
            } catch (e: Exception) {
                Logger.e("WeatherViewModel", "切换收藏城市失败", e)
            }
        }
    }
    
    private fun loadFavoriteCities() {
        viewModelScope.launch {
            try {
                val cities = repository.getFavoriteCities()
                _uiState.value = _uiState.value.copy(favoriteCities = cities)
            } catch (e: Exception) {
                Logger.e("WeatherViewModel", "加载收藏城市失败", e)
            }
        }
    }
}
```

### WeatherRepository.kt - 数据层

```kotlin
package com.unify.weather

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository {
    private val apiService = WeatherApiService()
    private val cache = CacheManager()
    private val storage = KeyValueStorage()
    
    companion object {
        private const val CACHE_TTL = 10 * 60 * 1000L // 10分钟
        private const val FAVORITE_CITIES_KEY = "favorite_cities"
    }
    
    suspend fun getCurrentWeather(lat: Double, lon: Double): WeatherData = withContext(Dispatchers.IO) {
        val cacheKey = "weather_${lat}_${lon}"
        
        // 尝试从缓存获取
        cache.get(cacheKey)?.let { cachedWeather ->
            return@withContext cachedWeather as WeatherData
        }
        
        // 从网络获取
        val weather = apiService.getCurrentWeather(lat, lon)
        
        // 缓存结果
        cache.put(cacheKey, weather, CACHE_TTL)
        
        weather
    }
    
    suspend fun getWeatherForecast(lat: Double, lon: Double): List<WeatherForecast> = withContext(Dispatchers.IO) {
        val cacheKey = "forecast_${lat}_${lon}"
        
        // 尝试从缓存获取
        cache.get(cacheKey)?.let { cachedForecast ->
            return@withContext cachedForecast as List<WeatherForecast>
        }
        
        // 从网络获取
        val forecast = apiService.getWeatherForecast(lat, lon)
        
        // 缓存结果
        cache.put(cacheKey, forecast, CACHE_TTL)
        
        forecast
    }
    
    suspend fun searchCities(query: String): List<City> = withContext(Dispatchers.IO) {
        apiService.searchCities(query)
    }
    
    suspend fun getFavoriteCities(): List<City> = withContext(Dispatchers.IO) {
        val jsonString = storage.getString(FAVORITE_CITIES_KEY)
        if (jsonString != null) {
            try {
                Json.decodeFromString<List<City>>(jsonString)
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    suspend fun toggleFavoriteCity(city: City) = withContext(Dispatchers.IO) {
        val currentFavorites = getFavoriteCities().toMutableList()
        val existingCity = currentFavorites.find { it.name == city.name && it.country == city.country }
        
        if (existingCity != null) {
            currentFavorites.remove(existingCity)
        } else {
            currentFavorites.add(city.copy(isFavorite = true))
        }
        
        val jsonString = Json.encodeToString(currentFavorites)
        storage.putString(FAVORITE_CITIES_KEY, jsonString)
    }
}
```

### WeatherApiService.kt - 网络服务

```kotlin
package com.unify.weather.api

import kotlinx.serialization.json.Json

class WeatherApiService {
    private val httpClient = HttpClient(
        baseUrl = "https://api.openweathermap.org/data/2.5",
        timeout = 15000L
    )
    
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }
    
    companion object {
        private const val API_KEY = "YOUR_API_KEY" // 替换为实际的API密钥
    }
    
    suspend fun getCurrentWeather(lat: Double, lon: Double): WeatherData {
        val response = httpClient.get(
            url = "/weather",
            queryParams = mapOf(
                "lat" to lat.toString(),
                "lon" to lon.toString(),
                "appid" to API_KEY,
                "units" to "metric",
                "lang" to "zh_cn"
            )
        )
        
        if (!response.isSuccessful) {
            throw Exception("天气API请求失败: ${response.status}")
        }
        
        return parseWeatherResponse(response.body)
    }
    
    suspend fun getWeatherForecast(lat: Double, lon: Double): List<WeatherForecast> {
        val response = httpClient.get(
            url = "/forecast",
            queryParams = mapOf(
                "lat" to lat.toString(),
                "lon" to lon.toString(),
                "appid" to API_KEY,
                "units" to "metric",
                "lang" to "zh_cn"
            )
        )
        
        if (!response.isSuccessful) {
            throw Exception("天气预报API请求失败: ${response.status}")
        }
        
        return parseForecastResponse(response.body)
    }
    
    suspend fun searchCities(query: String): List<City> {
        val response = httpClient.get(
            url = "/find",
            queryParams = mapOf(
                "q" to query,
                "appid" to API_KEY,
                "units" to "metric",
                "lang" to "zh_cn"
            )
        )
        
        if (!response.isSuccessful) {
            throw Exception("城市搜索API请求失败: ${response.status}")
        }
        
        return parseCitiesResponse(response.body)
    }
    
    private fun parseWeatherResponse(jsonString: String): WeatherData {
        val jsonElement = json.parseToJsonElement(jsonString)
        val jsonObject = jsonElement.jsonObject
        
        return WeatherData(
            location = jsonObject["name"]?.jsonPrimitive?.content ?: "",
            country = jsonObject["sys"]?.jsonObject?.get("country")?.jsonPrimitive?.content ?: "",
            temperature = jsonObject["main"]?.jsonObject?.get("temp")?.jsonPrimitive?.double ?: 0.0,
            feelsLike = jsonObject["main"]?.jsonObject?.get("feels_like")?.jsonPrimitive?.double ?: 0.0,
            description = jsonObject["weather"]?.jsonArray?.get(0)?.jsonObject?.get("description")?.jsonPrimitive?.content ?: "",
            humidity = jsonObject["main"]?.jsonObject?.get("humidity")?.jsonPrimitive?.int ?: 0,
            windSpeed = jsonObject["wind"]?.jsonObject?.get("speed")?.jsonPrimitive?.double ?: 0.0,
            windDirection = jsonObject["wind"]?.jsonObject?.get("deg")?.jsonPrimitive?.int ?: 0,
            pressure = jsonObject["main"]?.jsonObject?.get("pressure")?.jsonPrimitive?.double ?: 0.0,
            visibility = (jsonObject["visibility"]?.jsonPrimitive?.double ?: 0.0) / 1000.0,
            uvIndex = 0.0, // UV指数需要额外的API调用
            icon = jsonObject["weather"]?.jsonArray?.get(0)?.jsonObject?.get("icon")?.jsonPrimitive?.content ?: ""
        )
    }
    
    private fun parseForecastResponse(jsonString: String): List<WeatherForecast> {
        // 解析预报数据的实现
        return emptyList() // 简化实现
    }
    
    private fun parseCitiesResponse(jsonString: String): List<City> {
        // 解析城市搜索结果的实现
        return emptyList() // 简化实现
    }
}
```

## 🧪 测试实现

```kotlin
class WeatherViewModelTest {
    @Test
    fun `加载天气数据应该更新UI状态`() = runTest {
        val viewModel = WeatherViewModel()
        val testCity = City("北京", "中国", 39.9042, 116.4074)
        
        viewModel.loadCityWeather(testCity)
        
        val state = viewModel.uiState.value
        assertNotNull(state.currentWeather)
        assertEquals("北京", state.currentWeather?.location)
    }
}
```

---

这个天气应用示例展示了 Unify KMP 框架在处理网络请求、数据缓存、位置服务和复杂UI设计方面的能力，是学习高级跨平台开发技术的优秀案例。
