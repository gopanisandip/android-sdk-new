package com.mowplayer.demo

import android.content.res.Configuration
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_player_video.*


class VideoPlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player_video)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels

        val height = (width * 9) / 16

        val params = videoView.layoutParams
        params.height = height
        videoView.layoutParams = params

//        videoView.setVideoPath("https://mowplayer.nyc3.digitaloceanspaces.com/b5xciuir0vvsyg2w.mp4")
        videoView.setVideoPath("https://mowplayer.nyc3.digitaloceanspaces.com/2yvay1bqyy3dmuaj.mp4")

        videoView.setOnPreparedListener {
            videoView.start()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels - 2000
        val height = (width * 9) / 16

        val params = videoView.layoutParams
        params.height = height
        videoView.layoutParams = params
    }

}
