package ru.aslanisl.telegramtest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.annotation.AttrRes
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import java.util.Date
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.system.measureNanoTime

private const val AXIS_Y_LABEL_STEP = 50
private const val AXIS_Y_COUNT = 6
private const val AXIS_Y_STEP = 5

class ChartView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : BaseChartView(context, attrs, defStyleAttr) {

    private var lineStep = 0f
    private var lineCount = 0f
    private val lineWidth = resources.getDimensionPixelSize(R.dimen.Y_axis_width)
    private val textMargin = resources.getDimensionPixelSize(R.dimen.spacing_small).toFloat()
    private var oldMaxY = 0L

    private val infoLineWidthHalf = resources.getDimensionPixelSize(R.dimen.info_line_width) / 2
    private val infoBarMargin = resources.getDimensionPixelSize(R.dimen.info_bar_margin)

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

    private val infoBar = InfoBar()

    init {
        infoBar.initResources(context)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        infoBar.setMaxWidth(w.toFloat(), false)
    }

    override fun chartDataChanges() {
        if (height == 0) return
        if (oldMaxY == maxY) return
        oldMaxY = maxY
        lineStep = height.toFloat() / AXIS_Y_COUNT
        lineCount = maxY.toFloat() / AXIS_Y_COUNT
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
            for (i in 0 until AXIS_Y_COUNT) {
                val y = height - lineStep * i
                canvas.drawRect(0f, y, width.toFloat(), y - lineWidth, linePaint)
            }
            super.onDraw(canvas)
            for (i in 0 until AXIS_Y_COUNT) {
                val y = height - lineStep * i

                val textY = y - lineWidth - textMargin
                val text = (lineCount * i).roundToInt().toString()
                canvas.drawText(text, textMargin, textY, textPaint)
            }

            drawInfo(canvas)
        }
        Log.d("TAGLOGDrawChart", "draw time = $time ns")
    }

    private var previousValueX = 0f

    private fun drawInfo(canvas: Canvas) {
        if (showInfo.not()) return
        val closeValueX = nearestNumberBinarySearch(xCoordinates, touchX)
        val closeValueIndex = xCoordinates.indexOf(closeValueX)
        canvas.drawRect(
            closeValueX - infoLineWidthHalf,
            infoBarMargin.toFloat(),
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
        drawInfoBar(canvas, closeValueX, closeValueIndex)
    }

    private fun drawInfoBar(canvas: Canvas, closeValueX: Float, closeValueIndex: Int) {
        val xValue = revertX(closeValueX).roundToLong()

        if (previousValueX != closeValueX) {
            infoBar.setX(closeValueX)
            infoBar.setDate(Date().apply { time = xValue }, false)

            val yValuesText = mutableListOf<String>()
            val yTitlesText = mutableListOf<String>()
            val yColorsText = mutableListOf<Int>()

            yChartsFactored.forEach {
                yValuesText.add(revertY(it.yCoordinates[closeValueIndex]).roundToInt().toString())
                yTitlesText.add(it.title)
                yColorsText.add(it.color)
            }

            infoBar.setValueTitles(yValuesText, yTitlesText, yColorsText)
        }
        infoBar.draw(canvas)
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