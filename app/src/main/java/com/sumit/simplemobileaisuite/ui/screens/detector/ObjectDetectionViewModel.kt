package com.sumit.simplemobileaisuite.ui.screens.detector

import android.Manifest
import androidx.lifecycle.ViewModel
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult
import com.sumit.simplemobileaisuite.core.permission.PermissionManager
import com.sumit.simplemobileaisuite.domain.repository.ObjectDetectorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for the Object Detection feature.
 * Coordinates between the UI and the [ObjectDetectorRepository].
 */
@HiltViewModel
class ObjectDetectionViewModel @Inject constructor(
    private val repository: ObjectDetectorRepository,
    private val permissionManager: PermissionManager
) : ViewModel(), ObjectDetectorRepository.DetectorListener {

    private val _detections = MutableStateFlow<ObjectDetectorResult?>(null)
    val detections: StateFlow<ObjectDetectorResult?> = _detections.asStateFlow()

    private val _inferenceTime = MutableStateFlow(0L)
    val inferenceTime: StateFlow<Long> = _inferenceTime.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _hasCameraPermission = MutableStateFlow(
        permissionManager.hasPermission(Manifest.permission.CAMERA)
    )
    val hasCameraPermission: StateFlow<Boolean> = _hasCameraPermission.asStateFlow()

    init {
        repository.setupDetector(this)
    }

    /**
     * Updates the camera permission state.
     */
    fun onCameraPermissionResult(isGranted: Boolean) {
        _hasCameraPermission.value = isGranted
    }

    /**
     * Feeds an image frame to the detector.
     */
    fun detect(image: MPImage) {
        repository.detectAsync(image)
    }

    override fun onResults(results: ObjectDetectorResult, inferenceTime: Long) {
        _detections.value = results
        _inferenceTime.value = inferenceTime
    }

    override fun onError(error: String) {
        _error.value = error
    }

    override fun onCleared() {
        super.onCleared()
        repository.close()
    }
}
