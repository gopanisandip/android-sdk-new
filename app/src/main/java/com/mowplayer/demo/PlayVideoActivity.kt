package com.mowplayer.demo

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_play_video.*

class PlayVideoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_video)

        videoPlayer.initializeVideoPlayer(intent.getStringExtra("code")!!, this)
    }

    override fun finish() {
        super.finish()
        videoPlayer.resetPlayer()
    }

    override fun onResume() {
        super.onResume()
        videoPlayer.resume()
    }

    override fun onPause() {
        super.onPause()
        videoPlayer.onActivityPause()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
//        videoPlayer.requestedOrientation = newConfig.orientation
    }
}
