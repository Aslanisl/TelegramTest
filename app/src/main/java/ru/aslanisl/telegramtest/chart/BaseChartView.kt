package ru.aslanisl.telegramtest.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.*
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.support.annotation.AttrRes
import android.support.v4.graphics.ColorUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import ru.aslanisl.telegramtest.model.ChartData
import ru.aslanisl.telegramtest.R.*
import ru.aslanisl.telegramtest.model.Chart
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

private const val AXIS_X_HEIGHT = 250

private const val ANIMATION_STEP_FACTOR = 8

open class BaseChartView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    init {
        minimumHeight = resources.getDimensionPixelSize(dimen.chart_preview_min_height)
    }

    protected var xChart: Chart? = null
    protected val xCoordinatesFactored = mutableListOf<Float>()
    protected var yCharts: List<Chart> = listOf()
    protected val yChartsFactored = mutableListOf<YChart>()
    protected var factorX: Float = 0f
    protected var fromX: Float = 0f
    protected var factorY: Float = 0f

    protected var enableYMaxAdding = true
    protected var enableXAxis = true

    protected val chartHeight: Int
        get() = if (enableXAxis) height - axisXHeight else height

    private val paths = mutableListOf<ColorPath>()

    private var startXFactor: Float = -1f
    private var endXFactor: Float = -1f
    private var oldStartXFactor: Float = 0.1f
    private var oldEndXFactor: Float = 0.1f
    protected var startXChartIndex: Int = 0
    protected var endXChartIndex: Int = 0

    private var minXChart = Long.MAX_VALUE
    private var maxXChart = Long.MIN_VALUE

    protected var maxY = 0L
    protected var yTopValueMargin = 20
    protected var minY = 0L

    private var updateChartThread: UpdateChartThread? = null

    protected var axisXHeight = AXIS_X_HEIGHT

    private var linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = resources.getDimensionPixelSize(dimen.chart_preview_stroke_width).toFloat()
        style = Paint.Style.STROKE
        strokeCap = Cap.SQUARE
        strokeJoin = Join.ROUND
    }

    fun loadChartData(chartData: ChartData, animate: Boolean = true) {
        xChart = chartData.getXChart()
        yCharts = chartData.getYChars()

        calculateYMarginTop()

        updateMinMaxXChart()
        updateChartIndexesFactorX(true)

        setUpdateChartStep()
        update(animate)
        chartDataChanges()
    }

    fun updateDrawFactors(startXFactor: Float, endXFactor: Float) {
        this.startXFactor = startXFactor
        this.endXFactor = endXFactor
    }

    private fun updateMinMaxXChart() {
        val xChart = xChart ?: return
        minXChart = Long.MAX_VALUE
        maxXChart = Long.MIN_VALUE
        xChart.values.forEach {
            if (it < minXChart) minXChart = it
            if (it > maxXChart) maxXChart = it
        }
    }

    protected open fun chartDataChanges() {}
    protected open fun chartDataFactorsChanges() {}

    private fun updatePaths() {
        paths.clear()
        xCoordinatesFactored.clear()
        yChartsFactored.clear()
        if (chartHeight <= 0 || width == 0) return
        val xChart = xChart ?: return

        for (index in startXChartIndex..endXChartIndex) {
            val x = xChart.values[index]
            val factorXValue = (x - fromX) * factorX
            xCoordinatesFactored.add(factorXValue)
        }

        for (chart in yCharts) {
            val alphaDraw = chart.alpha
            if (alphaDraw <= 0f) continue

            val path = Path()
            var lastX = -1f
            var lastY = -1f
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

            var color = if (chart.color == null) Color.BLACK else Color.parseColor(chart.color)
            if (alphaDraw < 1) {
                val alphaRange = (255 * alphaDraw).roundToInt()
                color = ColorUtils.setAlphaComponent(color, alphaRange)
            }
            yChartsFactored.add(YChart(yCoordinates, chart.title, color))
            paths.add(ColorPath(path, color))
        }
    }

    private fun updateChartIndexesFactorX(force: Boolean = false) {
        val xChart = xChart ?: return
        var widthX = maxXChart - minXChart
        if (startXFactor == oldStartXFactor && endXFactor == oldEndXFactor && force.not()) return

        startXChartIndex = 0
        endXChartIndex = xChart.values.size - 1

        if (startXFactor > -1f && endXFactor > -1f) {
            fromX = minXChart + startXFactor * widthX
            val toX = minXChart + endXFactor * widthX
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
        }

        factorX = width.toFloat() / widthX
        oldStartXFactor = startXFactor
        oldEndXFactor = endXFactor
    }

    private fun calculateYMarginTop() {
        val maxY = getMaxYFromChart(true)
        // Add 5% for top margin
        yTopValueMargin = (maxY * 0.05).roundToInt()
    }

    private fun getMaxYFromChart(allChart: Boolean = false, checkEnable: Boolean = true): Long {
        var maxY = Long.MIN_VALUE
        yCharts.forEach { chart ->
            if (chart.enable && checkEnable) {
                chart.values.forEachIndexed { index, value ->
                    if (index in startXChartIndex..endXChartIndex || allChart) {
                        if (value > maxY) maxY = value
                    }
                }
            }
        }
        if (enableYMaxAdding) maxY += yTopValueMargin
        return maxY
    }

    private fun getMinYFromChart(allChart: Boolean = false, checkEnable: Boolean = true): Long {
        var minY = Long.MAX_VALUE
        yCharts.forEach { chart ->
            if (chart.enable && checkEnable) {
                chart.values.forEachIndexed { index, value ->
                    if (index in startXChartIndex..endXChartIndex || allChart) {
                        if (value < minY) minY = value
                    }
                }
            }
        }
        return minY
    }

    private fun updateMaxYFactorY(toMaxY: Long) {
        this.maxY = toMaxY
        if (enableYMaxAdding) maxY += yTopValueMargin
        factorY = chartHeight.toFloat() / (maxY - minY)
    }

    private fun initUpdate() {
        updateChartThread = UpdateChartThread()
        updateChartThread?.callback = { newMax ->
            updateMaxY()
            if (isNeedToUpdate(newMax)) updateForYMax(newMax)
        }
        setUpdateChartStep()
    }

    private fun isNeedToUpdate(newMax: Long) : Boolean {
        val newMaxY = if (enableYMaxAdding) newMax + yTopValueMargin else newMax
        val isNeedToRedraw = yCharts.any { it.isNeedToRedraw() }
        val notChange = newMaxY == maxY
            && startXFactor == oldStartXFactor
            && endXFactor == oldEndXFactor
            && yCharts.any { isNeedToRedraw.not() }
        return notChange.not()
    }

    fun update(animation: Boolean = true) {
        if (animation) {
            // Update Thread making work
        } else {
            updateForYMax(getMaxYFromChart())
        }
    }

    private fun updateForYMax(yMax: Long) {
        updateMaxYFactorY(yMax)
        updateChartIndexesFactorX()
        updatePaths()
        chartDataFactorsChanges()
        invalidate()
    }

    private fun setUpdateChartStep() {
        if (chartHeight <= 0) return
        val maxY = getMaxYFromChart(allChart = true, checkEnable = false)
        val minY = getMinYFromChart(allChart = true, checkEnable = false)
        val dif = abs(maxY - minY)
        val step = (dif.toFloat() / ANIMATION_STEP_FACTOR)
        updateChartThread?.setStep(step.roundToLong())
    }

    private fun updateMaxY() {
        updateChartThread?.setToMaxY(getMaxYFromChart())
    }

    // Min Y always == 0
    protected fun revertY(y: Float) = y / factorY

    protected fun revertX(x: Float) = x / factorX + fromX

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateChartIndexesFactorX(force = true)
        setUpdateChartStep()
        update(false)
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

        val time = System.currentTimeMillis()
        Log.d("TAGLOGDrawTime", "Time: (${time - lastTimeDraw})")
        lastTimeDraw = time
    }

    private var lastTimeDraw = System.currentTimeMillis()

    open fun drawXAxis(canvas: Canvas) {}

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initUpdate()
    }

    override fun onDetachedFromWindow() {
        updateChartThread?.exit()
        super.onDetachedFromWindow()
    }

    private class UpdateChartThread : Thread("ChartThread") {
        private val mainThreadHandler by lazy { Handler(Looper.getMainLooper()) }
        private var toMaxY: Long = 0L
        private var currentMaxY: Long = 0L
        private var step: Long = 0
        private var stepInternal: Long = 0

        var callback: ((Long) -> Unit)? = null

        private var isRunning = true

        private val delay = 8L

        init {
            isRunning = true
            start()
        }

        fun reset() {
            currentMaxY = 0L
        }

        fun setStep(step: Long) {
            this.step = step
        }

        fun setToMaxY(toMaxY: Long) {
            this.toMaxY = toMaxY
            if (toMaxY < currentMaxY) stepInternal = -step
            if (toMaxY >= currentMaxY) stepInternal = step
        }

        fun exit() {
            isRunning = false
        }

        override fun run() {
            while (isRunning) {
                try {
                    currentMaxY += stepInternal
                    if (currentMaxY > toMaxY && stepInternal > 0) currentMaxY = toMaxY
                    if (currentMaxY < toMaxY && stepInternal <= 0) currentMaxY = toMaxY
                    mainThreadHandler.post {
                        callback?.invoke(currentMaxY)
                    }
                    Thread.sleep(delay)
                    Log.d("TAGLOGThread", "Working")
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    protected data class ColorPath(val path: Path, val color: Int)
    protected data class YChart(val yCoordinates: List<Float>, val title: String, val color: Int)
}