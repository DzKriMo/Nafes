package com.gauravk.audiovisualizer.visualizer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class AudioVisualizerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var waveform: ByteArray? = null
    private val paint = Paint()

    init {
        paint.color = 0xFF000000.toInt() // Default color (black)
        paint.strokeWidth = 4f // Adjust the stroke width as needed
    }

    fun updateVisualizer(waveform: ByteArray) {
        this.waveform = waveform
        invalidate() // Trigger redraw of the view
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        waveform?.let { waveformData ->
            // Example drawing code for waveform visualization
            val centerY = height / 2f
            val startX = 0f
            val endX = width.toFloat()
            val scaleY = 128f // Adjust the scale factor as needed
            for (i in 0 until waveformData.size step 2) {
                val x = startX + i / 2
                val y = centerY + waveformData[i].toFloat() * scaleY
                canvas.drawLine(x, centerY, x, y, paint)
            }
        }
    }
}
