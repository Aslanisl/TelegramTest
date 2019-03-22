package ru.aslanisl.telegramtest.utils

import android.content.Context
import android.content.SharedPreferences
import ru.aslanisl.telegramtest.R

private const val KEY_THEME_STYLE = "KEY_THEME_STYLE"

object Prefs {

    private var themeId: Int = 0

    fun setThemeStyle(context: Context, themeRes: Int) {
        themeId = themeRes
        getSharedPref(context).edit().putInt(KEY_THEME_STYLE, themeRes).apply()
    }

    fun getThemeId(context: Context): Int {
        if (themeId == 0) {
            themeId = getSharedPref(context).getInt(KEY_THEME_STYLE, R.style.LightTheme)
        }
        return themeId
    }

    private fun getSharedPref(context: Context): SharedPreferences {
        return context.getSharedPreferences("theme", Context.MODE_PRIVATE)
    }
}