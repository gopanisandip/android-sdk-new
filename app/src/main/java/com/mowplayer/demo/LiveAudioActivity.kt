package com.mowplayer.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_live_audio.*

class LiveAudioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_audio)

        liveAudioPlayer.initializeLiveAudioPlayer(intent.getStringExtra("code")!!, intent.getBooleanExtra("adsTriton", false), intent.getBooleanExtra("adsIMA", false))
    }

    override fun finish() {
        super.finish()
        liveAudioPlayer.releasePlayer()
    }
}
