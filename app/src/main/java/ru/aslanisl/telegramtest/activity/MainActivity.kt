package ru.aslanisl.telegramtest.activity

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_main.*
import ru.aslanisl.telegramtest.R
import ru.aslanisl.telegramtest.chart.ChartView
import ru.aslanisl.telegramtest.chart.ChartViewPreview
import ru.aslanisl.telegramtest.chart.ChartViewPreview.*
import ru.aslanisl.telegramtest.model.Chart
import ru.aslanisl.telegramtest.model.ChartData
import ru.aslanisl.telegramtest.utils.JsonParser

class MainActivity : RecreateActivity() {

    private val spacingSmall by lazy { resources.getDimensionPixelSize(R.dimen.spacing_small) }
    private val dividerSpacing by lazy { resources.getDimensionPixelSize(R.dimen.spacing_big) }
    private val dividerHeight by lazy { resources.getDimensionPixelSize(R.dimen.divider_height) }

    private lateinit var chartData: List<ChartData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = getString(R.string.statistics)

        chartData = JsonParser.parseJson(this)
        initCharts()
    }

    private fun initCharts() {
        mainViewContent.removeAllViews()
        chartData.forEach { data ->
            val view = LayoutInflater.from(this).inflate(R.layout.chart_layout, null, false)
            view.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

            val chartView = view.findViewById<ChartView>(R.id.chartView)
            val chartViewPreview = view.findViewById<ChartViewPreview>(R.id.chartViewPreview)

            chartView.loadChartData(data, false)
            chartViewPreview.loadChartData(data, false)

            chartViewPreview.setPreviewAreaChangeListener(object : PreviewAreaChangeListener {
                override fun changeFactors(startXFactor: Float, endXFactor: Float) {
                    chartView.updateDrawFactors(startXFactor, endXFactor)
                }
            })

            // Init checkboxes
            val chartChecks = view.findViewById<ViewGroup>(R.id.chartChecks)
            val checkBoxes = data.getYChars().map { getCheckBox(it, data) }
            checkBoxes.forEachIndexed { index, checkBox ->
                chartChecks.addView(checkBox)
                if (index != checkBoxes.lastIndex) {
                    chartChecks.addView(getLineDivider())
                }
            }

            mainViewContent.addView(view)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(ru.aslanisl.telegramtest.R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (themeChanging) return false
        changeTheme(screenWidthPx / 2, screenHeightPx / 2)
        return true
    }

    private fun getCheckBox(chart: Chart, chartData: ChartData): CheckBox {
        return CheckBox(this).apply {
            text = chart.name
            val color = if (chart.color.isNullOrEmpty().not()) Color.parseColor(chart.color) else Color.BLUE
            buttonTintList = ColorStateList.valueOf(color)
            isChecked = chart.enable
            setTextColor(Color.BLACK)

            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked.not() && chartData.getYChars().count { it.enable } <= 1) {
                    setChecked(true)
                    return@setOnCheckedChangeListener
                }

                chart.enable = isChecked
            }

            layoutParams = getLayoutParamsCheckBox()
        }
    }

    private fun getLayoutParamsCheckBox(): ViewGroup.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, spacingSmall, 0, spacingSmall)
        }
    }

    private fun getLineDivider(): View {
        return View(this).apply {
            setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.yAxisLine))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dividerHeight
            ).apply {
                setMargins(dividerSpacing, 0, dividerSpacing, 0)
            }
        }
    }
}
