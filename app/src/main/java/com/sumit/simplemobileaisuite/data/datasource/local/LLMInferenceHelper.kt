package com.sumit.simplemobileaisuite.data.datasource.local

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LLMInferenceHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var llmInference: LlmInference? = null

    private val _partialResults = MutableSharedFlow<Pair<String, Boolean>>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val partialResults: SharedFlow<Pair<String, Boolean>> = _partialResults.asSharedFlow()

    init {
        initializeLLM()
    }

    private fun initializeLLM() {
        try {
            val modelName = "gemma-2b-it-gpu-int4.bin"
            val modelFile = File(context.filesDir, modelName)

            if (!modelFile.exists()) {
                Log.e("LLMHelper", "Model file not found!")
                return
            }

            // 1. The Builder is now strictly for Hardware/Memory Allocation
            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelFile.absolutePath)
                .setMaxTokens(1024)
                .build() // Clean, simple, and stripped down

            llmInference = LlmInference.createFromOptions(context, options)
            Log.d("LLMHelper", "Gemma 2B successfully loaded into GPU!")

        } catch (e: Exception) {
            Log.e("LLMHelper", "Failed to initialize LLM: ${e.message}")
            _partialResults.tryEmit(Pair("\n\n[Hardware Error: Initialization Failed.]", true))
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