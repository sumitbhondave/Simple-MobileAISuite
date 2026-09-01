package com.sumit.simplemobileaisuite.ui.screens.detector

import android.Manifest
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult
import com.sumit.simplemobileaisuite.core.permission.PermissionManager
import com.sumit.simplemobileaisuite.domain.usecase.DetectObjectsUseCase
import com.sumit.simplemobileaisuite.ui.screens.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ObjectDetectionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var detectObjectsUseCase: DetectObjectsUseCase
    private lateinit var permissionManager: PermissionManager
    private lateinit var viewModel: ObjectDetectionViewModel

    @Before
    fun setup() {
        detectObjectsUseCase = mockk(relaxed = true)
        permissionManager = mockk()
        every { permissionManager.hasPermission(Manifest.permission.CAMERA) } returns false
        viewModel = ObjectDetectionViewModel(detectObjectsUseCase, permissionManager)
    }

    @Test
    fun `initialization should setup detector and check permissions`() {
        verify { detectObjectsUseCase.setup(viewModel) }
        verify { permissionManager.hasPermission(Manifest.permission.CAMERA) }
        assertEquals(false, viewModel.uiState.value.hasCameraPermission)
    }

    @Test
    fun `onCameraPermissionResult should update state`() {
        viewModel.onCameraPermissionResult(true)
        assertEquals(true, viewModel.uiState.value.hasCameraPermission)
    }

    @Test
    fun `detect should delegate to use case`() {
        val image = mockk<MPImage>()
        viewModel.detect(image)
        verify { detectObjectsUseCase(image) }
    }

    @Test
    fun `onResults should update state`() {
        val result = mockk<ObjectDetectorResult>()
        val time = 100L
        viewModel.onResults(result, time)
        assertEquals(result, viewModel.uiState.value.detections)
        assertEquals(time, viewModel.uiState.value.inferenceTime)
    }

    @Test
    fun `onError should update state`() {
        val error = "Error"
        viewModel.onError(error)
        assertEquals(error, viewModel.uiState.value.error)
    }
}
