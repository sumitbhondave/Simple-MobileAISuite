package com.sumit.simplemobileaisuite.ui.screens.detector

import android.Manifest
import androidx.lifecycle.ViewModel
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult
import com.sumit.simplemobileaisuite.core.permission.PermissionManager
import com.sumit.simplemobileaisuite.domain.repository.ObjectDetectorRepository
import com.sumit.simplemobileaisuite.domain.usecase.DetectObjectsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Unified State for the Object Detection screen.
 */
data class ObjectDetectionState(
    val detections: ObjectDetectorResult? = null,
    val inferenceTime: Long = 0L,
    val error: String? = null,
    val hasCameraPermission: Boolean = false
)

/**
 * ViewModel for the Object Detection feature.
 * Coordinates between the UI and the [DetectObjectsUseCase].
 */
@HiltViewModel
class ObjectDetectionViewModel @Inject constructor(
    private val detectObjectsUseCase: DetectObjectsUseCase,
    private val permissionManager: PermissionManager
) : ViewModel(), ObjectDetectorRepository.DetectorListener {

    private val _uiState = MutableStateFlow(ObjectDetectionState())
    val uiState: StateFlow<ObjectDetectionState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(hasCameraPermission = permissionManager.hasPermission(Manifest.permission.CAMERA))
        }
        detectObjectsUseCase.setup(this)
    }

    /**
     * Updates the camera permission state.
     */
    fun onCameraPermissionResult(isGranted: Boolean) {
        _uiState.update { it.copy(hasCameraPermission = isGranted) }
    }

    /**
     * Feeds an image frame to the detector.
     */
    fun detect(image: MPImage) {
        detectObjectsUseCase(image)
    }

    override fun onResults(results: ObjectDetectorResult, inferenceTime: Long) {
        _uiState.update {
            it.copy(detections = results, inferenceTime = inferenceTime)
        }
    }

    override fun onError(error: String) {
        _uiState.update { it.copy(error = error) }
    }

    override fun onCleared() {
        super.onCleared()
        detectObjectsUseCase.cleanup()
    }
}
