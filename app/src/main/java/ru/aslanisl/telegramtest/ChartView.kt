package ru.aslanisl.telegramtest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.annotation.AttrRes
import android.util.AttributeSet

private const val AXIS_Y_LABEL_STEP = 50

class ChartView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : BaseChartView(context, attrs, defStyleAttr) {

    private var lineStep = 0f
    private var lineCount = 0
    private var lineWidth = 5f

    private val linePaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.GRAY
    }

    private val textPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.GRAY
        textSize = 56f
    }

//    override fun chartDataChanges() {
//        if (height == 0) return
//        lineStep = height.toFloat() / maxY
//        lineCount = Math.round(maxY.toDouble() / AXIS_Y_LABEL_STEP).toInt()
//    }
//
//    override fun onDraw(canvas: Canvas) {
//        for (i in 0 until lineCount) {
//            val y = height - lineStep * i * AXIS_Y_LABEL_STEP
//            canvas.drawRect(0f, y, width.toFloat(), y - lineWidth, linePaint)
//
//            // Just add margin 5
//            val textY = y - lineWidth - 50f
//            canvas.drawText((i * AXIS_Y_LABEL_STEP).toString(), 10f, textY, textPaint)
//        }
//        super.onDraw(canvas)
//    }
}