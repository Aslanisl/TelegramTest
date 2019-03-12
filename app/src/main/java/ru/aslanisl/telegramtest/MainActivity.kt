package ru.aslanisl.telegramtest

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import ru.aslanisl.telegramtest.ChartViewPreview.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val chartData = JsonParser.parseJson(this)

        chartViewPreview.loadChartData(chartData[4])
        chart.loadChartData(chartData[4])
        chartViewPreview.setPreviewAreaChangeListener(object : PreviewAreaChangeListener{
            override fun changeFactors(startXFactor: Float, endXFactor: Float) {
                chart.updateDrawFactors(startXFactor, endXFactor)
            }
        })
    }
}
