package com.mowplayer.demo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import com.mowplayer.utils.MowConstants
import kotlinx.android.synthetic.main.activity_ad_tester.*

class AdTesterActivity : Activity() {

    private var adPositionRadioGroup: RadioGroup? = null
    private var radioButton: RadioButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ad_tester)

        adPositionRadioGroup = findViewById(R.id.radioGroup)
    }

    fun onRunClick(view: View) {
        if (et_ad_tag_url.text.toString().trim().equals("")) {
            Toast.makeText(this, "Please enter Ad Tag URL", Toast.LENGTH_SHORT).show()
            return
        }

        var selectedId = adPositionRadioGroup!!.checkedRadioButtonId
        radioButton = findViewById(selectedId)

        val intent = Intent(this@AdTesterActivity, MainActivity::class.java)
        intent.putExtra("Key", "1")
        intent.putExtra(MowConstants.VIDEO_URL, et_video_url.text.toString().trim())
        intent.putExtra(MowConstants.AD_URL, et_ad_tag_url.text.toString().trim())
        if (selectedId != -1) {
            if (radioButton!!.text == resources.getString(R.string.pre)) {
                selectedId = 0
            } else if (radioButton!!.text == resources.getString(R.string.mid)) {
                selectedId = 1
            } else if (radioButton!!.text == resources.getString(R.string.post)) {
                selectedId = 2
            }

            intent.putExtra(MowConstants.AD_POSITION, selectedId)
        }

//        Crashlytics.log("onRunClick videoUrl--> " + et_video_url.text.toString().trim())
//        Crashlytics.log("onRunClick addUrl--> " + et_ad_tag_url.text.toString().trim())
//        Crashlytics.log("onRunClick adPosition--> $selectedId")

        startActivity(intent)
    }

    companion object {

        private val TAG = "AdTesterActivity"
    }
}
