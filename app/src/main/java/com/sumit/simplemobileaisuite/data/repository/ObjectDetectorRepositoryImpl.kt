package com.sumit.simplemobileaisuite.data.repository

import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult
import com.sumit.simplemobileaisuite.data.datasource.local.ObjectDetectorDataSource
import com.sumit.simplemobileaisuite.domain.repository.ObjectDetectorRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of [ObjectDetectorRepository] that delegates to [ObjectDetectorDataSource].
 */
@Singleton
class ObjectDetectorRepositoryImpl @Inject constructor(
    private val localDataSource: ObjectDetectorDataSource
) : ObjectDetectorRepository {

    override fun setupDetector(listener: ObjectDetectorRepository.DetectorListener) {
        localDataSource.setup(object : ObjectDetectorDataSource.DetectorListener {
            override fun onError(error: String) {
                listener.onError(error)
            }

            override fun onResults(results: ObjectDetectorResult, inferenceTime: Long) {
                listener.onResults(results, inferenceTime)
            }
        })
    }

    override fun detectAsync(image: MPImage) {
        localDataSource.detect(image)
    }

    override fun close() {
        localDataSource.stop()
    }
}
