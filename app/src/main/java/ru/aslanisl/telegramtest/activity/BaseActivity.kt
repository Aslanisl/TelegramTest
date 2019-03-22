package ru.aslanisl.telegramtest.activity

import android.app.Activity
import android.os.Bundle
import ru.aslanisl.telegramtest.utils.Prefs

abstract class BaseActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Prefs.getThemeId(this))
        super.onCreate(savedInstanceState)
    }
}