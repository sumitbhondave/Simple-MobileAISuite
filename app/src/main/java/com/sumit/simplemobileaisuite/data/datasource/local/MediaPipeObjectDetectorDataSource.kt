package com.sumit.simplemobileaisuite.data.datasource.local

import android.content.Context
import android.util.Log
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector
import com.sumit.simplemobileaisuite.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ObjectDetectorDataSource using Google's MediaPipe Tasks Vision.
 */
@Singleton
class MediaPipeObjectDetectorDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) : ObjectDetectorDataSource {

    private var objectDetector: ObjectDetector? = null
    private var listener: ObjectDetectorDataSource.DetectorListener? = null

    override fun setup(listener: ObjectDetectorDataSource.DetectorListener) {
        this.listener = listener
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("models/efficientdet_lite0.tflite")
                .build()

            val options = ObjectDetector.ObjectDetectorOptions.builder()
                .setBaseOptions(baseOptions)
                .setScoreThreshold(0.5f)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener { results, _ ->
                    listener.onResults(results, System.currentTimeMillis())
                }
                .setErrorListener { error ->
                    listener.onError(error.message ?: context.getString(R.string.unknown_error))
                }
                .build()

            objectDetector = ObjectDetector.createFromOptions(context, options)
        } catch (e: Exception) {
            val errorMsg = context.getString(R.string.failed_init_detector, e.message)
            listener.onError(errorMsg)
            Log.e("MediaPipeDataSource", errorMsg)
        }
    }

    override fun detect(image: MPImage) {
        try {
            objectDetector?.detectAsync(image, System.currentTimeMillis())
        } catch (e: Exception) {
            listener?.onError(e.message ?: context.getString(R.string.unknown_error))
        }
    }

    override fun stop() {
        objectDetector?.close()
        objectDetector = null
    }
}
