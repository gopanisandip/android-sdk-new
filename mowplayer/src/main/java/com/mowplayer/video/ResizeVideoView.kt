package com.mowplayer.video

import android.content.Context
import android.util.AttributeSet
import android.widget.VideoView

/**
 * Created by Brucetoo
 * On 2015/10/19
 * At 21:53
 */
class ResizeVideoView : VideoView {

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?) : super(context) {}

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
            val margin = (context.resources.displayMetrics.density * MARGIN_DP).toInt()
            var videoRatio = 0f
            videoRatio = if (windowWidth < windowHeight) {
                videoWidth.toFloat() / videoHeight
            } else {
                videoHeight.toFloat() / videoWidth
            }
            if (windowWidth < windowHeight) { // portrait
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
            } else if (windowWidth > windowHeight) { // landscape
                if (videoWidth > videoHeight) { //video is landscape
                    if (windowWidth * videoRatio > videoHeight) {
                        lp.height = windowHeight - margin
                        lp.width = ((windowHeight - margin) / videoRatio).toInt()
                    } else {
                        lp.height = (windowWidth * videoRatio).toInt()
                        lp.width = windowWidth
                    }
                } else if (videoWidth < videoHeight) { //video is portrait
                    lp.width = ((windowHeight - margin) / videoRatio).toInt()
                    lp.height = windowHeight - margin
                } else {
                    lp.height = windowHeight - margin
                    lp.width = lp.height
                }
            }
            layoutParams = lp
            holder.setFixedSize(videoWidth, videoHeight)
            visibility = VISIBLE
        }
    }

    companion object {
        private const val MARGIN_DP = 0 //margin of ResizeSurfaceView
    }
}