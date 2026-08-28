package com.sumit.simplemobileaisuite.data.repository

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult
import com.sumit.simplemobileaisuite.domain.repository.ObjectDetectorRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [ObjectDetectorRepository] using MediaPipe Tasks Vision.
 */
@Singleton
class ObjectDetectorRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : ObjectDetectorRepository {

    private var objectDetector: ObjectDetector? = null
    private var listener: ObjectDetectorRepository.DetectorListener? = null

    override fun setupDetector(listener: ObjectDetectorRepository.DetectorListener) {
        this.listener = listener
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("models/efficientdet_lite0.tflite")
                .setDelegate(Delegate.CPU) // CPU is more stable for this specific model
                .build()

            val optionsBuilder = ObjectDetector.ObjectDetectorOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setMaxResults(5)
                .setScoreThreshold(0.5f)
                .setResultListener(this::returnLivestreamResult)
                .setErrorListener(this::returnLivestreamError)

            objectDetector = ObjectDetector.createFromOptions(context, optionsBuilder.build())

        } catch (e: Exception) {
            listener.onError("Object detector failed to initialize: ${e.message}")
            Log.e("DetectorRepository", "MediaPipe failed: ${e.message}")
        }
    }

    override fun detectAsync(image: MPImage) {
        val frameTime = SystemClock.uptimeMillis()
        objectDetector?.detectAsync(image, frameTime)
    }

    override fun close() {
        objectDetector?.close()
        objectDetector = null
        listener = null
    }

    private fun returnLivestreamResult(
        result: ObjectDetectorResult,
        @Suppress("UNUSED_PARAMETER") image: MPImage,
    ) {
        val finishTimeMs = SystemClock.uptimeMillis()
        val inferenceTime = finishTimeMs - result.timestampMs()
        listener?.onResults(result, inferenceTime)
    }

    private fun returnLivestreamError(error: RuntimeException) {
        listener?.onError(error.message ?: "An unknown error occurred")
    }
}
