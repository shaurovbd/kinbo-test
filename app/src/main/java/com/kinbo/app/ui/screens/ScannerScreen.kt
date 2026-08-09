package com.kinbo.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.kinbo.app.data.KinboViewModel
import com.kinbo.app.model.ShoppingItem
import java.util.concurrent.Executors

@Composable
fun ScannerScreen(
    vm: KinboViewModel,
    listId: String,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val detected = remember { mutableStateListOf<String>() }
    var manualName by remember { mutableStateOf("") }
    var addedItem by remember { mutableStateOf<String?>(null) }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose { analysisExecutor.shutdown() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan text") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") } },
            )
        },
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .background(MaterialTheme.colorScheme.background),
        ) {
            if (hasPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center,
                ) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val providerFuture = ProcessCameraProvider.getInstance(ctx)
                            providerFuture.addListener({
                                val provider = providerFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val analysis = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                    .also { it.setAnalyzer(analysisExecutor, TextAnalyzer { text ->
                                        // Clean and dedupe recognized text lines
                                        val cleaned = text.split('\n')
                                            .map { it.trim() }
                                            .filter { it.isNotBlank() && it.length >= 2 }
                                        if (cleaned.isNotEmpty()) {
                                            cleaned.forEach { line ->
                                                if (detected.none { it.equals(line, ignoreCase = true) }) {
                                                    detected.add(0, line)
                                                    if (detected.size > 8) detected.removeAt(detected.lastIndex)
                                                }
                                            }
                                            // Auto-fill with the longest recognized line (likely the product name)
                                            if (manualName.isBlank()) {
                                                manualName = cleaned.maxByOrNull { it.length } ?: cleaned.first()
                                            }
                                        }
                                    }) }
                                try {
                                    provider.unbindAll()
                                    provider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        analysis,
                                    )
                                } catch (e: Exception) {
                                    Log.e("KinboScanner", "Camera bind failed", e)
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                    ScanOverlay(Modifier.fillMaxSize())
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().height(360.dp).padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(Icons.Rounded.DocumentScanner, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Text("Camera permission needed to scan text", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                        Text("Grant camera access")
                    }
                }
            }

            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Add to list", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Point the camera at a product name or label",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = manualName,
                    onValueChange = { manualName = it; addedItem = null },
                    label = { Text("Item name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    trailingIcon = {
                        if (manualName.isNotBlank()) {
                            IconButton(onClick = { manualName = "" }) { Icon(Icons.Rounded.Close, null) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            if (manualName.isNotBlank()) {
                                vm.addItem(listId, ShoppingItem(name = manualName.trim()))
                                addedItem = manualName.trim()
                                manualName = ""
                            }
                        },
                        enabled = manualName.isNotBlank(),
                        modifier = Modifier.weight(1f),
                    ) { Text("Add to list") }
                    if (addedItem != null) {
                        TextButton(onClick = onBack) { Text("Done") }
                    }
                }

                addedItem?.let {
                    Spacer(Modifier.height(8.dp))
                    AssistChip(
                        onClick = {},
                        label = { Text("Added: $it") },
                        leadingIcon = { Icon(Icons.Rounded.Check, null, Modifier.size(16.dp)) },
                    )
                }

                if (detected.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text("Recent scans", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    detected.forEach { code ->
                        AssistChip(
                            onClick = { manualName = code },
                            label = { Text(code, maxLines = 1) },
                            leadingIcon = { Icon(Icons.Rounded.TextFields, null, Modifier.size(16.dp)) },
                            modifier = Modifier.padding(vertical = 2.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanOverlay(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .width(260.dp)
                .height(180.dp)
                .border(2.dp, Color.White, RoundedCornerShape(16.dp)),
        )
    }
}

private class TextAnalyzer(val onDetected: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    val text = result.text.trim()
                    if (text.isNotBlank()) onDetected(text)
                }
                .addOnCompleteListener { imageProxy.close() }
        } else {
            imageProxy.close()
        }
    }
}
