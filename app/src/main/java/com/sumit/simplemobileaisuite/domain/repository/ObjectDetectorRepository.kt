package com.sumit.simplemobileaisuite.domain.repository

import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult

/**
 * Interface defining the local AI object detection capabilities.
 */
interface ObjectDetectorRepository {
    /**
     * Listener interface to receive detection results and errors.
     */
    interface DetectorListener {
        fun onError(error: String)
        fun onResults(results: ObjectDetectorResult, inferenceTime: Long)
    }

    /**
     * Initializes the detector with a listener.
     */
    fun setupDetector(listener: DetectorListener)

    /**
     * Performs asynchronous detection on the provided image.
     */
    fun detectAsync(image: MPImage)

    /**
     * Closes the detector and releases resources.
     */
    fun close()
}
