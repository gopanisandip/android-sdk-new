package com.mowplayer.demo

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_audio_reader.*

class AudioReaderActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_reader)

        btnSubmit.setOnClickListener {
            if (edtText.text.isNotEmpty()) {
                audioReader.initializeAudioPlayer(intent.getStringExtra("code")!!, edtText.text.toString())
            } else {
                Toast.makeText(this@AudioReaderActivity, "Please enter test to read...", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun finish() {
        super.finish()
        audioReader.releasePlayer()
    }
}