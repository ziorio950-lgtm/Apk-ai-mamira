package com.example.ui.screens

import android.Manifest
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.FoodAnalysis
import com.example.ui.components.AudioWaveform
import com.example.ui.components.GlassCard
import com.example.ui.components.NeonButton
import com.example.ui.theme.AlertYellow
import com.example.ui.theme.CardDark
import com.example.ui.theme.CardDarkElevated
import com.example.ui.theme.ElectricMagenta
import com.example.ui.theme.FuturisticPurple
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.HealthyGreen
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.CameraEvent
import com.example.viewmodel.CameraViewModel
import com.example.viewmodel.MainViewModel
import com.example.viewmodel.ScanUiState
import java.io.InputStream

@Composable
fun CameraScannerScreen(
    viewModel: MainViewModel,
    cameraViewModel: CameraViewModel,
    onOpenFullReport: (FoodAnalysis) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember { mutableStateOf(false) }
    val lensFacing by cameraViewModel.lensFacing.collectAsStateWithLifecycle()
    val isFlashEnabled by cameraViewModel.isFlashEnabled.collectAsStateWithLifecycle()
    val isCapturing by cameraViewModel.isCapturing.collectAsStateWithLifecycle()

    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.voiceAssistant.isSpeaking.collectAsStateWithLifecycle()

    // Listen to camera events from CameraViewModel
    LaunchedEffect(Unit) {
        cameraViewModel.cameraEvents.collect { event ->
            when (event) {
                is CameraEvent.ImageCaptured -> {
                    viewModel.processCapturedImage(event.bitmap)
                }
                is CameraEvent.Error -> {
                    // Fallback to sample scan on hardware/emulator errors
                    viewModel.triggerSampleFoodScan()
                }
            }
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    viewModel.processCapturedImage(bitmap)
                }
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBlack)
    ) {
        // Camera Viewfinder or Placeholder
        if (hasCameraPermission) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .setFlashMode(if (isFlashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
                            .build()

                        cameraViewModel.setImageCapture(capture)

                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                capture
                            )
                        } catch (e: Exception) {
                            // Camera bind fallback
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                update = { previewView ->
                    // Rebind on lensFacing update
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val capture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .setFlashMode(if (isFlashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
                            .build()

                        cameraViewModel.setImageCapture(capture)

                        val cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                capture
                            )
                        } catch (e: Exception) {
                            // Bind error
                        }
                    }, ContextCompat.getMainExecutor(context))
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Permission explanation / Fallback view
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SpaceBlack),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.sample_food_scan),
                    contentDescription = "Sample Food",
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                )

                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "نظام المسح البصري الذكي",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "للبدء في التعرف على الأطعمة والمشروبات بالذكاء الاصطناعي، يرجى تفعيل إذن الكاميرا، أو تجربة عينة المحاكاة الفورية.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        NeonButton(
                            text = "منح إذن الكاميرا",
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            glowColor = NeonCyan,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        NeonButton(
                            text = "فحص عينة تجريبية فورية",
                            onClick = { viewModel.triggerSampleFoodScan() },
                            glowColor = FuturisticPurple,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Sci-Fi Hologram Overlay HUD
        SciFiHologramHUD(
            isScanning = scanState is ScanUiState.Scanning || isCapturing,
            modifier = Modifier.fillMaxSize()
        )

        // Top Control Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassCard(
                shape = RoundedCornerShape(20.dp),
                backgroundColor = CardDark.copy(alpha = 0.7f),
                modifier = Modifier.padding(0.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (scanState is ScanUiState.Scanning || isCapturing) FuturisticPurple else HealthyGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (scanState is ScanUiState.Scanning || isCapturing) "مسح كمومي نشط..." else "جاهز للمسح",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Flash Toggle
                GlassCard(
                    shape = CircleShape,
                    backgroundColor = CardDark.copy(alpha = 0.7f),
                    modifier = Modifier.size(44.dp)
                ) {
                    IconButton(
                        onClick = { cameraViewModel.toggleFlash() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (isFlashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Toggle Flash",
                            tint = if (isFlashEnabled) AlertYellow else TextSecondary
                        )
                    }
                }

                // Audio status toggle
                GlassCard(
                    shape = CircleShape,
                    backgroundColor = CardDark.copy(alpha = 0.7f),
                    modifier = Modifier.size(44.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (isSpeaking) viewModel.stopVoice()
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Voice Assistant",
                            tint = if (isSpeaking) NeonCyan else TextSecondary
                        )
                    }
                }

                // Switch Camera Facing
                GlassCard(
                    shape = CircleShape,
                    backgroundColor = CardDark.copy(alpha = 0.7f),
                    modifier = Modifier.size(44.dp)
                ) {
                    IconButton(
                        onClick = { cameraViewModel.toggleCameraFacing() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Switch Camera",
                            tint = NeonCyan
                        )
                    }
                }
            }
        }

        // Live Result Floating HUD Card (when recognized)
        AnimatedVisibility(
            visible = scanState is ScanUiState.Success,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 115.dp, start = 16.dp, end = 16.dp)
        ) {
            val analysis = (scanState as? ScanUiState.Success)?.analysis
            if (analysis != null) {
                com.example.ui.components.NutritionAnalysisCard(
                    analysis = analysis,
                    isSpeaking = isSpeaking,
                    onOpenFullReport = { onOpenFullReport(analysis) },
                    onListenAudio = { viewModel.speakAnalysis(analysis) },
                    onClose = { viewModel.selectScanDetail(analysis) }
                )
            }
        }

        // Processing Scanner Banner
        AnimatedVisibility(
            visible = scanState is ScanUiState.Scanning || isCapturing,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
        ) {
            GlassCard(
                borderColor = NeonCyan,
                backgroundColor = CardDark.copy(alpha = 0.9f),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = NeonCyan,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "جارٍ تحليل المحتوى الغذائي بالذكاء الاصطناعي...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تقدير السعرات، نسب الماكروز، ومؤشر الجودة الصحية",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Bottom Scanner Control Center (Central Glow Trigger)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 24.dp, start = 20.dp, end = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery picker
                GlassCard(
                    shape = CircleShape,
                    backgroundColor = CardDark.copy(alpha = 0.8f),
                    borderColor = NeonBlue.copy(alpha = 0.5f),
                    modifier = Modifier.size(54.dp)
                ) {
                    IconButton(
                        onClick = { photoPickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxSize().testTag("gallery_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Pick Photo",
                            tint = NeonBlue
                        )
                    }
                }

                // Center Holographic Shutter Button via CameraViewModel
                CentralShutterButton(
                    isScanning = scanState is ScanUiState.Scanning || isCapturing,
                    onClick = {
                        if (hasCameraPermission) {
                            cameraViewModel.takePhoto(context)
                        } else {
                            viewModel.triggerSampleFoodScan()
                        }
                    }
                )

                // Demo simulation trigger button
                GlassCard(
                    shape = CircleShape,
                    backgroundColor = CardDark.copy(alpha = 0.8f),
                    borderColor = FuturisticPurple.copy(alpha = 0.5f),
                    modifier = Modifier.size(54.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.triggerSampleFoodScan() },
                        modifier = Modifier.fillMaxSize().testTag("sample_scan_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Sample Food",
                            tint = FuturisticPurple
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CentralShutterButton(
    isScanning: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shutter_anim")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(80.dp)
            .clickable(onClick = onClick)
            .testTag("scan_shutter_button")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 3.dp.toPx()
            drawCircle(
                brush = Brush.sweepGradient(
                    listOf(NeonCyan, FuturisticPurple, ElectricMagenta, NeonCyan)
                ),
                radius = (size.minDimension / 2f - stroke) * if (isScanning) pulseScale else 1f,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(NeonCyan.copy(alpha = 0.9f), CardDarkElevated)
                    )
                )
                .border(2.dp, NeonCyan, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Scan",
                tint = SpaceBlack,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
fun SciFiHologramHUD(
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hud_scan")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val boxWidth = w * 0.78f
        val boxHeight = h * 0.42f
        val left = (w - boxWidth) / 2f
        val top = (h - boxHeight) / 2.3f
        val cornerLen = 32.dp.toPx()
        val stroke = 3.dp.toPx()
        val hudColor = if (isScanning) FuturisticPurple else NeonCyan

        // 4 corner brackets
        drawLine(hudColor, Offset(left, top), Offset(left + cornerLen, top), stroke)
        drawLine(hudColor, Offset(left, top), Offset(left, top + cornerLen), stroke)

        drawLine(hudColor, Offset(left + boxWidth, top), Offset(left + boxWidth - cornerLen, top), stroke)
        drawLine(hudColor, Offset(left + boxWidth, top), Offset(left + boxWidth, top + cornerLen), stroke)

        drawLine(hudColor, Offset(left, top + boxHeight), Offset(left + cornerLen, top + boxHeight), stroke)
        drawLine(hudColor, Offset(left, top + boxHeight), Offset(left, top + boxHeight - cornerLen), stroke)

        drawLine(hudColor, Offset(left + boxWidth, top + boxHeight), Offset(left + boxWidth - cornerLen, top + boxHeight), stroke)
        drawLine(hudColor, Offset(left + boxWidth, top + boxHeight), Offset(left + boxWidth, top - cornerLen + boxHeight), stroke)

        val linePosY = top + (boxHeight * scanLineY)
        drawLine(
            brush = Brush.horizontalGradient(
                listOf(Color.Transparent, hudColor, hudColor, Color.Transparent)
            ),
            start = Offset(left + 8.dp.toPx(), linePosY),
            end = Offset(left + boxWidth - 8.dp.toPx(), linePosY),
            strokeWidth = 2.5.dp.toPx()
        )
    }
}

@Composable
fun LiveDetectionCard(
    analysis: FoodAnalysis,
    isSpeaking: Boolean,
    onOpenDetails: () -> Unit,
    onSpeak: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        borderColor = NeonCyan,
        backgroundColor = CardDark.copy(alpha = 0.95f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(HealthyGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تم التعرف بنجاح (${analysis.confidence}%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = HealthyGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = analysis.detectedName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = analysis.portionEstimate,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardDarkElevated)
                        .border(1.dp, HealthyGreen, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "التقييم: ${analysis.healthScore}/100",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = HealthyGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroMiniPill(label = "سعرات", value = "${analysis.calories}", color = NeonCyan)
                MacroMiniPill(label = "بروتين", value = "${analysis.proteinGrams}g", color = HealthyGreen)
                MacroMiniPill(label = "كارب", value = "${analysis.carbsGrams}g", color = AlertYellow)
                MacroMiniPill(label = "دهون", value = "${analysis.fatGrams}g", color = ElectricMagenta)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NeonButton(
                    text = "عرض التقرير الكامل",
                    onClick = onOpenDetails,
                    glowColor = NeonCyan,
                    modifier = Modifier.weight(1f),
                    testTag = "open_report_button"
                )

                GlassCard(
                    shape = RoundedCornerShape(14.dp),
                    backgroundColor = CardDarkElevated,
                    borderColor = if (isSpeaking) NeonCyan else GlassBorder,
                    modifier = Modifier.height(48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onSpeak)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Speak",
                            tint = if (isSpeaking) NeonCyan else TextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        if (isSpeaking) {
                            Spacer(modifier = Modifier.width(6.dp))
                            AudioWaveform(isSpeaking = true, barCount = 8)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MacroMiniPill(label: String, value: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(CardDarkElevated)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

