package com.sumit.simplemobileaisuite.ui.screens.detector

import android.Manifest
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult
import com.sumit.simplemobileaisuite.ui.components.PermissionHandler
import java.util.concurrent.Executors

/**
 * Screen for real-time object detection using CameraX and MediaPipe.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObjectDetectionScreen(
    viewModel: ObjectDetectionViewModel,
    onNavigateBack: () -> Unit,
) {
    val results = viewModel.detections.collectAsState().value
    val inferenceTime by viewModel.inferenceTime.collectAsState()
    val error by viewModel.error.collectAsState()
    val hasCameraPermission by viewModel.hasCameraPermission.collectAsState()

    var frameWidth by remember { mutableIntStateOf(0) }
    var frameHeight by remember { mutableIntStateOf(0) }

    var requestPermissionTrigger by remember { mutableStateOf(0) }

    PermissionHandler(
        permission = Manifest.permission.CAMERA,
        onResult = viewModel::onCameraPermissionResult,
        trigger = requestPermissionTrigger
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Local Object Detection", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (hasCameraPermission) {
                CameraPreviewContent(
                    viewModel = viewModel,
                    results = results,
                    inferenceTime = inferenceTime,
                    error = error,
                    onFrameSizeChanged = { width, height ->
                        frameWidth = width
                        frameHeight = height
                    }
                )

                // Overlay for Bounding Boxes
                results?.let {
                    DetectionOverlay(
                        results = it,
                        frameWidth = frameWidth,
                        frameHeight = frameHeight
                    )
                }
            } else {
                CameraPermissionDeniedContent(
                    onGrantPermission = {
                        requestPermissionTrigger++
                    }
                )
            }
        }
    }
}

@Composable
fun CameraPreviewContent(
    viewModel: ObjectDetectionViewModel,
    results: ObjectDetectorResult?,
    inferenceTime: Long,
    error: String?,
    onFrameSizeChanged: (Int, Int) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val backgroundExecutor = remember { Executors.newSingleThreadExecutor() }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                    val imageAnalyzer = ImageAnalysis.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build()
                        .also {
                            it.setAnalyzer(backgroundExecutor) { imageProxy ->
                                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                                val rawBitmap = imageProxy.toBitmap()
                                val matrix =
                                    Matrix().apply { postRotate(rotationDegrees.toFloat()) }
                                val rotatedBitmap = Bitmap.createBitmap(
                                    rawBitmap,
                                    0,
                                    0,
                                    rawBitmap.width,
                                    rawBitmap.height,
                                    matrix,
                                    true
                                )

                                onFrameSizeChanged(rotatedBitmap.width, rotatedBitmap.height)

                                val mpImage = BitmapImageBuilder(rotatedBitmap).build()
                                viewModel.detect(mpImage)

                                imageProxy.close()
                            }
                        }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalyzer
                        )
                    } catch (e: Exception) {
                        Log.e("DetectionScreen", "Binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        // Status Card
        error?.let {
            ErrorMessage(it)
        } ?: run {
            StatusCard(
                modifier = Modifier.align(Alignment.BottomCenter),
                inferenceTime = inferenceTime,
                objectCount = results?.detections()?.size ?: 0
            )
        }
    }
}

@Composable
fun CameraPermissionDeniedContent(
    onGrantPermission: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Camera permission is required for object detection",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
        Button(onClick = onGrantPermission) {
            Text("Grant Permission")
        }
    }
}

@Composable
fun DetectionOverlay(
    results: ObjectDetectorResult,
    frameWidth: Int,
    frameHeight: Int
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (frameWidth <= 0 || frameHeight <= 0) return@Canvas

        val scaleFactor = maxOf(size.width / frameWidth, size.height / frameHeight)
        val offsetX = (frameWidth * scaleFactor - size.width) / 2f
        val offsetY = (frameHeight * scaleFactor - size.height) / 2f

        for (detection in results.detections()) {
            val boundingBox = detection.boundingBox()
            val category = detection.categories().firstOrNull()
            val labelText =
                "${category?.categoryName() ?: "Object"} ${((category?.score() ?: 0f) * 100).toInt()}%"

            val left = (boundingBox.left * scaleFactor) - offsetX
            val top = (boundingBox.top * scaleFactor) - offsetY
            val right = (boundingBox.right * scaleFactor) - offsetX
            val bottom = (boundingBox.bottom * scaleFactor) - offsetY

            drawRect(
                color = Color(0xFF00FF66),
                topLeft = Offset(left, top),
                size = Size(right - left, bottom - top),
                style = Stroke(width = 8f)
            )

            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.GREEN
                    textSize = 46f
                    isFakeBoldText = true
                }
                drawText(labelText, left.coerceAtLeast(10f), top - 15f, paint)
            }
        }
    }
}

@Composable
fun StatusCard(
    modifier: Modifier,
    inferenceTime: Long,
    objectCount: Int
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(
                alpha = 0.8f
            )
        )
    ) {
        Text(
            text = "Speed: ${inferenceTime}ms | Objects: $objectCount",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ErrorMessage(error: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Text(
                text = error,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}
