package com.example.ui.viewport

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.geometry.ArchitecturalModel
import com.example.geometry.ProjectedPoint
import com.example.geometry.Vec3
import com.example.model.AcousticState
import com.example.model.InteractionMode
import com.example.model.MaterialStyle
import com.example.model.SculptBrush
import com.example.model.VisualPalette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// Seeded 3D orbital particles for the Audio-Reactive Halo
private val haloParticleSeeds: List<Vec3> = run {
    val list = mutableListOf<Vec3>()
    val count = 48
    for (i in 0 until count) {
        val u = (i.toFloat() / count) * 2f * PI.toFloat()
        val v = ((i * 1.618f) % 1f) * PI.toFloat() - (PI.toFloat() / 2f)
        val r = 2.4f + ((i % 5) * 0.25f)
        val x = r * cos(v) * cos(u)
        val y = r * sin(v)
        val z = r * cos(v) * sin(u)
        list.add(Vec3(x, y, z))
    }
    list
}

@Composable
fun ArchitecturalViewport(
    model: ArchitecturalModel,
    sculptDisplacements: Map<Int, Vec3>,
    acousticState: AcousticState,
    interactionMode: InteractionMode,
    sculptBrush: SculptBrush,
    brushRadius: Float,
    palette: VisualPalette,
    materialStyle: MaterialStyle,
    audioReactivity: Float,
    wireframeOnly: Boolean,
    rotationX: Float,
    rotationY: Float,
    panX: Float,
    panY: Float,
    zoomScale: Float,
    onRotateChange: (Float, Float) -> Unit,
    onPanChange: (Float, Float) -> Unit,
    onZoomChange: (Float) -> Unit,
    onSculptApply: (touchPoint: Offset, projected: List<ProjectedPoint>, brush: SculptBrush, radius: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var touchFeedbackPos by remember { mutableStateOf<Offset?>(null) }
    var lastProjectedPoints by remember { mutableStateOf<List<ProjectedPoint>>(emptyList()) }

    val primaryColor = Color(palette.primaryHex)
    val secondaryColor = Color(palette.secondaryHex)
    val accentColor = Color(palette.accentHex)
    val bgColor = Color(palette.bgHex)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        bgColor.copy(alpha = 0.95f),
                        Color(0xFF030508)
                    ),
                    radius = 1400f
                )
            )
            .testTag("viewport_container")
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    if (zoom != 1f) {
                        onZoomChange((zoomScale * zoom).coerceIn(0.35f, 4.5f))
                    }
                    if (interactionMode == InteractionMode.PAN) {
                        onPanChange(panX + pan.x, panY + pan.y)
                    }
                }
            }
            .pointerInput(interactionMode, sculptBrush, brushRadius, zoomScale, panX, panY, rotationX, rotationY) {
                detectDragGestures(
                    onDragStart = { offset ->
                        if (interactionMode == InteractionMode.SCULPT) {
                            touchFeedbackPos = offset
                            onSculptApply(offset, lastProjectedPoints, sculptBrush, brushRadius)
                        }
                    },
                    onDragEnd = {
                        touchFeedbackPos = null
                    },
                    onDragCancel = {
                        touchFeedbackPos = null
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        when (interactionMode) {
                            InteractionMode.ORBIT -> {
                                val newY = rotationY + dragAmount.x * 0.008f
                                val newX = (rotationX - dragAmount.y * 0.008f).coerceIn(-1.4f, 1.4f)
                                onRotateChange(newX, newY)
                            }
                            InteractionMode.PAN -> {
                                onPanChange(panX + dragAmount.x, panY + dragAmount.y)
                            }
                            InteractionMode.ZOOM -> {
                                val deltaZoom = -dragAmount.y * 0.008f
                                onZoomChange((zoomScale + deltaZoom).coerceIn(0.35f, 4.5f))
                            }
                            InteractionMode.SCULPT -> {
                                touchFeedbackPos = change.position
                                onSculptApply(change.position, lastProjectedPoints, sculptBrush, brushRadius)
                            }
                            InteractionMode.THEREMIN -> {
                                // Handled by Theremin overlay
                            }
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize().testTag("viewport_canvas")) {
            val canvasW = size.width
            val canvasH = size.height
            val centerX = canvasW * 0.5f + panX
            val centerY = canvasH * 0.48f + panY
            val baseUnit = minOf(canvasW, canvasH) * 0.28f * zoomScale

            // 1. Draw Ambient Acoustic Grid / Horizon Plane
            drawGroundRings(
                centerX = centerX,
                centerY = centerY + baseUnit * 1.6f,
                radius = baseUnit * 2.2f,
                infrasound = acousticState.infrasonicEnergy * audioReactivity,
                subBass = acousticState.subBassEnergy * audioReactivity,
                color = secondaryColor.copy(alpha = 0.22f)
            )

            // 2. Modulate vertices with audio frequencies + user sculpt displacements
            val deformedVertices = ArrayList<Vec3>(model.vertices.size)
            val subBassScale = 1.0f + (acousticState.subBassEnergy * 0.35f * audioReactivity)
            val midScale = 1.0f + (acousticState.midEnergy * 0.4f * audioReactivity)
            val highScale = 1.0f + (acousticState.highEnergy * 0.5f * audioReactivity)
            val ultraWave = acousticState.ultrasonicEnergy * 0.25f * audioReactivity

            for (i in model.vertices.indices) {
                val orig = model.vertices[i]
                val sculptOffset = sculptDisplacements[i] ?: Vec3()

                val bandFactor = when {
                    orig.y < -0.8f -> subBassScale
                    orig.y < 0.6f -> midScale
                    else -> highScale
                }

                val distFromCenter = sqrt(orig.x * orig.x + orig.z * orig.z)
                val waveRipple = sin(distFromCenter * 6f + acousticState.peakFrequencyHz * 0.01f) * ultraWave

                val vx = (orig.x + sculptOffset.x) * bandFactor + (orig.x * waveRipple)
                val vy = (orig.y + sculptOffset.y) * bandFactor + waveRipple * 0.5f
                val vz = (orig.z + sculptOffset.z) * bandFactor + (orig.z * waveRipple)

                deformedVertices.add(Vec3(vx, vy, vz))
            }

            // 3. 3D Rotation and Perspective Projection
            val cameraDist = 6.0f
            val projectedPoints = ArrayList<ProjectedPoint>(deformedVertices.size)

            for (i in deformedVertices.indices) {
                val v = deformedVertices[i]
                val rotated = v.rotateEuler(rotationX, rotationY, 0f)
                val zDepth = rotated.z + cameraDist
                val fov = 1.0f / maxOf(0.5f, zDepth)

                val sx = centerX + (rotated.x * baseUnit * fov * 3.5f)
                val sy = centerY - (rotated.y * baseUnit * fov * 3.5f)

                projectedPoints.add(ProjectedPoint(sx, sy, zDepth, i))
            }

            lastProjectedPoints = projectedPoints

            // 4. Render Audio-Reactive Particle Halo
            val haloBurst = (acousticState.decibels / 100f) * audioReactivity * 0.6f
            for (pSeed in haloParticleSeeds) {
                val pMod = pSeed * (1.0f + haloBurst)
                val pRot = pMod.rotateEuler(rotationX * 0.8f, rotationY + (acousticState.peakFrequencyHz * 0.0005f), 0f)
                val pZ = pRot.z + cameraDist
                if (pZ > 0.5f) {
                    val pFov = 1.0f / pZ
                    val psx = centerX + (pRot.x * baseUnit * pFov * 3.5f)
                    val psy = centerY - (pRot.y * baseUnit * pFov * 3.5f)
                    val pSize = (3.5f * pFov * 3f).coerceIn(1.5f, 6.5f)
                    val pAlpha = (0.25f + haloBurst * 0.6f).coerceIn(0.1f, 0.95f)
                    drawCircle(
                        color = accentColor.copy(alpha = pAlpha),
                        radius = pSize,
                        center = Offset(psx, psy)
                    )
                }
            }

            // 5. Render Faces with Material Style Shading
            if (!wireframeOnly && model.faces.isNotEmpty()) {
                val sortedFaces = model.faces.map { face ->
                    val d1 = projectedPoints[face.i1].depth
                    val d2 = projectedPoints[face.i2].depth
                    val d3 = projectedPoints[face.i3].depth
                    val avgDepth = (d1 + d2 + d3) / 3f
                    Triple(face, avgDepth, face.tier)
                }.sortedByDescending { it.second }

                for ((face, _, tier) in sortedFaces) {
                    val p1 = projectedPoints[face.i1]
                    val p2 = projectedPoints[face.i2]
                    val p3 = projectedPoints[face.i3]

                    val vRotNorm = face.normal.rotateEuler(rotationX, rotationY, 0f)
                    val lightDir = Vec3(0.35f, 0.75f, 0.55f).normalize()
                    val lightDot = (vRotNorm.dot(lightDir)).coerceIn(0.05f, 1.0f)

                    val faceBaseColor = when (tier) {
                        0 -> secondaryColor
                        1 -> primaryColor
                        else -> accentColor
                    }

                    // Material Shader application
                    val fillColor = when (materialStyle) {
                        MaterialStyle.NEON_HOLOGRAM -> {
                            faceBaseColor.copy(alpha = 0.15f + lightDot * 0.25f)
                        }
                        MaterialStyle.CRYSTAL_QUARTZ -> {
                            val prismatic = if (lightDot > 0.6f) accentColor else faceBaseColor
                            prismatic.copy(alpha = 0.28f + (lightDot * 0.45f))
                        }
                        MaterialStyle.LIQUID_CHROME -> {
                            val specular = if (lightDot > 0.75f) Color.White else faceBaseColor
                            specular.copy(alpha = 0.45f + lightDot * 0.45f)
                        }
                        MaterialStyle.OBSIDIAN_STONE -> {
                            Color(0xFF141923).copy(alpha = 0.85f + lightDot * 0.12f)
                        }
                    }

                    val facePath = Path().apply {
                        moveTo(p1.screenX, p1.screenY)
                        lineTo(p2.screenX, p2.screenY)
                        lineTo(p3.screenX, p3.screenY)
                        close()
                    }
                    drawPath(path = facePath, color = fillColor)
                }
            }

            // 6. Render Architectural Edges
            for (edge in model.edges) {
                val p1 = projectedPoints[edge.i1]
                val p2 = projectedPoints[edge.i2]

                val edgeColor = when (materialStyle) {
                    MaterialStyle.OBSIDIAN_STONE -> Color(0xFF64748B)
                    MaterialStyle.LIQUID_CHROME -> Color(0xFFE2E8F0)
                    else -> when (edge.bandCategory) {
                        0 -> secondaryColor
                        1 -> primaryColor
                        else -> accentColor
                    }
                }

                val strokeW = when (edge.bandCategory) {
                    0 -> 2.2f
                    1 -> 1.8f
                    else -> 1.5f
                } * (zoomScale.coerceIn(0.7f, 2.0f))

                drawLine(
                    color = edgeColor.copy(alpha = if (materialStyle == MaterialStyle.NEON_HOLOGRAM) 0.95f else 0.75f),
                    start = Offset(p1.screenX, p1.screenY),
                    end = Offset(p2.screenX, p2.screenY),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }

            // 7. Glowing Vertex Nodes
            for (p in projectedPoints) {
                val nodeSize = (3.5f * (4.5f / p.depth)).coerceIn(2f, 7.5f)
                val nodeColor = if (materialStyle == MaterialStyle.CRYSTAL_QUARTZ) accentColor else primaryColor
                drawCircle(
                    color = nodeColor,
                    radius = nodeSize,
                    center = Offset(p.screenX, p.screenY)
                )
            }

            // 8. Interactive Touch Sculpt Reticle
            touchFeedbackPos?.let { pos ->
                drawCircle(
                    color = accentColor.copy(alpha = 0.25f),
                    radius = brushRadius,
                    center = pos
                )
                drawCircle(
                    color = accentColor,
                    radius = brushRadius,
                    center = pos,
                    style = Stroke(width = 3f)
                )
                drawCircle(
                    color = primaryColor,
                    radius = 8f,
                    center = pos
                )
            }
        }
    }
}

private fun DrawScope.drawGroundRings(
    centerX: Float,
    centerY: Float,
    radius: Float,
    infrasound: Float,
    subBass: Float,
    color: Color
) {
    val ringCount = 5
    for (i in 1..ringCount) {
        val r = (radius * (i.toFloat() / ringCount)) + (infrasound * 30f * (i % 2))
        val alpha = (0.28f - (i * 0.04f)).coerceAtLeast(0.05f)
        drawOval(
            color = color.copy(alpha = alpha),
            topLeft = Offset(centerX - r, centerY - (r * 0.35f)),
            size = androidx.compose.ui.geometry.Size(r * 2f, r * 0.7f),
            style = Stroke(width = 1.2f)
        )
    }

    val spokeCount = 12
    for (s in 0 until spokeCount) {
        val angle = (2f * PI.toFloat() * s) / spokeCount
        val sx = centerX + cos(angle) * (radius * 0.3f)
        val sy = centerY + sin(angle) * (radius * 0.3f * 0.35f)
        val ex = centerX + cos(angle) * radius
        val ey = centerY + sin(angle) * (radius * 0.35f)
        drawLine(
            color = color.copy(alpha = 0.12f + subBass * 0.15f),
            start = Offset(sx, sy),
            end = Offset(ex, ey),
            strokeWidth = 1f
        )
    }
}
