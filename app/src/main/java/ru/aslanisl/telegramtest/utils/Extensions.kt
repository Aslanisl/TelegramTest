package ru.aslanisl.telegramtest.utils

import android.graphics.Paint

fun  List<Float>.nearestNumberBinarySearch(myNumber: Float, start: Int = 0, end: Int = size - 1): Float {
    if (isEmpty()) return 0f
    val mid = (start + end) / 2
    if (this[mid] == myNumber)
        return this[mid]
    if (start == end - 1)
        return if (Math.abs(this[end] - myNumber) >= Math.abs(this[start] - myNumber))
            this[start]
        else
            this[end]
    return if (this[mid] > myNumber)
        nearestNumberBinarySearch(myNumber, start, mid)
    else
        nearestNumberBinarySearch(myNumber, mid, end)
}

fun Paint.getTextHeight(): Float {
    val fm = fontMetrics
    return fm.descent - fm.ascent
}