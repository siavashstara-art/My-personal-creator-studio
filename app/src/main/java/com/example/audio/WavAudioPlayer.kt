package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WavAudioPlayer {
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs = _currentPositionMs.asStateFlow()

    private val _totalDurationMs = MutableStateFlow(0L)
    val totalDurationMs = _totalDurationMs.asStateFlow()

    private val _activeTakeIdentifier = MutableStateFlow<String?>(null)
    val activeTakeIdentifier = _activeTakeIdentifier.asStateFlow()

    @Volatile
    private var isPaused = false

    @Volatile
    private var volumeGain = 1.0f

    @Volatile
    private var seekRequestedPositionMs: Long? = null

    fun playTake(
        filePath: String,
        takeId: String,
        startFromMs: Long = 0L,
        onFinished: () -> Unit = {}
    ) {
        stop()

        val audioFile = File(filePath)
        if (!audioFile.exists() || audioFile.length() <= 44) return

        val sampleRate = 44100
        val channels = 1
        val dataSize = audioFile.length() - 44
        val durationMs = (dataSize / (sampleRate * 2)) * 1000L
        _totalDurationMs.value = durationMs
        _activeTakeIdentifier.value = takeId

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = maxOf(minBufferSize * 2, 4096)

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.setVolume(volumeGain)
        audioTrack?.play()
        _isPlaying.value = true
        isPaused = false

        playbackJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                FileInputStream(audioFile).use { fis ->
                    // Skip WAV 44-byte header
                    fis.skip(44)

                    if (startFromMs > 0) {
                        val skipBytes = (startFromMs * sampleRate * 2 / 1000L).coerceAtMost(dataSize)
                        fis.skip(skipBytes)
                    }

                    val buffer = ByteArray(2048)
                    var bytesPlayed = if (startFromMs > 0) (startFromMs * sampleRate * 2 / 1000L) else 0L

                    while (isActive && _isPlaying.value) {
                        if (isPaused) {
                            Thread.sleep(20)
                            continue
                        }

                        val seekMs = seekRequestedPositionMs
                        if (seekMs != null) {
                            seekRequestedPositionMs = null
                            val targetByteOffset = (seekMs * sampleRate * 2 / 1000L).coerceIn(0L, dataSize)
                            fis.channel.position(44 + targetByteOffset)
                            bytesPlayed = targetByteOffset
                        }

                        val bytesRead = fis.read(buffer)
                        if (bytesRead == -1) break

                        audioTrack?.write(buffer, 0, bytesRead)
                        bytesPlayed += bytesRead
                        _currentPositionMs.value = (bytesPlayed * 1000L) / (sampleRate * 2)
                    }
                }
            } catch (e: Exception) {
                // Playback error or interrupt
            } finally {
                _isPlaying.value = false
                audioTrack?.stop()
                audioTrack?.release()
                audioTrack = null
                onFinished()
            }
        }
    }

    fun pause() {
        if (_isPlaying.value && !isPaused) {
            isPaused = true
            audioTrack?.pause()
        }
    }

    fun resume() {
        if (_isPlaying.value && isPaused) {
            isPaused = false
            audioTrack?.play()
        }
    }

    fun seekTo(positionMs: Long) {
        seekRequestedPositionMs = positionMs
        _currentPositionMs.value = positionMs
    }

    fun setVolume(volume: Float) {
        volumeGain = volume.coerceIn(0f, 1f)
        audioTrack?.setVolume(volumeGain)
    }

    fun stop() {
        _isPlaying.value = false
        isPaused = false
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            // Ignored
        }
        audioTrack = null
        _currentPositionMs.value = 0L
    }
}
