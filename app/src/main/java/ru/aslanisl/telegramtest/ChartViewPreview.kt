package ru.aslanisl.telegramtest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.support.annotation.AttrRes
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import ru.aslanisl.telegramtest.ChartViewPreview.ResizeCorner.*
import kotlin.system.measureNanoTime

private const val AREA_WIDTH_DEFAULT = 300f
private const val AREA_RESIZE_WIDTH = 50f
private const val AREA_WIDTH_MIN = 200f

class ChartViewPreview
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0
) : BaseChartView(context, attrs, defStyleAttr) {

    private var listener: PreviewAreaChangeListener? = null
    private val selectAreaRectF = RectF()
    private val selectAreaPaintStroke = Paint().apply {
        color = ContextCompat.getColor(context, R.color.greyLight)
        strokeWidth = 8f
        style = Paint.Style.STROKE
    }
    private val selectAreaPaintBackground = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    init {
        enableYMaxAdding = false
        enableXAxis = false
        enableAnimation = false
    }

    fun setPreviewAreaChangeListener(listener: PreviewAreaChangeListener) {
        this.listener = listener
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateSelectAreaRectF()
    }

    private fun updateSelectAreaRectF() {
        selectAreaRectF.set(0f, 0f, AREA_WIDTH_DEFAULT, height.toFloat())
        calculatePreviewAreaFactors()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val eventMasked = event.actionMasked
        when (eventMasked) {
            MotionEvent.ACTION_MOVE -> {
                val id = event.getPointerId(0)
                val index = event.findPointerIndex(id)
                val x = event.getX(index)
                setSelectAreaPosition(x)
            }
        }
        Log.d("TAGLOGTouch", "time = ${System.currentTimeMillis()}")
        return true
    }

    private fun setSelectAreaPosition(x: Float) {
        val width = selectAreaRectF.width()
        val resizeCorner = getResizeCorner(x)

        when (resizeCorner) {
            LEFT -> {
                if (x < 0) return
                if (selectAreaRectF.right - x < AREA_WIDTH_MIN) return
                selectAreaRectF.left = x
            }
            RIGHT -> {
                if (x > this.width) return
                if (x - selectAreaRectF.left < AREA_WIDTH_MIN) return
                selectAreaRectF.right = x
            }
            else -> {
                val widthCenter = width / 2
                if (x - widthCenter < 0) return
                if (x + widthCenter > this.width) return
                selectAreaRectF.left = x - widthCenter
                selectAreaRectF.right = x + widthCenter
            }
        }
        calculatePreviewAreaFactors()
        invalidate()
    }

    private fun getResizeCorner(x: Float): ResizeCorner? {
        val currentLeft = selectAreaRectF.left
        val isLeft = x in (currentLeft - AREA_RESIZE_WIDTH..currentLeft + AREA_RESIZE_WIDTH)
        val currentRight = selectAreaRectF.right
        val isRight = x in (currentRight - AREA_RESIZE_WIDTH..currentRight + AREA_RESIZE_WIDTH)
        return if (isLeft) ResizeCorner.LEFT else if (isRight) ResizeCorner.RIGHT else null
    }

    private enum class ResizeCorner {
        LEFT,
        RIGHT
    }

    override fun onDraw(canvas: Canvas) {
        val time= measureNanoTime {
            canvas.drawRect(selectAreaRectF, selectAreaPaintBackground)
            super.onDraw(canvas)
            canvas.drawRect(selectAreaRectF, selectAreaPaintStroke)
        }

        Log.d("TAGLOGDrawPreview", "draw time = $time ns")
    }

    private fun calculatePreviewAreaFactors() {
        val startXFactor = selectAreaRectF.left / width
        val endXFactor = selectAreaRectF.right / width
        listener?.changeFactors(
            if (startXFactor < 0) 0f else startXFactor,
            if (endXFactor > 1) 1f else endXFactor
        )
    }

    interface PreviewAreaChangeListener {
        // Return factors in 0..1 (absolute about width)
        fun changeFactors(startXFactor: Float, endXFactor: Float)
    }
}