package ru.aslanisl.telegramtest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.*
import android.graphics.Path
import android.support.annotation.AttrRes
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.roundToLong
import kotlin.system.measureNanoTime

private const val Y_TOP_VALUE_MARGIN = 20
private const val AXIS_X_HEIGHT = 250

open class BaseChartView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    init {
        minimumHeight = resources.getDimensionPixelSize(R.dimen.chart_preview_min_height)
    }

    protected var xChart: Chart? = null
    protected val xCoordinatesFactored = mutableListOf<Float>()
    protected var yCharts: List<Chart> = listOf()
    protected val yChartsFactored = mutableListOf<YChart>()
    protected var factorX: Float = 0f
    protected var fromX: Float = 0f
    protected var factorY: Float = 0f

    protected var enableYMaxAdding = true
        set(value) {
            field = value
            updatePaths()
            invalidate()
        }
    protected var enableXAxis = true
        set(value) {
            field = value
            updatePaths()
            invalidate()
        }

    private val paths = mutableListOf<ColorPath>()

    private var startXFactor: Float = -1f
    private var endXFactor: Float = -1f
    protected var startXChartIndex: Int = 0
    protected var endXChartIndex: Int = 0

    private var dirty = false

    protected var maxY = 0L

    protected var axisXHeight = AXIS_X_HEIGHT
        set(value) {
            field = value
            updatePaths()
            invalidate()
        }

    private var linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = resources.getDimensionPixelSize(R.dimen.chart_preview_stroke_width).toFloat()
        style = Paint.Style.STROKE
        strokeCap = Cap.SQUARE
        strokeJoin = Join.ROUND
    }

    fun loadChartData(chartData: ChartData) {
        xChart = chartData.getXChart()
        yCharts = chartData.getYChars()

        updatePaths()
        chartDataChanges()
        invalidate()
    }

    fun updateDrawFactors(startXFactor: Float, endXFactor: Float) {
        this.startXFactor = startXFactor
        this.endXFactor = endXFactor

        if (dirty) return
        dirty = true

        updatePaths()
        chartDataFactorsChanges()
        invalidate()
    }

    protected open fun chartDataChanges() {}
    protected open fun chartDataFactorsChanges() {}

    private fun updatePaths() {
        val time = measureNanoTime {
            paths.clear()
            xCoordinatesFactored.clear()
            yChartsFactored.clear()
            if (chartHeight <= 0 || width == 0) return
            val xChart = xChart ?: return
            var minXChart = Long.MAX_VALUE
            var maxXChart = Long.MIN_VALUE
            xChart.values.forEach {
                if (it < minXChart) minXChart = it
                if (it > maxXChart) maxXChart = it
            }
            var widthX = maxXChart - minXChart

            startXChartIndex = 0
            endXChartIndex = xChart.values.size - 1

            val toX: Float
            if (startXFactor > -1f && endXFactor > -1f) {
                fromX = minXChart + startXFactor * widthX
                toX = minXChart + endXFactor * widthX
                xChart.values.forEachIndexed { index, value ->
                    if (value > fromX && startXChartIndex == 0) {
                        startXChartIndex = if (index > 0) index + 1 else index
                    }
                    if (value > toX && endXChartIndex == xChart.values.size - 1) {
                        endXChartIndex = if (index < xChart.values.size - 1) index - 1 else index
                    }
                }
                widthX = (toX - fromX).roundToLong()
            } else {
                fromX = minXChart.toFloat()
                toX = maxXChart.toFloat()
            }

            factorX = width.toFloat() / widthX

            val minY = 0
            var maxY = Long.MIN_VALUE
            yCharts.forEach { chart ->
                chart.values.forEachIndexed { index, value ->
                    if (index in startXChartIndex..endXChartIndex) {
                        if (value > maxY) maxY = value
                    }
                }
            }
            if (enableYMaxAdding) maxY += Y_TOP_VALUE_MARGIN
            factorY = chartHeight.toFloat() / (maxY - minY)
            this.maxY = maxY

            for (index in startXChartIndex..endXChartIndex) {
                val x = xChart.values[index]
                val factorXValue = (x - fromX) * factorX
                xCoordinatesFactored.add(factorXValue)
            }

            yCharts.forEachIndexed { chartIndex, chart ->
                val path = Path()
                var lastX = -1f
                var lastY = -1f

//                if (fromX > -1 && startXChartIndex > 0 && startXChartIndex < xChart.values.size - 1 && chartIndex == 0) {
//                    // Previous X
//                    val x0 = xChart.values[startXChartIndex - 1]
//                    // Next X
//                    val x1 = xChart.values[startXChartIndex + 1]
//                    // Previous Y
//                    val y0 = chart.values[startXChartIndex - 1]
//                    // Next Y
//                    val y1 = chart.values[startXChartIndex + 1]
//
//                    val deltaY = abs(fromX - x0) * (y1 - y0) / (x1 - x0)
//                    val y = y0 - deltaY
//
//                    Log.d("TAGLOGY", "y0 = $y0 y1 = $y1 y = $y")
//
//                    val factorXValue = 0f
//                    val factorYValue = (y - minY) * factorY
//                    path.moveTo(factorXValue, factorYValue)
//
//                    if (y > maxY) maxY = y.roundToLong()
//
//                    lastX = factorXValue
//                    lastY = factorYValue
//                }
                val yCoordinates = mutableListOf<Float>()

                for (index in startXChartIndex..endXChartIndex) {
                    val x = xChart.values[index]
                    val y = chart.values[index]

                    val factorXValue = (x - fromX) * factorX
                    val factorYValue = (y - minY) * factorY

                    if (lastX == -1f && lastY == -1f) {
                        path.moveTo(factorXValue, if (enableXAxis) factorYValue + axisXHeight else factorYValue)
                    } else {
                        path.lineTo(factorXValue, if (enableXAxis) factorYValue + axisXHeight else factorYValue)
                    }
                    lastX = factorXValue
                    lastY = factorYValue

                    yCoordinates.add(factorYValue)
                }

                val color = if (chart.color == null) Color.BLACK else Color.parseColor(chart.color)
                yChartsFactored.add(YChart(yCoordinates, chart.title, color))
                paths.add(ColorPath(path, color))
            }
        }

        Log.d("TAGLOGCalculate", "calculate time = $time ns")
    }

    // Min Y always == 0
    protected fun revertY(y: Float) = y / factorY
    protected fun revertX(x: Float) = x / factorX + fromX

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updatePaths()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.scale(1f, -1f, width / 2f, height / 2f)
        paths.forEach {
            canvas.drawPath(it.path, linePaint.apply { color = it.color })
        }
        canvas.restore()

        if (enableXAxis) {
            canvas.save()
            canvas.translate(0f, chartHeight.toFloat())
            drawXAxis(canvas)
            canvas.restore()
        }

        dirty = false
    }

    open fun drawXAxis(canvas: Canvas) {}

    protected val chartHeight: Int
        get() = if (enableXAxis) height - axisXHeight else height

    protected data class ColorPath(val path: Path, val color: Int)
    protected data class YChart(val yCoordinates: List<Float>, val title: String, val color: Int)
}