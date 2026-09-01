package com.sumit.simplemobileaisuite.data.repository

import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult
import com.sumit.simplemobileaisuite.data.datasource.local.ObjectDetectorDataSource
import com.sumit.simplemobileaisuite.domain.repository.ObjectDetectorRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class ObjectDetectorRepositoryImplTest {

    private lateinit var localDataSource: ObjectDetectorDataSource
    private lateinit var repository: ObjectDetectorRepositoryImpl

    @Before
    fun setup() {
        localDataSource = mockk()
        repository = ObjectDetectorRepositoryImpl(localDataSource)
    }

    @Test
    fun `setupDetector should delegate to local data source and handle callback`() {
        val listener = mockk<ObjectDetectorRepository.DetectorListener>(relaxed = true)
        val dataSourceListenerSlot = slot<ObjectDetectorDataSource.DetectorListener>()

        every { localDataSource.setup(capture(dataSourceListenerSlot)) } returns Unit

        repository.setupDetector(listener)

        val result = mockk<ObjectDetectorResult>()
        val inferenceTime = 10L
        dataSourceListenerSlot.captured.onResults(result, inferenceTime)

        verify { listener.onResults(result, inferenceTime) }

        val error = "Error"
        dataSourceListenerSlot.captured.onError(error)
        verify { listener.onError(error) }
    }

    @Test
    fun `detectAsync should delegate to local data source`() {
        val image = mockk<MPImage>()
        every { localDataSource.detect(image) } returns Unit

        repository.detectAsync(image)

        verify { localDataSource.detect(image) }
    }

    @Test
    fun `close should delegate to local data source`() {
        every { localDataSource.stop() } returns Unit

        repository.close()

        verify { localDataSource.stop() }
    }
}
