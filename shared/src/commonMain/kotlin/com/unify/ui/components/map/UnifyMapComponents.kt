package com.unify.ui.components.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unify.core.utils.UnifyStringUtils
import kotlinx.coroutines.delay

/**
 * Unify地图组件 - 跨平台统一地图系统
 * 支持8大平台的地图显示、标记、导航等功能
 */

/**
 * 地理坐标数据类
 */
data class UnifyLatLng(
    val latitude: Double,
    val longitude: Double
) {
    companion object {
        val BEIJING = UnifyLatLng(39.9042, 116.4074)
        val SHANGHAI = UnifyLatLng(31.2304, 121.4737)
        val GUANGZHOU = UnifyLatLng(23.1291, 113.2644)
        val SHENZHEN = UnifyLatLng(22.5431, 114.0579)
    }
}

/**
 * 地图标记数据类
 */
data class UnifyMapMarker(
    val id: String,
    val position: UnifyLatLng,
    val title: String,
    val snippet: String? = null,
    val icon: ImageVector = Icons.Default.LocationOn,
    val color: Color = Color.Red,
    val isVisible: Boolean = true,
    val isDraggable: Boolean = false,
    val zIndex: Float = 0f,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * 地图区域数据类
 */
data class UnifyMapBounds(
    val southwest: UnifyLatLng,
    val northeast: UnifyLatLng
) {
    val center: UnifyLatLng
        get() = UnifyLatLng(
            (southwest.latitude + northeast.latitude) / 2,
            (southwest.longitude + northeast.longitude) / 2
        )
}

/**
 * 地图类型枚举
 */
enum class UnifyMapType {
    NORMAL,      // 普通地图
    SATELLITE,   // 卫星地图
    TERRAIN,     // 地形地图
    HYBRID       // 混合地图
}

/**
 * 地图配置
 */
data class UnifyMapConfig(
    val mapType: UnifyMapType = UnifyMapType.NORMAL,
    val isZoomControlsEnabled: Boolean = true,
    val isCompassEnabled: Boolean = true,
    val isMyLocationEnabled: Boolean = false,
    val isTrafficEnabled: Boolean = false,
    val isBuildingsEnabled: Boolean = true,
    val isIndoorEnabled: Boolean = false,
    val minZoomLevel: Float = 2f,
    val maxZoomLevel: Float = 20f,
    val initialZoomLevel: Float = 10f,
    val isScrollGesturesEnabled: Boolean = true,
    val isZoomGesturesEnabled: Boolean = true,
    val isRotateGesturesEnabled: Boolean = true,
    val isTiltGesturesEnabled: Boolean = true
)

/**
 * 地图状态
 */
@Stable
class UnifyMapState {
    var center by mutableStateOf(UnifyLatLng.BEIJING)
    var zoomLevel by mutableStateOf(10f)
    var bearing by mutableStateOf(0f)
    var tilt by mutableStateOf(0f)
    var bounds by mutableStateOf<UnifyMapBounds?>(null)
    var isLoading by mutableStateOf(false)
    
    fun animateToLocation(
        location: UnifyLatLng,
        zoom: Float = zoomLevel,
        duration: Long = 1000L
    ) {
        // 模拟动画效果
        center = location
        zoomLevel = zoom
    }
    
    fun animateToBounds(
        bounds: UnifyMapBounds,
        padding: Dp = 50.dp
    ) {
        this.bounds = bounds
        center = bounds.center
        // 根据边界计算合适的缩放级别
        zoomLevel = calculateZoomLevel(bounds)
    }
    
    private fun calculateZoomLevel(bounds: UnifyMapBounds): Float {
        val latDiff = kotlin.math.abs(bounds.northeast.latitude - bounds.southwest.latitude)
        val lngDiff = kotlin.math.abs(bounds.northeast.longitude - bounds.southwest.longitude)
        val maxDiff = maxOf(latDiff, lngDiff)
        
        return when {
            maxDiff > 10 -> 4f
            maxDiff > 5 -> 6f
            maxDiff > 2 -> 8f
            maxDiff > 1 -> 10f
            maxDiff > 0.5 -> 12f
            maxDiff > 0.1 -> 14f
            else -> 16f
        }
    }
}

/**
 * 创建地图状态
 */
@Composable
fun rememberUnifyMapState(
    initialCenter: UnifyLatLng = UnifyLatLng.BEIJING,
    initialZoom: Float = 10f
): UnifyMapState {
    return remember {
        UnifyMapState().apply {
            center = initialCenter
            zoomLevel = initialZoom
        }
    }
}

/**
 * 统一地图组件
 */
@Composable
fun UnifyMap(
    modifier: Modifier = Modifier,
    state: UnifyMapState = rememberUnifyMapState(),
    config: UnifyMapConfig = UnifyMapConfig(),
    markers: List<UnifyMapMarker> = emptyList(),
    onMapClick: ((UnifyLatLng) -> Unit)? = null,
    onMapLongClick: ((UnifyLatLng) -> Unit)? = null,
    onMarkerClick: ((UnifyMapMarker) -> Unit)? = null,
    onCameraMove: ((UnifyLatLng, Float) -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Gray.copy(alpha = 0.1f))
            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
    ) {
        // 地图背景
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    when (config.mapType) {
                        UnifyMapType.NORMAL -> Color(0xFFF5F5DC)
                        UnifyMapType.SATELLITE -> Color(0xFF2E4057)
                        UnifyMapType.TERRAIN -> Color(0xFF8FBC8F)
                        UnifyMapType.HYBRID -> Color(0xFF696969)
                    }
                )
                .clickable {
                    // 模拟地图点击
                    onMapClick?.invoke(state.center)
                }
        ) {
            // 地图网格线（模拟）
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridSize = 50.dp.toPx()
                val width = size.width
                val height = size.height
                
                // 绘制网格
                for (x in 0..width.toInt() step gridSize.toInt()) {
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.2f),
                        start = androidx.compose.ui.geometry.Offset(x.toFloat(), 0f),
                        end = androidx.compose.ui.geometry.Offset(x.toFloat(), height),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                
                for (y in 0..height.toInt() step gridSize.toInt()) {
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.2f),
                        start = androidx.compose.ui.geometry.Offset(0f, y.toFloat()),
                        end = androidx.compose.ui.geometry.Offset(width, y.toFloat()),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        }
        
        // 标记层
        markers.filter { it.isVisible }.forEach { marker ->
            UnifyMapMarkerComponent(
                marker = marker,
                modifier = Modifier.align(Alignment.Center),
                onClick = { onMarkerClick?.invoke(marker) }
            )
        }
        
        // 控制层
        if (config.isZoomControlsEnabled) {
            UnifyMapZoomControls(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                onZoomIn = { 
                    state.zoomLevel = (state.zoomLevel + 1f).coerceAtMost(config.maxZoomLevel)
                },
                onZoomOut = { 
                    state.zoomLevel = (state.zoomLevel - 1f).coerceAtLeast(config.minZoomLevel)
                }
            )
        }
        
        if (config.isCompassEnabled) {
            UnifyMapCompass(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                bearing = state.bearing,
                onClick = { state.bearing = 0f }
            )
        }
        
        // 加载指示器
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // 地图信息
        UnifyMapInfo(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            center = state.center,
            zoomLevel = state.zoomLevel,
            mapType = config.mapType
        )
    }
}

/**
 * 地图标记组件
 */
@Composable
fun UnifyMapMarkerComponent(
    marker: UnifyMapMarker,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.clickable { onClick?.invoke() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 标记图标
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(marker.color, CircleShape)
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = marker.icon,
                contentDescription = marker.title,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        
        // 标记标题
        if (marker.title.isNotEmpty()) {
            Card(
                modifier = Modifier.padding(top = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = marker.title,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 地图缩放控制
 */
@Composable
fun UnifyMapZoomControls(
    modifier: Modifier = Modifier,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FloatingActionButton(
            onClick = onZoomIn,
            modifier = Modifier.size(40.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "放大",
                modifier = Modifier.size(20.dp)
            )
        }
        
        FloatingActionButton(
            onClick = onZoomOut,
            modifier = Modifier.size(40.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "缩小",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 地图指南针
 */
@Composable
fun UnifyMapCompass(
    modifier: Modifier = Modifier,
    bearing: Float,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(40.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Icon(
            imageVector = Icons.Default.Navigation,
            contentDescription = "指南针",
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer {
                    rotationZ = bearing
                }
        )
    }
}

/**
 * 地图信息显示
 */
@Composable
fun UnifyMapInfo(
    modifier: Modifier = Modifier,
    center: UnifyLatLng,
    zoomLevel: Float,
    mapType: UnifyMapType
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "经度: ${center.longitude}",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 10.sp
            )
            Text(
                text = "纬度: ${center.latitude}",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 10.sp
            )
            Text(
                text = "缩放: $zoomLevel",
                color = Color.White,
                fontSize = 10.sp
            )
            Text(
                text = "类型: ${mapType.name}",
                color = Color.White,
                fontSize = 10.sp
            )
        }
    }
}

/**
 * 地图搜索结果数据类
 */
data class UnifyMapSearchResult(
    val name: String,
    val location: UnifyLocation,
    val distance: Float? = null
)

/**
 * 地理位置数据类
 */
data class UnifyLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f
)

/**
 * 地图搜索组件
 */
@Composable
fun UnifyMapSearch(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchResults: List<UnifyMapSearchResult> = emptyList(),
    onResultClick: (UnifyMapSearchResult) -> Unit,
    onSearch: (String) -> Unit
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("搜索地点...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "搜索"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "清除"
                        )
                    }
                }
            },
            singleLine = true
        )
        
        if (searchResults.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(searchResults) { result ->
                        UnifyMapSearchResultItem(
                            result = result,
                            onClick = { onResultClick(result) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 搜索结果项组件
 */
@Composable
fun UnifyMapSearchResultItem(
    result: UnifyMapSearchResult,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(result.name) },
        supportingContent = { Text(UnifyStringUtils.format("%.6f", result.location.latitude)) },
        trailingContent = {
            result.distance?.let { distance ->
                Text(
                    text = "${UnifyStringUtils.format("%.2f", distance)}km",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
}

/**
 * 地图路线规划组件
 */
@Composable
fun UnifyMapRoute(
    startLocation: UnifyLatLng,
    endLocation: UnifyLatLng,
    waypoints: List<UnifyLatLng> = emptyList(),
    routeType: UnifyRouteType = UnifyRouteType.DRIVING,
    onRouteCalculated: (UnifyMapRoute) -> Unit
) {
    LaunchedEffect(startLocation, endLocation, waypoints, routeType) {
        // 模拟路线计算
        delay(1000)
        val route = UnifyMapRoute(
            points = listOf(startLocation) + waypoints + listOf(endLocation),
            distance = calculateDistance(startLocation, endLocation),
            duration = calculateDuration(startLocation, endLocation, routeType),
            instructions = generateInstructions(startLocation, endLocation)
        )
        onRouteCalculated(route)
    }
}

/**
 * 路线类型枚举
 */
enum class UnifyRouteType {
    DRIVING,    // 驾车
    WALKING,    // 步行
    CYCLING,    // 骑行
    TRANSIT     // 公交
}

/**
 * 地图路线数据类
 */
data class UnifyMapRoute(
    val points: List<UnifyLatLng>,
    val distance: Double, // 距离（米）
    val duration: Long,   // 时长（秒）
    val instructions: List<String>
)

/**
 * 计算两点间距离（简化版）
 */
private fun calculateDistance(start: UnifyLatLng, end: UnifyLatLng): Double {
    val latDiff = end.latitude - start.latitude
    val lngDiff = end.longitude - start.longitude
    return kotlin.math.sqrt(latDiff * latDiff + lngDiff * lngDiff) * 111000 // 粗略计算
}

/**
 * 计算路线时长（简化版）
 */
private fun calculateDuration(start: UnifyLatLng, end: UnifyLatLng, type: UnifyRouteType): Long {
    val distance = calculateDistance(start, end)
    val speed = when (type) {
        UnifyRouteType.DRIVING -> 50.0 // 50km/h
        UnifyRouteType.WALKING -> 5.0  // 5km/h
        UnifyRouteType.CYCLING -> 15.0 // 15km/h
        UnifyRouteType.TRANSIT -> 30.0 // 30km/h
    }
    return ((distance / 1000) / speed * 3600).toLong()
}

/**
 * 生成导航指令（简化版）
 */
private fun generateInstructions(start: UnifyLatLng, end: UnifyLatLng): List<String> {
    return listOf(
        "从起点出发",
        "直行 ${(calculateDistance(start, end) / 1000.0).toString()}公里",
        "到达目的地"
    )
}

/**
 * 默认地图配置
 */
val DefaultMapConfig = UnifyMapConfig()
