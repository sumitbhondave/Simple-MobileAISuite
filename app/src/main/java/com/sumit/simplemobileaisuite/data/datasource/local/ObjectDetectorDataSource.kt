package com.sumit.simplemobileaisuite.data.datasource.local

import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult

/**
 * Interface for on-device object detection data source.
 */
interface ObjectDetectorDataSource {
    interface DetectorListener {
        fun onError(error: String)
        fun onResults(results: ObjectDetectorResult, inferenceTime: Long)
    }

    fun setup(listener: DetectorListener)
    fun detect(image: MPImage)
    fun stop()
}
