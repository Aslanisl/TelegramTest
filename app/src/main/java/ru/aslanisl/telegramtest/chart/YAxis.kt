package ru.aslanisl.telegramtest.chart

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import ru.aslanisl.telegramtest.R
import kotlin.math.roundToInt

private const val AXIS_Y_COUNT = 6

class YAxis {

    private var chartHeight: Float = 0f
    private var chartWidth: Float = 0f

    private var lineStep = 0f
    private var lineCount = 0f

    private var lineWidth = 0f
    private var textMargin = 0f

    private var maxY: Float = 0f
    private var minY: Float = 0f
    private var spacingNormal = 0f

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    fun initResources(context: Context) {
        val res = context.resources
        spacingNormal = res.getDimensionPixelSize(R.dimen.spacing_normal).toFloat()
        lineWidth = res.getDimensionPixelSize(R.dimen.Y_axis_width).toFloat()
        textMargin = res.getDimensionPixelSize(R.dimen.spacing_small).toFloat()

        linePaint.color = ContextCompat.getColor(context, R.color.yAxisLine)
        textPaint.color = ContextCompat.getColor(context, R.color.YAxisLabel)
        textPaint.textSize = res.getDimensionPixelSize(R.dimen.Y_axis_label).toFloat()
    }

    fun setChartWidthHeight(chartWidth: Int, chartHeight: Int) {
        this.chartHeight = chartHeight.toFloat()
        this.chartWidth = chartWidth.toFloat()
        updateYAxis()
    }

    fun setMaxMinY(maxY: Long, minY: Long) {
        this.maxY = maxY.toFloat()
        this.minY = minY.toFloat()
        updateYAxis()
    }

    private fun updateYAxis() {
        if (chartHeight <= 0) return
        lineStep = chartHeight / AXIS_Y_COUNT
        lineCount = (maxY - minY) / AXIS_Y_COUNT
    }

    fun drawLines(canvas: Canvas) {
        for (i in 0 until AXIS_Y_COUNT) {
            val y = chartHeight - lineStep * i
            canvas.drawRect(spacingNormal, y, chartWidth - spacingNormal, y - lineWidth, linePaint)
        }
    }

    fun drawLabels(canvas: Canvas) {
        for (i in 0 until AXIS_Y_COUNT) {
            val y = chartHeight - lineStep * i

            val textY = y - lineWidth - textMargin
            val text = (lineCount * i).roundToInt().toString()
            canvas.drawText(text, textMargin + spacingNormal, textY, textPaint)
        }
    }
}