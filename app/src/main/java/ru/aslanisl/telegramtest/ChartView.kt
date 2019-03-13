package ru.aslanisl.telegramtest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.annotation.AttrRes
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import kotlin.math.abs
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
    private val lineWidth = resources.getDimensionPixelSize(R.dimen.Y_axis_width)
    private val textMargin = resources.getDimensionPixelSize(R.dimen.spacing_small).toFloat()
    private var oldMaxY = 0L

    private val infoLineWidthHalf = resources.getDimensionPixelSize(R.dimen.info_line_width) / 2

    private var showInfo = false
    private var touchX: Float = 0f

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.yAxisLine)
    }

    private val infoLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.infoLine)
    }

    private val infoPointColor = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.YAxisLabel)
        textSize = resources.getDimensionPixelSize(R.dimen.Y_axis_label).toFloat()
    }

    override fun chartDataChanges() {
        if (height == 0) return
        if (oldMaxY == maxY) return
        oldMaxY = maxY
        lineStep = height.toFloat() / maxY
        lineCount = Math.round(maxY.toDouble() / AXIS_Y_LABEL_STEP).toInt()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val id = event.getPointerId(0)
        val index = event.findPointerIndex(id)
        touchX = event.getX(index)
        val eventMasked = event.actionMasked

        showInfo = eventMasked == MotionEvent.ACTION_DOWN
            || eventMasked == MotionEvent.ACTION_POINTER_DOWN
            || eventMasked == MotionEvent.ACTION_MOVE

        Log.d("TAGLOGChartTouch", "time = ${System.currentTimeMillis()}")
        invalidate()
        return true
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
            drawInfo(canvas)
        }
        Log.d("TAGLOGDrawChart", "draw time = $time ns")
    }

    private fun drawInfo(canvas: Canvas) {
        if (showInfo.not()) return
        val closeValueX = nearestNumberBinarySearch(xCoordinates, touchX)
        val closeValueIndex = xCoordinates.indexOf(closeValueX)
        canvas.drawRect(
            closeValueX - infoLineWidthHalf,
            0f,
            closeValueX + infoLineWidthHalf,
            height.toFloat(),
            infoLinePaint
        )
        yChartsFactored.forEach {
            canvas.drawCircle(
                closeValueX,
                height - it.yCoordinates[closeValueIndex],
                15f,
                infoPointColor.apply { color = it.color }
            )
        }
    }

    private fun nearestNumberBinarySearch(
        numbers: List<Float>,
        myNumber: Float,
        start: Int = 0,
        end: Int = numbers.size - 1
    ): Float {
        val mid = (start + end) / 2
        if (numbers[mid] == myNumber)
            return numbers[mid]
        if (start == end - 1)
            return if (Math.abs(numbers[end] - myNumber) >= Math.abs(numbers[start] - myNumber))
                numbers[start]
            else
                numbers[end]
        return if (numbers[mid] > myNumber)
            nearestNumberBinarySearch(numbers, myNumber, start, mid)
        else
            nearestNumberBinarySearch(numbers, myNumber, mid, end)
    }
}