package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VisualPalette
import kotlin.math.roundToInt

@Composable
fun ThereminPadOverlay(
    currentFreqHz: Float,
    currentResonance: Float,
    onThereminChange: (pitchHz: Float, resonance: Float) -> Unit,
    onClose: () -> Unit,
    palette: VisualPalette,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(palette.primaryHex)
    val accentColor = Color(palette.accentHex)

    var touchPos by remember { mutableStateOf<Offset?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("theremin_pad_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xF20B101A)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(primaryColor, accentColor)))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Theremin",
                    tint = primaryColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Interactive Acoustic Theremin",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Slide X for Pitch (40Hz - 2000Hz) • Slide Y for Harmonic Resonance",
                        color = primaryColor,
                        fontSize = 10.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(primaryColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${currentFreqHz.roundToInt()} Hz",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp).testTag("btn_close_theremin")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Theremin",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Interactive Touch Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF060A12))
                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                    .testTag("theremin_touch_surface")
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                touchPos = offset
                                val w = size.width.toFloat().coerceAtLeast(1f)
                                val h = size.height.toFloat().coerceAtLeast(1f)
                                val normX = (offset.x / w).coerceIn(0f, 1f)
                                val normY = (1f - (offset.y / h)).coerceIn(0f, 1f)
                                val freq = 40f + (normX * normX * 1960f) // Quadratic scale for natural pitch distribution
                                val res = 0.1f + (normY * 0.9f)
                                onThereminChange(freq, res)
                            },
                            onDragEnd = {
                                touchPos = null
                            },
                            onDragCancel = {
                                touchPos = null
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                touchPos = change.position
                                val w = size.width.toFloat().coerceAtLeast(1f)
                                val h = size.height.toFloat().coerceAtLeast(1f)
                                val normX = (change.position.x / w).coerceIn(0f, 1f)
                                val normY = (1f - (change.position.y / h)).coerceIn(0f, 1f)
                                val freq = 40f + (normX * normX * 1960f)
                                val res = 0.1f + (normY * 0.9f)
                                onThereminChange(freq, res)
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Grid pitch marks
                    val freqs = listOf(100f, 220f, 440f, 880f, 1760f)
                    for (f in freqs) {
                        val norm = kotlin.math.sqrt((f - 40f) / 1960f).coerceIn(0f, 1f)
                        val gx = norm * w
                        drawLine(
                            color = Color.White.copy(alpha = 0.12f),
                            start = Offset(gx, 0f),
                            end = Offset(gx, h),
                            strokeWidth = 1f
                        )
                    }

                    // Horizontal resonance lines
                    for (r in 1..3) {
                        val gy = (h * r) / 4f
                        drawLine(
                            color = Color.White.copy(alpha = 0.08f),
                            start = Offset(0f, gy),
                            end = Offset(w, gy),
                            strokeWidth = 1f
                        )
                    }

                    // Active Touch Reticle
                    touchPos?.let { pos ->
                        // Crosshair lines
                        drawLine(
                            color = primaryColor.copy(alpha = 0.4f),
                            start = Offset(pos.x, 0f),
                            end = Offset(pos.x, h),
                            strokeWidth = 1.5f
                        )
                        drawLine(
                            color = accentColor.copy(alpha = 0.4f),
                            start = Offset(0f, pos.y),
                            end = Offset(w, pos.y),
                            strokeWidth = 1.5f
                        )
                        drawCircle(
                            color = accentColor.copy(alpha = 0.25f),
                            radius = 24.dp.toPx(),
                            center = pos
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = 6.dp.toPx(),
                            center = pos
                        )
                    }
                }
            }
        }
    }
}
