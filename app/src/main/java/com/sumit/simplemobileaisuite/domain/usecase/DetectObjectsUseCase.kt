package com.sumit.simplemobileaisuite.domain.usecase

import com.google.mediapipe.framework.image.MPImage
import com.sumit.simplemobileaisuite.domain.repository.ObjectDetectorRepository

/**
 * Use case for on-device object detection.
 * Manages the interaction with the [ObjectDetectorRepository].
 */
class DetectObjectsUseCase(
    private val repository: ObjectDetectorRepository
) {
    /**
     * Initializes the detector with a listener.
     */
    fun setup(listener: ObjectDetectorRepository.DetectorListener) {
        repository.setupDetector(listener)
    }

    /**
     * Performs detection on the given image frame.
     */
    operator fun invoke(image: MPImage) {
        repository.detectAsync(image)
    }

    /**
     * Releases resources when the feature is no longer in use.
     */
    fun cleanup() {
        repository.close()
    }
}
