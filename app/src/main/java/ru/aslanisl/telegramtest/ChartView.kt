package ru.aslanisl.telegramtest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.annotation.AttrRes
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import kotlin.system.measureNanoTime

private const val AXIS_Y_LABEL_STEP = 50

class ChartView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : BaseChartView(context, attrs, defStyleAttr) {

    private var lineStep = 0f
    private var lineCount = 0
    private var lineWidth = resources.getDimensionPixelSize(R.dimen.Y_axis_width)
    private val textMargin = resources.getDimensionPixelSize(R.dimen.spacing_small).toFloat()
    private var oldMaxY = 0L

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.Y_axis_line)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.Y_axis_label)
        textSize = resources.getDimensionPixelSize(R.dimen.Y_axis_label).toFloat()
    }

    override fun chartDataChanges() {
        if (height == 0) return
        if (oldMaxY == maxY) return
        oldMaxY = maxY
        lineStep = height.toFloat() / maxY
        lineCount = Math.round(maxY.toDouble() / AXIS_Y_LABEL_STEP).toInt()
    }

    override fun onDraw(canvas: Canvas) {
        val time = measureNanoTime {
            for (i in 0 until lineCount) {
                val y = height - lineStep * i * AXIS_Y_LABEL_STEP
                canvas.drawRect(0f, y, width.toFloat(), y - lineWidth, linePaint)

                // Just add margin 5
                val textY = y - lineWidth - textMargin
                canvas.drawText((i * AXIS_Y_LABEL_STEP).toString(), textMargin, textY, textPaint)
            }
            super.onDraw(canvas)
        }
        Log.d("TAGLOGDrawChart", "draw time = $time ns")
    }
}