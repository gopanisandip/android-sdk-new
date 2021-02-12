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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.ads.interactivemedia.v3.api.*
import com.mowplayer.R
import com.mowplayer.adapter.AudioPlayListAdapter
import com.mowplayer.callbacks.APICallback
import com.mowplayer.models.audio.MOWAds
import com.mowplayer.models.audio.MOWConfig
import com.mowplayer.models.audio.Media
import com.mowplayer.retrofit.MowApiClient
import com.mowplayer.retrofit.MowApiInterface
import com.mowplayer.retrofit.MowApiMethods
import com.mowplayer.utils.*
import kotlinx.android.synthetic.main.layout_ads_seek_bar.view.*
import kotlinx.android.synthetic.main.layout_audio_head.view.*
import kotlinx.android.synthetic.main.layout_audio_seek_bar.view.*
import kotlinx.android.synthetic.main.mow_audio_player.view.*
import okhttp3.ResponseBody
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*
import kotlin.math.roundToInt


class MOWAudioPlayer : LinearLayoutCompat, APICallback<ResponseBody>, OnAudioClickListener {

    private lateinit var mowConfig: MOWConfig
    internal var context: Context
    private lateinit var apiCallback: APICallback<ResponseBody>
    private lateinit var code: String
    private var firstTime = true

    var mowMediaPlayer = MOWMediaPlayer()
    private var currentMOWAds: MOWAds? = null
    lateinit var currentMedia: Media
    var playingStatus: PlayingStatus? = null
    var muteStatus: MuteStatus? = null
    var currentIndex: Int = 0

    private var isAdsShowing: Boolean = false
    private var isPlayList: Boolean = true

    // IMA ads
    private var mSdkFactory: ImaSdkFactory? = null
    private var mAdsLoader: AdsLoader? = null
    private var mAdsManager: AdsManager? = null
    private var mIsAdDisplayed: Boolean = false
    private lateinit var adDisplayContainer: AdDisplayContainer
    private lateinit var adEventListener: AdEvent.AdEventListener
    private lateinit var adErrorEventListener: AdErrorEvent.AdErrorListener

    lateinit var mowApiInterface: MowApiInterface
    lateinit var mowApiClient: MowApiClient
    lateinit var audioManager: AudioManager

    var updateHandler = Handler()
    lateinit var adMediaPlayer: MediaPlayer

    lateinit var adapter: AudioPlayListAdapter

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
        View.inflate(context, R.layout.mow_audio_player, this)

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
                    showIMAAds()
                    mAdsManager!!.start()
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
//                        mAdsManager = null
                    }
                    hideAds()
                }

                AdEvent.AdEventType.AD_PROGRESS -> {
                    seekBarAds!!.max = mAdsManager!!.adProgress.duration.roundToInt()

                    tvAdsDuration.text = MowUtils.milliSecondsToTimer((mAdsManager!!.adProgress.duration * 1000).toLong())
                    tvAdsCurrentTime.text = MowUtils.milliSecondsToTimer((mAdsManager!!.adProgress.currentTime * 1000).toLong())

                    seekBarAds!!.visibility = View.VISIBLE
                    seekBarAds!!.progress = mAdsManager!!.adProgress.currentTime.roundToInt()
                    seekBarAds!!.secondaryProgress = mAdsManager!!.adProgress.currentTime.roundToInt()
                }

                else -> {

                }
            }
        }

        adErrorEventListener = AdErrorEvent.AdErrorListener {

            if (mAdsManager != null) {
                mAdsManager!!.destroy()
                mAdsLoader!!.contentComplete()
//                mAdsManager = null
//                setupIMAAds()
            }

            hideAds()
        }

        setupIMAAds()
    }

    private fun requestIMAAds(adTagUrl: String, isPublisher: String) {
        Log.e("URL---- ", adTagUrl)
        // Create the ads request.
        val request = mSdkFactory!!.createAdsRequest()
        request.adTagUrl = adTagUrl
        // Request the ad. After the ad is loaded, onAdsManagerLoaded() will be called.
        mAdsLoader!!.requestAds(request)
    }

    private fun setupIMAAds() {
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

    fun initializeAudioPlayer(code: String, adsTriton: Boolean, adsIMA: Boolean) {
        hideAudioView()
        hideAdsView()

        ivPlayPause.visibility = View.VISIBLE
        iv10SecForward.visibility = View.GONE
        iv10SecPrevious.visibility = View.GONE
        ivSetting.visibility = View.VISIBLE

        progressVisible()

        this.code = code
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
            if (currentMOWAds != null) {
                if (currentMOWAds!!.isTritonAds) {
                    requestTritonAd(currentMOWAds!!.url, currentMOWAds!!.loadFrom)
                } else {
                    setupIMAAds()
                    requestIMAAds(currentMOWAds!!.url, currentMOWAds!!.loadFrom)
                }
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    showAdsView()
                }
            } else {
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    showAudioView()
                }
                if (firstTime) {
                    TrackerManager.updateCurrentMedai(currentMedia)
                    TrackerManager.trackFirstPlay("Single", false, false)
                }
                if (playingStatus == PlayingStatus.PLAYING) {
                    ivPlayPause.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_play, null))
                    mowMediaPlayer.pause()
                    playingStatus = PlayingStatus.PAUSE
                    if (!firstTime)
                        TrackerManager.stopTracking()
                } else if (playingStatus == PlayingStatus.STOP || playingStatus == PlayingStatus.PAUSE || playingStatus == PlayingStatus.FINISH) {
                    ivPlayPause.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_pause, null))
                    mowMediaPlayer.start()
                    playingStatus = PlayingStatus.PLAYING
                    initializeSeekBar()
                    if (!firstTime)
                        TrackerManager.resumeTracking(mowConfig)
                }
            }
        }

        ivNext.setOnClickListener {
            if (currentIndex < (mowConfig.media!!.size - 1)) {
                currentIndex += 1
                playSong(currentIndex)
            } else {
                // play first song
                currentIndex = 0
                playSong(currentIndex)
            }
        }
        ivPrevious.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex -= 1
                playSong(currentIndex)
            } else {
                // play last song
                currentIndex = mowConfig.media!!.size - 1
                playSong(currentIndex)
            }
        }

        iv10SecForward.setOnClickListener {
            mowMediaPlayer.seekForward()
        }
        iv10SecPrevious.setOnClickListener {
            mowMediaPlayer.seekBackward()
        }

        ivShuffle.setOnClickListener {

        }

        ivMute.setOnClickListener {
            updateMuteStatus()
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

        seekBarAudio.splitTrack = false
        seekBarAudio.thumb.mutate().alpha = 0
        // Audio Seek bar change listener
        seekBarAudio.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (b) {
                    mowMediaPlayer.seekTo(i)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                seekBar.thumb.mutate().alpha = 255
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                seekBar.splitTrack = false
                seekBar.thumb.mutate().alpha = 0
            }
        })


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

    private fun initializeSeekBar() {
        seekBarAudio.max = mowMediaPlayer.totalMilliSeconds

        mowMediaPlayer.getMediaPlayer!!.setOnBufferingUpdateListener { _, percent ->
            seekBarAudio.secondaryProgress = percent
        }

        updateProgressBar()

        if (firstTime) {
            firstTime = false
            TrackerManager.updateCurrentMedai(currentMedia)
            TrackerManager.trackProcess(mowConfig, seekBarAudio)
        }
    }

    private fun initializeAdsSeekBar() {
        seekBarAds.max = adMediaPlayer.seconds
        updateAdsProgressBar()
    }

    override fun onAPISuccess(responseBody: ResponseBody) {
        playingStatus = PlayingStatus.STOP

        mowConfig = MOWConfig(responseBody)

        TrackerManager.configTracker(mowApiInterface, mowConfig, code, context.getString(R.string.mow_referer))

        // Media Model
        currentMedia = mowConfig.media?.first()!!
        isPlayList = mowConfig.media!!.size > 1

        onConfigurationChanged(resources.configuration)

        if (isPlayList) {
            llPlayList.visibility = View.VISIBLE
        } else {
            llPlayList.visibility = View.GONE
        }
        mowMediaPlayer.setURL(currentMedia.file!!)
        if (currentMedia.ads != null) {
            currentMOWAds = currentMedia.ads!!.getAdsFor("0", "m")
            currentMedia.ads!!.processDuplicateAds(mowMediaPlayer.totalMilliSeconds / 1000)
        }
        mowMediaPlayer.getMediaPlayer!!.setOnCompletionListener { mp ->
            Log.e("stop", "asddd")
            mp.stop()
            mp.release()
            playingStatus = PlayingStatus.FINISH
            updateHandler.removeCallbacks(mUpdateTimeTask)
            TrackerManager.stopTracking()

//            mowMediaPlayer.getMediaPlayer!!.stop()
//            mowMediaPlayer.getMediaPlayer!!.release()

            if (mowConfig.media!!.size <= 1) {
                mowMediaPlayer.setURL(currentMedia.file!!)
            } else {
                if (currentIndex < (mowConfig.media!!.size - 1)) {
                    currentIndex += 1
                    currentMedia = mowConfig.media!![currentIndex]
                    playSong(currentIndex)
                } else {
                    currentMedia = mowConfig.media!!.first()
                    currentIndex = 0
                    playSong(currentIndex)
                }
            }

            ivPlayPause.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_play, null))

            currentMOWAds = currentMedia.ads!!.getAdsFor("-1", "m")
            if (currentMOWAds != null) {
                if (currentMOWAds!!.isTritonAds) {
                    requestTritonAd(currentMOWAds!!.url, currentMOWAds!!.loadFrom)
                } else {
                    setupIMAAds()
                    requestIMAAds(currentMOWAds!!.url, currentMOWAds!!.loadFrom)
                }
            }
        }

        if (currentMedia.thumbnail!!.isNotEmpty()) {
//            Picasso.get()
//                    .load(currentMedia.thumbnail)
//                    .apply(RequestOptions.circleCropTransform())
//                    .into(ivThumbnail)

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
//        seekBarVolume.progress = audioModel.volume.toInt()
        muteStatus = MuteStatus.NOISY

        adapter = AudioPlayListAdapter(context, mowConfig.media, 0, audio.theme!!, this)
        recyclerView.adapter = adapter

        progressHide()
    }

    override fun onAPIFailure(throwable: Throwable) {
        progressHide()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if (newConfig!!.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (isAdsShowing) {
                showAdsView()
            } else {
                showAudioView()
                if (isPlayList) {
                    ivNext.visibility = View.VISIBLE
                    ivPrevious.visibility = View.VISIBLE
                }
            }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (isAdsShowing) {
                hideAudioView()
                hideAdsView()
            } else {
                hideAudioView()
                hideAdsView()

                ivPlayPause.visibility = View.VISIBLE
                ivSetting.visibility = View.VISIBLE
            }
        }
    }

    override fun onAudioClickListener(media: Media?, position: Int) {
        if (playingStatus != PlayingStatus.FINISH) {
            mowMediaPlayer.stop()
            mowMediaPlayer.release()
        }
        mowMediaPlayer.setURL(media!!.file!!)
        mowMediaPlayer.start()

        initializeSeekBar()

        currentMedia = media

        playingStatus = PlayingStatus.PLAYING

        if (currentMedia.thumbnail!!.isNotEmpty()) {
            Glide.with(this)
                    .load(currentMedia.thumbnail)
                    .apply(RequestOptions.circleCropTransform())
                    .into(ivThumbnail)
        }

        tvTitle.text = currentMedia.title
        tvDescription.text = currentMedia.description

        adapter.update(position)
        adapter.notifyDataSetChanged()
        progressHide()
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

    private fun setLightTheme() {
        llMainBackground.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_light))

        tvDescription.setTextColor(ContextCompat.getColor(context, R.color.text_dark))
        tvAdsDuration.setTextColor(ContextCompat.getColor(context, R.color.text_dark))
        tvAdsCurrentTime.setTextColor(ContextCompat.getColor(context, R.color.text_dark))
        tvDuration.setTextColor(ContextCompat.getColor(context, R.color.text_dark))
        tvCurrentTime.setTextColor(ContextCompat.getColor(context, R.color.text_dark))
        ivShuffle.setColorFilter(ContextCompat.getColor(context, R.color.text_dark))
        iv10SecPrevious.setColorFilter(ContextCompat.getColor(context, R.color.text_dark))
        ivPrevious.setColorFilter(ContextCompat.getColor(context, R.color.text_dark))
        ivPlayPause.setColorFilter(ContextCompat.getColor(context, R.color.text_dark))
        ivNext.setColorFilter(ContextCompat.getColor(context, R.color.text_dark))
        iv10SecForward.setColorFilter(ContextCompat.getColor(context, R.color.text_dark))
        ivSetting.setColorFilter(ContextCompat.getColor(context, R.color.text_dark))
        ivMute.setColorFilter(ContextCompat.getColor(context, R.color.text_dark))

        tvTrack.setTextColor(ContextCompat.getColor(context, R.color.text_dark))
        tvDurations.setTextColor(ContextCompat.getColor(context, R.color.text_dark))

        recyclerView.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_light))
    }

    private fun setDarkTheme() {
        llMainBackground.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_dark))

        tvDescription.setTextColor(ContextCompat.getColor(context, R.color.text_white))
        tvAdsDuration.setTextColor(ContextCompat.getColor(context, R.color.text_white))
        tvAdsCurrentTime.setTextColor(ContextCompat.getColor(context, R.color.text_white))
        tvDuration.setTextColor(ContextCompat.getColor(context, R.color.text_white))
        tvCurrentTime.setTextColor(ContextCompat.getColor(context, R.color.text_white))
        ivShuffle.setColorFilter(ContextCompat.getColor(context, R.color.text_white))
        iv10SecPrevious.setColorFilter(ContextCompat.getColor(context, R.color.text_white))
        ivPrevious.setColorFilter(ContextCompat.getColor(context, R.color.text_white))
        ivPlayPause.setColorFilter(ContextCompat.getColor(context, R.color.text_white))
        ivNext.setColorFilter(ContextCompat.getColor(context, R.color.text_white))
        iv10SecForward.setColorFilter(ContextCompat.getColor(context, R.color.text_white))
        ivSetting.setColorFilter(ContextCompat.getColor(context, R.color.text_white))
        ivMute.setColorFilter(ContextCompat.getColor(context, R.color.text_white))

        tvTrack.setTextColor(ContextCompat.getColor(context, R.color.text_white))
        tvDurations.setTextColor(ContextCompat.getColor(context, R.color.text_white))

        recyclerView.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_dark))
    }

    private fun playSong(index: Int) {
        progressVisible()

        ivPlayPause.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_pause, null))
        onAudioClickListener(mowConfig.media?.get(index), index)
    }

    fun requestTritonAd(url: String, ad_from: String) {
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

    fun showAds(ad: Bundle) {
        isAdsShowing = true
        onConfigurationChanged(resources.configuration)

        TrackerManager.updateCurrentAds(currentMOWAds!!)
        TrackerManager.trackAdRequest(currentMOWAds!!.id, "marketplace", "m")
        if (!firstTime)
            TrackerManager.stopTracking()
    }

    fun showIMAAds() {
        isAdsShowing = true
        onConfigurationChanged(resources.configuration)

        TrackerManager.updateCurrentAds(currentMOWAds!!)
        TrackerManager.trackAdRequest(currentMOWAds!!.id, "marketplace", "m")
        llAdsViewLiveAudio.visibility = View.VISIBLE

        val params = llAdsViewLiveAudio.layoutParams
        params.width = 1
        params.height = 1
        llAdsViewLiveAudio.layoutParams = params


        if (!firstTime)
            TrackerManager.stopTracking()
    }

    fun hideAds() {
        if (currentMOWAds != null) {
            TrackerManager.updateCurrentAds(currentMOWAds!!)
            TrackerManager.trackAdImpression(currentMOWAds!!.id, "marketplace", "m")
        }
//        marketplace = mediaList?.get(1)!!.ads?.let { AdsManager.getAdsObject(it) }
        if (playingStatus == PlayingStatus.FINISH) {
            updateHandler.removeCallbacks(mUpdateTimeTask)
            mowMediaPlayer.release()
        } else {
            currentMOWAds!!.isUsedOrExpired = true
            mowMediaPlayer.start()
            playingStatus = PlayingStatus.PLAYING
            initializeSeekBar()
        }

        llAudioControls.visibility = View.VISIBLE
        isAdsShowing = false

        ivPlayPause.isEnabled = true
        ivPlayPause.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_pause, null))

        onConfigurationChanged(resources.configuration)
    }

    fun releasePlayer() {
        updateHandler.removeCallbacks(mUpdateTimeTask)
        mowMediaPlayer.release()
        if (adMediaPlayer.isPlaying) {
            updateHandler.removeCallbacks(mUpdateAdsTimeTask)
            adMediaPlayer.release()
        }
    }

    /**
     * Update timer on seek bar
     */
    private fun updateProgressBar() {
        updateHandler.postDelayed(mUpdateTimeTask, 15)
    }

    /**
     * Background Runnable thread
     */
    private val mUpdateTimeTask = object : Runnable {
        override fun run() {
            currentMOWAds = currentMedia.ads!!.getAdsFor((mowMediaPlayer.currentMilliSeconds / 1000).toString(), "m")

            if (currentMOWAds != null) {
                mowMediaPlayer.pause()
                playingStatus = PlayingStatus.PAUSE
                updateHandler.removeCallbacks(this)
                if (currentMOWAds!!.isTritonAds) {
                    requestTritonAd(currentMOWAds!!.url, currentMOWAds!!.loadFrom)
                } else {
                    setupIMAAds()
                    requestIMAAds(currentMOWAds!!.url, currentMOWAds!!.loadFrom)
                }
            } else {
//                mowMediaPlayer.start()
                updateHandler.postDelayed(this, 15)
            }

            // Displaying Total Duration time
            tvDuration.text = MowUtils.milliSecondsToTimer((mowMediaPlayer.totalMilliSeconds).toLong())
            // Displaying time completed playing
            tvCurrentTime.text = MowUtils.milliSecondsToTimer((mowMediaPlayer.currentMilliSeconds).toLong())

            seekBarAudio.progress = mowMediaPlayer.currentMilliSeconds

            // Running this thread after 100 milliseconds
//            updateHandler.postDelayed(this, 15)
        }
    }

    /**
     * Update timer on seek bar
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

    private fun showAudioView() {
        hideAdsView()
        llAudioSeekBar.visibility = View.VISIBLE
        iv10SecForward.visibility = View.VISIBLE
        ivPlayPause.visibility = View.VISIBLE
        iv10SecPrevious.visibility = View.VISIBLE
        ivSetting.visibility = View.VISIBLE
    }

    private fun showAdsView() {
        hideAudioView()
        llAdsSeekBar.visibility = View.VISIBLE
    }

    private fun hideAudioView() {
        llAudioSeekBar.visibility = View.GONE
        iv10SecForward.visibility = View.GONE
        ivPrevious.visibility = View.GONE
        ivPlayPause.visibility = View.GONE
        ivNext.visibility = View.GONE
        iv10SecPrevious.visibility = View.GONE
        ivSetting.visibility = View.GONE
    }

    private fun hideAdsView() {
        llAdsSeekBar.visibility = View.GONE
    }

    /**
     * Hide all controls and component and show progress
     */
    private fun progressVisible() {
        llContainer.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE
    }

    /**
     * Show all controls and component and hide progress
     */
    private fun progressHide() {
        llContainer.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        releasePlayer()
    }
}
