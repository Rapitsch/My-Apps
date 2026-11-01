package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AcousticState
import com.example.model.ArchitecturalPreset
import com.example.model.AudioSourceType
import com.example.model.InteractionMode
import com.example.model.MaterialStyle
import com.example.model.SculptBrush
import com.example.model.VisualPalette

@Composable
fun ModeToolbar(
    activeMode: InteractionMode,
    onModeSelected: (InteractionMode) -> Unit,
    onResetCamera: () -> Unit,
    autoOrbit: Boolean,
    onToggleAutoOrbit: () -> Unit,
    onOpenGallery: () -> Unit,
    palette: VisualPalette,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(palette.primaryHex)
    val accentColor = Color(palette.accentHex)

    Card(
        modifier = modifier
            .testTag("mode_toolbar")
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xDD0D111A)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(primaryColor.copy(alpha = 0.5f), accentColor.copy(alpha = 0.5f)))
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ModeButton(
                title = "Orbit",
                icon = Icons.Default.RotateRight,
                isSelected = activeMode == InteractionMode.ORBIT,
                activeColor = primaryColor,
                testTag = "btn_mode_orbit",
                onClick = { onModeSelected(InteractionMode.ORBIT) }
            )
            ModeButton(
                title = "Pan",
                icon = Icons.Default.OpenWith,
                isSelected = activeMode == InteractionMode.PAN,
                activeColor = primaryColor,
                testTag = "btn_mode_pan",
                onClick = { onModeSelected(InteractionMode.PAN) }
            )
            ModeButton(
                title = "Zoom",
                icon = Icons.Default.ZoomIn,
                isSelected = activeMode == InteractionMode.ZOOM,
                activeColor = primaryColor,
                testTag = "btn_mode_zoom",
                onClick = { onModeSelected(InteractionMode.ZOOM) }
            )
            ModeButton(
                title = "Sculpt",
                icon = Icons.Default.Brush,
                isSelected = activeMode == InteractionMode.SCULPT,
                activeColor = accentColor,
                testTag = "btn_mode_sculpt",
                onClick = { onModeSelected(InteractionMode.SCULPT) }
            )
            ModeButton(
                title = "Theremin",
                icon = Icons.Default.MusicNote,
                isSelected = activeMode == InteractionMode.THEREMIN,
                activeColor = Color(0xFF00E676),
                testTag = "btn_mode_theremin",
                onClick = { onModeSelected(InteractionMode.THEREMIN) }
            )

            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
                    .background(Color.White.copy(alpha = 0.2f))
            )

            IconButton(
                onClick = onOpenGallery,
                modifier = Modifier.size(36.dp).testTag("btn_toolbar_gallery")
            ) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = "Gallery",
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onResetCamera,
                modifier = Modifier.size(36.dp).testTag("btn_reset_camera")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Camera View",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onToggleAutoOrbit,
                modifier = Modifier.size(36.dp).testTag("btn_toggle_autoorbit")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Toggle Auto Orbit",
                    tint = if (autoOrbit) accentColor else Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ModeButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .testTag(testTag)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) activeColor.copy(alpha = 0.25f) else Color.Transparent)
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) activeColor else Color.Transparent,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) activeColor else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = title,
                color = if (isSelected) activeColor else Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun SculptPanel(
    activeBrush: SculptBrush,
    brushRadius: Float,
    brushIntensity: Float,
    sculptCount: Int,
    onBrushSelect: (SculptBrush) -> Unit,
    onRadiusChange: (Float) -> Unit,
    onIntensityChange: (Float) -> Unit,
    onResetSculpt: () -> Unit,
    palette: VisualPalette,
    modifier: Modifier = Modifier
) {
    val accentColor = Color(palette.accentHex)
    val primaryColor = Color(palette.primaryHex)

    Card(
        modifier = modifier
            .testTag("sculpt_panel")
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xEE101622)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(accentColor, primaryColor)))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Brush,
                        contentDescription = "Sculpting Tools",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Real-time Mesh Sculpting",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (sculptCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Red.copy(alpha = 0.2f))
                            .border(1.dp, Color.Red.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable(onClick = onResetSculpt)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("btn_reset_sculpt")
                    ) {
                        Text(
                            text = "Reset Mesh ($sculptCount)",
                            color = Color(0xFFFF8A80),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SculptBrush.values().forEach { brush ->
                    FilterChip(
                        selected = activeBrush == brush,
                        onClick = { onBrushSelect(brush) },
                        label = { Text(brush.label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.3f),
                            selectedLabelColor = accentColor
                        ),
                        modifier = Modifier.testTag("chip_brush_${brush.name}")
                    )
                }
            }

            Text(
                text = activeBrush.description,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Radius", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, modifier = Modifier.width(55.dp))
                Slider(
                    value = brushRadius,
                    onValueChange = onRadiusChange,
                    valueRange = 40f..180f,
                    colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor),
                    modifier = Modifier.weight(1f).testTag("slider_brush_radius")
                )
                Text("${brushRadius.toInt()}px", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, modifier = Modifier.width(42.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Force", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, modifier = Modifier.width(55.dp))
                Slider(
                    value = brushIntensity,
                    onValueChange = onIntensityChange,
                    valueRange = 0.1f..1.0f,
                    colors = SliderDefaults.colors(thumbColor = primaryColor, activeTrackColor = primaryColor),
                    modifier = Modifier.weight(1f).testTag("slider_brush_intensity")
                )
                Text("${(brushIntensity * 100).toInt()}%", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, modifier = Modifier.width(42.dp))
            }
        }
    }
}

@Composable
fun ArchitecturalPresetBar(
    selectedPreset: ArchitecturalPreset,
    onPresetSelected: (ArchitecturalPreset) -> Unit,
    palette: VisualPalette,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(palette.primaryHex)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ArchitecturalPreset.values().forEach { preset ->
            val isSelected = selectedPreset == preset
            Card(
                modifier = Modifier
                    .testTag("card_preset_${preset.name}")
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onPresetSelected(preset) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) primaryColor.copy(alpha = 0.22f) else Color(0xAA0D121D)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        if (isSelected) listOf(primaryColor, Color(palette.accentHex)) else listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.05f))
                    )
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewInAr,
                        contentDescription = preset.title,
                        tint = if (isSelected) primaryColor else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = preset.title,
                        color = if (isSelected) primaryColor else Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun AudioControlBar(
    acousticState: AcousticState,
    onSourceChange: (AudioSourceType) -> Unit,
    onToggleRunning: () -> Unit,
    onRecordLoop: () -> Unit,
    onToggleLoopPlayback: () -> Unit,
    audioReactivity: Float,
    onReactivityChange: (Float) -> Unit,
    palette: VisualPalette,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val primaryColor = Color(palette.primaryHex)
    val accentColor = Color(palette.accentHex)

    Card(
        modifier = modifier
            .testTag("audio_control_bar")
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xDD0D111A)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(primaryColor.copy(alpha = 0.4f), accentColor.copy(alpha = 0.4f))))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (acousticState.isAudioRunning) Color(0xFF00E676) else Color.Red)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = acousticState.audioSource.label,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${acousticState.decibels.toInt()} dB  •  ${acousticState.peakFrequencyHz.toInt()} Hz Peak",
                            color = primaryColor,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleRunning,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_toggle_audio")
                    ) {
                        Icon(
                            imageVector = if (acousticState.isAudioRunning) Icons.Default.GraphicEq else Icons.Default.MicOff,
                            contentDescription = "Toggle Audio Input",
                            tint = if (acousticState.isAudioRunning) primaryColor else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_expand_audio_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Audio Settings",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = "Acoustic Source:",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AudioSourceType.values().forEach { src ->
                            FilterChip(
                                selected = acousticState.audioSource == src,
                                onClick = { onSourceChange(src) },
                                label = { Text(src.label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryColor.copy(alpha = 0.3f),
                                    selectedLabelColor = primaryColor
                                ),
                                modifier = Modifier.testTag("chip_source_${src.name}")
                            )
                        }
                    }

                    // 5-Second Looper Controls
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0A0F18))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (acousticState.isRecordingLoop) Color.Red.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f))
                                    .clickable(onClick = onRecordLoop)
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                    .testTag("btn_record_loop")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FiberManualRecord,
                                        contentDescription = "Record Loop",
                                        tint = if (acousticState.isRecordingLoop) Color.Red else Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (acousticState.isRecordingLoop) "Recording..." else "Rec 5s Loop",
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (acousticState.isLoopActive) accentColor.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.1f))
                                    .clickable(onClick = onToggleLoopPlayback)
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                                    .testTag("btn_play_loop")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Loop,
                                        contentDescription = "Loop Play",
                                        tint = if (acousticState.isLoopActive) accentColor else Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (acousticState.isLoopActive) "Loop Playing" else "Loop Off",
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        if (acousticState.isRecordingLoop) {
                            LinearProgressIndicator(
                                progress = { acousticState.loopProgress },
                                color = Color.Red,
                                trackColor = Color.DarkGray,
                                modifier = Modifier.width(60.dp).height(4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Deformation Gain",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            modifier = Modifier.width(110.dp)
                        )
                        Slider(
                            value = audioReactivity,
                            onValueChange = onReactivityChange,
                            valueRange = 0.2f..3.0f,
                            colors = SliderDefaults.colors(thumbColor = primaryColor, activeTrackColor = primaryColor),
                            modifier = Modifier.weight(1f).testTag("slider_audio_reactivity")
                        )
                        Text(
                            text = String.format("%.1fx", audioReactivity),
                            color = primaryColor,
                            fontSize = 11.sp,
                            modifier = Modifier.width(36.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RealtimeSpectrumVisualizer(
    spectrum: FloatArray,
    palette: VisualPalette,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(palette.primaryHex)
    val accentColor = Color(palette.accentHex)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .padding(horizontal = 14.dp, vertical = 2.dp)
            .testTag("spectrum_visualizer"),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        val binCount = minOf(spectrum.size, 48)
        for (i in 0 until binCount) {
            val magnitude = spectrum[i].coerceIn(0.05f, 1f)
            val barColor = if (i < binCount / 3) {
                Color(palette.secondaryHex)
            } else if (i < (binCount * 2) / 3) {
                primaryColor
            } else {
                accentColor
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .height((24.dp * magnitude).coerceAtLeast(3.dp))
                    .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                    .background(barColor.copy(alpha = 0.35f + magnitude * 0.65f))
            )
        }
    }
}

@Composable
fun DisplaySettingsDialog(
    wireframeOnly: Boolean,
    onWireframeToggle: (Boolean) -> Unit,
    palette: VisualPalette,
    onPaletteSelect: (VisualPalette) -> Unit,
    materialStyle: MaterialStyle,
    onMaterialSelect: (MaterialStyle) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .testTag("display_settings_dialog"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xF0121724)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(Color(palette.primaryHex), Color(palette.accentHex))))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Acoustic Architecture Settings",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Material Shaders
            Text("Architectural Material Style:", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MaterialStyle.values().forEach { mat ->
                    val isSelected = mat == materialStyle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(palette.primaryHex).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                            .clickable { onMaterialSelect(mat) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = mat.title,
                            color = if (isSelected) Color(palette.primaryHex) else Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.width(130.dp)
                        )
                        Text(
                            text = mat.description,
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Wireframe Only", color = Color.White, fontSize = 12.sp)
                Switch(
                    checked = wireframeOnly,
                    onCheckedChange = onWireframeToggle,
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(palette.primaryHex)),
                    modifier = Modifier.testTag("switch_wireframe")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text("Color Resonance Palette:", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                VisualPalette.values().forEach { pal ->
                    val isSelected = pal == palette
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color.White.copy(alpha = 0.12f) else Color.Transparent)
                            .clickable { onPaletteSelect(pal) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(pal.primaryHex))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Color(pal.accentHex))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = pal.title,
                            color = if (isSelected) Color(pal.primaryHex) else Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(palette.primaryHex).copy(alpha = 0.25f))
                    .clickable(onClick = onDismiss)
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Done", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
