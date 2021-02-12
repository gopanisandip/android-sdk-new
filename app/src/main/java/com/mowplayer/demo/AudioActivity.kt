package com.mowplayer.demo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_audio.*

class AudioActivity : AppCompatActivity() {

    private lateinit var rb: RadioButton
    var isAdsTritonOn: Boolean = false
    var isAdsIMAOn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio)

        edtCode.setText("als-mzcmqxlh6a1")
        switchPlayList.isEnabled = false

        intent = Intent(this@AudioActivity, LiveAudioActivity::class.java)
        intent!!.putExtra("code", edtCode.text.toString())
        intent!!.putExtra("adsTriton", isAdsTritonOn)
        intent!!.putExtra("adsIMA", isAdsIMAOn)

        rgAudioPlayer.setOnCheckedChangeListener { group, checkedId ->
            rb = group.findViewById<View>(checkedId) as RadioButton

            if (checkedId > -1) {
                when {
                    rb.text == "Live Big Audio Player" -> {
                        switchPlayList.isEnabled = false
                        edtCode.setText("als-mzcmqxlh6a1")
                        intent = Intent(this@AudioActivity, LiveAudioActivity::class.java)
                    }
                    rb.text == "Live Slim Audio Player" -> {
                        switchPlayList.isEnabled = false
                        edtCode.setText("als-mzcmqxlh6a1")
                        intent = Intent(this@AudioActivity, LiveSlimAudioActivity::class.java)
                    }
                    rb.text == "Single Audio Player" -> {
                        switchPlayList.isEnabled = true
                        edtCode.setText("a-mxceiupavpd")
                        intent = Intent(this@AudioActivity, SingleAudioActivity::class.java)
                    }
                    rb.text == "Audio Play List" -> {
                        switchPlayList.isEnabled = true
                        edtCode.setText("apl-mpphwk22m78")
                        intent = Intent(this@AudioActivity, SingleAudioActivity::class.java)
                    }
                    rb.text == "Audio Reader" -> {
                        switchPlayList.isEnabled = false
                        edtCode.setText("ar-mynxafnvmos")
                        intent = Intent(this@AudioActivity, AudioReaderActivity::class.java)
                    }
                }
            }
        }

        switchPlayList.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                edtCode.setText("apl-mpphwk22m78")
            } else {
                edtCode.setText("a-mxceiupavpd")
            }
        }

        switchAdsTriton.setOnCheckedChangeListener { buttonView, isChecked ->
            isAdsTritonOn = isChecked
        }

        switchAdsIMA.setOnCheckedChangeListener { buttonView, isChecked ->
            isAdsIMAOn = isChecked
        }

        btnPlay.setOnClickListener {
            intent!!.putExtra("code", edtCode.text.toString())
            intent!!.putExtra("adsTriton", isAdsTritonOn)
            intent!!.putExtra("adsIMA", isAdsIMAOn)
            startActivity(intent)
        }
    }
}