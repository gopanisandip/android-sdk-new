package com.mowplayer.demo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.mowplayer.callbacks.VideoFilesCallback
import com.mowplayer.models.MediaModel
import com.mowplayer.retrofit.MowApiClient
import com.mowplayer.retrofit.MowApiInterface
import com.mowplayer.retrofit.MowApiMethods
import com.mowplayer.utils.MowConstants
import com.mowplayer.utils.MowUtils
import kotlinx.android.synthetic.main.activity_video_code.*
import retrofit2.Response

class VideoCodeActivity : Activity(), VideoFilesCallback<MediaModel> {

    lateinit var mVideoFilesCallback: VideoFilesCallback<MediaModel>
    lateinit var mMowApiInterface: MowApiInterface
    lateinit var mMowApiClient: MowApiClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_code)
        initView()


    }

    private fun initView() {
        mVideoFilesCallback = this;
        mMowApiClient = MowApiClient()

        mMowApiInterface = mMowApiClient.createService(MowApiInterface::class.java)
        System.gc()
    }

    fun onPlayClick(view: View) {

        if (MowUtils.isConnectToInternet(this)) {
            if (et_video_code.text.toString().trim { it <= ' ' } != "") {
//                Answers.getInstance().logCustom(CustomEvent("Mow Player Event")
//                        .putCustomAttribute("VideoCodeActivity", "onPlayClick")
//                        .putCustomAttribute("VideoCodeActivity ", "videoCode-->" + et_video_code.text.toString().trim()))

                getVideoFiles()
            } else {
                Toast.makeText(this, "Please enter video code", Toast.LENGTH_SHORT).show()
            }
        } else {
            MowUtils.showSnackbar(main_container, "Internet not working")
        }
    }

    private fun getVideoFiles() {
        //  MowUtils.showProgress(this,false)

        progress_bar.visibility = View.VISIBLE
        MowApiMethods.getMediaFiles(mVideoFilesCallback, mMowApiInterface, et_video_code.text.toString().trim())
    }

    override fun onVideoFilesReceived(response: Response<MediaModel>) {
        progress_bar.visibility = View.GONE

        // MowUtils.hideProgress()
        if (response.body() != null) {
            if (response.isSuccessful) {

//                Crashlytics.log("VideoCodeActivity onVideoFilesReceived")

                val intent = Intent(this@VideoCodeActivity, MainActivity::class.java)
                intent.putExtra(MowConstants.VIDEO_CODE, et_video_code.text.toString().trim { it <= ' ' })
                startActivity(intent)
            } else {
                Toast.makeText(applicationContext, "Something went wrong, please try again", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(applicationContext, "Something went wrong, please try again", Toast.LENGTH_LONG).show()
        }
    }

    override fun onVideoFilesFailure() {
        progress_bar.visibility = View.GONE

//        Crashlytics.log("VideoCodeActivity onVideoFilesFailure")

        // MowUtils.hideProgress()
        Toast.makeText(applicationContext, "Something went wrong, please try again", Toast.LENGTH_LONG).show()
    }

    fun onAdTesterClick(view: View) {
//        Answers.getInstance().logCustom(CustomEvent("Mow Player Event")
//                .putCustomAttribute("VideoCodeActivity", "onAdTesterClick"))

        startActivity(Intent(this@VideoCodeActivity, AdTesterActivity::class.java))
    }

}
