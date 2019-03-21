package ru.aslanisl.telegramtest

import android.content.res.ColorStateList
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_main.*
import ru.aslanisl.telegramtest.ChartViewPreview.*

class MainActivity : AppCompatActivity() {

    private lateinit var charts: ChartData
    private val spacingSmall by lazy { resources.getDimensionPixelSize(R.dimen.spacing_small) }
    private val dividerSpacing by lazy { resources.getDimensionPixelSize(R.dimen.spacing_big) }
    private val dividerHeight by lazy { resources.getDimensionPixelSize(R.dimen.divider_height) }

    private lateinit var chartData: List<ChartData>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = getString(R.string.statistics)

        chartData = JsonParser.parseJson(this)

        charts = chartData[4]
        loadChartData(true)
        chartViewPreview.setPreviewAreaChangeListener(object : PreviewAreaChangeListener {
            override fun changeFactors(startXFactor: Float, endXFactor: Float) {
                chart.updateDrawFactors(startXFactor, endXFactor)
            }
        })
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        chartData.forEachIndexed { index, chartData ->
//            menu.add(Menu.NONE, index, Menu.NONE, "Chart ${index + 1}")
//        }
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        charts = chartData[item.itemId]
//        loadChartData(false)
//        return true
//    }

    private fun loadChartData(animate: Boolean) {
        chartViewPreview.loadChartData(charts, animate)
        chart.loadChartData(charts, animate)

        chartChecks.removeAllViews()
        val checkBoxes = charts.getYChars(false).map { getCheckBox(it) }
        checkBoxes.forEachIndexed { index, checkBox ->
            chartChecks.addView(checkBox)
            if (index != checkBoxes.lastIndex) {
                chartChecks.addView(getLineDivider())
            }
        }
    }

    private fun getCheckBox(chart: Chart): CheckBox {
        return CheckBox(this).apply {
            text = chart.name
            val color = if (chart.color.isNullOrEmpty().not()) Color.parseColor(chart.color) else Color.BLUE
            buttonTintList = ColorStateList.valueOf(color)
            isChecked = chart.enable

            setOnCheckedChangeListener { _, isChecked ->
                if (isChecked.not() && charts.getYChars(false).count { it.enable } <= 1) {
                    setChecked(true)
                    return@setOnCheckedChangeListener
                }

                chart.enable = isChecked
                loadChartData(true)
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
