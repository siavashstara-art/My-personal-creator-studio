package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import java.util.UUID
import kotlin.math.sqrt

data class RecordingResult(
    val canonicalSourceId: String,
    val filePath: String,
    val sha256Checksum: String,
    val sampleRate: Int,
    val bitDepth: Int,
    val channelCount: Int,
    val durationSamples: Long,
    val durationMs: Long,
    val fileSizeBytes: Long
)

class WavAudioRecorder(private val context: Context) {
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null

    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _currentRmsLevel = MutableStateFlow(0f)
    val currentRmsLevel = _currentRmsLevel.asStateFlow()

    private val _recordedDurationMs = MutableStateFlow(0L)
    val recordedDurationMs = _recordedDurationMs.asStateFlow()

    @SuppressLint("MissingPermission")
    fun startRecording(projectId: String, takeId: String, onStartFailed: (String) -> Unit) {
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            onStartFailed("خطا در تنظیم بافر کارت صدای دستگاه.")
            return
        }

        val bufferSize = maxOf(minBufferSize * 2, 4096)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                onStartFailed("میکروفون مقداردهی نشد. دسترسی میکروفون را بررسی فرمایید.")
                audioRecord?.release()
                audioRecord = null
                return
            }

            audioRecord?.startRecording()
            _isRecording.value = true
            _recordedDurationMs.value = 0L

            val takesDir = File(context.filesDir, "projects/$projectId/takes/$takeId").apply { mkdirs() }
            val rawPcmFile = File(takesDir, "raw_temp.pcm")

            recordingJob = CoroutineScope(Dispatchers.IO).launch {
                val outputStream = FileOutputStream(rawPcmFile)
                val buffer = ShortArray(1024)
                var totalSamples = 0L
                val startTime = System.currentTimeMillis()

                try {
                    while (isActive && _isRecording.value) {
                        val readCount = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                        if (readCount > 0) {
                            // Calculate RMS level for real-time visualizer
                            var sumSq = 0.0
                            for (i in 0 until readCount) {
                                val sample = buffer[i]
                                sumSq += sample * sample
                            }
                            val rms = sqrt(sumSq / readCount) / 32768.0
                            _currentRmsLevel.value = rms.toFloat().coerceIn(0f, 1f)

                            // Write raw PCM
                            val byteBuffer = ByteBuffer.allocate(readCount * 2).order(ByteOrder.LITTLE_ENDIAN)
                            for (i in 0 until readCount) {
                                byteBuffer.putShort(buffer[i])
                            }
                            outputStream.write(byteBuffer.array())
                            totalSamples += readCount
                            _recordedDurationMs.value = System.currentTimeMillis() - startTime
                        }
                    }
                } finally {
                    outputStream.flush()
                    outputStream.close()
                }
            }
        } catch (e: Exception) {
            onStartFailed("خطا هنگام شروع ضبط: ${e.localizedMessage}")
            _isRecording.value = false
        }
    }

    suspend fun stopRecording(projectId: String, takeId: String): RecordingResult? {
        if (!_isRecording.value) return null
        _isRecording.value = false

        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) {
            // Ignored
        }

        recordingJob?.join()
        recordingJob = null
        _currentRmsLevel.value = 0f

        val takesDir = File(context.filesDir, "projects/$projectId/takes/$takeId")
        val rawPcmFile = File(takesDir, "raw_temp.pcm")
        if (!rawPcmFile.exists() || rawPcmFile.length() == 0L) {
            return null
        }

        // Convert PCM to immutable Canonical WAV
        val canonicalWavFile = File(takesDir, "raw_canonical.wav")
        writeWavFile(rawPcmFile, canonicalWavFile, sampleRate, 1, 16)
        rawPcmFile.delete()

        // Set OS-level Read-Only to guarantee non-destructive canonical status
        canonicalWavFile.setReadOnly()

        val checksum = calculateSha256(canonicalWavFile)
        val fileSizeBytes = canonicalWavFile.length()
        val durationSamples = (fileSizeBytes - 44) / 2
        val durationMs = (durationSamples * 1000L) / sampleRate

        return RecordingResult(
            canonicalSourceId = UUID.randomUUID().toString(),
            filePath = canonicalWavFile.absolutePath,
            sha256Checksum = checksum,
            sampleRate = sampleRate,
            bitDepth = 16,
            channelCount = 1,
            durationSamples = durationSamples,
            durationMs = durationMs,
            fileSizeBytes = fileSizeBytes
        )
    }

    private fun writeWavFile(pcmFile: File, wavFile: File, sampleRate: Int, channels: Int, bitDepth: Int) {
        val pcmSize = pcmFile.length()
        val totalDataLen = pcmSize + 36
        val byteRate = sampleRate * channels * bitDepth / 8

        val header = ByteArray(44)
        // RIFF header
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        // WAVE
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        // 'fmt ' chunk
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // SubChunk1Size (16 for PCM)
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // AudioFormat (1 = PCM)
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = ((channels * bitDepth) / 8).toByte() // BlockAlign
        header[33] = 0
        header[34] = bitDepth.toByte() // BitsPerSample
        header[35] = 0
        // 'data' chunk
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (pcmSize and 0xff).toByte()
        header[41] = ((pcmSize shr 8) and 0xff).toByte()
        header[42] = ((pcmSize shr 16) and 0xff).toByte()
        header[43] = ((pcmSize shr 24) and 0xff).toByte()

        val wavOut = FileOutputStream(wavFile)
        wavOut.write(header)

        val pcmIn = FileInputStream(pcmFile)
        val buf = ByteArray(4096)
        var bytesRead: Int
        while (pcmIn.read(buf).also { bytesRead = it } != -1) {
            wavOut.write(buf, 0, bytesRead)
        }
        pcmIn.close()
        wavOut.flush()
        wavOut.close()
    }

    private fun calculateSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buf = ByteArray(8192)
            var n: Int
            while (fis.read(buf).also { n = it } != -1) {
                digest.update(buf, 0, n)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
