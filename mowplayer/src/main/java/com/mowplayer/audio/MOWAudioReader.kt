package com.mowplayer.audio

import android.content.Context
import android.media.AudioManager
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.mowplayer.R
import com.mowplayer.callbacks.APICallback
import com.mowplayer.callbacks.ReaderAPICallback
import com.mowplayer.models.audio.MOWConfig
import com.mowplayer.retrofit.MowApiClient
import com.mowplayer.retrofit.MowApiInterface
import com.mowplayer.retrofit.MowApiMethods
import com.mowplayer.utils.MuteStatus
import com.mowplayer.utils.PlayingStatus
import kotlinx.android.synthetic.main.audio_reader.view.*
import okhttp3.ResponseBody
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject

class MOWAudioReader : LinearLayoutCompat, APICallback<ResponseBody>, ReaderAPICallback<ResponseBody> {

    private lateinit var mowConfig: MOWConfig
    internal var context: Context
    private lateinit var apiCallback: APICallback<ResponseBody>
    private lateinit var readerAPICallback: ReaderAPICallback<ResponseBody>
    private lateinit var code: String
    private lateinit var textForReader: String

    var mowMediaPlayer = MOWMediaPlayer()
    var playingStatus: PlayingStatus? = null
    var muteStatus: MuteStatus? = null

    private lateinit var mowApiInterface: MowApiInterface
    lateinit var mowApiClient: MowApiClient
    lateinit var audioManager: AudioManager

    constructor(context: Context) : super(context) {
        this.context = context
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.context = context
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.context = context
        init(context)
    }

    fun init(context: Context) {
        View.inflate(context, R.layout.audio_reader, this)

        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        apiCallback = this
        readerAPICallback = this

        initializeAPI()
    }

    fun initializeAudioPlayer(code: String, textForReader: String) {
        progressVisible()

        this.code = code
        this.textForReader = textForReader
        doAsync {
            MowApiMethods.getAudioConfiguration(apiCallback, mowApiInterface, code, "", "")
            uiThread {
            }
        }

        ivPlayPause.setOnClickListener {
            if (playingStatus == PlayingStatus.PLAYING) {
                ivPlayPause.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_play, null))
                mowMediaPlayer.pause()
                playingStatus = PlayingStatus.PAUSE
            } else if (playingStatus == PlayingStatus.STOP || playingStatus == PlayingStatus.PAUSE || playingStatus == PlayingStatus.FINISH) {
                ivPlayPause.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_pause, null))
                mowMediaPlayer.start()
                playingStatus = PlayingStatus.PLAYING
            }
        }
    }

    private fun initializeAPI() {
        mowApiClient = MowApiClient()
        mowApiInterface = mowApiClient.createService(MowApiInterface::class.java)
        System.gc()
    }

    override fun onAPISuccess(responseBody: ResponseBody) {
        playingStatus = PlayingStatus.STOP

        mowConfig = MOWConfig(responseBody)
        val audio = mowConfig.audio
        val reader = audio!!.reader

        MowApiMethods.textToSpeech(readerAPICallback, mowApiInterface, context.getString(R.string.mow_referer), code, reader!!.id!!, textForReader)

        onConfigurationChanged(resources.configuration)

        // Audio model
        if (audio.theme!! == "light") {
            setLightTheme()
        } else {
            setDarkTheme()
        }

        // Set Volume
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (mowConfig.volume!! * 10).toInt(), 0)
//        seekBarVolume.progress = audioModel.volume.toInt()
        muteStatus = MuteStatus.NOISY
    }

    override fun onAPIFailure(throwable: Throwable) {
        progressHide()
    }

    override fun onReaderAPISuccess(responseBody: ResponseBody) {
        progressHide()

        val response = responseBody.string()
        val responseObject = JSONObject(response.toString())
        var file: String? = null

        if (responseObject.has("file")) {
            file = responseObject.getString("file")
            mowMediaPlayer.setURL(file)
        }

        mowMediaPlayer.getMediaPlayer!!.setOnCompletionListener { mp ->
            mp.stop()
            mp.release()
            playingStatus = PlayingStatus.FINISH
            mowMediaPlayer.setURL(file!!)
            ivPlayPause.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_play, null))
        }
    }

    override fun onReaderAPIFailure(throwable: Throwable) {
        progressHide()
    }

//    override fun onConfigurationChanged(newConfig: Configuration?) {
//        super.onConfigurationChanged(newConfig)
//        if (newConfig!!.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            if (isAdsShowing) {
//                showAdsView()
//            } else {
//                showAudioView()
//                if (isPlayList) {
//                    ivNext.visibility = View.VISIBLE
//                    ivPrevious.visibility = View.VISIBLE
//                }
//            }
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            if (isAdsShowing) {
//                hideAudioView()
//                hideAdsView()
//            } else {
//                hideAudioView()
//                hideAdsView()
//
//                ivPlayPause.visibility = View.VISIBLE
//                ivSetting.visibility = View.VISIBLE
//            }
//        }
//    }

    private fun setLightTheme() {
        ivBackground.setImageResource(R.drawable.light_circle)

        ivPlayPause.setColorFilter(ContextCompat.getColor(context, R.color.text_dark))
    }

    private fun setDarkTheme() {
        ivBackground.setImageResource(R.drawable.dark_circle)

        ivPlayPause.setColorFilter(ContextCompat.getColor(context, R.color.text_white))
    }

    fun releasePlayer() {
        mowMediaPlayer.release()
    }

    /**
     * Hide all controls and component and show progress
     */
    private fun progressVisible() {
        ivPlayPause.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }

    /**
     * Show all controls and component and hide progress
     */
    private fun progressHide() {
        ivPlayPause.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        releasePlayer()
    }
}
