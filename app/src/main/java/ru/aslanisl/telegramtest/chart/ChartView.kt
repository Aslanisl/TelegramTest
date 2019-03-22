package ru.aslanisl.telegramtest.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.annotation.AttrRes
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import ru.aslanisl.telegramtest.R
import ru.aslanisl.telegramtest.utils.nearestNumberBinarySearch
import java.util.Date
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

    private val infoLineWidthHalf = resources.getDimensionPixelSize(R.dimen.info_line_width) / 2
    private val infoBarMargin = resources.getDimensionPixelSize(R.dimen.info_bar_margin)

    private var showInfo = false
    private var touchX: Float = 0f
    private var previousValueX = 0f

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
    private val xAxis = XAxis()

    init {
        infoBar.initResources(context)
        xAxis.initResources(context)

        axisXHeight = xAxis.findMaxHeight().roundToInt()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        infoBar.setMaxWidth(w.toFloat(), false)
        xAxis.setMaxWidth(w)
    }

    override fun chartDataChanges() {
        xAxis.setXLabels(xChart)
        updateYAxis()
    }

    override fun chartDataFactorsChanges() {
        updateYAxis()
    }

    private fun updateYAxis() {
        if (chartHeight <= 0) return
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

    override fun drawXAxis(canvas: Canvas) {
        xAxis.drawFromToIndex(canvas, startXChartIndex, endXChartIndex, xCoordinatesFactored)
    }

    private fun drawInfo(canvas: Canvas) {
        if (showInfo.not()) return
        val closeValueX = xCoordinatesFactored.nearestNumberBinarySearch(touchX)
        val closeValueIndex = xCoordinatesFactored.indexOf(closeValueX)
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