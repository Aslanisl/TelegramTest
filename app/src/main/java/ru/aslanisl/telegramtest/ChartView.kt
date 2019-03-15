package ru.aslanisl.telegramtest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.annotation.AttrRes
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.system.measureNanoTime

private const val AXIS_Y_COUNT = 6
private const val VALUE_CIRCLE_RADIUS = 16f

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
    private val infoPointStrokeWidth = resources.getDimensionPixelSize(R.dimen.info_point_stoke_width).toFloat()
    private var oldMaxY = 0L

    private val infoLineWidthHalf = resources.getDimensionPixelSize(R.dimen.info_line_width) / 2
    private val infoBarMargin = resources.getDimensionPixelSize(R.dimen.info_bar_margin)

    private var showInfo = false
    private var touchX: Float = 0f
    private var previousValueX = 0f

    private val xAxisDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    private val xAxisMargin = resources.getDimensionPixelSize(R.dimen.X_axis_margin)

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.yAxisLine)
    }

    private val infoLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.infoLine)
    }

    private val infoPointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val infoPointStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = infoPointStrokeWidth
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.YAxisLabel)
        textSize = resources.getDimensionPixelSize(R.dimen.Y_axis_label).toFloat()
    }

    private val xAxisTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.YAxisLabel)
        textSize = resources.getDimensionPixelSize(R.dimen.Y_axis_label).toFloat()
        textAlign = Paint.Align.CENTER
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
        if (chartHeight <= 0) return
        if (oldMaxY == maxY) return
        oldMaxY = maxY
        lineStep = chartHeight.toFloat() / AXIS_Y_COUNT
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
            // Draw Axis Y
            for (i in 0 until AXIS_Y_COUNT) {
                val y = chartHeight - lineStep * i
                canvas.drawRect(0f, y, width.toFloat(), y - lineWidth, linePaint)
            }
            super.onDraw(canvas)
            for (i in 0 until AXIS_Y_COUNT) {
                val y = chartHeight - lineStep * i

                val textY = y - lineWidth - textMargin
                val text = (lineCount * i).roundToInt().toString()
                canvas.drawText(text, textMargin, textY, textPaint)
            }
            // Draw Info bar if need it
            drawInfo(canvas)
        }
        Log.d("TAGLOGDrawChart", "draw time = $time ns")
    }


    private val xDate = Date()
    private val texts = mutableListOf<String>()
    override fun drawXAxis(canvas: Canvas) {
        val xChart = xChart ?: return
        texts.clear()
        for (i in startXChartIndex..endXChartIndex) {
            val value = xChart.values[i]
            val text = xAxisDateFormat.format(xDate.apply { time = value })
            texts.add(text)
        }
        val ratioXAxis = calculateRatioXAxis(texts)
        xCoordinates.forEachIndexed { index, x ->
            if ((index + startXChartIndex).rem(ratioXAxis) == 0) {
                val text = texts[index]
                canvas.drawText(text, x, 50f, xAxisTextPaint)
            }
        }
    }

    private fun calculateRatioXAxis(items: List<String>): Int {
        val maxWidth = width
        var width = 0f
        var index = 0
        for (i in 0..items.lastIndex) {
            val text = items[i]
            width += xAxisTextPaint.measureText(text)
            width += xAxisMargin

            if (width >= maxWidth) {
                index = i + 1
                break
            }
        }
        val ratio = if (index == 0) return items.size else Math.round(items.size.toFloat() / index)
        Log.d("TAGLOGRatio", "Ratio $ratio")
        return checkWidth(items, ratio)
    }

    private fun checkWidth(items: List<String>, ration: Int): Int {
        if (ration == 0) return 1
        val maxWidth = width
        var width = 0f
        for (i in 0..items.lastIndex) {
            if ((i + startXChartIndex).rem(ration) == 0) {
                val text = texts[i]

                width += xAxisTextPaint.measureText(text)
                width += xAxisMargin

                if (width >= maxWidth) {
                    return checkWidth(items, ration + 1)
                }
            }
        }
        return ration
    }

    private fun drawInfo(canvas: Canvas) {
        if (showInfo.not()) return
        val closeValueX = xCoordinates.nearestNumberBinarySearch(touchX)
        val closeValueIndex = xCoordinates.indexOf(closeValueX)
        canvas.drawRect(
            closeValueX - infoLineWidthHalf,
            infoBarMargin.toFloat(),
            closeValueX + infoLineWidthHalf,
            chartHeight.toFloat(),
            infoLinePaint
        )
        yChartsFactored.forEach {
            canvas.drawCircle(
                closeValueX,
                chartHeight - it.yCoordinates[closeValueIndex],
                VALUE_CIRCLE_RADIUS,
                infoPointPaint
            )
            canvas.drawCircle(
                closeValueX,
                chartHeight - it.yCoordinates[closeValueIndex],
                VALUE_CIRCLE_RADIUS,
                infoPointStrokePaint.apply { color = it.color }
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
}