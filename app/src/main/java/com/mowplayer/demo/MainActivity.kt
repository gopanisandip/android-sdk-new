package com.mowplayer.demo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAudioPlayer.setOnClickListener {
            startActivity(Intent(this@MainActivity, AudioActivity::class.java))
        }

        btnViddeoPlayer.setOnClickListener {
            startActivity(Intent(this@MainActivity, VideoActivity::class.java))
        }
    }
}
