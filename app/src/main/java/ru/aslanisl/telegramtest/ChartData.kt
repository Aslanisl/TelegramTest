package ru.aslanisl.telegramtest

data class ChartData(
    val charts: List<Chart>
) {

    fun getXChart() = charts.firstOrNull { it.title == "x" }

    fun getYChars() = charts.filterNot { it.title == "x" }
}