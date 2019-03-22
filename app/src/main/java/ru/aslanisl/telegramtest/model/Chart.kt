package ru.aslanisl.telegramtest.model

private const val ALPHA_STEP = 0.01f

data class Chart(
    val title: String,
    val values: List<Long>,
    val type: String,
    val name: String?,
    val color: String?
) {
    var enable: Boolean = true

    private var tempAlpha = 1f
    var alpha: Float = 1f
        private set
        get() {
            if (enable) {
                tempAlpha += ALPHA_STEP
                if (tempAlpha > 1f) tempAlpha = 1f
                return tempAlpha
            }
            tempAlpha -= ALPHA_STEP
            if (tempAlpha < 0) tempAlpha = 0f
            return tempAlpha
        }

    fun isNeedToRedraw() = (enable && tempAlpha < 1f) || (enable.not() && tempAlpha > 0f)
}