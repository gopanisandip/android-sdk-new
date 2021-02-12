package com.mowplayer.utils

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup

class ResizeSurfaceView : SurfaceView {
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context) : super(context) {}

    /**
     * adjust SurfaceView area according to video width and height
     * @param surfaceViewWidth original
     * @param surfaceViewHeight
     * @param videoWidth
     * @param videoHeight
     */
    fun adjustSize(surfaceViewWidth: Int, surfaceViewHeight: Int, videoWidth: Int, videoHeight: Int) {
        if (videoWidth > 0 && videoHeight > 0) {
            val lp = layoutParams
            val displayMetrics = context.resources.displayMetrics
            val windowWidth = displayMetrics.widthPixels
            val windowHeight = displayMetrics.heightPixels
            var videoRatio = 0f
            if (windowWidth < windowHeight) {
                videoRatio = videoWidth.toFloat() / videoHeight
            } else {
                videoRatio = videoHeight.toFloat() / videoWidth
            }
            if (windowWidth < windowHeight) {// portrait
                if (videoWidth > videoHeight) {
                    if (surfaceViewWidth / videoRatio > surfaceViewHeight) {
                        lp.height = surfaceViewHeight
                        lp.width = (surfaceViewHeight * videoRatio).toInt()
                    } else {
                        lp.height = (surfaceViewWidth / videoRatio).toInt()
                        lp.width = surfaceViewWidth
                    }
                } else if (videoWidth <= videoHeight) {
                    if (surfaceViewHeight * videoRatio > surfaceViewWidth) {
                        lp.height = (surfaceViewWidth / videoRatio).toInt()
                        lp.width = surfaceViewWidth
                    } else {
                        lp.height = surfaceViewHeight
                        lp.width = (surfaceViewHeight * videoRatio).toInt()
                    }
                }
            } else if (windowWidth > windowHeight) {// landscape
                if (videoWidth > videoHeight) {//video is landscape
                    if (windowWidth * videoRatio > videoHeight) {
                        lp.height = windowHeight
                        lp.width = (windowHeight / videoRatio).toInt()
                    } else {
                        lp.height = (windowWidth * videoRatio).toInt()
                        lp.width = windowWidth
                    }
                } else if (videoWidth < videoHeight) {//video is portrait
                    lp.width = (windowHeight / videoRatio).toInt()
                    lp.height = windowHeight
                } else {
                    lp.height = windowHeight
                    lp.width = lp.height
                }
            }
            layoutParams = lp
            holder.setFixedSize(videoWidth, videoHeight)
            visibility = View.VISIBLE
        }
    }
}