package com.mowplayer.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_single_audio.*

class SingleAudioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_audio)

        audioPlayer.initializeAudioPlayer(intent.getStringExtra("code")!!, intent.getBooleanExtra("adsTriton", false), intent.getBooleanExtra("adsIMA", false))
    }

    override fun finish() {
        super.finish()
        audioPlayer.releasePlayer()
    }
}