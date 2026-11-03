package com.example.audio

import com.example.data.model.ProcessingChainEntity
import kotlin.math.*

class NonDestructiveDsp {

    /**
     * Applies non-destructive DSP processing on an array of 16-bit PCM samples.
     * The input samples remain untouched; a newly processed array is returned.
     */
    fun processBuffer(
        inputSamples: ShortArray,
        chain: ProcessingChainEntity,
        sampleRate: Int = 44100
    ): ShortArray {
        val output = ShortArray(inputSamples.size)
        val lowGainLin = dbToLinear(chain.eqLowGain)
        val midGainLin = dbToLinear(chain.eqMidGain)
        val highGainLin = dbToLinear(chain.eqHighGain)
        val volumeGain = chain.volumeGain

        // Reverb delay buffer (approx 80ms)
        val delaySamples = (sampleRate * 0.075).toInt()
        val delayBuffer = FloatArray(delaySamples)
        var delayWriteIndex = 0

        val compThresholdLin = dbToLinear(chain.compressorThreshold) * 32767.0
        val compRatio = chain.compressorRatio.coerceAtLeast(1.0f)

        for (i in inputSamples.indices) {
            val sample = inputSamples[i].toFloat()

            // 1. Equalizer simulation (3-band tone balancing)
            // Low filter (~250Hz), High filter (~4000Hz)
            var eqSample = sample
            if (chain.eqLowGain != 0f || chain.eqMidGain != 0f || chain.eqHighGain != 0f) {
                val lowPart = sample * 0.35f * lowGainLin
                val midPart = sample * 0.40f * midGainLin
                val highPart = sample * 0.25f * highGainLin
                eqSample = lowPart + midPart + highPart
            }

            // 2. Compressor (Dynamic Range Compression)
            var compSample = eqSample
            val absSample = abs(eqSample)
            if (absSample > compThresholdLin) {
                val excess = absSample - compThresholdLin
                val compressedExcess = excess / compRatio
                val newMagnitude = compThresholdLin + compressedExcess
                compSample = sign(eqSample) * newMagnitude.toFloat()
            }

            // 3. Reverb simulation (Comb filter delay line)
            var revSample = compSample
            if (chain.reverbWetDry > 0f) {
                val delayed = delayBuffer[delayWriteIndex]
                val feedback = delayed * (chain.reverbRoomSize * 0.65f)
                delayBuffer[delayWriteIndex] = compSample + feedback
                delayWriteIndex = (delayWriteIndex + 1) % delaySamples

                revSample = (compSample * (1f - chain.reverbWetDry)) + (delayed * chain.reverbWetDry)
            }

            // 4. Volume Gain & Soft Limiting
            val finalSample = revSample * volumeGain
            val clamped = finalSample.coerceIn(-32768f, 32767f)
            output[i] = clamped.toInt().toShort()
        }

        return output
    }

    private fun dbToLinear(db: Float): Float {
        return 10.0.pow((db / 20.0).toDouble()).toFloat()
    }
}
