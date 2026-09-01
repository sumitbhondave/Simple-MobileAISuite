package com.sumit.simplemobileaisuite.domain.usecase

import com.google.mediapipe.framework.image.MPImage
import com.sumit.simplemobileaisuite.domain.repository.ObjectDetectorRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class DetectObjectsUseCaseTest {

    private lateinit var repository: ObjectDetectorRepository
    private lateinit var useCase: DetectObjectsUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = DetectObjectsUseCase(repository)
    }

    @Test
    fun `setup should delegate to repository`() {
        val listener = mockk<ObjectDetectorRepository.DetectorListener>()
        every { repository.setupDetector(listener) } returns Unit

        useCase.setup(listener)

        verify { repository.setupDetector(listener) }
    }

    @Test
    fun `invoke should delegate to repository`() {
        val image = mockk<MPImage>()
        every { repository.detectAsync(image) } returns Unit

        useCase(image)

        verify { repository.detectAsync(image) }
    }

    @Test
    fun `cleanup should delegate to repository`() {
        every { repository.close() } returns Unit

        useCase.cleanup()

        verify { repository.close() }
    }
}
