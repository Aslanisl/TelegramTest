package ru.aslanisl.telegramtest

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception

object JsonParser {

    fun parseJson(context: Context): List<ChartData>  {
        val chartData = context
            .resources
            .openRawResource(R.raw.chart_data)
            .bufferedReader()
            .use { it.readText() }
        return parseJsonString(chartData)
    }

    private fun parseJsonString(jsonString: String): List<ChartData> {
        val jsonArray = JSONArray(jsonString)
        val chartsData = mutableListOf<ChartData>()

        jsonArray.forEachObject { item, _ ->
            val charts = mutableListOf<Chart>()

            val columns = item["columns"] as JSONArray
            val types = item["types"] as JSONObject
            val names = item["names"] as JSONObject
            val colors = item["colors"] as JSONObject

            columns.forEachArray { column, _ ->
                var title = ""
                val values = mutableListOf<Long>()
                var type = ""
                var name: String? = null
                var color: String? = null

                column.forEachAny { columnValue, index ->
                    if (index == 0) {
                        title = columnValue as String
                        type = types.getOrNull(title) as String? ?: ""
                        name = names.getOrNull(title) as String?
                        color = colors.getOrNull(title) as String?
                    } else {
                        if (columnValue is Long) {
                            values.add(columnValue)
                        } else if (columnValue is Int){
                            values.add(columnValue.toLong())
                        }
                    }
                }
                val chart = Chart(title,values, type, name, color)
                charts.add(chart)
            }

            chartsData.add(ChartData(charts))
        }
        return chartsData
    }


    fun JSONArray.forEachObject(action: (JSONObject, index :Int) -> Unit) {
        for (i in 0..(length() - 1)) {
            val item = getJSONObject(i)
            action.invoke(item, i)
        }
    }

    fun JSONArray.forEachArray(action: (JSONArray, index :Int) -> Unit) {
        for (i in 0..(length() - 1)) {
            val item = getJSONArray(i)
            action.invoke(item, i)
        }
    }

    fun JSONArray.forEachAny(action: (Any, index :Int) -> Unit) {
        for (i in 0..(length() - 1)) {
            val item = get(i)
            action.invoke(item, i)
        }
    }

    fun JSONObject.getOrNull(name: String): Any? {
        return try {
            get(name)
        } catch (e: Exception){
            e.printStackTrace()
            null
        }
    }
}