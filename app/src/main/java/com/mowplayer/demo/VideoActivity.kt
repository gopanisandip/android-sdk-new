package com.mowplayer.demo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_audio.btnPlay
import kotlinx.android.synthetic.main.activity_audio.edtCode
import kotlinx.android.synthetic.main.activity_video.*

class VideoActivity : AppCompatActivity() {

    private lateinit var rb: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        edtCode.setText("v-m9kvsimdwhl")

        intent = Intent(this@VideoActivity, PlayVideoActivity::class.java)
        intent!!.putExtra("code", edtCode.text.toString())

        rgVideoPlayer.setOnCheckedChangeListener { group, checkedId ->
            rb = group.findViewById<View>(checkedId) as RadioButton

            if (checkedId > -1) {
                when (rb.text) {
                    "Test" -> {
                        edtCode.setText("v-m9kvsimdwhl")
                    }
                    "The Smoking Tire" -> {
                        edtCode.setText("v-mgm2bgjuifl")
                    }
                    "Big Buck Bunn" -> {
                        edtCode.setText("v-mgsdlj6fnzu")
                    }
                    "Cardiac arrhythmia heart rhythm" -> {
                        edtCode.setText("v-mm75j4b0jta")
                    }
                }
            }
        }

        btnPlay.setOnClickListener {
            intent!!.putExtra("code", edtCode.text.toString())
            startActivity(intent)
        }
    }
}