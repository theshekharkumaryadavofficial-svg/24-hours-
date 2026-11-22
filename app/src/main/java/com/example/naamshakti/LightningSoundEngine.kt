package com.example.naamshakti

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Lightweight procedural lightning & thunder synthesizer using Android AudioTrack.
 * Delivers punchy anime combat sound effects without heavy bundled audio files.
 */
class LightningSoundEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Volatile
    var soundEnabled: Boolean = true

    fun playAbilitySound(ability: LightningAbility) {
        if (!soundEnabled) return
        when (ability) {
            LightningAbility.LIGHTNING_ATTACK -> playZap(durationMs = 150, baseFreq = 720.0, rumble = 0.25)
            LightningAbility.THUNDER_STRIKE -> playZap(durationMs = 260, baseFreq = 520.0, rumble = 0.55)
            LightningAbility.ENERGY_SHIELD -> playShieldHum(durationMs = 280)
            LightningAbility.LIGHTNING_DASH -> playZap(durationMs = 190, baseFreq = 980.0, rumble = 0.35)
            LightningAbility.MEGA_THUNDER -> playZap(durationMs = 420, baseFreq = 320.0, rumble = 0.85)
            LightningAbility.ULTIMATE_POWER -> playZap(durationMs = 560, baseFreq = 240.0, rumble = 1.0)
        }
    }

    fun playVictoryChime() {
        if (!soundEnabled) return
        scope.launch {
            runCatching {
                val sampleRate = 22050
                val totalSamples = (sampleRate * 0.42).toInt()
                val pcm = ShortArray(totalSamples)
                val notes = doubleArrayOf(523.25, 659.25, 783.99, 1046.5)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val noteIdx = ((t / 0.105).toInt()).coerceIn(0, notes.lastIndex)
                    val env = exp(-t * 4.5)
                    val wave = sin(2.0 * PI * notes[noteIdx] * t) * 0.45 * env
                    pcm[i] = (wave * Short.MAX_VALUE).toInt().coerceIn(-32767, 32767).toShort()
                }
                writePcm(sampleRate, pcm)
            }
        }
    }

    fun playKoTone() {
        if (!soundEnabled) return
        scope.launch {
            runCatching {
                val sampleRate = 22050
                val totalSamples = (sampleRate * 0.38).toInt()
                val pcm = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val freq = 340.0 - (t * 480.0).coerceAtMost(240.0)
                    val env = exp(-t * 5.0)
                    val wave = sin(2.0 * PI * freq * t) * 0.45 * env
                    pcm[i] = (wave * Short.MAX_VALUE).toInt().coerceIn(-32767, 32767).toShort()
                }
                writePcm(sampleRate, pcm)
            }
        }
    }

    private fun playZap(durationMs: Int, baseFreq: Double, rumble: Double) {
        scope.launch {
            runCatching {
                val sampleRate = 22050
                val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val pcm = ShortArray(totalSamples)
                val rng = Random(System.nanoTime())
                var lowPass = 0.0
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val env = exp(-t * (6.5 - rumble * 3.0))
                    val white = rng.nextDouble(-1.0, 1.0)
                    lowPass = lowPass * 0.88 + white * 0.12
                    val crackle = if (rng.nextDouble() > 0.72) white * 0.7 else 0.0
                    val sweepFreq = baseFreq * exp(-t * 5.5)
                    val zapTone = sin(2.0 * PI * sweepFreq * t) * 0.45
                    val thunder = lowPass * rumble * 1.15
                    val mix = ((zapTone + crackle * 0.45 + thunder) * env * 0.65)
                        .coerceIn(-1.0, 1.0)
                    pcm[i] = (mix * Short.MAX_VALUE).toInt().toShort()
                }
                writePcm(sampleRate, pcm)
            }
        }
    }

    private fun playShieldHum(durationMs: Int) {
        scope.launch {
            runCatching {
                val sampleRate = 22050
                val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val pcm = ShortArray(totalSamples)
                for (i in 0 until totalSamples) {
                    val t = i.toDouble() / sampleRate
                    val env = sin(PI * (i.toDouble() / totalSamples))
                    val wave = (sin(2.0 * PI * 420.0 * t) * 0.35 + sin(2.0 * PI * 840.0 * t) * 0.22) * env
                    pcm[i] = (wave * Short.MAX_VALUE).toInt().coerceIn(-32767, 32767).toShort()
                }
                writePcm(sampleRate, pcm)
            }
        }
    }

    private fun writePcm(sampleRate: Int, pcm: ShortArray) {
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(pcm.size * 2)

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(minBuf)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        try {
            track.write(pcm, 0, pcm.size)
            track.play()
            Thread.sleep((pcm.size * 1000L / sampleRate) + 40L)
        } finally {
            runCatching { track.stop() }
            runCatching { track.release() }
        }
    }
}
