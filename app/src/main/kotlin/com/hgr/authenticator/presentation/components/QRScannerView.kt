package com.hgr.authenticator.presentation.components

import android.Manifest
import android.util.Size as AndroidSize
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.hgr.authenticator.R
import com.hgr.authenticator.presentation.addaccount.AddAccountContract
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * 纯 UI 扫码组件。
 *
 * 不持有业务逻辑，只负责 CameraX 预览、扫描动画、状态指示器和权限申请界面。
 */
@Composable
fun QRScannerView(
    scanState: AddAccountContract.ScanState,
    hasPermission: Boolean,
    onPermissionResult: (Boolean) -> Unit,
    onRequestPermission: () -> Unit,
    onQrCodeDetected: (String) -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        onRequestPermission()
    }

    if (hasPermission) {
        CameraPreview(
            scanState = scanState,
            onQrCodeDetected = onQrCodeDetected,
            onDismissError = onDismissError,
            modifier = modifier
        )
    } else {
        PermissionDeniedContent(
            onRequestPermission = { launcher.launch(Manifest.permission.CAMERA) },
            modifier = modifier
        )
    }
}

@Composable
private fun CameraPreview(
    scanState: AddAccountContract.ScanState,
    onQrCodeDetected: (String) -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        ScanOverlay(scanState = scanState)

        if (scanState is AddAccountContract.ScanState.Scanning) {
            ScanLineAnimation()
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (scanState) {
                is AddAccountContract.ScanState.Processing -> ProcessingIndicator()
                is AddAccountContract.ScanState.Success -> SuccessIndicator(message = "Scan successful")
                is AddAccountContract.ScanState.Error -> ErrorIndicator(
                    message = scanState.message,
                    onDismiss = onDismissError
                )
                else -> {}
            }
        }

        AnimatedVisibility(
            visible = scanState is AddAccountContract.ScanState.Scanning,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier.padding(bottom = 100.dp, start = 32.dp, end = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.scan_instruction),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.scan_instruction_detail),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }

        LaunchedEffect(Unit) {
            bindCameraUseCases(
                context = context,
                lifecycleOwner = lifecycleOwner,
                previewView = previewView,
                executor = executor,
                scanState = scanState,
                onQrCodeDetected = onQrCodeDetected
            )
        }
    }
}

private fun bindCameraUseCases(
    context: android.content.Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    executor: ExecutorService,
    scanState: AddAccountContract.ScanState,
    onQrCodeDetected: (String) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder()
            .build()
            .also { it.surfaceProvider = previewView.surfaceProvider }

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(AndroidSize(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        val scanner = BarcodeScanning.getClient(options)

        imageAnalysis.setAnalyzer(executor) { imageProxy ->
            if (scanState !is AddAccountContract.ScanState.Scanning) {
                imageProxy.close()
                return@setAnalyzer
            }

            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(
                    mediaImage,
                    imageProxy.imageInfo.rotationDegrees
                )

                scanner.process(image)
                    .addOnSuccessListener { barcodes ->
                        for (barcode in barcodes) {
                            val value = barcode.rawValue
                            if (value != null) {
                                onQrCodeDetected(value)
                            }
                        }
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } else {
                imageProxy.close()
            }
        }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )

            val cameraControl = camera.cameraControl
            val focusBuilder = FocusMeteringAction.Builder(
                previewView.meteringPointFactory.createPoint(
                    previewView.width / 2f,
                    previewView.height / 2f
                )
            )
            focusBuilder.setAutoCancelDuration(3, java.util.concurrent.TimeUnit.SECONDS)
            cameraControl.startFocusAndMetering(focusBuilder.build())
        } catch (_: Exception) {
        }
    }, ContextCompat.getMainExecutor(context))
}

@Composable
private fun PermissionDeniedContent(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(R.string.camera_permission_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.camera_permission_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text(stringResource(R.string.camera_permission_grant))
        }
    }
}

@Composable
private fun ScanOverlay(scanState: AddAccountContract.ScanState) {
    val isDetected = scanState is AddAccountContract.ScanState.Detected
    val scale by animateFloatAsState(
        targetValue = if (isDetected) 1.2f else 1f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "zoom"
    )

    val borderColor = when (scanState) {
        is AddAccountContract.ScanState.Scanning -> Color.White
        is AddAccountContract.ScanState.Detected -> Color(0xFF4CAF50)
        is AddAccountContract.ScanState.Processing -> Color(0xFF2196F3)
        is AddAccountContract.ScanState.Success -> Color(0xFF4CAF50)
        is AddAccountContract.ScanState.Error -> Color(0xFFE53935)
        is AddAccountContract.ScanState.Idle -> Color.White
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithContent {
                val baseScanSize = size.minDimension * 0.65f
                val scanSize = baseScanSize * scale
                val scanRect = Rect(
                    Offset(
                        (size.width - scanSize) / 2f,
                        (size.height - scanSize) / 2f
                    ),
                    Size(scanSize, scanSize)
                )

                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(160, 0, 0, 0)
                    }
                    canvas.nativeCanvas.drawRect(0f, 0f, size.width, size.height, paint)

                    val clearPaint = android.graphics.Paint().apply {
                        xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR)
                    }
                    canvas.nativeCanvas.drawRoundRect(
                        scanRect.left, scanRect.top, scanRect.right, scanRect.bottom,
                        16.dp.toPx(), 16.dp.toPx(), clearPaint
                    )
                }

                drawRoundRect(
                    color = borderColor,
                    topLeft = scanRect.topLeft,
                    size = scanRect.size,
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    style = Stroke(width = if (isDetected) 4.dp.toPx() else 2.dp.toPx())
                )
            }
    )
}

@Composable
private fun ScanLineAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanLinePosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val scanAreaHeightPx = 200f
        val offsetY = (scanLinePosition * scanAreaHeightPx - scanAreaHeightPx / 2).dp

        Box(
            modifier = Modifier
                .width(200.dp)
                .height(2.dp)
                .offset(y = offsetY)
                .background(Color(0xFF4CAF50), RoundedCornerShape(1.dp))
        )
    }
}

@Composable
private fun ProcessingIndicator() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = Color.White,
            strokeWidth = 3.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.scanning),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White
        )
    }
}

@Composable
private fun SuccessIndicator(message: String) {
    AnimatedVisibility(
        visible = true,
        enter = scaleIn(animationSpec = tween(400, easing = FastOutSlowInEasing)) + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(72.dp).background(Color(0xFF4CAF50), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, style = MaterialTheme.typography.titleMedium, color = Color.White)
        }
    }
}

@Composable
private fun ErrorIndicator(
    message: String,
    onDismiss: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1500)
        onDismiss()
    }

    AnimatedVisibility(
        visible = true,
        enter = scaleIn(animationSpec = tween(400, easing = FastOutSlowInEasing)) + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(72.dp).background(Color(0xFFE53935), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(message, style = MaterialTheme.typography.titleMedium, color = Color.White)
        }
    }
}
