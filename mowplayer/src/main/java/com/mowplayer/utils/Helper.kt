package com.mowplayer.utils

import android.content.Context

internal object Helper {

    private var scale: Float = 0.toFloat()

    fun dpToPixel(dp: Float, context: Context): Int {
        if (scale == 0f) {
            scale = context.resources.displayMetrics.density
        }
        return (dp * scale).toInt()
    }
}
