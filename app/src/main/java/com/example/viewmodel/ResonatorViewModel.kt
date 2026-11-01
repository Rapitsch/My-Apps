package com.example.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AcousticEngine
import com.example.geometry.ArchitectureMeshes
import com.example.geometry.ArchitecturalModel
import com.example.geometry.ProjectedPoint
import com.example.geometry.Vec3
import com.example.model.AcousticState
import com.example.model.ArchitecturalPreset
import com.example.model.AudioSourceType
import com.example.model.InteractionMode
import com.example.model.MaterialStyle
import com.example.model.SculptBrush
import com.example.model.SculptureSnapshot
import com.example.model.VisualPalette
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.sin
import kotlin.math.sqrt

data class ViewportState(
    val preset: ArchitecturalPreset = ArchitecturalPreset.RESONANT_CITADEL,
    val interactionMode: InteractionMode = InteractionMode.ORBIT,
    val sculptBrush: SculptBrush = SculptBrush.EXTRUDE,
    val brushRadius: Float = 90f,
    val brushIntensity: Float = 0.5f,
    val palette: VisualPalette = VisualPalette.CYBER_OBSIDIAN,
    val materialStyle: MaterialStyle = MaterialStyle.NEON_HOLOGRAM,
    val audioReactivity: Float = 1.3f,
    val wireframeOnly: Boolean = false,
    val autoOrbit: Boolean = false,
    val rotationX: Float = -0.3f,
    val rotationY: Float = 0.6f,
    val panX: Float = 0f,
    val panY: Float = 0f,
    val zoomScale: Float = 1.0f,
    val sculptHistoryCount: Int = 0
)

class ResonatorViewModel : ViewModel() {

    private val acousticEngine = AcousticEngine(viewModelScope)
    val acousticState: StateFlow<AcousticState> = acousticEngine.acousticState

    private val _viewportState = MutableStateFlow(ViewportState())
    val viewportState: StateFlow<ViewportState> = _viewportState.asStateFlow()

    private val _currentModel = MutableStateFlow(ArchitectureMeshes.generateModel(ArchitecturalPreset.RESONANT_CITADEL))
    val currentModel: StateFlow<ArchitecturalModel> = _currentModel.asStateFlow()

    private val _sculptDisplacements = MutableStateFlow<Map<Int, Vec3>>(emptyMap())
    val sculptDisplacements: StateFlow<Map<Int, Vec3>> = _sculptDisplacements.asStateFlow()

    private val _snapshots = MutableStateFlow<List<SculptureSnapshot>>(emptyList())
    val snapshots: StateFlow<List<SculptureSnapshot>> = _snapshots.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                if (_viewportState.value.autoOrbit && _viewportState.value.interactionMode != InteractionMode.SCULPT && _viewportState.value.interactionMode != InteractionMode.THEREMIN) {
                    _viewportState.update {
                        it.copy(rotationY = (it.rotationY + 0.006f) % (2f * Math.PI.toFloat()))
                    }
                }
                delay(16)
            }
        }
    }

    fun setInteractionMode(mode: InteractionMode) {
        _viewportState.update { it.copy(interactionMode = mode) }
        if (mode == InteractionMode.THEREMIN) {
            acousticEngine.setAudioSource(AudioSourceType.INTERACTIVE_THEREMIN, false)
        }
    }

    fun setPreset(preset: ArchitecturalPreset) {
        _viewportState.update { it.copy(preset = preset) }
        _currentModel.value = ArchitectureMeshes.generateModel(preset)
        _sculptDisplacements.value = emptyMap()
    }

    fun setMaterialStyle(style: MaterialStyle) {
        _viewportState.update { it.copy(materialStyle = style) }
    }

    fun setSculptBrush(brush: SculptBrush) {
        _viewportState.update { it.copy(sculptBrush = brush) }
    }

    fun setBrushRadius(radius: Float) {
        _viewportState.update { it.copy(brushRadius = radius) }
    }

    fun setBrushIntensity(intensity: Float) {
        _viewportState.update { it.copy(brushIntensity = intensity) }
    }

    fun setPalette(palette: VisualPalette) {
        _viewportState.update { it.copy(palette = palette) }
    }

    fun setAudioReactivity(gain: Float) {
        _viewportState.update { it.copy(audioReactivity = gain) }
    }

    fun setWireframeOnly(enabled: Boolean) {
        _viewportState.update { it.copy(wireframeOnly = enabled) }
    }

    fun toggleAutoOrbit() {
        _viewportState.update { it.copy(autoOrbit = !it.autoOrbit) }
    }

    fun updateRotation(x: Float, y: Float) {
        _viewportState.update { it.copy(rotationX = x, rotationY = y) }
    }

    fun updatePan(x: Float, y: Float) {
        _viewportState.update { it.copy(panX = x, panY = y) }
    }

    fun updateZoom(zoom: Float) {
        _viewportState.update { it.copy(zoomScale = zoom) }
    }

    fun resetCamera() {
        _viewportState.update {
            it.copy(
                rotationX = -0.3f,
                rotationY = 0.6f,
                panX = 0f,
                panY = 0f,
                zoomScale = 1.0f
            )
        }
    }

    fun resetSculpt() {
        _sculptDisplacements.value = emptyMap()
        _viewportState.update { it.copy(sculptHistoryCount = 0) }
    }

    fun applySculpt(
        touchPoint: Offset,
        projectedPoints: List<ProjectedPoint>,
        brush: SculptBrush,
        radius: Float
    ) {
        if (projectedPoints.isEmpty()) return

        val currentMap = _sculptDisplacements.value.toMutableMap()
        val intensity = _viewportState.value.brushIntensity * 0.08f
        val model = _currentModel.value

        var modifiedAny = false

        for (p in projectedPoints) {
            val dx = p.screenX - touchPoint.x
            val dy = p.screenY - touchPoint.y
            val dist = sqrt(dx * dx + dy * dy)

            if (dist < radius) {
                val falloff = 1.0f - (dist / radius)
                val idx = p.index
                val orig = model.vertices[idx]
                val currentOffset = currentMap[idx] ?: Vec3()

                val newOffset = when (brush) {
                    SculptBrush.EXTRUDE -> {
                        val normal = orig.normalize()
                        currentOffset + (normal * (intensity * falloff))
                    }
                    SculptBrush.INDENT -> {
                        val normal = orig.normalize()
                        currentOffset - (normal * (intensity * falloff))
                    }
                    SculptBrush.TWIST -> {
                        val angle = (falloff * intensity * 2.5f)
                        val twisted = (orig + currentOffset).rotateY(angle)
                        twisted - orig
                    }
                    SculptBrush.WAVE_PULSE -> {
                        val rippleH = sin((dist / radius) * Math.PI.toFloat() * 2f) * (intensity * falloff * 1.5f)
                        currentOffset + Vec3(0f, rippleH, 0f)
                    }
                    SculptBrush.SMOOTH -> {
                        currentOffset * (1.0f - (falloff * 0.25f))
                    }
                }

                currentMap[idx] = newOffset
                modifiedAny = true
            }
        }

        if (modifiedAny) {
            _sculptDisplacements.value = currentMap
            _viewportState.update { it.copy(sculptHistoryCount = it.sculptHistoryCount + 1) }
        }
    }

    // Theremin Pad Controls
    fun setThereminParams(pitchHz: Float, resonance: Float) {
        acousticEngine.setThereminParams(pitchHz, resonance)
    }

    // Looper Controls
    fun startRecordLoop() {
        acousticEngine.startRecordingLoop()
    }

    fun toggleLoopPlayback() {
        acousticEngine.toggleLoopPlayback()
    }

    // Gallery Snapshot Save / Load / Delete
    fun saveSnapshot(name: String) {
        val timeFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
        val serializableMap = _sculptDisplacements.value.mapValues {
            Triple(it.value.x, it.value.y, it.value.z)
        }
        val snapshot = SculptureSnapshot(
            id = UUID.randomUUID().toString(),
            name = name,
            preset = _viewportState.value.preset,
            material = _viewportState.value.materialStyle,
            palette = _viewportState.value.palette,
            formattedTime = timeFormat.format(Date()),
            peakFrequencyHz = acousticState.value.peakFrequencyHz,
            sculptOffsets = serializableMap
        )
        _snapshots.update { listOf(snapshot) + it }
    }

    fun loadSnapshot(snapshot: SculptureSnapshot) {
        setPreset(snapshot.preset)
        setMaterialStyle(snapshot.material)
        setPalette(snapshot.palette)
        val restoredMap = snapshot.sculptOffsets.mapValues {
            Vec3(it.value.first, it.value.second, it.value.third)
        }
        _sculptDisplacements.value = restoredMap
        _viewportState.update { it.copy(sculptHistoryCount = restoredMap.size) }
    }

    fun deleteSnapshot(id: String) {
        _snapshots.update { it.filterNot { s -> s.id == id } }
    }

    fun setAudioSource(source: AudioSourceType, hasMicPermission: Boolean) {
        acousticEngine.setAudioSource(source, hasMicPermission)
    }

    fun toggleAudioRunning() {
        acousticEngine.toggleRunning()
    }
}
