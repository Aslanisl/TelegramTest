package ru.aslanisl.telegramtest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.support.v4.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.floor
import kotlin.math.round

class XAxis {

    private val xAxisDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    private var xAxisMargin: Float = 0f
    private var xAxisMarginHalf: Float = 0f
    private var fontAscent = 0f

    private val xDate = Date()
    private val texts = mutableListOf<String>()

    private val xAxisTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
    }

    private var maxWidth = 0
    private var maxLabelWidth = 0f
    private var maxLabelCount: Int = 0

    private var lastStartXIndex: Int = 0
    private var lastEndXIndex: Int = 0

    private var remRatio: Int = 1

    fun initResources(context: Context) {
        val res = context.resources
        xAxisMargin = res.getDimensionPixelSize(R.dimen.X_axis_margin).toFloat()
        xAxisMarginHalf = xAxisMargin / 2

        xAxisTextPaint.color = ContextCompat.getColor(context, R.color.YAxisLabel)
        xAxisTextPaint.textSize = res.getDimensionPixelSize(R.dimen.Y_axis_label).toFloat()

        fontAscent = xAxisTextPaint.ascent()
    }

    fun setMaxWidth(maxWidth: Int) {
        this.maxWidth = maxWidth
        findMaxLabelCount()
    }

    fun setXLabels(chart: Chart?) {
        texts.clear()
        chart ?: return
        texts.addAll(chart.values
            .map {
                xAxisDateFormat.format(xDate.apply { time = it })
            }
        )

        findMaxLabelWidth()
        findMaxLabelCount()
    }

    fun findMaxHeight() = xAxisMarginHalf * 2 + xAxisTextPaint.getTextHeight()

    fun drawFromToIndex(canvas: Canvas, startXIndex: Int, endXIndex: Int, coordinates: List<Float>) {
        if (startXIndex < 0 || endXIndex > texts.size) return
        if (lastStartXIndex != startXIndex || lastEndXIndex != endXIndex) {
            findRemRatio(startXIndex, endXIndex)
        }
        // TODO check index of bound
        for (i in startXIndex..endXIndex) {
            if (i.rem(remRatio) == 0) {
                val text = texts[i]
                val x = coordinates[i - startXIndex]
                canvas.drawText(text, x, xAxisMarginHalf - fontAscent, xAxisTextPaint)
            }
        }
    }

    private fun findMaxLabelWidth() {
        texts.forEach {
            val textWidth = xAxisTextPaint.measureText(it)
            if (textWidth > maxLabelWidth) maxLabelWidth = textWidth
        }
    }

    // (maxLabelWidth * maxLabelCount) + (maxLabelCount - 1) * xAxisMargin = maxWidth
    private fun findMaxLabelCount() {
        maxLabelCount = floor((maxWidth - xAxisMargin) / (maxLabelWidth + xAxisMargin)).toInt()
    }

    private fun findRemRatio(startXIndex: Int, endXIndex: Int) {
        lastStartXIndex = startXIndex
        lastEndXIndex = endXIndex

        // Add 1 cuz indexes includes
        val indexCount = lastEndXIndex - lastStartXIndex + 1
        remRatio = round(indexCount.toFloat() / maxLabelCount).toInt()
        if (remRatio <= 0) remRatio = 1
    }
}