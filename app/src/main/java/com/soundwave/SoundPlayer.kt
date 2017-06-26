package com.soundwave

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class SoundPlayer {
    private var service = Executors.newSingleThreadExecutor()
    private var SAMPLE_RATE: Int = 44100

    var synthFrequency: Int = 440
    var waveFunction: ((Float) -> Float) = WaveForms.sin

    private val play: AtomicBoolean = AtomicBoolean(false)

    val isPlaying: Boolean
        get() = play.get()

    public fun start() {
        service = Executors.newSingleThreadExecutor()
        service.execute { playSound() }
    }

    public fun pause() {
        play.set(false);
    }

    public fun resume() {
        play.set(true);
    }

    public fun stop() {
        service.shutdown()
        service.awaitTermination(200, TimeUnit.MILLISECONDS)
    }

    private fun playSound() {
        val minSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT)
        val audioTrack = AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, minSize,
                AudioTrack.MODE_STREAM)
        audioTrack.play()
        val buffer = ShortArray(minSize)
        var x = 0f
        while (!Thread.interrupted()) {
            if (play.get()) {
                for (i in buffer.indices) {
                    //                    Log.d(TAG, "playSound: angle = " + (angle));
                    //                    Log.d(TAG, "playSound: sin(angle) = " + Math.sin(angle));
                    buffer[i] = (java.lang.Short.MAX_VALUE * waveFunction(x)).toShort()
                    x += synthFrequency.toFloat() / SAMPLE_RATE
                }
                audioTrack.write(buffer, 0, buffer.size)
            }
        }
        audioTrack.release()
    }

}

object WaveForms {
    val sin: ((Float) -> Float) = { x -> Math.sin(2 * Math.PI * x).toFloat() }
    val saw: ((Float) -> Float) = { x -> (x % 1 - 1f) * 2 }
    val square: ((Float) -> Float) = { x -> (if (x % 1 < 0.5) 0 else 1).toFloat() }
}
