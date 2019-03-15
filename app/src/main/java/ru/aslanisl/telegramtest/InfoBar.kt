package ru.aslanisl.telegramtest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

class InfoBar {

    private val infoBarBounds = RectF()

    private var barHeight = 0f
    private var barWidth = 0f

    private var maxWidth = 0f

    private var contentPadding = 0f
    private var textPadding = 0f
    private var cornersRadius = 16f

    private var dateTextHeight = 0f
    private val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        typeface = Typeface.DEFAULT_BOLD
    }

    private var valueTextHeight = 0f
    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        typeface = Typeface.DEFAULT_BOLD
    }

    private var titleTextHeight = 0f
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GREEN
        typeface = Typeface.DEFAULT_BOLD
    }

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val barStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val infoDataStamp = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    private val dateTextField = TextField("", 0f, 0f, datePaint)

    private val valueTitles = mutableListOf<ValueTitle>()
    private val textFields = mutableListOf<TextField>()

    fun initResources(context: Context) {
        val res = context.resources
        contentPadding = res.getDimensionPixelSize(R.dimen.info_bar_margin).toFloat()
        textPadding = res.getDimensionPixelSize(R.dimen.info_bar_small_margin).toFloat()

        datePaint.textSize = res.getDimensionPixelSize(R.dimen.font_normal).toFloat()
        valuePaint.textSize = res.getDimensionPixelSize(R.dimen.font_big).toFloat()
        titlePaint.textSize = res.getDimensionPixelSize(R.dimen.font_normal).toFloat()

        barStrokePaint.color = ContextCompat.getColor(context, R.color.infoLine)
        barStrokePaint.strokeWidth = res.getDimensionPixelSize(R.dimen.Y_axis_width).toFloat()
    }

    fun setDate(date: Date, updateLayout: Boolean = true) {
        dateTextField.text = infoDataStamp.format(date)
        if (updateLayout) requestLayout()
    }

    fun setValueTitles(values: List<String>, title: List<String>, colors: List<Int>, updateLayout: Boolean = true) {
        valueTitles.clear()
        if (values.size != title.size || values.size != colors.size) return
        values.forEachIndexed { index, value ->
            valueTitles.add(ValueTitle(value, title[index], colors[index]))
        }
        if (updateLayout) requestLayout()
    }

    fun setMaxWidth(maxWidth: Float, updateLayout: Boolean = true) {
        this.maxWidth = maxWidth
        if (updateLayout) requestLayout()
    }

    fun requestLayout() {
        measure()
        layout()
    }

    fun measure() {
        measureHeight()
        measureWidth()
    }

    fun setX(x: Float) {
        val infoBarXStart = x - barWidth / 2
        val infoBarXEnd = x + barWidth / 2
        var barXStart = if (infoBarXStart < 0 + contentPadding) 0f + contentPadding else infoBarXStart
        var barXEnd = if (infoBarXEnd > maxWidth - contentPadding) maxWidth - contentPadding else infoBarXEnd
        if (barXStart <= 0f + contentPadding) barXEnd = barWidth + contentPadding
        if (barXEnd >= maxWidth - contentPadding) barXStart = maxWidth - barWidth - contentPadding

        infoBarBounds.set(barXStart, contentPadding, barXEnd, contentPadding + barHeight)
    }

    fun draw(canvas: Canvas) {
        canvas.drawRoundRect(infoBarBounds, cornersRadius, cornersRadius, barPaint)
        canvas.drawRoundRect(infoBarBounds, cornersRadius, cornersRadius, barStrokePaint)
        canvas.save()
        canvas.translate(infoBarBounds.left + contentPadding, infoBarBounds.top + contentPadding)
        textFields.forEach { field ->
            canvas.drawText(field.text, 0, field.text.length, field.x, field.y, field.paint)
        }

        canvas.restore()
    }

    private fun measureHeight() {
        barHeight = 0f

        // Add padding from top
        barHeight += contentPadding

        val dateHeight = datePaint.getTextHeight()
        barHeight += dateHeight
        dateTextHeight = dateHeight

        barHeight += contentPadding

        val valueHeight = valuePaint.getTextHeight()
        barHeight += valueHeight
        valueTextHeight = valueHeight

        val titleHeight = titlePaint.getTextHeight()
        barHeight += titleHeight
        titleTextHeight = titleHeight

        // Add padding from bottom
        barHeight += contentPadding
    }

    private fun measureWidth() {
        barWidth = 0f

        // Add padding from left
        barWidth += contentPadding

        val dateTextWidth = datePaint.measureText(dateTextField.text)

        var valuesTextWidth = 0f
        var titlesTextWidth = 0f

        valueTitles.forEach {
            valuesTextWidth += valuePaint.measureText(it.value)
            titlesTextWidth += titlePaint.measureText(it.title)
        }
        // Add padding's
        valuesTextWidth += valueTitles.lastIndex * contentPadding
        titlesTextWidth += valueTitles.lastIndex * contentPadding

        barWidth += max(dateTextWidth, max(valuesTextWidth, titlesTextWidth))

        // Add padding from right
        barWidth += contentPadding
    }

    private fun layout() {
        // Left Top corner
        var height = 0f
        dateTextField.x = 0f
        dateTextField.y = 0f + datePaint.textSize

        height += dateTextHeight
        height += contentPadding

        textFields.clear()
        textFields.add(dateTextField)
        var valueTitleWidth = 0f
        valueTitles.forEach { valueTitle ->
            val valueTextField = TextField(
                valueTitle.value,
                0f,
                height + valuePaint.textSize,
                Paint(valuePaint).apply { color = valueTitle.color }
            )
            val titleTextField = TextField(
                valueTitle.title,
                0f,
                height + titleTextHeight + textPadding + titlePaint.textSize,
                Paint(titlePaint).apply { color = valueTitle.color }
            )

            valueTextField.x = valueTitleWidth
            titleTextField.x = valueTitleWidth

            val valueWidth = valuePaint.measureText(valueTitle.value)
            val titleWidth = titlePaint.measureText(valueTitle.title)

            valueTitleWidth += max(valueWidth, titleWidth)
            valueTitleWidth += contentPadding

            textFields.add(valueTextField)
            textFields.add(titleTextField)
        }
    }

    private data class TextField(var text: String, var x: Float, var y: Float, var paint: Paint)
    private data class ValueTitle(var value: String, var title: String, var color: Int)
}