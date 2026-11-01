package com.example.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.example.model.AcousticState
import com.example.model.AudioSourceType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class AcousticEngine(private val scope: CoroutineScope) {

    private val _acousticState = MutableStateFlow(AcousticState())
    val acousticState: StateFlow<AcousticState> = _acousticState.asStateFlow()

    private var audioJob: Job? = null
    private var audioRecord: AudioRecord? = null

    private val sampleRate = 44100
    private val bufferSize = 1024
    private val fftSize = 1024

    private val smoothedSpectrum = FloatArray(64) { 0f }
    private var synthPhase = 0.0
    private var lfoPhase = 0.0

    // Interactive Theremin State
    private var thereminFreq = 440.0
    private var thereminResonance = 0.5
    private var thereminTargetFreq = 440.0

    // 5-Second Looper State
    private val loopMaxSamples = sampleRate * 5 // 5 seconds
    private val loopBuffer = DoubleArray(loopMaxSamples)
    private var loopRecordedSamples = 0
    private var loopPlaybackIndex = 0
    private var loopRecordingJob: Job? = null

    init {
        startProcessing()
    }

    fun setThereminParams(pitchHz: Float, resonance: Float) {
        thereminTargetFreq = pitchHz.toDouble().coerceIn(40.0, 2400.0)
        thereminResonance = resonance.toDouble().coerceIn(0.1, 1.0)
        _acousticState.update {
            it.copy(
                thereminPitchHz = pitchHz,
                thereminResonance = resonance
            )
        }
    }

    fun startRecordingLoop() {
        if (_acousticState.value.isRecordingLoop) return

        loopRecordedSamples = 0
        loopPlaybackIndex = 0
        _acousticState.update { it.copy(isRecordingLoop = true, loopProgress = 0f, isLoopActive = false) }

        loopRecordingJob?.cancel()
        loopRecordingJob = scope.launch(Dispatchers.Default) {
            val totalSteps = 50
            for (step in 1..totalSteps) {
                if (!isActive) break
                delay(100) // 100ms * 50 = 5000ms = 5s
                val progress = step.toFloat() / totalSteps
                _acousticState.update { it.copy(loopProgress = progress) }
            }

            _acousticState.update {
                it.copy(
                    isRecordingLoop = false,
                    isLoopActive = true,
                    loopProgress = 1.0f,
                    audioSource = AudioSourceType.AUDIO_LOOPER
                )
            }
            restartProcessing()
        }
    }

    fun toggleLoopPlayback() {
        val current = _acousticState.value.isLoopActive
        if (!current && loopRecordedSamples > 0) {
            _acousticState.update {
                it.copy(
                    isLoopActive = true,
                    audioSource = AudioSourceType.AUDIO_LOOPER
                )
            }
            restartProcessing()
        } else {
            _acousticState.update { it.copy(isLoopActive = false) }
        }
    }

    fun setAudioSource(source: AudioSourceType, hasMicPermission: Boolean) {
        val effectiveSource = if (source == AudioSourceType.LIVE_MICROPHONE && !hasMicPermission) {
            AudioSourceType.SYNTHETIC_RESONANCE
        } else {
            source
        }

        _acousticState.update { it.copy(audioSource = effectiveSource) }
        restartProcessing()
    }

    fun toggleRunning() {
        val running = !_acousticState.value.isAudioRunning
        _acousticState.update { it.copy(isAudioRunning = running) }
        if (running) {
            startProcessing()
        } else {
            stopProcessing()
        }
    }

    private fun restartProcessing() {
        stopProcessing()
        if (_acousticState.value.isAudioRunning) {
            startProcessing()
        }
    }

    private fun startProcessing() {
        audioJob?.cancel()
        audioJob = scope.launch(Dispatchers.Default) {
            if (_acousticState.value.audioSource == AudioSourceType.LIVE_MICROPHONE) {
                runMicLoop()
            } else {
                runSynthLoop()
            }
        }
    }

    private fun stopProcessing() {
        audioJob?.cancel()
        audioJob = null
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
    }

    @SuppressLint("MissingPermission")
    private suspend fun CoroutineScope.runMicLoop() {
        val minBuf = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val readBufferSize = maxOf(minBuf, bufferSize * 2)
        var record: AudioRecord? = null

        try {
            record = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                readBufferSize
            )

            if (record.state != AudioRecord.STATE_INITIALIZED) {
                Log.w("AcousticEngine", "AudioRecord initialization failed, falling back to Synth")
                fallbackToSynth()
                return
            }

            record.startRecording()
            audioRecord = record
            _acousticState.update { it.copy(isMicActive = true) }

            val audioBuffer = ShortArray(fftSize)
            val real = DoubleArray(fftSize)
            val imag = DoubleArray(fftSize)

            while (isActive) {
                val readCount = record.read(audioBuffer, 0, fftSize)
                if (readCount > 0) {
                    var sumSquare = 0.0
                    for (i in 0 until fftSize) {
                        val sample = audioBuffer[i] / 32768.0
                        sumSquare += sample * sample

                        // Capture into looper buffer if recording
                        if (_acousticState.value.isRecordingLoop && loopRecordedSamples < loopMaxSamples) {
                            loopBuffer[loopRecordedSamples++] = sample
                        }

                        // Hann windowing
                        val window = 0.5 * (1.0 - cos(2.0 * PI * i / (fftSize - 1)))
                        real[i] = sample * window
                        imag[i] = 0.0
                    }

                    val rms = sqrt(sumSquare / fftSize)
                    val decibels = (20.0 * kotlin.math.log10(rms.coerceAtLeast(0.0001)) + 90.0).toFloat().coerceIn(0f, 100f)

                    fft(real, imag)
                    processMagnitudes(real, imag, decibels)
                }
                delay(20) // ~50 fps update
            }
        } catch (_: CancellationException) {
            // Normal coroutine cancellation
        } catch (e: Exception) {
            Log.e("AcousticEngine", "Error recording audio: ${e.message}")
            fallbackToSynth()
        } finally {
            try {
                record?.stop()
                record?.release()
            } catch (_: Exception) {}
            if (audioRecord == record) {
                audioRecord = null
            }
            _acousticState.update { it.copy(isMicActive = false) }
        }
    }

    private suspend fun CoroutineScope.runSynthLoop() {
        _acousticState.update { it.copy(isMicActive = false) }
        val real = DoubleArray(fftSize)
        val imag = DoubleArray(fftSize)

        var time = 0.0
        try {
            while (isActive) {
                val source = _acousticState.value.audioSource
                var sumSquare = 0.0

                // Smooth glide for Theremin pitch
                thereminFreq += (thereminTargetFreq - thereminFreq) * 0.25

                for (i in 0 until fftSize) {
                    val t = time + (i.toDouble() / sampleRate)
                    val sample = when (source) {
                        AudioSourceType.INTERACTIVE_THEREMIN -> {
                            val f = thereminFreq
                            val s1 = sin(2.0 * PI * f * t) * 0.65
                            val s2 = sin(2.0 * PI * (f * 2.0) * t) * (0.25 * thereminResonance)
                            val s3 = sin(2.0 * PI * (f * 3.0) * t) * (0.15 * thereminResonance)
                            val sub = sin(2.0 * PI * (f * 0.5) * t) * 0.3
                            s1 + s2 + s3 + sub
                        }
                        AudioSourceType.AUDIO_LOOPER -> {
                            if (loopRecordedSamples > fftSize) {
                                val s = loopBuffer[loopPlaybackIndex]
                                loopPlaybackIndex = (loopPlaybackIndex + 1) % loopRecordedSamples
                                s
                            } else {
                                // Default rhythmic echo if loop buffer empty
                                sin(2.0 * PI * 220.0 * t) * 0.5
                            }
                        }
                        AudioSourceType.SEISMIC_INFRASOUND -> {
                            val sub1 = sin(2.0 * PI * 14.0 * t) * 0.7
                            val sub2 = sin(2.0 * PI * 28.0 * t + 0.3) * 0.4
                            val rumble = (Random.nextDouble() - 0.5) * 0.15
                            (sub1 + sub2 + rumble) * 0.85
                        }
                        AudioSourceType.SOLFEGGIO_CRYSTAL -> {
                            val baseFreq = 528.0
                            val highFreq = 852.0
                            val s1 = sin(2.0 * PI * baseFreq * t) * 0.6
                            val s2 = sin(2.0 * PI * highFreq * t) * 0.35
                            val chime = sin(2.0 * PI * (baseFreq * 2.0) * t) * 0.2
                            s1 + s2 + chime
                        }
                        else -> {
                            val lfo = (sin(lfoPhase) + 1.0) * 0.5
                            val f0 = 130.81 + (lfo * 180.0)
                            val s1 = sin(2.0 * PI * f0 * t) * 0.5
                            val s2 = sin(2.0 * PI * (f0 * 1.5) * t) * 0.3
                            val s3 = sin(2.0 * PI * (f0 * 2.0) * t) * 0.25
                            val air = (Random.nextDouble() - 0.5) * 0.08
                            s1 + s2 + s3 + air
                        }
                    }

                    // Also capture synth sample if recording loop
                    if (_acousticState.value.isRecordingLoop && loopRecordedSamples < loopMaxSamples) {
                        loopBuffer[loopRecordedSamples++] = sample
                    }

                    sumSquare += sample * sample
                    val window = 0.5 * (1.0 - cos(2.0 * PI * i / (fftSize - 1)))
                    real[i] = sample * window
                    imag[i] = 0.0
                }

                time += (fftSize.toDouble() / sampleRate)
                lfoPhase += 0.04
                synthPhase += 0.02

                val rms = sqrt(sumSquare / fftSize)
                val decibels = (20.0 * kotlin.math.log10(rms.coerceAtLeast(0.0001)) + 90.0).toFloat().coerceIn(10f, 95f)

                fft(real, imag)
                processMagnitudes(real, imag, decibels)

                delay(20)
            }
        } catch (_: CancellationException) {
            // Normal coroutine cancellation
        }
    }

    private fun fallbackToSynth() {
        _acousticState.update { it.copy(audioSource = AudioSourceType.SYNTHETIC_RESONANCE, isMicActive = false) }
        restartProcessing()
    }

    private fun processMagnitudes(real: DoubleArray, imag: DoubleArray, decibels: Float) {
        val halfSize = fftSize / 2
        val magnitudes = FloatArray(halfSize)
        var maxMag = 0f
        var peakBin = 0

        for (i in 0 until halfSize) {
            val mag = sqrt(real[i] * real[i] + imag[i] * imag[i]).toFloat()
            magnitudes[i] = mag
            if (mag > maxMag && i > 1) {
                maxMag = mag
                peakBin = i
            }
        }

        val peakFreq = peakBin * (sampleRate.toFloat() / fftSize)

        val binCount = 64
        val displayBins = FloatArray(binCount)
        val minFreq = 10f
        val maxFreq = sampleRate / 2f
        val logMin = kotlin.math.ln(minFreq)
        val logMax = kotlin.math.ln(maxFreq)

        for (b in 0 until binCount) {
            val fStart = kotlin.math.exp(logMin + (b.toFloat() / binCount) * (logMax - logMin))
            val fEnd = kotlin.math.exp(logMin + ((b + 1f) / binCount) * (logMax - logMin))

            val binStart = (fStart / (sampleRate.toFloat() / fftSize)).toInt().coerceIn(0, halfSize - 1)
            val binEnd = (fEnd / (sampleRate.toFloat() / fftSize)).toInt().coerceIn(binStart + 1, halfSize)

            var sum = 0f
            for (k in binStart until binEnd) {
                sum += magnitudes[k]
            }
            val avg = sum / (binEnd - binStart)
            val normalized = (avg * 1.8f).coerceIn(0f, 1f)
            smoothedSpectrum[b] = smoothedSpectrum[b] * 0.7f + normalized * 0.3f
            displayBins[b] = smoothedSpectrum[b]
        }

        var infrasonic = 0f
        var subBass = 0f
        var lowMid = 0f
        var mid = 0f
        var high = 0f
        var ultrasonic = 0f

        for (b in 0 until binCount) {
            val v = displayBins[b]
            when (b) {
                in 0..4 -> infrasonic = maxOf(infrasonic, v)
                in 5..14 -> subBass = maxOf(subBass, v)
                in 15..28 -> lowMid = maxOf(lowMid, v)
                in 29..44 -> mid = maxOf(mid, v)
                in 45..56 -> high = maxOf(high, v)
                else -> ultrasonic = maxOf(ultrasonic, v)
            }
        }

        val centroid = (peakFreq / 4000f).coerceIn(0f, 1f)
        val coherence = ((subBass + mid + high) / 3f).coerceIn(0.1f, 1.0f)

        _acousticState.update {
            it.copy(
                decibels = decibels,
                peakFrequencyHz = peakFreq,
                spectralCentroid = centroid,
                infrasonicEnergy = infrasonic,
                subBassEnergy = subBass,
                lowMidEnergy = lowMid,
                midEnergy = mid,
                highEnergy = high,
                ultrasonicEnergy = ultrasonic,
                rawSpectrum = displayBins,
                harmonicCoherence = coherence
            )
        }
    }

    private fun fft(real: DoubleArray, imag: DoubleArray) {
        val n = real.size
        var j = 0
        for (i in 0 until n - 1) {
            if (i < j) {
                val tempR = real[i]
                real[i] = real[j]
                real[j] = tempR
                val tempI = imag[i]
                imag[i] = imag[j]
                imag[j] = tempI
            }
            var k = n shr 1
            while (k <= j) {
                j -= k
                k = k shr 1
            }
            j += k
        }

        var length = 2
        while (length <= n) {
            val halfLength = length shr 1
            val angleStep = -2.0 * PI / length
            val wStepR = cos(angleStep)
            val wStepI = sin(angleStep)

            var i = 0
            while (i < n) {
                var wR = 1.0
                var wI = 0.0
                for (k in 0 until halfLength) {
                    val pos = i + k
                    val partner = pos + halfLength
                    val uR = real[pos]
                    val uI = imag[pos]
                    val vR = real[partner] * wR - imag[partner] * wI
                    val vI = real[partner] * wI + imag[partner] * wR

                    real[pos] = uR + vR
                    imag[pos] = uI + vI
                    real[partner] = uR - vR
                    imag[partner] = uI - vI

                    val nextWR = wR * wStepR - wI * wStepI
                    val nextWI = wR * wStepI + wI * wStepR
                    wR = nextWR
                    wI = nextWI
                }
                i += length
            }
            length = length shl 1
        }
    }
}
