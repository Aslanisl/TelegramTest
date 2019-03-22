package ru.aslanisl.telegramtest.activity

import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_select_chart.*
import ru.aslanisl.telegramtest.R
import ru.aslanisl.telegramtest.utils.JsonParser

class SelectChartActivity : AppCompatActivity() {

    private val fontNormalSize by lazy { resources.getDimensionPixelSize(R.dimen.font_big).toFloat() }
    private val spacingNormal by lazy { resources.getDimensionPixelSize(R.dimen.spacing_normal) }
    private val spacingHalf by lazy { resources.getDimensionPixelSize(R.dimen.spacing_normal_half) }
    private val dividerHeight by lazy { resources.getDimensionPixelSize(R.dimen.divider_height) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_chart)

        title = getString(R.string.select_chart)

        val charts = JsonParser.parseJson(this)

        chartSelect.removeAllViews()
        val textViews = charts.mapIndexed { index, _ -> getChartText("Chart #$index", index) }
        textViews.forEach {
            chartSelect.addView(it)
            chartSelect.addView(getLineDivider())
        }
    }

    private fun getChartText(name: String, index: Int): TextView {
        return TextView(this).apply {
            text = name
            setTextSize(TypedValue.COMPLEX_UNIT_PX, fontNormalSize)
            setTextColor(Color.BLACK)

            setOnClickListener {
                MainActivity.selectChart(this@SelectChartActivity, index)
            }

            layoutParams = getLayoutParamsTextView()
        }
    }

    private fun getLayoutParamsTextView(): ViewGroup.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(spacingNormal, spacingHalf, spacingNormal, spacingHalf)
        }
    }

    private fun getLineDivider(): View {
        return View(this).apply {
            setBackgroundColor(ContextCompat.getColor(this@SelectChartActivity, R.color.yAxisLine))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dividerHeight
            ).apply {
                setMargins(spacingNormal, 0, spacingNormal, 0)
            }
        }
    }
}
