package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AudioSourceType
import com.example.model.InteractionMode
import com.example.ui.components.ArchitecturalPresetBar
import com.example.ui.components.AudioControlBar
import com.example.ui.components.DisplaySettingsDialog
import com.example.ui.components.ModeToolbar
import com.example.ui.components.RealtimeSpectrumVisualizer
import com.example.ui.components.SculptPanel
import com.example.ui.components.SnapshotGalleryDialog
import com.example.ui.components.ThereminPadOverlay
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewport.ArchitecturalViewport
import com.example.viewmodel.ResonatorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ResonatorMainScreen()
            }
        }
    }
}

@Composable
fun ResonatorMainScreen(
    viewModel: ResonatorViewModel = viewModel()
) {
    val context = LocalContext.current
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            viewModel.setAudioSource(AudioSourceType.LIVE_MICROPHONE, true)
        }
    }

    LaunchedEffect(hasMicPermission) {
        if (hasMicPermission) {
            viewModel.setAudioSource(AudioSourceType.LIVE_MICROPHONE, true)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val viewportState by viewModel.viewportState.collectAsState()
    val acousticState by viewModel.acousticState.collectAsState()
    val currentModel by viewModel.currentModel.collectAsState()
    val sculptDisplacements by viewModel.sculptDisplacements.collectAsState()
    val snapshots by viewModel.snapshots.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showGalleryDialog by remember { mutableStateOf(false) }

    val primaryColor = Color(viewportState.palette.primaryHex)
    val accentColor = Color(viewportState.palette.accentHex)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_scaffold"),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(viewportState.palette.bgHex))
        ) {
            // Main 3D Viewport with Particle Halo and Material Shaders
            ArchitecturalViewport(
                model = currentModel,
                sculptDisplacements = sculptDisplacements,
                acousticState = acousticState,
                interactionMode = viewportState.interactionMode,
                sculptBrush = viewportState.sculptBrush,
                brushRadius = viewportState.brushRadius,
                palette = viewportState.palette,
                materialStyle = viewportState.materialStyle,
                audioReactivity = viewportState.audioReactivity,
                wireframeOnly = viewportState.wireframeOnly,
                rotationX = viewportState.rotationX,
                rotationY = viewportState.rotationY,
                panX = viewportState.panX,
                panY = viewportState.panY,
                zoomScale = viewportState.zoomScale,
                onRotateChange = { rx, ry -> viewModel.updateRotation(rx, ry) },
                onPanChange = { px, py -> viewModel.updatePan(px, py) },
                onZoomChange = { z -> viewModel.updateZoom(z) },
                onSculptApply = { touch, projected, brush, radius ->
                    viewModel.applySculpt(touch, projected, brush, radius)
                },
                modifier = Modifier.fillMaxSize()
            )

            // Top Header Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.2f))
                                .border(1.5.dp, primaryColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Waves,
                                contentDescription = "Resonator",
                                tint = primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "RESONATOR",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "${viewportState.preset.title} • ${viewportState.materialStyle.title}",
                                color = accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!hasMicPermission) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0x33FFB300))
                                    .border(1.dp, Color(0x66FFB300), RoundedCornerShape(14.dp))
                                    .clickable { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("btn_request_mic")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Enable Mic",
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mic On", color = Color(0xFFFFB300), fontSize = 11.sp)
                                }
                            }
                        }

                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x880F1522))
                                .testTag("btn_open_settings")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Display Settings",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Floating Interaction Mode Bar
                ModeToolbar(
                    activeMode = viewportState.interactionMode,
                    onModeSelected = { viewModel.setInteractionMode(it) },
                    onResetCamera = { viewModel.resetCamera() },
                    autoOrbit = viewportState.autoOrbit,
                    onToggleAutoOrbit = { viewModel.toggleAutoOrbit() },
                    onOpenGallery = { showGalleryDialog = true },
                    palette = viewportState.palette
                )

                // Contextual Sculpting Tool Panel
                AnimatedVisibility(
                    visible = viewportState.interactionMode == InteractionMode.SCULPT,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    SculptPanel(
                        activeBrush = viewportState.sculptBrush,
                        brushRadius = viewportState.brushRadius,
                        brushIntensity = viewportState.brushIntensity,
                        sculptCount = viewportState.sculptHistoryCount,
                        onBrushSelect = { viewModel.setSculptBrush(it) },
                        onRadiusChange = { viewModel.setBrushRadius(it) },
                        onIntensityChange = { viewModel.setBrushIntensity(it) },
                        onResetSculpt = { viewModel.resetSculpt() },
                        palette = viewportState.palette
                    )
                }

                // Interactive Frequency Theremin Pad Overlay
                AnimatedVisibility(
                    visible = viewportState.interactionMode == InteractionMode.THEREMIN,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    ThereminPadOverlay(
                        currentFreqHz = acousticState.thereminPitchHz,
                        currentResonance = acousticState.thereminResonance,
                        onThereminChange = { freq, res -> viewModel.setThereminParams(freq, res) },
                        onClose = { viewModel.setInteractionMode(InteractionMode.ORBIT) },
                        palette = viewportState.palette
                    )
                }
            }

            // Bottom Navigation & Controls Dock
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(bottom = 6.dp)
            ) {
                // Architectural Preset Selector
                ArchitecturalPresetBar(
                    selectedPreset = viewportState.preset,
                    onPresetSelected = { viewModel.setPreset(it) },
                    palette = viewportState.palette
                )

                // Audio Source, 5s Looper, & Deformation Control
                AudioControlBar(
                    acousticState = acousticState,
                    onSourceChange = { viewModel.setAudioSource(it, hasMicPermission) },
                    onToggleRunning = { viewModel.toggleAudioRunning() },
                    onRecordLoop = { viewModel.startRecordLoop() },
                    onToggleLoopPlayback = { viewModel.toggleLoopPlayback() },
                    audioReactivity = viewportState.audioReactivity,
                    onReactivityChange = { viewModel.setAudioReactivity(it) },
                    palette = viewportState.palette
                )

                // Real-time Audio Spectrum Visualizer Bar
                RealtimeSpectrumVisualizer(
                    spectrum = acousticState.rawSpectrum,
                    palette = viewportState.palette
                )
            }

            // Display & Theme Settings Dialog
            if (showSettingsDialog) {
                Dialog(onDismissRequest = { showSettingsDialog = false }) {
                    DisplaySettingsDialog(
                        wireframeOnly = viewportState.wireframeOnly,
                        onWireframeToggle = { viewModel.setWireframeOnly(it) },
                        palette = viewportState.palette,
                        onPaletteSelect = { viewModel.setPalette(it) },
                        materialStyle = viewportState.materialStyle,
                        onMaterialSelect = { viewModel.setMaterialStyle(it) },
                        onDismiss = { showSettingsDialog = false }
                    )
                }
            }

            // Sculpture Snapshot & Gallery Dialog
            if (showGalleryDialog) {
                Dialog(onDismissRequest = { showGalleryDialog = false }) {
                    SnapshotGalleryDialog(
                        snapshots = snapshots,
                        onSaveCurrent = { viewModel.saveSnapshot(it) },
                        onLoadSnapshot = {
                            viewModel.loadSnapshot(it)
                            showGalleryDialog = false
                        },
                        onDeleteSnapshot = { viewModel.deleteSnapshot(it) },
                        onDismiss = { showGalleryDialog = false },
                        palette = viewportState.palette
                    )
                }
            }
        }
    }
}
