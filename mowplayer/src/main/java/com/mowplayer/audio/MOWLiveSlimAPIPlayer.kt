package com.mowplayer.audio

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.ads.interactivemedia.v3.api.*
import com.mowplayer.R
import com.mowplayer.callbacks.APICallback
import com.mowplayer.models.audio.MOWAds
import com.mowplayer.models.audio.MOWConfig
import com.mowplayer.models.audio.Media
import com.mowplayer.retrofit.MowApiClient
import com.mowplayer.retrofit.MowApiInterface
import com.mowplayer.retrofit.MowApiMethods
import com.mowplayer.utils.MowUtils
import com.mowplayer.utils.MuteStatus
import com.mowplayer.utils.PlayingStatus
import com.mowplayer.utils.TrackerManager
import kotlinx.android.synthetic.main.layout_ads_seek_bar.view.*
import kotlinx.android.synthetic.main.layout_audio_head.view.*
import kotlinx.android.synthetic.main.layout_live_seek_bar.view.*
import kotlinx.android.synthetic.main.mow_live_slim_audio_player.view.*
import okhttp3.ResponseBody
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*

class MOWLiveSlimAPIPlayer : LinearLayoutCompat, APICallback<ResponseBody> {

    private lateinit var mowConfig: MOWConfig
    private var playPause: Boolean = false
    private var initialStage = true
    internal var context: Context
    lateinit var apiCallback: APICallback<ResponseBody>

    private var isAdsShowing: Boolean = true
    private var firstTime = true
    private lateinit var code: String

    var mowMediaPlayer = MOWMediaPlayer()
    private var currentMOWAds: MOWAds? = null
    lateinit var currentMedia: Media
    var playingStatus: PlayingStatus? = null
    var muteStatus: MuteStatus? = null

    // IMA ads
    private var mSdkFactory: ImaSdkFactory? = null
    private var mAdsLoader: AdsLoader? = null
    private var mAdsManager: AdsManager? = null
    private var mIsAdDisplayed: Boolean = false
    private lateinit var adDisplayContainer: AdDisplayContainer
    private lateinit var adEventListener: AdEvent.AdEventListener
    private lateinit var adErrorEventListener: AdErrorEvent.AdErrorListener

    private val TAG = "MowAudioActivity"

    lateinit var mowApiInterface: MowApiInterface
    lateinit var mowApiClient: MowApiClient
    lateinit var audioManager: AudioManager

    var updateHandler = Handler()
    lateinit var adMediaPlayer: MediaPlayer

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

    init {

    }

    fun init(context: Context) {
        View.inflate(context, R.layout.mow_live_slim_audio_player, this)

        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        apiCallback = this

        initializeAPI()
        initIMAAds()
    }

    private fun initIMAAds() {
        // IMA Ads
        adEventListener = AdEvent.AdEventListener { adEvent ->
            when (adEvent!!.type) {
                AdEvent.AdEventType.LOADED -> {
                    if (mAdsManager != null) {
                        mAdsManager!!.start()
                        showIMAAds()
                    }
                }

                AdEvent.AdEventType.CONTENT_PAUSE_REQUESTED -> {
                    mIsAdDisplayed = true
                }

                AdEvent.AdEventType.CONTENT_RESUME_REQUESTED -> {
                    mIsAdDisplayed = false
                }

                AdEvent.AdEventType.ALL_ADS_COMPLETED -> {
//                mAdSeekBar!!.visibility = View.GONE
                    if (mAdsManager != null) {
                        mAdsManager!!.destroy()
                        mAdsManager = null
                    }
                    mowMediaPlayer.start()
                    hideAds()
                }

                AdEvent.AdEventType.AD_PROGRESS -> {
                    seekBarAds!!.max = Math.round(mAdsManager!!.adProgress.duration)

                    tvAdsDuration.text = MowUtils.milliSecondsToTimer((mAdsManager!!.adProgress.duration * 1000).toLong())
                    tvAdsCurrentTime.text = MowUtils.milliSecondsToTimer((mAdsManager!!.adProgress.currentTime * 1000).toLong())

                    seekBarAds!!.visibility = View.VISIBLE
                    seekBarAds!!.progress = Math.round(mAdsManager!!.adProgress.currentTime)
                    seekBarAds!!.secondaryProgress = Math.round(mAdsManager!!.adProgress.currentTime)
                }

                else -> {

                }
            }
        }

        adErrorEventListener = AdErrorEvent.AdErrorListener {

            if (mAdsManager != null) {
                mAdsManager!!.destroy()
                mAdsManager = null
            }
        }

        mSdkFactory = ImaSdkFactory.getInstance()
        adDisplayContainer = mSdkFactory!!.createAdDisplayContainer()
        adDisplayContainer.adContainer = llAdsViewLiveAudio

        val settings = mSdkFactory!!.createImaSdkSettings()
        mAdsLoader = mSdkFactory!!.createAdsLoader(context, settings, adDisplayContainer)

        // Add listeners for when ads are loaded and for errors.
        mAdsLoader!!.addAdErrorListener(adErrorEventListener)

        mAdsLoader!!.addAdsLoadedListener { adsManagerLoadedEvent ->
            // Ads were successfully loaded, so get the AdsManager instance. AdsManager has
            // events for ad playback and errors.
            mAdsManager = adsManagerLoadedEvent.adsManager

            // Attach event and error event listeners.
            mAdsManager!!.addAdErrorListener(adErrorEventListener)
            mAdsManager!!.addAdEventListener(adEventListener)
            mAdsManager!!.init()

        }
    }

    private fun requestIMAAds(adTagUrl: String, isPublisher: String) {
        // Create the ads request.
        val request = mSdkFactory!!.createAdsRequest()
        request.adTagUrl = adTagUrl
        // Request the ad. After the ad is loaded, onAdsManagerLoaded() will be called.
        mAdsLoader!!.requestAds(request)
    }

    fun initializeLiveAudioPlayer(code: String, adsTriton: Boolean, adsIMA: Boolean) {
        this.code = code

        progressVisible()
        hideAdsView()

        doAsync {
            if (adsTriton && !adsIMA) {
                MowApiMethods.getAudioConfiguration(apiCallback, mowApiInterface, code, "triton", "ondemand")
            } else if (adsIMA && !adsTriton) {
                MowApiMethods.getAudioConfiguration(apiCallback, mowApiInterface, code, "ima", "ondemand")
            } else if (adsIMA && adsTriton) {
                MowApiMethods.getAudioConfiguration(apiCallback, mowApiInterface, code, "triton-ima", "ondemand")
            } else {
                MowApiMethods.getAudioConfiguration(apiCallback, mowApiInterface, code, "", "")
            }
            uiThread {

            }
        }

        ivPlayPause.setOnClickListener {
            if (currentMOWAds != null && !currentMOWAds!!.isUsedOrExpired) {
                if (currentMOWAds!!.isTritonAds) {
                    requestTritonAd(currentMOWAds!!.url, currentMOWAds!!.loadFrom)
                } else {
                    requestIMAAds(currentMOWAds!!.url, currentMOWAds!!.loadFrom)
                }
            } else {
                if (firstTime) {
                    firstTime = false
                    TrackerManager.updateCurrentMedai(currentMedia)
                    TrackerManager.trackFirstPlay("Single", true, true)
                }
                if (playingStatus == PlayingStatus.PLAYING) {
                    ivPlayPause.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_play, null))
                    mowMediaPlayer.pause()
                    playingStatus = PlayingStatus.PAUSE
                } else if (playingStatus == PlayingStatus.STOP || playingStatus == PlayingStatus.PAUSE) {
                    ivPlayPause.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_stop, null))
                    mowMediaPlayer.start()
                    playingStatus = PlayingStatus.PLAYING
                }
            }
        }

        ivSetting.setOnClickListener {
            val popupMenu = PopupMenu(context, it)
            popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.actionMOWPlayer -> {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.mowplayer.com"))
                        context.startActivity(intent)
                    }
                    R.id.actionReport -> {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("www.mowplayer.com/reportissue?code=a-askdaslk"))
                        context.startActivity(intent)
                    }
                }
                true
            }
            popupMenu.show()
        }

        ivMute.setOnClickListener {
            updateMuteStatus()
        }

        seekBarVolume.max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, newVolume: Int, b: Boolean) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun initializeAPI() {
        mowApiClient = MowApiClient()
        mowApiInterface = mowApiClient.createService(MowApiInterface::class.java)
        System.gc()
    }

    override fun onAPISuccess(responseBody: ResponseBody) {
        mowConfig = MOWConfig(responseBody)
        playingStatus = PlayingStatus.STOP
        ivPlayPause.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_play, null))

        TrackerManager.configTracker(mowApiInterface, mowConfig, code, context.getString(R.string.mow_referer))

        // Media Model
        currentMedia = mowConfig.media?.first()!!

        currentMOWAds = currentMedia.ads!!.getAdsFor("0", "m")

        val url = currentMedia.file!!
        mowMediaPlayer.setURL(url)
        currentMedia.ads!!.processDuplicateAds(mowMediaPlayer.totalMilliSeconds / 1000)
        mowMediaPlayer.getMediaPlayer!!.setOnCompletionListener { mp ->
            mp.stop()
        }

        if (currentMedia.thumbnail!!.isNotEmpty()) {
            Glide.with(this)
                    .load(currentMedia.thumbnail)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivThumbnail)
        }

        tvTitle.text = currentMedia.title
        tvDescription.text = currentMedia.description

        // Audio model
        val audio = mowConfig.audio
        if (audio!!.theme!! == "light") {
            setLightTheme()
        } else {
            setDarkTheme()
        }

        // Set Volume
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (mowConfig.volume!! * 10).toInt(), 0)
//            seekBarVolume.progress = audioModel.volume.toInt()
        muteStatus = MuteStatus.NOISY

        progressHide()
    }

    override fun onAPIFailure(throwable: Throwable) {
        progressHide()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (newConfig!!.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (isAdsShowing) {
                llAdsSeekBar.visibility = View.VISIBLE
                llAudioSeekBar.visibility = View.GONE
            } else {
                llAdsSeekBar.visibility = View.GONE
                llAudioSeekBar.visibility = View.VISIBLE
                ivPlayPause.visibility = View.VISIBLE
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (isAdsShowing) {
                llAdsSeekBar.visibility = View.GONE
                llAudioSeekBar.visibility = View.GONE
            } else {
                llAdsSeekBar.visibility = View.GONE
                llAudioSeekBar.visibility = View.GONE
                ivPlayPause.visibility = View.VISIBLE
            }
        }
    }

    fun setLightTheme() {
        llMainBackground.setBackgroundColor(ContextCompat.getColor(context, R.color.text_white))

        ivSetting.setColorFilter(ContextCompat.getColor(context, R.color.text_dark))
//        tvTotalTime.setTextColor(ContextCompat.getColor(context, R.color.text_dark))
        tvDescription.setTextColor(ContextCompat.getColor(context, R.color.text_dark))
        tvLive.setTextColor(ContextCompat.getColor(context, R.color.text_dark))
        ivMute.setColorFilter(ContextCompat.getColor(context, R.color.text_dark))
        ivPlayPause.setColorFilter(ContextCompat.getColor(context, R.color.text_dark))
    }

    fun setDarkTheme() {
        llMainBackground.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_dark))

        ivSetting.setColorFilter(ContextCompat.getColor(context, R.color.text_white))
//        tvTotalTime.setTextColor(ContextCompat.getColor(context, R.color.text_white))
        tvDescription.setTextColor(ContextCompat.getColor(context, R.color.text_white))
        tvLive.setTextColor(ContextCompat.getColor(context, R.color.text_white))
        ivMute.setColorFilter(ContextCompat.getColor(context, R.color.text_white))
        ivPlayPause.setColorFilter(ContextCompat.getColor(context, R.color.text_white))
    }

    fun requestTritonAd(url: String, ad_from: String) {
//        progressHide()

        val hostUrl = StringTokenizer(url, "?").nextToken()
        Log.e("hostUrl", "--------$hostUrl")

        val otherParams = StringTokenizer(url, "&")

        var gdpr: String? = null
        var type: String? = null
        var stid: String? = null
        if (otherParams.hasMoreTokens()) {
            gdpr = otherParams.nextToken()
            gdpr = gdpr!!.replace("gdpr=", "")
        }
        if (otherParams.hasMoreTokens()) {
            type = otherParams.nextToken()
            type = type!!.replace("type=", "")
        }
        if (otherParams.hasMoreTokens()) {
            stid = otherParams.nextToken()
            stid = stid!!.replace("stid=", "")
        }
    }

    private fun updateMuteStatus() {
        when (muteStatus) {
            MuteStatus.NOISY -> {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
                ivMute.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_muted, null))
                muteStatus = MuteStatus.MUTE
            }
            MuteStatus.MUTE -> {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (mowConfig.volume!! * 10).toInt(), 0)
                ivMute.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_volume, null))
                muteStatus = MuteStatus.NOISY
            }
        }
    }

    private fun initializeSeekBar() {
        seekBarAds.max = adMediaPlayer.seconds
//        runnable = Runnable {
//            // Displaying Total Duration time
//            tvAdsDuration.text = MowUtils.milliSecondsToTimer((adMediaPlayer.seconds * 1000).toLong())
//            // Displaying time completed playing
//            tvAdsCurrentTime.text = MowUtils.milliSecondsToTimer((adMediaPlayer.currentSeconds * 1000).toLong())
//
//            seekBarAds.progress = adMediaPlayer.currentSeconds
//            updateHandler.postDelayed(runnable, 15)
////            if (seekBarAds.max == seekBarAds.progress) {
////                llAdsSeekBar.visibility = View.GONE
////                seekBarAudio.visibility = View.VISIBLE
////            }
//        }
//        updateHandler.postDelayed(runnable, 15)
        updateAdsProgressBar()
    }

    /**
     * Update timer on seekbar
     */
    fun updateAdsProgressBar() {
        updateHandler.postDelayed(mUpdateAdsTimeTask, 15)
    }

    /**
     * Background Runnable thread
     */
    private val mUpdateAdsTimeTask = object : Runnable {
        override fun run() {
            // Displaying Total Duration time
            tvAdsDuration.text = MowUtils.milliSecondsToTimer((adMediaPlayer.seconds).toLong())
            // Displaying time completed playing
            tvAdsCurrentTime.text = MowUtils.milliSecondsToTimer((adMediaPlayer.currentSeconds).toLong())

            seekBarAds.progress = adMediaPlayer.currentSeconds

            // Running this thread after 100 milliseconds
            updateHandler.postDelayed(this, 15)
        }
    }

    // Creating an extension property to get the media mediaPlayer time duration in seconds
    private val MediaPlayer.seconds: Int
        get() {
            return this.duration
        }
    // Creating an extension property to get media mediaPlayer current position in seconds
    private val MediaPlayer.currentSeconds: Int
        get() {
            return this.currentPosition
        }

    fun showAds(ad: Bundle) {
        isAdsShowing = true
        onConfigurationChanged(resources.configuration)

        TrackerManager.updateCurrentAds(currentMOWAds!!)
        TrackerManager.trackAdRequest(currentMOWAds!!.id, "marketplace", "m")

//        progressHide()
    }

    fun hideAds() {
        if (currentMOWAds != null) {
            TrackerManager.updateCurrentAds(currentMOWAds!!)
            TrackerManager.trackAdImpression(currentMOWAds!!.id, "marketplace", "m")
        }

        if (firstTime) {
            firstTime = false
            TrackerManager.updateCurrentMedai(currentMedia)
            TrackerManager.trackFirstPlay("Single", true, true)
        }

        isAdsShowing = false
        ivPlayPause.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_stop, null))
        llAdsViewLiveAudio.visibility = View.GONE
        if (currentMOWAds != null)
            currentMOWAds!!.isUsedOrExpired = true
        updateHandler.removeCallbacks(mUpdateAdsTimeTask)

        onConfigurationChanged(resources.configuration)
    }

    fun hideAdsView() {
        llAdsSeekBar.visibility = View.GONE
        llAudioSeekBar.visibility = View.GONE
        ivPlayPause.visibility = View.VISIBLE
    }

    fun showIMAAds() {
        isAdsShowing = true
        onConfigurationChanged(resources.configuration)

        TrackerManager.updateCurrentAds(currentMOWAds!!)
        TrackerManager.trackAdRequest(currentMOWAds!!.id, "marketplace", "m")

//        progressHide()
        ivPlayPause.visibility = View.INVISIBLE

        val params = llAdsViewLiveAudio.layoutParams
        params.width = 1
        params.height = 1
        llAdsViewLiveAudio.layoutParams = params
    }

    fun releasePlayer() {
        mowMediaPlayer.release()
        if (adMediaPlayer.isPlaying) {
            adMediaPlayer.release()
        }
    }


    /**
     * Hide all controls and component and show progress
     */
    private fun progressVisible() {
        llMainBackground.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE
    }

    /**
     * Show all controls and component and hide progress
     */
    private fun progressHide() {
        llMainBackground.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }
}
