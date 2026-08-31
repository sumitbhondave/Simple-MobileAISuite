package com.sumit.simplemobileaisuite.data.datasource.local

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.sumit.simplemobileaisuite.domain.model.OfflineLLMStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LLMInferenceHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var llmInference: LlmInference? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _partialResults = MutableSharedFlow<Pair<String, Boolean>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val partialResults: SharedFlow<Pair<String, Boolean>> = _partialResults.asSharedFlow()

    private val _offlineLLMStatus = MutableStateFlow<OfflineLLMStatus>(OfflineLLMStatus.Idle)
    val offlineLLMStatus: StateFlow<OfflineLLMStatus> = _offlineLLMStatus.asStateFlow()

    fun initializeLLM() {
        if (_offlineLLMStatus.value is OfflineLLMStatus.Ready || 
            _offlineLLMStatus.value is OfflineLLMStatus.Loading) return

        _offlineLLMStatus.value = OfflineLLMStatus.Loading

        scope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val modelName = "gemma-2b-it-gpu-int4.bin"
                    val modelFile = File(context.filesDir, modelName)

                    if (!modelFile.exists()) {
                        return@withContext OfflineLLMStatus.Error("Model file not found! Push it via Device File Explorer.")
                    }

                    val options = LlmInference.LlmInferenceOptions.builder()
                        .setModelPath(modelFile.absolutePath)
                        .setMaxTokens(1024)
                        .build()

                    llmInference = LlmInference.createFromOptions(context, options)
                    OfflineLLMStatus.Ready
                }
                _offlineLLMStatus.value = result
                Log.d("LLMHelper", "Gemma 2B status: $result")

            } catch (e: Exception) {
                val errorMsg = "Failed to initialize LLM: ${e.message}"
                Log.e("LLMHelper", errorMsg)
                _offlineLLMStatus.value = OfflineLLMStatus.Error(errorMsg)
            }
        }
    }

    fun generateResponseAsync(prompt: String) {
        try {
            val formattedPrompt =
                "<start_of_turn>user\n$prompt<end_of_turn>\n<start_of_turn>model\n"

            // 2. Use Kotlin's trailing lambda syntax. The compiler will automatically
            // translate this into the correct Java ProgressListener<String!>!
            llmInference?.generateResponseAsync(formattedPrompt) { partialResult, done ->
                _partialResults.tryEmit(Pair(partialResult ?: "", done))
            }
        } catch (e: Exception) {
            Log.e("LLMHelper", "Native Engine Error: ${e.message}")
            _partialResults.tryEmit(
                Pair(
                    "\n\n[Hardware Error: GPU Work Group limits exceeded. Device not supported.]",
                    true
                )
            )
        }
    }

    fun close() {
        llmInference?.close()
    }
}