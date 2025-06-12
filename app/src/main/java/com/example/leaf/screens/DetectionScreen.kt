package com.example.leaf.screens

import android.Manifest
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.leaf.DetectionResult
import com.example.leaf.OliveDetectionService
import com.example.leaf.data.Detection
import com.example.leaf.data.DetectionHistory
import com.example.leaf.viewmodel.DetectionViewModel
import com.example.leaf.viewmodel.HistoryViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.example.leaf.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionScreen(
    modifier: Modifier = Modifier,
    serverUrl: String,
    historyViewModel: HistoryViewModel = viewModel(),
    detectionViewModel: DetectionViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val oliveDetectionService = remember(serverUrl) { OliveDetectionService(context, serverUrl) }

    val selectedImageUri by detectionViewModel.selectedImageUri.collectAsState()
    val detectionResult by detectionViewModel.detectionResult.collectAsState()
    val isProcessing by detectionViewModel.isProcessing.collectAsState()
    var hasCameraPermission by remember { mutableStateOf(false) }

    val imageUri = remember {
        val imageFile = createImageFile(context)
        FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile!!)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            detectionViewModel.setSelectedImage(imageUri)
            detectionViewModel.setProcessing(true)
            scope.launch {
                try {
                    val result = oliveDetectionService.detectOlives(imageUri)
                    detectionViewModel.setDetectionResult(result)
                    result?.let { res ->
                        historyViewModel.addDetection(
                            DetectionHistory(
                                imageUri = imageUri.toString(),
                                leafCount = res.leafCount,
                                detections = res.detections.map { detection ->
                                    Detection(
                                        className = detection.className,
                                        confidence = detection.confidence.toFloat()
                                    )
                                },
                                resultImage = res.resultImage
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.e("DetectionScreen", "Error during detection: ", e)
                    detectionViewModel.setDetectionResult(DetectionResult(success = false, error = e.message ?: "Unknown error"))
                }
                detectionViewModel.setProcessing(false)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            cameraLauncher.launch(imageUri)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        detectionViewModel.setSelectedImage(uri)
        if (uri != null) {
            detectionViewModel.setProcessing(true)
            scope.launch {
                try {
                    val result = oliveDetectionService.detectOlives(uri)
                    detectionViewModel.setDetectionResult(result)
                    result?.let { res ->
                        historyViewModel.addDetection(
                            DetectionHistory(
                                imageUri = uri.toString(),
                                leafCount = res.leafCount,
                                detections = res.detections.map { detection ->
                                    Detection(
                                        className = detection.className,
                                        confidence = detection.confidence.toFloat()
                                    )
                                },
                                resultImage = res.resultImage
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.e("DetectionScreen", "Error during detection: ", e)
                    detectionViewModel.setDetectionResult(DetectionResult(success = false, error = e.message ?: "Unknown error"))
                }
                detectionViewModel.setProcessing(false)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.detection_title),
                            textAlign = TextAlign.Center
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                enabled = !isProcessing
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.detection_gallery))
            }

            Button(
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                enabled = !isProcessing
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.detection_camera))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isProcessing -> {
                        CircularProgressIndicator()
                    }
                    detectionResult?.resultImage != null -> {
                        Image(
                            bitmap = detectionResult!!.resultImage!!.asImageBitmap(),
                            contentDescription = "Processed Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    selectedImageUri != null -> {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    else -> {
                        Text("No image selected")
                    }
                }
            }

            if (detectionResult != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (!detectionResult!!.success) {
                            Text(
                                "Error",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                detectionResult!!.error ?: "Unknown error occurred",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Text(
                                "Detection Results",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Total Leaves: ${detectionResult!!.leafCount}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            detectionResult!!.detections.forEach { detection ->
                                Text(
                                    "${detection.className}: ${(detection.confidence * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            if (detectionResult?.error != null) {
                Text(
                    text = "Error: ${detectionResult!!.error}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun createImageFile(context: android.content.Context): File? {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val storageDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        imageFileName,
        ".jpg",
        storageDir
    )
} 