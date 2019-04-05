package ru.aslanisl.telegramtest.activity

import android.animation.Animator
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.support.annotation.CallSuper
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import ru.aslanisl.telegramtest.R
import ru.aslanisl.telegramtest.utils.Prefs
import kotlin.math.max

private const val KEY_EXTRA_CIRCULAR_REVEAL = "KEY_EXTRA_CIRCULAR_REVEAL"
private const val KEY_EXTRA_CIRCULAR_REVEAL_X = "KEY_EXTRA_CIRCULAR_REVEAL_X"
private const val KEY_EXTRA_CIRCULAR_REVEAL_Y = "KEY_EXTRA_CIRCULAR_REVEAL_Y"

abstract class RecreateActivity: BaseActivity() {

    private val circularAnimationDuration by lazy { resources.getInteger(R.integer.circular_animation_duration).toLong() }
    private val mainView by lazy { findViewById<View>(android.R.id.content) }

    private var previousWindowBackground: Drawable? = null

    protected var themeChanging = false

    protected val screenWidthPx
        get() = resources.displayMetrics.widthPixels

    protected val screenHeightPx
        get() = resources.displayMetrics.heightPixels

    @CallSuper
    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        startRevealAnimation()
    }

    protected fun changeTheme(centerX: Int, centerY: Int) {
        themeChanging = true
        val oldTheme = Prefs.getThemeId(this)
        Prefs.setThemeStyle(this, if (oldTheme == R.style.LightTheme) R.style.DarkTheme else R.style.LightTheme)
        recreateActivityCircular(centerX, centerY)
    }

    private fun recreateActivityCircular(centerX: Int, centerY: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val intent = Intent(this, this::class.java)
            intent.putExtra(KEY_EXTRA_CIRCULAR_REVEAL, true)
            intent.putExtra(KEY_EXTRA_CIRCULAR_REVEAL_X, centerX)
            intent.putExtra(KEY_EXTRA_CIRCULAR_REVEAL_Y, centerY)

            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, R.anim.fade_out_long)
            Handler().postDelayed({
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }, circularAnimationDuration * 2)
        } else {
            recreate()
        }
    }

    private fun startRevealAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
            intent.getBooleanExtra(KEY_EXTRA_CIRCULAR_REVEAL, false)
        ) {
            recreateActivity()
            intent.putExtra(KEY_EXTRA_CIRCULAR_REVEAL, false)
            val centerX = intent.getIntExtra(KEY_EXTRA_CIRCULAR_REVEAL_X, screenWidthPx)
            val centerY = intent.getIntExtra(KEY_EXTRA_CIRCULAR_REVEAL_Y, screenHeightPx)
            previousWindowBackground = window.decorView.background
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            mainView.visibility = View.INVISIBLE
            val viewTreeObserver = mainView.viewTreeObserver
            if (viewTreeObserver?.isAlive == true) {
                viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        revealActivity(centerX, centerY)
                        mainView.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                    }
                })
            }
        }
    }

    private fun revealActivity(centerX: Int, centerY: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            val finalRadius = (max(screenWidthPx, screenHeightPx) * 1.4).toFloat()

            // create the animator for this view (the start radius is zero)
            val circularReveal = ViewAnimationUtils.createCircularReveal(mainView, centerX, centerY, 0f, finalRadius)
            circularReveal.duration = circularAnimationDuration
            circularReveal.interpolator = AccelerateInterpolator()
            circularReveal.addListener(object : Animator.AnimatorListener{
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationStart(animation: Animator?) {}

                override fun onAnimationEnd(animation: Animator?) {
                    window.setBackgroundDrawable(previousWindowBackground)
                }
            })
            mainView.visibility = View.VISIBLE

            circularReveal.start()
        }
    }

    open fun recreateActivity() {}
}