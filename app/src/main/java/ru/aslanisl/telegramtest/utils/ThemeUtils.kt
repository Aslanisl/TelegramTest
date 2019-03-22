package ru.aslanisl.telegramtest.utils

import android.content.Context
import android.content.SharedPreferences
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import ru.aslanisl.telegramtest.R

const val THEME_LIGHT = 0
const val THEME_DARK = 1

private const val THEME_KEY = "THEME_KEY"

object ThemeUtils {

    private var themeInt = -1

    fun setTheme(themeInt: Int) {
        this.themeInt = themeInt
    }

    fun changeTheme() {
        if (themeInt == THEME_LIGHT) {
            themeInt = THEME_DARK
            return
        }
        themeInt = THEME_LIGHT
    }

    fun getStatusBarColor(context: Context): Int {
        return getColorCompat(context, if (isLightTheme(context)) R.color.statusBarLight else R.color.statusBarDark)
    }

    fun getToolbarColor(context: Context): Int {
        return getColorCompat(context, if (isLightTheme(context)) R.color.toolbarLight else R.color.toolbarDark)
    }

    fun getWindowColor(context: Context): Int {
        return getColorCompat(context, if (isLightTheme(context)) R.color.windowBackgroundLight else R.color.windowBackgroundDark)
    }

    fun getBackgroundColor(context: Context): Int {
        return getColorCompat(context, if (isLightTheme(context)) R.color.backgroundLight else R.color.backgroundDark)
    }

    fun getChartPreviewBackgroundColor(context: Context): Int {
        return getColorCompat(context, if (isLightTheme(context)) R.color.chartPreviewBackgroundColorLight else R.color.chartPreviewBackgroundColorDark)
    }

    private fun getColorCompat(context: Context, @ColorRes colorRes: Int): Int {
        return ContextCompat.getColor(context, colorRes)
    }

    private fun isLightTheme(context: Context): Boolean {
        if (themeInt < 0) themeInt = getSharedPref(context).getInt(THEME_KEY, THEME_LIGHT)
        return themeInt == THEME_LIGHT
    }

    private fun getSharedPref(context: Context): SharedPreferences {
        return context.getSharedPreferences("theme", Context.MODE_PRIVATE)
    }
}