package com.example.audio

import android.content.Context
import com.example.data.model.ProcessingChainEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import kotlin.math.sqrt

data class ExportedMasterResult(
    val exportFile: File,
    val sha256Checksum: String,
    val durationSeconds: Float,
    val fileSizeBytes: Long
)

class AudioMixerExporter(private val context: Context) {
    private val dsp = NonDestructiveDsp()

    suspend fun exportProjectMaster(
        projectId: String,
        projectName: String,
        takeFilesWithChains: List<Pair<File, ProcessingChainEntity?>>
    ): ExportedMasterResult = withContext(Dispatchers.IO) {
        val exportDir = File(context.filesDir, "exports").apply { mkdirs() }
        val cleanName = projectName.replace(Regex("[^a-zA-Z0-9آ-ی\\s]"), "").trim().replace(" ", "_")
        val timestamp = System.currentTimeMillis()
        val masterWavFile = File(exportDir, "Master_${cleanName}_$timestamp.wav")

        if (takeFilesWithChains.isEmpty()) {
            // Write 1 second silence WAV if empty
            val silenceSamples = ShortArray(44100)
            writeWav(masterWavFile, silenceSamples, 44100)
            val checksum = calculateSha256(masterWavFile)
            return@withContext ExportedMasterResult(
                exportFile = masterWavFile,
                sha256Checksum = checksum,
                durationSeconds = 1.0f,
                fileSizeBytes = masterWavFile.length()
            )
        }

        // Read all inputs and find max sample length
        val processedSampleArrays = mutableListOf<ShortArray>()
        var maxSamples = 0

        for ((file, chain) in takeFilesWithChains) {
            if (file.exists() && file.length() > 44) {
                val samples = readWavSamples(file)
                val processed = if (chain != null) {
                    dsp.processBuffer(samples, chain, 44100)
                } else {
                    samples
                }
                processedSampleArrays.add(processed)
                if (processed.size > maxSamples) {
                    maxSamples = processed.size
                }
            }
        }

        val masterSamples = ShortArray(maxSamples)
        val trackCount = processedSampleArrays.size.coerceAtLeast(1)

        for (i in 0 until maxSamples) {
            var sum = 0.0
            for (track in processedSampleArrays) {
                if (i < track.size) {
                    sum += track[i]
                }
            }
            // Soft master limiting
            val mixed = (sum / sqrt(trackCount.toDouble())).coerceIn(-32768.0, 32767.0)
            masterSamples[i] = mixed.toInt().toShort()
        }

        writeWav(masterWavFile, masterSamples, 44100)
        val checksum = calculateSha256(masterWavFile)
        val duration = masterSamples.size.toFloat() / 44100f

        ExportedMasterResult(
            exportFile = masterWavFile,
            sha256Checksum = checksum,
            durationSeconds = duration,
            fileSizeBytes = masterWavFile.length()
        )
    }

    private fun readWavSamples(wavFile: File): ShortArray {
        val fis = FileInputStream(wavFile)
        val data = fis.readBytes()
        fis.close()
        if (data.size <= 44) return shortArrayOf()

        val sampleCount = (data.size - 44) / 2
        val shorts = ShortArray(sampleCount)
        val buffer = ByteBuffer.wrap(data, 44, data.size - 44).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until sampleCount) {
            shorts[i] = buffer.short
        }
        return shorts
    }

    private fun writeWav(wavFile: File, samples: ShortArray, sampleRate: Int) {
        val pcmSize = samples.size * 2
        val totalDataLen = pcmSize + 36
        val channels = 1
        val bitDepth = 16
        val byteRate = sampleRate * channels * bitDepth / 8

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1
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
        header[32] = 2
        header[33] = 0
        header[34] = 16
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (pcmSize and 0xff).toByte()
        header[41] = ((pcmSize shr 8) and 0xff).toByte()
        header[42] = ((pcmSize shr 16) and 0xff).toByte()
        header[43] = ((pcmSize shr 24) and 0xff).toByte()

        val fos = FileOutputStream(wavFile)
        fos.write(header)
        val byteBuffer = ByteBuffer.allocate(samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        for (s in samples) {
            byteBuffer.putShort(s)
        }
        fos.write(byteBuffer.array())
        fos.flush()
        fos.close()
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
