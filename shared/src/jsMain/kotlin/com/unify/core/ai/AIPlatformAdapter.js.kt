package com.unify.core.ai

import kotlinx.coroutines.await
import kotlinx.browser.window
import org.khronos.webgl.Uint8Array
import org.w3c.dom.mediacapture.MediaStream
import org.w3c.dom.mediacapture.MediaStreamConstraints
import kotlin.js.Promise

/**
 * JavaScript/Web平台AI适配器实现
 */
actual class AIPlatformAdapter {
    private var mediaStream: MediaStream? = null
    private var speechSynthesis: dynamic = null
    private var speechRecognition: dynamic = null
    private var isInitialized = false
    
    actual suspend fun initialize() {
        try {
            // 初始化Web Speech API
            speechSynthesis = window.asDynamic().speechSynthesis
            
            // 初始化语音识别
            val SpeechRecognition = window.asDynamic().SpeechRecognition 
                ?: window.asDynamic().webkitSpeechRecognition
            
            if (SpeechRecognition != null) {
                speechRecognition = js("new SpeechRecognition()")
                speechRecognition.continuous = true
                speechRecognition.interimResults = true
                speechRecognition.lang = "zh-CN"
            }
            
            isInitialized = true
        } catch (e: Exception) {
            console.log("Web AI适配器初始化失败: ${e.message}")
        }
    }
    
    actual fun processAudioInput(audioData: ByteArray): String {
        // 将音频数据转换为Base64字符串
        val uint8Array = Uint8Array(audioData.size)
        audioData.forEachIndexed { index, byte ->
            uint8Array[index] = byte
        }
        
        // 使用Web API转换为Base64
        return js("btoa(String.fromCharCode.apply(null, arguments[0]))") as String
    }
    
    actual fun processAudioOutput(audioData: ByteArray): ByteArray {
        // Web平台的音频输出处理
        // 可能需要使用Web Audio API
        return audioData
    }
    
    actual fun getSupportedLanguages(): List<String> {
        return listOf(
            "zh-CN", "zh-TW", "en-US", "en-GB", 
            "ja-JP", "ko-KR", "fr-FR", "de-DE", 
            "es-ES", "it-IT", "ru-RU", "pt-BR",
            "ar-SA", "hi-IN", "th-TH", "vi-VN",
            "nl-NL", "sv-SE", "da-DK", "no-NO"
        )
    }
    
    actual fun getPlatformCapabilities(): List<AICapabilityType> {
        return listOf(
            AICapabilityType.TEXT_GENERATION,
            AICapabilityType.IMAGE_GENERATION,
            AICapabilityType.SPEECH_RECOGNITION,
            AICapabilityType.TEXT_TO_SPEECH,
            AICapabilityType.TRANSLATION,
            AICapabilityType.SENTIMENT_ANALYSIS,
            AICapabilityType.SUMMARIZATION,
            AICapabilityType.CODE_GENERATION,
            AICapabilityType.QUESTION_ANSWERING,
            AICapabilityType.DOCUMENT_ANALYSIS
        )
    }
    
    actual suspend fun cleanup() {
        try {
            mediaStream?.getTracks()?.forEach { track ->
                track.stop()
            }
            mediaStream = null
            speechRecognition = null
            isInitialized = false
        } catch (e: Exception) {
            console.log("Web AI适配器清理失败: ${e.message}")
        }
    }
    
    /**
     * Web特有的语音识别功能
     */
    fun startSpeechRecognition(
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        speechRecognition?.let { recognition ->
            recognition.onresult = { event ->
                val results = event.asDynamic().results
                val transcript = results[results.length - 1][0].transcript as String
                onResult(transcript)
            }
            
            recognition.onerror = { event ->
                onError("语音识别错误: ${event.asDynamic().error}")
            }
            
            recognition.start()
        } ?: onError("语音识别不支持")
    }
    
    /**
     * Web特有的文本转语音功能
     */
    fun speakText(
        text: String,
        language: String = "zh-CN",
        onComplete: () -> Unit = {}
    ) {
        speechSynthesis?.let { synthesis ->
            val utterance = js("new SpeechSynthesisUtterance()")
            utterance.text = text
            utterance.lang = language
            utterance.rate = 1.0
            utterance.pitch = 1.0
            utterance.volume = 1.0
            
            utterance.onend = {
                onComplete()
            }
            
            synthesis.speak(utterance)
        }
    }
    
    /**
     * Web特有的媒体流获取功能
     */
    suspend fun getUserMedia(
        constraints: MediaStreamConstraints = js("{audio: true}")
    ): MediaStream? {
        return try {
            val promise = window.navigator.mediaDevices.getUserMedia(constraints) as Promise<MediaStream>
            mediaStream = promise.await()
            mediaStream
        } catch (e: Exception) {
            console.log("获取媒体流失败: ${e.message}")
            null
        }
    }
    
    /**
     * 获取可用的语音合成声音
     */
    fun getAvailableVoices(): List<String> {
        return try {
            speechSynthesis?.let { synthesis ->
                val voices = synthesis.getVoices()
                voices.map { voice -> voice.name as String }
            } ?: emptyList()
        } catch (e: Exception) {
            console.log("获取语音列表失败: ${e.message}")
            emptyList()
        }
    }
}
