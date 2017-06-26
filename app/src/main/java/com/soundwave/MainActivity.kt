package com.soundwave

import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import com.shawnlin.numberpicker.NumberPicker


class MainActivity : AppCompatActivity() {
    lateinit var canvasView: CanvasView
    lateinit var button: Button
    lateinit var button2: Button
    lateinit var spinner: Spinner
    lateinit var number: NumberPicker

    val player = SoundPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        canvasView = findViewById(R.id.canvas) as CanvasView
        button = findViewById(R.id.button) as Button
        button2 = findViewById(R.id.button2) as Button
        spinner = findViewById(R.id.spinner2) as Spinner
        number = findViewById(R.id.numberPicker) as NumberPicker
        number.orientation = NumberPicker.HORIZONTAL
        number.minValue = 110
        number.maxValue = 990
        number.value = 440
        number.setOnValueChangedListener { picker, oldVal, newVal ->
            player.synthFrequency = newVal
        }

        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayOf("Sine", "Sawtooth", "Square", "Custom"))
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position == 3)
                    canvasView.visibility = View.VISIBLE
                else
                    canvasView.visibility = View.GONE
                selectWaveFunction(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        button.setOnClickListener {
            if (player.isPlaying) player.pause()
            else {
                selectWaveFunction(spinner.selectedItemPosition)
                player.resume()
            }
            button.text = if (player.isPlaying) "Stop" else "Play"
        }
        button.text = if (player.isPlaying) "Stop" else "Play"
        button2.setOnClickListener {
            player.stop()
            player.start()
        }

        player.synthFrequency = number.value
        player.start()
    }

    override fun onPause() {
        super.onPause()
        player.stop()
    }

    fun funFromFun(f: ((Float) -> Float)): ((Float) -> Float) {
        val sampleRate = 2000
        val y = Array<Float>(sampleRate, { _ -> 0f })
        val step = 1 / sampleRate.toFloat();
        var i = 0;
        while (i < sampleRate - 1) {
            y[i] = f(i * step)
            i++
        }
        return { x -> y[((x % 1) * y.size).toInt()] }
    }

    fun funFromBitmap(bitmap: Bitmap): ((Float) -> Float) {
        val sampleRate = bitmap.width
        val y = Array<Float>(sampleRate, { _ -> 0f })
        val step = 1;
        var i = 0;
        while (i < sampleRate - 1) {
            var firstNonWhite = 0f;
            for (j in bitmap.height - 1 downTo 0) {
                if (bitmap.getPixel(i * step, j) != 0) {
                    firstNonWhite = (-(j / (bitmap.height).toFloat()) + 0.5f) * 2
                    break
                }
            }
            y[i] = firstNonWhite
            i++
        }
        return { x -> y[((x % 1) * y.size).toInt()] }
    }

    fun selectWaveFunction(id: Int) {
        when (id) {
            0 -> player.waveFunction = funFromFun(WaveForms.sin)
            1 -> player.waveFunction = funFromFun(WaveForms.saw)
            2 -> player.waveFunction = funFromFun(WaveForms.square)
            3 -> player.waveFunction = funFromBitmap(canvasView.bitmap!!)
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.name
    }
}
