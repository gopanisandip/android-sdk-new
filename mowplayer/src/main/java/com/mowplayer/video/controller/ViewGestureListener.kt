package com.mowplayer.video.controller

import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.WindowManager

class ViewGestureListener(private val context: Context, private val listener: VideoGestureListener) : GestureDetector.SimpleOnGestureListener() {

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        listener.onSingleTap()
        return true
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        val deltaX = e1.rawX - e2.rawX
        val deltaY = e1.rawY - e2.rawY

        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            if (Math.abs(deltaX) > SWIPE_THRESHOLD) {
                listener.onHorizontalScroll(deltaX < 0)
            }
        } else {
            if (Math.abs(deltaY) > SWIPE_THRESHOLD) {
                Log.i(TAG, "deltaY->$deltaY")
                if (e1.x < getDeviceWidth(context) * 1.0 / 5) {//left edge
                    listener.onVerticalScroll(deltaY / getDeviceHeight(context), SWIPE_LEFT)
                } else if (e1.x > getDeviceWidth(context) * 4.0 / 5) {//right edge
                    listener.onVerticalScroll(deltaY / getDeviceHeight(context), SWIPE_RIGHT)
                }
            }
        }
        return true
    }

    companion object {

        private val TAG = "ViewGestureListener"

        private val SWIPE_THRESHOLD = 60//threshold of swipe
        val SWIPE_LEFT = 1
        val SWIPE_RIGHT = 2

        fun getDeviceWidth(context: Context): Int {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val mDisplayMetrics = DisplayMetrics()
            wm.defaultDisplay.getMetrics(mDisplayMetrics)
            return mDisplayMetrics.widthPixels
        }

        fun getDeviceHeight(context: Context): Int {
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val mDisplayMetrics = DisplayMetrics()
            wm.defaultDisplay.getMetrics(mDisplayMetrics)
            return mDisplayMetrics.heightPixels
        }
    }

}
