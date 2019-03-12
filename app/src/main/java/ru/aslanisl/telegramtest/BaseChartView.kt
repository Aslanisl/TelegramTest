package ru.aslanisl.telegramtest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.support.annotation.AttrRes
import android.util.AttributeSet
import android.view.View

open class BaseChartView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    init {
        minimumHeight = resources.getDimensionPixelSize(R.dimen.chart_preview_min_height)
    }

    private var xChart: Chart? = null
    private var yCharts: List<Chart> = listOf()

    private val paths = mutableListOf<ColorPath>()

    private var startXFactor: Float = -1f
    private var endXFactor: Float = -1f

    private var dirty = false

    protected var maxY = 0L

    private var linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = resources.getDimensionPixelSize(R.dimen.chart_preview_stroke_width).toFloat()
        color = Color.BLACK
        style = Paint.Style.STROKE
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

//        if (dirty) return
//        dirty = true

        updatePaths()
        chartDataChanges()
        invalidate()
    }

    protected open fun chartDataChanges() {}

    private fun updatePaths() {
        paths.clear()
        if (height == 0 || width == 0) return
        val xChart = xChart ?: return
        var minX = xChart.values.min() ?: return
        var maxX = xChart.values.max() ?: return
        var widthX = maxX - minX

        var startIndex = 0
        var endIndex = xChart.values.size - 1
        if (startXFactor > -1f && endXFactor > -1f) {
            val fromX = minX + startXFactor * widthX
            val toX = minX + endXFactor * widthX

            xChart.values.forEachIndexed { index, value ->
                if (value > fromX && startIndex == 0) {
                    startIndex = index
                    minX = value
                }
                if (value > toX && endIndex == xChart.values.size - 1) {
                    endIndex = index
                    maxX = value
                }
            }
            widthX = maxX - minX
        }

        val factorX = width.toFloat() / widthX

        val minY = 0
        var maxY = Long.MIN_VALUE
        yCharts.forEach { chart ->
            chart.values.forEachIndexed { index, value ->
                if (index in (startIndex)..(endIndex)) {
                    if (value > maxY) maxY = value
                    // MinY always == 0
//                    if (value < minY) minY = value
                }
            }
        }
        val factorY = height.toFloat() / (maxY - minY)
        this.maxY = maxY

        yCharts.forEachIndexed { _, chart ->
            val path = Path()
            var lastX = -1f
            var lastY = -1f

            chart.values.forEachIndexed { index, y ->
                if (index in (startIndex - 1)..(endIndex + 1)) {
                    val x = xChart.values[index]
                    val factorXValue = (x - minX) * factorX
                    val factorYValue = (y - minY) * factorY

                    if (lastX == -1f && lastY == -1f) {
                        path.moveTo(factorXValue, factorYValue)
                    } else {
                        path.quadTo(lastX, lastY, (factorXValue + lastX) / 2, (factorYValue + lastY) / 2)
                    }
                    lastX = factorXValue
                    lastY = factorYValue
                }
            }
            val color = if (chart.color == null) Color.BLACK else Color.parseColor(chart.color)
            paths.add(ColorPath(path, color))
        }
    }

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

        dirty = false
    }

    protected data class ColorPath(val path: Path, val color: Int)
}