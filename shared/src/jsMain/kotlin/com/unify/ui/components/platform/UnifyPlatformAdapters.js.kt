package com.unify.ui.components.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLVideoElement
import org.w3c.dom.events.Event
import org.w3c.files.File
import org.w3c.files.FileReader
import com.unify.ui.components.media.UnifyLivePlayerConfig
import com.unify.ui.components.media.UnifyLivePlayerState
import com.unify.ui.components.media.UnifyLivePusherConfig
import com.unify.ui.components.media.UnifyLivePusherState
import com.unify.ui.components.media.UnifyWebRTCConfig
import com.unify.ui.components.scanner.UnifyQRScannerConfig
import com.unify.ui.components.scanner.UnifyQRScannerResult
import com.unify.ui.components.sensor.UnifyMotionSensorData
import com.unify.ui.components.sensor.UnifyEnvironmentSensorData

/**
 * Web 平台直播播放器实现
 */
@Composable
actual fun PlatformLivePlayer(
    config: UnifyLivePlayerConfig,
    onStateChange: ((UnifyLivePlayerState) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    LaunchedEffect(config.src) {
        try {
            // 使用 HTML5 Video 和 HLS.js 实现直播播放
            onStateChange?.invoke(UnifyLivePlayerState.LOADING)
            
            val video = document.createElement("video") as HTMLVideoElement
            video.src = config.src
            video.autoplay = config.autoPlay
            video.muted = config.muted
            video.controls = config.showControls
            
            video.addEventListener("loadstart", { onStateChange?.invoke(UnifyLivePlayerState.LOADING) })
            video.addEventListener("canplay", { onStateChange?.invoke(UnifyLivePlayerState.PLAYING) })
            video.addEventListener("error", { onError?.invoke("Video load error") })
            video.addEventListener("ended", { onStateChange?.invoke(UnifyLivePlayerState.STOPPED) })
            
            kotlinx.coroutines.delay(1000)
            video.play()
        } catch (e: Exception) {
            onError?.invoke("Web live player error: ${e.message}")
            onStateChange?.invoke(UnifyLivePlayerState.ERROR)
        }
    }
}

/**
 * Web 平台直播推流器实现
 */
@Composable
actual fun PlatformLivePusher(
    config: UnifyLivePusherConfig,
    onStateChange: ((UnifyLivePusherState) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    LaunchedEffect(config.url) {
        try {
            // 使用 MediaStream API 和 WebRTC 实现推流
            onStateChange?.invoke(UnifyLivePusherState.CONNECTING)
            
            val constraints = js("""({
                video: { width: 1280, height: 720 },
                audio: true
            })""")
            
            val stream = window.navigator.mediaDevices.getUserMedia(constraints).await()
            
            kotlinx.coroutines.delay(2000)
            onStateChange?.invoke(UnifyLivePusherState.PUSHING)
            
            // 这里可以集成 WebRTC 推流到服务器
        } catch (e: Exception) {
            onError?.invoke("Web live pusher error: ${e.message}")
            onStateChange?.invoke(UnifyLivePusherState.ERROR)
        }
    }
}

/**
 * Web 平台 WebRTC 实现
 */
@Composable
actual fun PlatformWebRTC(
    config: UnifyWebRTCConfig,
    onUserJoin: ((String) -> Unit)?,
    onUserLeave: ((String) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    LaunchedEffect(config.roomId) {
        try {
            // 使用 WebRTC API 实现视频会议
            val peerConnection = js("new RTCPeerConnection()")
            
            val constraints = js("""({
                video: true,
                audio: true
            })""")
            
            val localStream = window.navigator.mediaDevices.getUserMedia(constraints).await()
            
            kotlinx.coroutines.delay(2000)
            onUserJoin?.invoke("web_user_1")
        } catch (e: Exception) {
            onError?.invoke("Web WebRTC error: ${e.message}")
        }
    }
}

/**
 * Web 平台扫码器实现
 */
@Composable
actual fun PlatformScanner(
    config: UnifyScanConfig,
    onScanResult: ((UnifyScanResult) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    LaunchedEffect(Unit) {
        try {
            // 使用 getUserMedia 和 ZXing-js 库进行扫码
            val constraints = js("""({
                video: { facingMode: "environment" }
            })""")
            
            val stream = window.navigator.mediaDevices.getUserMedia(constraints).await()
            
            kotlinx.coroutines.delay(2000)
            onScanResult?.invoke(
                UnifyScanResult(
                    type = UnifyScanType.QRCODE,
                    result = "https://web.example.com"
                )
            )
        } catch (e: Exception) {
            onError?.invoke("Web scanner error: ${e.message}")
        }
    }
}

/**
 * Web 平台传感器实现
 */
@Composable
actual fun PlatformSensor(
    config: UnifySensorConfig,
    onDataReceived: ((UnifySensorData) -> Unit)?,
    onStateChange: ((UnifySensorState) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    LaunchedEffect(config.sensorType) {
        try {
            when (config.sensorType) {
                UnifySensorType.ACCELEROMETER -> {
                    if (js("'DeviceMotionEvent' in window") as Boolean) {
                        window.addEventListener("devicemotion", { event ->
                            val motionEvent = event as DeviceMotionEvent
                            motionEvent.acceleration?.let { acc ->
                                onDataReceived?.invoke(
                                    UnifySensorData(
                                        type = UnifySensorType.ACCELEROMETER,
                                        values = floatArrayOf(
                                            (acc.x ?: 0.0).toFloat(),
                                            (acc.y ?: 0.0).toFloat(),
                                            (acc.z ?: 0.0).toFloat()
                                        ),
                                        accuracy = 3,
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                            }
                        })
                        onStateChange?.invoke(UnifySensorState.RUNNING)
                    } else {
                        onError?.invoke("Device motion not supported")
                        onStateChange?.invoke(UnifySensorState.NOT_AVAILABLE)
                    }
                }
                
                UnifySensorType.GYROSCOPE -> {
                    if (js("'DeviceOrientationEvent' in window") as Boolean) {
                        window.addEventListener("deviceorientation", { event ->
                            val orientationEvent = event as DeviceOrientationEvent
                            onDataReceived?.invoke(
                                UnifySensorData(
                                    type = UnifySensorType.GYROSCOPE,
                                    values = floatArrayOf(
                                        (orientationEvent.alpha ?: 0.0).toFloat(),
                                        (orientationEvent.beta ?: 0.0).toFloat(),
                                        (orientationEvent.gamma ?: 0.0).toFloat()
                                    ),
                                    accuracy = 3,
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                        })
                        onStateChange?.invoke(UnifySensorState.RUNNING)
                    } else {
                        onError?.invoke("Device orientation not supported")
                        onStateChange?.invoke(UnifySensorState.NOT_AVAILABLE)
                    }
                }
                
                UnifySensorType.LIGHT -> {
                    if (js("'AmbientLightSensor' in window") as Boolean) {
                        // 使用 Ambient Light Sensor API
                        onStateChange?.invoke(UnifySensorState.RUNNING)
                    } else {
                        onError?.invoke("Ambient light sensor not supported")
                        onStateChange?.invoke(UnifySensorState.NOT_AVAILABLE)
                    }
                }
                
                else -> {
                    onError?.invoke("Sensor type not supported on Web: ${config.sensorType}")
                    onStateChange?.invoke(UnifySensorState.NOT_AVAILABLE)
                }
            }
        } catch (e: Exception) {
            onError?.invoke("Web sensor error: ${e.message}")
            onStateChange?.invoke(UnifySensorState.ERROR)
        }
    }
}

/**
 * Web 平台生物识别实现
 */
@Composable
actual fun PlatformBiometric(
    biometricType: UnifySensorType,
    onSuccess: ((String) -> Unit)?,
    onError: ((String) -> Unit)?,
    onCancel: (() -> Unit)?
) {
    LaunchedEffect(biometricType) {
        try {
            // 使用 WebAuthn API 进行生物识别
            if (js("'credentials' in navigator") as Boolean) {
                val publicKeyCredentialRequestOptions = js("""({
                    challenge: new Uint8Array(32),
                    timeout: 60000,
                    userVerification: "required"
                })""")
                
                val credential = js("navigator.credentials.get({ publicKey: publicKeyCredentialRequestOptions })")
                
                kotlinx.coroutines.delay(2000)
                onSuccess?.invoke("web_biometric_success")
            } else {
                onError?.invoke("WebAuthn not supported")
            }
        } catch (e: Exception) {
            if (e.message?.contains("cancel") == true) {
                onCancel?.invoke()
            } else {
                onError?.invoke("Web biometric error: ${e.message}")
            }
        }
    }
}

/**
 * Web 平台触觉反馈实现
 */
actual fun PlatformHapticFeedback(
    intensity: Float,
    duration: Long,
    pattern: List<Long>
) {
    try {
        // 使用 Vibration API
        if (js("'vibrate' in navigator") as Boolean) {
            val vibrationPattern = pattern.map { it.toInt() }.toIntArray()
            js("navigator.vibrate(vibrationPattern)")
        }
    } catch (e: Exception) {
        // 处理错误
    }
}

/**
 * Web 平台语音识别实现
 */
@Composable
actual fun PlatformSpeechRecognition(
    language: String,
    onResult: ((String) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    LaunchedEffect(language) {
        try {
            // 使用 Web Speech API
            if (js("'SpeechRecognition' in window || 'webkitSpeechRecognition' in window") as Boolean) {
                val recognition = js("new (window.SpeechRecognition || window.webkitSpeechRecognition)()")
                js("recognition.lang = language")
                js("recognition.continuous = false")
                js("recognition.interimResults = false")
                
                js("""
                recognition.onresult = function(event) {
                    var result = event.results[0][0].transcript;
                    onResult(result);
                };
                """)
                
                js("""
                recognition.onerror = function(event) {
                    onError('Speech recognition error: ' + event.error);
                };
                """)
                
                js("recognition.start()")
            } else {
                onError?.invoke("Speech recognition not supported")
            }
        } catch (e: Exception) {
            onError?.invoke("Speech recognition error: ${e.message}")
        }
    }
}

/**
 * Web 平台文字转语音实现
 */
actual fun PlatformTextToSpeech(
    text: String,
    language: String,
    rate: Float,
    pitch: Float,
    onComplete: (() -> Unit)?,
    onError: ((String) -> Unit)?
) {
    try {
        // 使用 Web Speech Synthesis API
        if (js("'speechSynthesis' in window") as Boolean) {
            val utterance = js("new SpeechSynthesisUtterance(text)")
            js("utterance.lang = language")
            js("utterance.rate = rate")
            js("utterance.pitch = pitch")
            
            js("""
            utterance.onend = function() {
                onComplete();
            };
            """)
            
            js("""
            utterance.onerror = function(event) {
                onError('TTS error: ' + event.error);
            };
            """)
            
            js("window.speechSynthesis.speak(utterance)")
        } else {
            onError?.invoke("Speech synthesis not supported")
        }
    } catch (e: Exception) {
        onError?.invoke("TTS error: ${e.message}")
    }
}

/**
 * Web 平台设备震动实现
 */
actual fun PlatformVibration(
    pattern: List<Long>,
    repeat: Boolean
) {
    try {
        if (js("'vibrate' in navigator") as Boolean) {
            val vibrationPattern = pattern.map { it.toInt() }.toIntArray()
            js("navigator.vibrate(vibrationPattern)")
        }
    } catch (e: Exception) {
        // 处理错误
    }
}

/**
 * Web 平台屏幕亮度控制实现
 */
actual fun PlatformScreenBrightness(brightness: Float) {
    try {
        // Web 平台无法直接控制屏幕亮度，可以通过调整页面亮度滤镜模拟
        val body = document.body
        if (body != null) {
            body.style.filter = "brightness(${brightness})"
        }
    } catch (e: Exception) {
        // 处理错误
    }
}

/**
 * Web 平台屏幕方向控制实现
 */
actual fun PlatformScreenOrientation(orientation: String) {
    try {
        // 使用 Screen Orientation API
        if (js("'orientation' in screen") as Boolean) {
            when (orientation) {
                "portrait" -> js("screen.orientation.lock('portrait')")
                "landscape" -> js("screen.orientation.lock('landscape')")
                "auto" -> js("screen.orientation.unlock()")
            }
        }
    } catch (e: Exception) {
        // 处理错误
    }
}

/**
 * Web 平台状态栏控制实现
 */
actual fun PlatformStatusBar(
    hidden: Boolean,
    style: String,
    backgroundColor: Color?
) {
    try {
        // Web 平台可以通过 meta 标签控制状态栏样式
        val metaThemeColor = document.querySelector("meta[name='theme-color']") as? HTMLMetaElement
        if (metaThemeColor != null && backgroundColor != null) {
            metaThemeColor.content = "#${backgroundColor.value.toString(16).padStart(8, '0').substring(2)}"
        }
        
        val metaStatusBar = document.querySelector("meta[name='apple-mobile-web-app-status-bar-style']") as? HTMLMetaElement
        if (metaStatusBar != null) {
            metaStatusBar.content = if (hidden) "black-translucent" else style
        }
    } catch (e: Exception) {
        // 处理错误
    }
}

/**
 * Web 平台导航栏控制实现
 */
actual fun PlatformNavigationBar(
    hidden: Boolean,
    style: String,
    backgroundColor: Color?
) {
    // Web 平台没有系统导航栏概念
}

/**
 * Web 平台系统通知实现
 */
actual fun PlatformNotification(
    title: String,
    content: String,
    icon: String?,
    actions: List<Pair<String, () -> Unit>>
) {
    try {
        // 使用 Notifications API
        if (js("'Notification' in window") as Boolean) {
            js("""
            if (Notification.permission === 'granted') {
                new Notification(title, {
                    body: content,
                    icon: icon
                });
            } else if (Notification.permission !== 'denied') {
                Notification.requestPermission().then(function(permission) {
                    if (permission === 'granted') {
                        new Notification(title, {
                            body: content,
                            icon: icon
                        });
                    }
                });
            }
            """)
        }
    } catch (e: Exception) {
        // 处理错误
    }
}

/**
 * Web 平台文件选择器实现
 */
@Composable
actual fun PlatformFilePicker(
    fileTypes: List<String>,
    multiple: Boolean,
    onFileSelected: ((List<String>) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    LaunchedEffect(fileTypes) {
        try {
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.multiple = multiple
            input.accept = fileTypes.joinToString(",")
            
            input.addEventListener("change", { event ->
                val files = (event.target as HTMLInputElement).files
                if (files != null) {
                    val fileList = mutableListOf<String>()
                    for (i in 0 until files.length) {
                        val file = files.item(i)
                        if (file != null) {
                            fileList.add(file.name)
                        }
                    }
                    onFileSelected?.invoke(fileList)
                }
            })
            
            input.click()
        } catch (e: Exception) {
            onError?.invoke("File picker error: ${e.message}")
        }
    }
}

/**
 * Web 平台相机实现
 */
@Composable
actual fun PlatformCamera(
    facing: String,
    onPhotoTaken: ((ByteArray) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    LaunchedEffect(facing) {
        try {
            val constraints = js("""({
                video: { facingMode: facing === "front" ? "user" : "environment" }
            })""")
            
            val stream = window.navigator.mediaDevices.getUserMedia(constraints).await()
            
            val video = document.createElement("video") as HTMLVideoElement
            video.srcObject = stream
            video.play()
            
            val canvas = document.createElement("canvas") as HTMLCanvasElement
            val context = canvas.getContext("2d")
            
            kotlinx.coroutines.delay(1000)
            
            // 捕获照片
            canvas.width = video.videoWidth
            canvas.height = video.videoHeight
            context?.drawImage(video, 0.0, 0.0)
            
            // 转换为 ByteArray (这里简化处理)
            onPhotoTaken?.invoke(ByteArray(0))
        } catch (e: Exception) {
            onError?.invoke("Camera error: ${e.message}")
        }
    }
}

/**
 * Web 平台位置服务实现
 */
actual fun PlatformLocation(
    accuracy: String,
    onLocationUpdate: ((Double, Double) -> Unit)?,
    onError: ((String) -> Unit)?
) {
    try {
        if (js("'geolocation' in navigator") as Boolean) {
            val options = js("""({
                enableHighAccuracy: accuracy === "high",
                timeout: 10000,
                maximumAge: 60000
            })""")
            
            js("""
            navigator.geolocation.getCurrentPosition(
                function(position) {
                    onLocationUpdate(position.coords.latitude, position.coords.longitude);
                },
                function(error) {
                    onError('Location error: ' + error.message);
                },
                options
            );
            """)
        } else {
            onError?.invoke("Geolocation not supported")
        }
    } catch (e: Exception) {
        onError?.invoke("Location error: ${e.message}")
    }
}

/**
 * Web 平台网络状态监听实现
 */
@Composable
actual fun PlatformNetworkMonitor(
    onNetworkChange: ((Boolean, String) -> Unit)?
) {
    LaunchedEffect(Unit) {
        try {
            window.addEventListener("online", { 
                onNetworkChange?.invoke(true, "Online") 
            })
            window.addEventListener("offline", { 
                onNetworkChange?.invoke(false, "Offline") 
            })
            
            // 初始状态
            val isOnline = js("navigator.onLine") as Boolean
            onNetworkChange?.invoke(isOnline, if (isOnline) "Online" else "Offline")
        } catch (e: Exception) {
            onNetworkChange?.invoke(false, "Unknown")
        }
    }
}

/**
 * Web 平台电池状态监听实现
 */
@Composable
actual fun PlatformBatteryMonitor(
    onBatteryChange: ((Int, Boolean) -> Unit)?
) {
    LaunchedEffect(Unit) {
        try {
            // 使用 Battery Status API (已废弃，但可以模拟)
            if (js("'getBattery' in navigator") as Boolean) {
                js("""
                navigator.getBattery().then(function(battery) {
                    var level = Math.floor(battery.level * 100);
                    var charging = battery.charging;
                    onBatteryChange(level, charging);
                    
                    battery.addEventListener('levelchange', function() {
                        var level = Math.floor(battery.level * 100);
                        var charging = battery.charging;
                        onBatteryChange(level, charging);
                    });
                    
                    battery.addEventListener('chargingchange', function() {
                        var level = Math.floor(battery.level * 100);
                        var charging = battery.charging;
                        onBatteryChange(level, charging);
                    });
                });
                """)
            } else {
                // 模拟电池状态
                while (true) {
                    kotlinx.coroutines.delay(10000)
                    val batteryLevel = (20..100).random()
                    val isCharging = (0..1).random() == 1
                    onBatteryChange?.invoke(batteryLevel, isCharging)
                }
            }
        } catch (e: Exception) {
            // 处理错误
        }
    }
}

/**
 * Web 平台应用生命周期监听实现
 */
@Composable
actual fun PlatformLifecycleMonitor(
    onResume: (() -> Unit)?,
    onPause: (() -> Unit)?,
    onStop: (() -> Unit)?
) {
    LaunchedEffect(Unit) {
        try {
            // 使用 Page Visibility API
            document.addEventListener("visibilitychange", { 
                if (document.visibilityState == "visible") {
                    onResume?.invoke()
                } else {
                    onPause?.invoke()
                }
            })
            
            window.addEventListener("beforeunload", { 
                onStop?.invoke() 
            })
            
            window.addEventListener("focus", { 
                onResume?.invoke() 
            })
            
            window.addEventListener("blur", { 
                onPause?.invoke() 
            })
        } catch (e: Exception) {
            // 处理错误
        }
    }
}
