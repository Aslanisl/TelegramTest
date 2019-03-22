package ru.aslanisl.telegramtest.model

data class ChartData(val charts: List<Chart>) {

    fun getXChart() = charts.firstOrNull { it.title == "x" }

    fun getYChars() = charts.filter { it.title != "x" }
}