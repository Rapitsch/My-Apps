package com.example.model

/**
 * Encapsulates real-time acoustic analysis metrics.
 */
data class AcousticState(
    val isMicActive: Boolean = false,
    val isAudioRunning: Boolean = true,
    val audioSource: AudioSourceType = AudioSourceType.SYNTHETIC_RESONANCE,
    val decibels: Float = 0f,
    val peakFrequencyHz: Float = 440f,
    val spectralCentroid: Float = 0.5f,
    // Frequency band energies normalized to 0f..1f
    val infrasonicEnergy: Float = 0f,   // < 40 Hz
    val subBassEnergy: Float = 0f,      // 40 - 150 Hz
    val lowMidEnergy: Float = 0f,       // 150 - 600 Hz
    val midEnergy: Float = 0f,          // 600 - 2500 Hz
    val highEnergy: Float = 0f,         // 2500 - 8000 Hz
    val ultrasonicEnergy: Float = 0f,   // > 8000 Hz
    val rawSpectrum: FloatArray = FloatArray(64) { 0f },
    val harmonicCoherence: Float = 0.5f,
    // Looper state
    val isRecordingLoop: Boolean = false,
    val isLoopActive: Boolean = false,
    val loopProgress: Float = 0f,
    // Theremin state
    val thereminPitchHz: Float = 440f,
    val thereminResonance: Float = 0.5f
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AcousticState
        return isMicActive == other.isMicActive &&
                isAudioRunning == other.isAudioRunning &&
                audioSource == other.audioSource &&
                decibels == other.decibels &&
                peakFrequencyHz == other.peakFrequencyHz &&
                isRecordingLoop == other.isRecordingLoop &&
                isLoopActive == other.isLoopActive &&
                rawSpectrum.contentEquals(other.rawSpectrum)
    }

    override fun hashCode(): Int {
        var result = isMicActive.hashCode()
        result = 31 * result + isAudioRunning.hashCode()
        result = 31 * result + audioSource.hashCode()
        result = 31 * result + decibels.hashCode()
        result = 31 * result + peakFrequencyHz.hashCode()
        result = 31 * result + isRecordingLoop.hashCode()
        result = 31 * result + isLoopActive.hashCode()
        result = 31 * result + rawSpectrum.contentHashCode()
        return result
    }
}

enum class AudioSourceType(val label: String) {
    LIVE_MICROPHONE("Live Microphone"),
    SYNTHETIC_RESONANCE("Harmonic Ambient Synth"),
    SEISMIC_INFRASOUND("Sub-Bass Infrasonic Sweep"),
    SOLFEGGIO_CRYSTAL("Crystal Solfeggio Tones"),
    INTERACTIVE_THEREMIN("Frequency Theremin Pad"),
    AUDIO_LOOPER("5s Acoustic Looper")
}

enum class ArchitecturalPreset(val title: String, val subtitle: String) {
    RESONANT_CITADEL("Resonant Citadel", "Gothic towers & soaring spires modulated by octave peaks"),
    CYMATIC_TORUS("Cymatic Torus", "Sacred geometry torus pulsating with standing acoustic waves"),
    BRUTALIST_MONOLITH("Brutalist Monolith", "Layered cantilevered blocks with acoustic resonance chambers"),
    GEODESIC_POLYHEDRON("Geodesic Pavilion", "Interlocking crystalline lattice flexing with audio flux"),
    CHLADNI_PLATE_2D("2D Chladni Surface", "Acoustic modal plane with harmonic nodal lines")
}

enum class InteractionMode(val label: String) {
    ORBIT("3D Orbit"),
    PAN("Pan / Translate"),
    ZOOM("Zoom / Scale"),
    SCULPT("Touch Sculpt"),
    THEREMIN("Theremin Pad")
}

enum class SculptBrush(val label: String, val description: String) {
    EXTRUDE("Extrude", "Pulls vertices outwards along surface normals"),
    INDENT("Indent", "Pushes vertices inwards creating acoustic chambers"),
    TWIST("Twist Vortex", "Rotates vertices around touch contact point"),
    WAVE_PULSE("Harmonic Wave", "Spreads concentric ripples from touch location"),
    SMOOTH("Harmonize", "Gently relaxes vertices toward original geometry")
}

enum class MaterialStyle(val title: String, val description: String) {
    NEON_HOLOGRAM("Neon Hologram", "Laser wireframe with glowing ethereal core"),
    CRYSTAL_QUARTZ("Crystal Quartz", "Prismatic translucent facets and faceted refraction"),
    LIQUID_CHROME("Liquid Chrome", "High-specular polished metallic surface reflections"),
    OBSIDIAN_STONE("Raw Obsidian", "Deep monolithic stone with sharp chisel highlights")
}

enum class VisualPalette(
    val title: String,
    val primaryHex: Long,
    val secondaryHex: Long,
    val accentHex: Long,
    val bgHex: Long
) {
    CYBER_OBSIDIAN("Obsidian Cyan", 0xFF00E5FF, 0xFF7C4DFF, 0xFFFFB300, 0xFF080C14),
    EMERALD_MATRIX("Bioluminescent", 0xFF00E676, 0xFF1DE9B6, 0xFFFFEA00, 0xFF06110D),
    SOLAR_AMBER("Solar Resonance", 0xFFFF9100, 0xFFFF3D00, 0xFFFFD700, 0xFF140C07),
    VIOLET_ETHEREAL("Cosmic Nebula", 0xFFE040FB, 0xFF7C4DFF, 0xFF00E5FF, 0xFF0D0814)
}

data class SculptureSnapshot(
    val id: String,
    val name: String,
    val preset: ArchitecturalPreset,
    val material: MaterialStyle,
    val palette: VisualPalette,
    val formattedTime: String,
    val peakFrequencyHz: Float,
    val sculptOffsets: Map<Int, Triple<Float, Float, Float>>
)
