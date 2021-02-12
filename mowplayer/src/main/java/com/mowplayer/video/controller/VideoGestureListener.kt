package com.mowplayer.video.controller

interface VideoGestureListener {
    /**
     * single tap controller view
     */
    fun onSingleTap()

    /**
     * Horizontal scroll to control progress of video
     *
     * @param seekForward seek to forward or not
     */
    fun onHorizontalScroll(seekForward: Boolean)

    /**
     * vertical scroll listen
     *
     * @param percent   swipe percent
     * @param direction left or right edge for control brightness or volume
     */
    fun onVerticalScroll(percent: Float, direction: Int)
}
