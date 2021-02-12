package com.mowplayer.video.controller

import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.SensorManager
import android.view.OrientationEventListener

import com.mowplayer.BuildConfig

class OrientationDetector(private val context: Context) {
    private var orientationEventListener: OrientationEventListener? = null

    private var rotationThreshold = 20
    private var holdingTime: Long = 0
    private var lastCalcTime: Long = 0
    private var lastDirection: Direction? = Direction.PORTRAIT

    private var currentOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT//初始为竖屏

    private var listener: OrientationChangeListener? = null


    fun setOrientationChangeListener(listener: OrientationChangeListener) {
        this.listener = listener
    }

    fun enable() {
        if (orientationEventListener == null) {
            orientationEventListener = object : OrientationEventListener(context, SensorManager.SENSOR_DELAY_UI) {
                override fun onOrientationChanged(orientation: Int) {
                    val currDirection = calcDirection(orientation) ?: return

                    if (currDirection != lastDirection) {
                        resetTime()
                        lastDirection = currDirection
                        if (BuildConfig.DEBUG) {
                        }
                    } else {
                        calcHoldingTime()
                        if (holdingTime > HOLDING_THRESHOLD) {
                            if (currDirection == Direction.LANDSCAPE) {
                                if (currentOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                                    //MLog.d(TAG, "switch to SCREEN_ORIENTATION_LANDSCAPE");
                                    currentOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                                    if (listener != null) {
                                        listener!!.onOrientationChanged(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, currDirection)
                                    }
                                }

                            } else if (currDirection == Direction.PORTRAIT) {
                                if (currentOrientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                                    //MLog.d(TAG, "switch to SCREEN_ORIENTATION_PORTRAIT");
                                    currentOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                    if (listener != null) {
                                        listener!!.onOrientationChanged(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, currDirection)
                                    }
                                }

                            } else if (currDirection == Direction.REVERSE_PORTRAIT) {
                                if (currentOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                                    // MLog.d(TAG, "switch to SCREEN_ORIENTATION_REVERSE_PORTRAIT");
                                    currentOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                                    if (listener != null) {
                                        listener!!.onOrientationChanged(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT, currDirection)
                                    }
                                }

                            } else if (currDirection == Direction.REVERSE_LANDSCAPE) {
                                if (currentOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                                    // MLog.d(TAG, "switch to SCREEN_ORIENTATION_REVERSE_LANDSCAPE");
                                    currentOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                                    if (listener != null) {
                                        listener!!.onOrientationChanged(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE, currDirection)
                                    }
                                }

                            }

                        }
                    }

                }
            }
        }

        orientationEventListener!!.enable()
    }

    private fun calcHoldingTime() {
        val current = System.currentTimeMillis()
        if (lastCalcTime == 0L) {
            lastCalcTime = current
        }
        holdingTime += current - lastCalcTime
        //      MLog.d(TAG, "calcHoldingTime holdingTime=" + holdingTime);
        lastCalcTime = current
    }

    private fun resetTime() {
        lastCalcTime = 0
        holdingTime = lastCalcTime
    }

    private fun calcDirection(orientation: Int): Direction? {
        if (orientation <= rotationThreshold || orientation >= 360 - rotationThreshold) {
            return Direction.PORTRAIT
        } else if (Math.abs(orientation - 180) <= rotationThreshold) {
            return Direction.REVERSE_PORTRAIT
        } else if (Math.abs(orientation - 90) <= rotationThreshold) {
            return Direction.REVERSE_LANDSCAPE
        } else if (Math.abs(orientation - 270) <= rotationThreshold) {
            return Direction.LANDSCAPE
        }
        return null
    }


    fun setInitialDirection(direction: Direction) {
        lastDirection = direction
    }

    fun disable() {
        if (orientationEventListener != null) {
            orientationEventListener!!.disable()
        }
    }

    fun setThresholdDegree(degree: Int) {
        rotationThreshold = degree
    }

    interface OrientationChangeListener {
        /***
         * @param screenOrientation ActivityInfo.SCREEN_ORIENTATION_PORTRAIT or ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
         * or ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE or ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
         * @param direction         PORTRAIT or REVERSE_PORTRAIT when screenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
         * LANDSCAPE or REVERSE_LANDSCAPE when screenOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE.
         */
        fun onOrientationChanged(screenOrientation: Int, direction: Direction?)
    }


    enum class Direction {
        PORTRAIT, REVERSE_PORTRAIT, LANDSCAPE, REVERSE_LANDSCAPE
    }

    companion object {


        private val TAG = "OrientationDetector"
        private val HOLDING_THRESHOLD = 1500
    }

}
