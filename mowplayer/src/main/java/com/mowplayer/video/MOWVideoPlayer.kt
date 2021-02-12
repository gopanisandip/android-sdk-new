package com.mowplayer.video

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.media.MediaPlayer
import android.media.MediaPlayer.OnVideoSizeChangedListener
import android.os.Handler
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.ads.interactivemedia.v3.api.*
import com.google.ads.interactivemedia.v3.api.player.ContentProgressProvider
import com.google.ads.interactivemedia.v3.api.player.VideoProgressUpdate
import com.google.android.gms.ads.*
import com.mowplayer.R
import com.mowplayer.adapter.PlaylistAdapter
import com.mowplayer.adapter.VideoPlayListAdapter
import com.mowplayer.callbacks.APICallback
import com.mowplayer.callbacks.RelatedVideosCallback
import com.mowplayer.callbacks.TrackAdsCallback
import com.mowplayer.models.Marketplace
import com.mowplayer.models.Publisher
import com.mowplayer.models.RelatedVideoModel
import com.mowplayer.models.audio.MOWAds
import com.mowplayer.models.audio.MOWConfig
import com.mowplayer.models.audio.Media
import com.mowplayer.retrofit.MowApiClient
import com.mowplayer.retrofit.MowApiInterface
import com.mowplayer.retrofit.MowApiMethods
import com.mowplayer.utils.MowConstants
import com.mowplayer.utils.OnAudioClickListener
import com.mowplayer.utils.PlayerType
import com.mowplayer.video.controller.OrientationDetector
import com.mowplayer.video.controller.VideoControllerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.layout_article.view.*
import kotlinx.android.synthetic.main.mow_video_player.view.*
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.IOException
import java.util.*

class MOWVideoPlayer : LinearLayoutCompat, APICallback<ResponseBody>, OnAudioClickListener,
        VideoControllerView.MediaPlayerControlListener,
        SurfaceHolder.Callback, RelatedVideosCallback<List<RelatedVideoModel>>,
        TrackAdsCallback<ResponseBody>, OrientationDetector.OrientationChangeListener,
        VideoControllerView.VideoViewCallback, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, OnVideoSizeChangedListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnInfoListener {

    private val mMediaPlayer: MediaPlayer? = MediaPlayer()

    // New code*************************************************************************************
    private val TAG = "MowPlayerActivity"
    private lateinit var apiCallback: APICallback<ResponseBody>

    var code: String = ""
    private lateinit var activity: Activity
    private lateinit var mowConfig: MOWConfig

    var requestedOrientation: Int = 0

    private var mediaList: List<Media>? = null


    lateinit var currentMedia: Media

    var currentPlayingIndex: Int = 0

    var mIsComplete: Boolean = false

    //
//    var mediaModel: MediaModel? = MediaModel()
//
    fun getDeviceWidth(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val mDisplayMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(mDisplayMetrics)
        return mDisplayMetrics.widthPixels
    }

    fun getDeviceHeight(context: Context): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val mDisplayMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(mDisplayMetrics)
        return mDisplayMetrics.heightPixels
    }

    private var videoController: VideoControllerView? = null

    //    private var mVideoWidth = 0
//    private var mVideoHeight = 0
    private var relatedVideos: List<RelatedVideoModel>? = null


    private var mAdsManager: AdsManager? = null
    private var mIsAdDisplayed: Boolean = false
    private var mMowApiClient: MowApiClient = MowApiClient()
    private lateinit var mMowApiInterface: MowApiInterface
    internal var duration = 0

    private var marketPlaceAdsArray: MutableList<Marketplace>? = null
    private var publisherAdsArray: MutableList<Publisher>? = null

    private var adPriority = ""

    private var postMarketPlaceAdUrl: Marketplace? = null
    private var postPublisherAdUrl: Publisher? = null
    private var currentMarketPlaceAd: Marketplace? = null
    private var currentPublisherAd: Publisher? = null
    private var surfaceHolder: SurfaceHolder? = null

    //    private var videoTitle: String? = null
//    private var videoUrl: String? = null
    private var authToken: String = ""
    private var logoUrl: String = ""

    private var onAdEventCalled: Boolean = false
    private var playMidAd: Boolean = false

    private lateinit var mRelatedVideosCallback: RelatedVideosCallback<List<RelatedVideoModel>>
    private lateinit var mTrackAdsCallback: TrackAdsCallback<ResponseBody>

    private var mAutoRotation = false
    private var mOrientationDetector: OrientationDetector? = null

    //    private var isLandscape: Boolean = true
//    private var mVideoViewLayoutWidth = 0
//    private var mVideoViewLayoutHeight = 0
    private var mSeekPosition: Int = 0
    private var isPaused = false
    private var videoError: Boolean = false

    private var relatedVideoId: Long = 0

    private var mContext: Context
    private var timer = Timer()

    private var currentMOWAds: MOWAds? = null

    // Ads variable define
    private lateinit var adErrorEventListener: AdErrorEvent.AdErrorListener
    private lateinit var adEventListener: AdEvent.AdEventListener
    private var mSdkFactory: ImaSdkFactory? = null
    private var mAdsLoader: AdsLoader? = null
    private lateinit var adDisplayContainer: AdDisplayContainer
    private lateinit var adView: AdView
    private var isAdTester: Boolean = false

    constructor(context: Context) : super(context) {
        this.mContext = context
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        this.mContext = context
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.mContext = context
        init(context)
    }

    fun init(context: Context) {
        View.inflate(context, R.layout.mow_video_player, this)

        apiCallback = this
        initIMAAds()

        System.gc()
    }

    fun initializeVideoPlayer(code: String, activity: Activity) {
        this.code = code
        this.activity = activity

        resetVideoContainerRatio(MowConstants.DEFAULT_RATIO)

        progressVisible()
        progressBar.indeterminateDrawable.setColorFilter(ContextCompat.getColor(mContext, R.color.white), PorterDuff.Mode.SRC_IN)

        var linearLayoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rvPlayList.setHasFixedSize(false)
        rvPlayList.isNestedScrollingEnabled = false
        rvPlayList.layoutManager = linearLayoutManager

        linearLayoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rvRelatedVideos.setHasFixedSize(false)
        rvRelatedVideos.isNestedScrollingEnabled = false
        rvRelatedVideos.layoutManager = linearLayoutManager
        mMediaPlayer!!.setOnVideoSizeChangedListener(this)

        mMowApiInterface = mMowApiClient.createService(MowApiInterface::class.java)
        MowApiMethods.getVideoConfiguration(apiCallback, mMowApiInterface, code)

        MobileAds.initialize(mContext, "ca-app-pub-3940256099942544~3347511713")

        adView = AdView(mContext)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = "ca-app-pub-3940256099942544/6300978111"

        ad_seekbar!!.visibility = View.GONE

        marketPlaceAdsArray = ArrayList()
        publisherAdsArray = ArrayList()
        relatedVideos = ArrayList()
        mMowApiClient = MowApiClient()


        surfaceHolder = videoView!!.holder
        surfaceHolder!!.setFixedSize(400, 300)
        surfaceHolder!!.addCallback(this)
        mRelatedVideosCallback = this
        mTrackAdsCallback = this

        ivNext.setOnClickListener {
            playNext()
        }

        ivPrevious.setOnClickListener {
            playPrevious()
        }

        // Testing
//        mowConfig = MOWConfigTest(loadJSONFromAsset()!!)
//        currentMedia = mowConfig.media?.first()!!
//        resetVideoContainerRatio(mowConfig.ratio)
//        authToken = mowConfig.auth.toString()
//        mediaList = mowConfig.media
//        adPriority = mowConfig.adPriority.toString()
//        val adapter = VideoPlayListAdapter(context, mediaList, 0, "dark", this)
//        rvPlayList.adapter = adapter
//        playVideo(currentPlayingIndex)
    }

//    fun loadJSONFromAsset(): String? {
//        val json = try {
////            val inputStream = mContext.assets.open("ads_with_ad_structure.json")
////            val inputStream = mContext.assets.open("first_ad_fail_run_callback.json")
////            val inputStream = mContext.assets.open("first_ad_fail_run_second_ad.json")
////            val inputStream = mContext.assets.open("only_publisher_ad_success.json")
////            val inputStream = mContext.assets.open("aonly_publisher_ad_success_so_skip_callback_even_exists .json")
//            val inputStream = mContext.assets.open("pre_mid_post_roll_ad.json")
////            val inputStream = mContext.assets.open("single_ad_at_one_position_at_a_time.json")
//            val size: Int = inputStream.available()
//            val buffer = ByteArray(size)
//            inputStream.read(buffer)
//            inputStream.close()
//            String(buffer, Charset.forName("utf-8"))
//        } catch (ex: IOException) {
//            ex.printStackTrace()
//            return null
//        }
//        return json
//    }

    private fun initIMAAds() {
        // IMA Ads
        adEventListener = AdEvent.AdEventListener { adEvent ->
            when (adEvent!!.type) {
                AdEvent.AdEventType.LOADED -> {
                    timer.cancel()
                    videoView.pause()
                    progressHide()
                    if (mAdsManager != null) {
                        mAdsManager!!.start()
                    }
                    ad_seekbar!!.visibility = View.VISIBLE
                    if (currentMOWAds != null)
                        currentMOWAds!!.isUsedOrExpired = true
                }
                AdEvent.AdEventType.CONTENT_PAUSE_REQUESTED -> {
                    mIsAdDisplayed = true
                    videoView!!.pause()
                }

                AdEvent.AdEventType.CONTENT_RESUME_REQUESTED -> {
                    mIsAdDisplayed = false
                }

                AdEvent.AdEventType.ALL_ADS_COMPLETED -> {
                    if (currentMOWAds != null)
                        currentMOWAds!!.isUsedOrExpired = true

                    if (!isAdTester) {
                        trackProgress()
                    }
                    ad_seekbar!!.visibility = View.GONE
                    if (mAdsManager != null) {
                        mAdsManager!!.destroy()
                        mAdsManager = null
                    }
                    progressVisible()
                    if (videoController == null) {
                        playVideo(currentPlayingIndex)
                    } else {
                        videoController!!.doPauseResume()
                    }
                }
                AdEvent.AdEventType.AD_BUFFERING -> progressVisible()

                AdEvent.AdEventType.AD_PROGRESS -> {
                    progressHide()
                    ad_seekbar!!.max = Math.round(mAdsManager!!.adProgress.duration)
                    ad_seekbar!!.visibility = View.VISIBLE
                    ad_seekbar!!.progress = Math.round(mAdsManager!!.adProgress.currentTime)
                    ad_seekbar!!.secondaryProgress = Math.round(mAdsManager!!.adProgress.currentTime)
                }

                else -> {

                }
            }
        }

        adErrorEventListener = AdErrorEvent.AdErrorListener {
            Log.d(TAG, "onAdError: ")

            resetVideoContainerRatio(mowConfig.ratio)
//            onAdEventCalled = true
            ad_seekbar!!.visibility = View.GONE
            if (mAdsManager != null) {
                mAdsManager!!.destroy()
                mAdsManager = null
            }

            if (currentMOWAds!!.callbackAds!!.isNotEmpty()) {
                currentMOWAds = currentMedia.callbackAds!!.linear
                setupIMAAds()
                requestIMAAds(currentMOWAds!!.url, currentMOWAds!!.loadFrom)
            } else {
                currentMOWAds!!.isUsedOrExpired = true
                playOtherAdIfFails()
            }
        }

        setupIMAAds()
    }

    private fun setupIMAAds() {
        mSdkFactory = ImaSdkFactory.getInstance()
        adDisplayContainer = mSdkFactory!!.createAdDisplayContainer()
        adDisplayContainer.adContainer = videoPlayerWithAdPlayback

        val settings = mSdkFactory!!.createImaSdkSettings()
        mAdsLoader = mSdkFactory!!.createAdsLoader(mContext, settings, adDisplayContainer)

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

//    private val mUpdateTimeTask = object : Runnable {
//        override fun run() {
//            currentMOWAds = currentMedia.ads!!.getAdsFor((videoView.currentPosition / 1000).toString(), "m")
//
//            if (currentMOWAds != null) {
//                videoView.pause()
//                updateHandler.removeCallbacks(this)
//                setupIMAAds()
//                requestIMAAds(currentMOWAds!!.url, currentMOWAds!!.loadFrom)
//            } else {
////                mowMediaPlayer.start()
//                updateHandler.postDelayed(this, 15)
//            }
//        }
//    }

    override fun onAPISuccess(responseBody: ResponseBody) {
        mowConfig = MOWConfig(responseBody)
        currentMedia = mowConfig.media?.first()!!

        resetVideoContainerRatio(mowConfig.ratio)

        authToken = mowConfig.auth.toString()
        mediaList = mowConfig.media
        adPriority = mowConfig.adPriority.toString()

        val adapter = VideoPlayListAdapter(context, mediaList, 0, "dark", this)
        rvPlayList.adapter = adapter

        playVideo(currentPlayingIndex)
    }

    private fun resetVideoContainerRatio(ratio: String?) {
        if (isFullScreen) {
            val layoutParams = videoPlayerWithAdPlayback!!.layoutParams
            layoutParams.height = getDeviceHeight(context)
            layoutParams.width = getDeviceWidth(context)
            videoPlayerWithAdPlayback.layoutParams = layoutParams
        } else {
            val layoutParams = videoPlayerWithAdPlayback!!.layoutParams
            val stringTokenizer = StringTokenizer(ratio, ":")
            val ratioWidth = stringTokenizer.nextToken().toInt()
            val ratioHeight = stringTokenizer.nextToken().toInt()

            val height = (getDeviceWidth(mContext) * ratioHeight) / ratioWidth
            layoutParams.height = height
            videoPlayerWithAdPlayback.layoutParams = layoutParams
        }
    }

    override fun onAPIFailure(throwable: Throwable) {
        Toast.makeText(mContext, "Something went wrong, please try again", Toast.LENGTH_LONG).show()
    }

    private fun playVideo(index: Int) {
        currentMedia = mediaList!![index]

        if (currentMedia.thumbnail!!.isNotEmpty()) {
            Glide.with(this)
                    .load(currentMedia.thumbnail)
                    .into(ivThumbnail)
        }

        rvRelatedVideos.visibility = View.GONE

        currentMOWAds = if (currentMedia.ads == null) {
            if (mowConfig.ads != null) {
                mowConfig.ads!!.getAdsFor("0", "m")
            } else {
                null
            }
        } else {
            currentMedia.ads!!.getAdsFor("0", "m")
        }
        if (currentMOWAds != null) {
            setupIMAAds()
            requestIMAAds(currentMOWAds!!.url, currentMOWAds!!.loadFrom)
        } else {
            try {
                videoView.setVideoPath(currentMedia.file)

                videoView!!.setOnInfoListener(this)
                videoView!!.setOnPreparedListener(this)
                videoView!!.setOnCompletionListener(this)
                videoView!!.setOnErrorListener(this)

            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: SecurityException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun setupArticle() {
        llArticleView.visibility = VISIBLE

        if (currentMedia._nv != null) {
            tvTitle.text = currentMedia._nv!!.title
            tvDescription.text = currentMedia._nv!!.description
            if (currentMedia._nv!!.url != null) {
                ivLink.visibility = VISIBLE
            } else {
                ivLink.visibility = GONE
            }
        } else {
            tvTitle.text = currentMedia.title
            tvDescription.text = currentMedia.description
        }
    }

    private fun hideArticle() {
        llArticleView.visibility = View.GONE
    }

    override fun onScaleChange(isFullscreen: Boolean) {
//        if (isFullScreen) {
//            val layoutParams = videoContainer!!.layoutParams
//            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
//            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
//            videoView!!.layoutParams = layoutParams
//        } else {
//            val layoutParams = videoContainer!!.layoutParams
//            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
//            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
//            videoView!!.layoutParams = layoutParams
//        }
    }

    // Implement SurfaceHolder.Callback
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
//        mSurfaceWidth = width
//        mSurfaceHeight = height
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        //  mMediaPlayer!!.setOnVideoSizeChangedListener(this)
        surfaceHolder = videoView!!.holder
        surfaceHolder!!.addCallback(this)
        surfaceHolder!!.setFormat(PixelFormat.TRANSPARENT)
//        surfaceHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        mMediaPlayer!!.setDisplay(holder)
        try {
            mMediaPlayer.prepareAsync()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
        enableOrientationDetect()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
//        surfaceHolder = null
        resetPlayer()
//        if (mMediaPlayer != null)
//            mMediaPlayer!!.setDisplay(null)

//        disableOrientationDetect()
    }

    private fun disableOrientationDetect() {
        if (mOrientationDetector != null) {
            mOrientationDetector!!.disable()
        }
    }

    private fun enableOrientationDetect() {
        if (mAutoRotation && mOrientationDetector == null) {
            mOrientationDetector = OrientationDetector(mContext)
            mOrientationDetector!!.setOrientationChangeListener(this)
            mOrientationDetector!!.enable()
        }
    }

    override fun onOrientationChanged(screenOrientation: Int, direction: OrientationDetector.Direction?) {
        if (!mAutoRotation) {
            return
        }

        when {
            direction === OrientationDetector.Direction.PORTRAIT -> setFullscreen(false, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            direction === OrientationDetector.Direction.REVERSE_PORTRAIT -> setFullscreen(false, ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT)
            direction === OrientationDetector.Direction.LANDSCAPE -> setFullscreen(true, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            direction === OrientationDetector.Direction.REVERSE_LANDSCAPE -> setFullscreen(true, ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE)
        }
    }

    override fun onInfo(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        when (what) {
            MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START -> {
                progressHide()
            }
            MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                progressVisible()
            }
            MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                progressHide()
//                        if (mowConfig.autoPlayRelated!!.enabled!!) {
//                            if (relatedVideos!!.isEmpty()) {
//                                playVideo(currentIndex)
//                            }
//                        }
            }
        }
        return false
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mp!!.setScreenOnWhilePlaying(true)

        progressVisible()

        videoController = VideoControllerView.Builder(context, this)
                .withVideoTitle(currentMedia.title)
                .withVideoSurfaceView(videoView)//to enable toggle display controller view
                .canControlBrightness(true)
                .canControlVolume(true)
                .canSeekVideo(true)
                .pauseIcon(R.drawable.ic_pause)
                .playIcon(R.drawable.ic_play)
                .shrinkIcon(R.drawable.ic_media_fullscreen_shrink)
                .stretchIcon(R.drawable.ic_maximize)
                .build(videoContainer)//layout container that hold video play view

        val playerType = videoController!!.playerType(code)
        if (playerType == PlayerType.Article) {
            setupArticle()
        } else {
            hideArticle()
        }

        if (logoUrl.isNotEmpty()) {
            Picasso.get()
                    .load(logoUrl)
                    .placeholder(R.drawable.logo_white)
                    .error(R.drawable.logo_white)
                    .into(videoController!!.ivPlayerLogo)
        }

        videoView!!.setOnTouchListener { v, event ->
            videoController!!.toggleControllerView()
            false
        }

        onConfigurationChanged(resources.configuration)

//                if (isAdTester)
//                    surfaceHolder!!.addCallback(mContext)

        rvRelatedVideos.visibility = View.GONE

        videoView!!.visibility = View.VISIBLE

        progressHide()

        videoController!!.setMuteVolume(mowConfig.mute!!)

        if (mowConfig.autoplay!!) {
            mp.start()
        }
        mp.setDisplay(surfaceHolder)

        mIsComplete = false

//        if (isAdTester && playMidAd) {
        trackProgress()
//        }
    }

    override fun onCompletion(mp: MediaPlayer) {
        videoController!!.mPauseButton.setOnClickListener {
            videoController!!.togglePausePlay()
        }

        mIsComplete = true
        videoController!!.mSeekBar.invalidate()
        videoController!!.doPauseResume()

        if (currentPlayingIndex < (mediaList!!.size - 1)) {
            currentPlayingIndex += 1
            playVideo(currentPlayingIndex)
        } else {
            currentPlayingIndex = 0
            playVideo(currentPlayingIndex)
        }

        loadRelatedVideos(authToken, relatedVideoId, resources.getString(R.string.mow_referer))
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        if (videoError) {
            progressHide()
            Toast.makeText(mContext, "We can't play this video AccessDenied", Toast.LENGTH_SHORT).show()
            loadRelatedVideos(authToken, relatedVideoId, resources.getString(R.string.mow_referer))
        }
        return false
    }

    private fun loadRelatedVideos(authToken: String, mediaId: Long, mowReferer: String) {
        videoError = false
        MowApiMethods.getRelatedVideos(mRelatedVideosCallback, mMowApiInterface, authToken, mediaId, mowReferer)
    }

    override fun onRelatedVideosReceived(response: Response<List<RelatedVideoModel>>) {
        if (response.body() != null) {
            if (response.isSuccessful) {
                Log.d("onResponse", response.message())
                relatedVideos = response.body()

                try {
                    if (relatedVideos!!.isNotEmpty()) {
                        //Load related videos
                        rvRelatedVideos.visibility = View.VISIBLE
                        setRelatedVideos(relatedVideos!!, mContext)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(mContext, "Something went wrong Please try again", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(mContext, "Something went wrong Please try again", Toast.LENGTH_LONG).show()
        }
    }

    private fun onRelatedVideoSelected(relatedVideoModel: RelatedVideoModel, position: Int) {
//        mMediaPlayer!!.reset()
        videoError = true

        rvRelatedVideos.visibility = View.GONE
        progressVisible()
        //MowUtils.showProgress(this, false)
//        playVideo(relatedVideoModel.title, relatedVideoModel.file)

        relatedVideoId = relatedVideoModel.id
        marketPlaceAdsArray = ArrayList()
        for (marketplace in relatedVideoModel.ads.marketplace) {
            (marketPlaceAdsArray as ArrayList<Marketplace>).add(marketplace)
        }
    }

    override fun onRelatedVideosFailure() {
        Toast.makeText(mContext, "Something went wrong Please try again", Toast.LENGTH_LONG).show()
    }

    private fun setRelatedVideos(relatedVideos: List<RelatedVideoModel>, context: Context) {
        //Show Related Video

        val playlistAdapter = PlaylistAdapter(mContext, relatedVideos)
        videoController!!.mPauseButton.visibility = View.GONE
        rvRelatedVideos.adapter = playlistAdapter

        playlistAdapter.setOnItemClickListener(object : PlaylistAdapter.onRecyclerViewItemClickListener {
            override fun onItemClickListener(view: View, position: Int) {
                rvRelatedVideos.visibility = View.GONE

                onRelatedVideoSelected(relatedVideos[position], position)
            }
        })
    }

    private fun trackProgress() {
        if (!mIsComplete) {
            timer.cancel()
            timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    if (videoController != null) {
                        currentMOWAds = currentMedia.ads!!.getAdsFor((videoView!!.currentPosition / 1000).toString(), "m")

                        if (currentMOWAds != null) {
                            setupIMAAds()
                            requestIMAAds(currentMOWAds!!.url, currentMOWAds!!.loadFrom)
                        }
                    }
                }
            }, 500, 1000)
        }
    }

    private fun trackMidRollAdTester() {
        var adTimings = videoView!!.duration.toLong()

        adTimings /= 2

        val totalDuration = videoView!!.duration.toLong()
        val currentDuration = videoView!!.currentPosition.toLong()

        if (currentDuration > totalDuration / 2) {
            videoController!!.doPauseResume()
//            initAds()
//            requestAds(intent.getStringExtra(MowConstants.AD_URL), false)
            timer.cancel()
        }
    }

    private fun checkMarketPlaceAd() {
        if (marketPlaceAdsArray!!.size > 0) {
            if (marketPlaceAdsArray!![0].time == -1) {
                timer.cancel()
            } else {
                for (i in 0 until marketPlaceAdsArray!!.size) {
                    val adTimeInMillis: Long = 1000 * (marketPlaceAdsArray!![i].time.toLong())
                    val currentMediaTimeInMillis: Long = videoView!!.currentPosition.toLong()

                    if (currentMediaTimeInMillis > (adTimeInMillis - 1000) && currentMediaTimeInMillis < (adTimeInMillis + 1000)) {
                        currentMarketPlaceAd = marketPlaceAdsArray!![i]
                        trackAndPlayVideoAds(false)
                        return
                    }
                }
            }
        }
    }

    private fun checkPublisherAd() {
        if (publisherAdsArray!!.size > 0) {
            if (publisherAdsArray!![0].time == -1) {
                timer.cancel()
            } else {
                for (i in 0 until publisherAdsArray!!.size) {
                    val adTimeInMillis: Long = 1000 * (publisherAdsArray!![i].time.toLong())
                    val currentMediaTimeInMillis: Long = videoView!!.currentPosition.toLong()

                    if (currentMediaTimeInMillis > (adTimeInMillis - 1000) && currentMediaTimeInMillis < (adTimeInMillis + 1000)) {
                        currentPublisherAd = publisherAdsArray!![i]
                        trackAndPlayVideoAds(true)
                        return
                    }
                }
            }
        }
    }

    private fun trackAndPlayVideoAds(isPublisher: Boolean) {
//        val randomNumber = Random().nextInt()
//
//        if (isPublisher) {
//            val stringHashMap = HashMap<String, String>()
//            stringHashMap["id"] = currentPublisherAd!!.id.toString()
//            stringHashMap["data2"] = "publisher"
//            stringHashMap["ad_from"] = "p"
//            stringHashMap["ad_priority"] = randomNumber.toString()
//
//            trackAds(stringHashMap, authToken, randomNumber)
//            if (currentPublisherAd!!.type == "video") {
////                initAds()
//                requestAds(currentPublisherAd!!.url, isPublisher)
//            } else if (currentPublisherAd!!.type == "banner") {
//                playBannerAds(currentPublisherAd!!.url, isPublisher)
//            }
//        } else {
//            val stringHashMap = HashMap<String, String>()
//            stringHashMap["id"] = currentMarketPlaceAd!!.id.toString()
//            stringHashMap["data2"] = "marketplace"
//            stringHashMap["ad_from"] = "m"
//            stringHashMap["ad_priority"] = randomNumber.toString()
//
//            trackAds(stringHashMap, authToken, randomNumber)
//            if (currentMarketPlaceAd!!.type == "video") {
////                initAds()
//                requestAds(currentMarketPlaceAd!!.url, isPublisher)
//            } else if (currentMarketPlaceAd!!.type == "banner") {
//                playBannerAds(currentMarketPlaceAd!!.url, isPublisher)
//            }
//        }
    }

    private fun requestAds(adTagUrl: String, isPublisher: Boolean) {
        if (isPublisher) {
            publisherAdsArray!!.remove(currentPublisherAd)
            currentMarketPlaceAd = null
        } else {
            marketPlaceAdsArray!!.remove(currentMarketPlaceAd)
            currentPublisherAd = null
        }

        // Create the ads request.
        val request = mSdkFactory!!.createAdsRequest()
        request.adTagUrl = adTagUrl
        request.contentProgressProvider = ContentProgressProvider {
            if (mIsAdDisplayed || videoView == null || videoView!!.duration <= 0) {
                VideoProgressUpdate.VIDEO_TIME_NOT_READY
            } else VideoProgressUpdate(videoView!!.currentPosition.toLong(),
                    videoView!!.duration.toLong())
        }

        // Request the ad. After the ad is loaded, onAdsManagerLoaded() will be called.
        mAdsLoader!!.requestAds(request)

        Handler().postDelayed({
            // This method will be executed once the timer is over
            // Start your app main activity
            if (videoController == null && !onAdEventCalled) {
//                Answers.getInstance().logCustom(CustomEvent("Mow Player Event")
//                        .putCustomAttribute("Adrequest", "Ad request timed out"))
                Toast.makeText(mContext, "Ad request timed out", Toast.LENGTH_SHORT).show()
                playVideo(currentPlayingIndex)
            }
        }, 10000)
    }

    private fun playBannerAds(url: String, isPublisher: Boolean) {

        if (isPublisher) {
            publisherAdsArray!!.remove(currentPublisherAd)
            currentMarketPlaceAd = null
        } else {
            marketPlaceAdsArray!!.remove(currentMarketPlaceAd)
            currentPublisherAd = null
        }

        timer.cancel()
        val adRequest = AdRequest.Builder().setContentUrl(url).build()
        bannerAdView.loadAd(adRequest)

//        Answers.getInstance().logCustom(CustomEvent("Mow Player Event")
//                .putCustomAttribute("playBannerAds", "banner ad error"))


        if (!isAdTester) {
            trackProgress()
        }

        bannerAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                bannerAdView.visibility = View.VISIBLE
                timer.schedule(object : TimerTask() {
                    override fun run() {
//                        runOnUiThread {
//                            bannerAdView.visibility = View.GONE
//                        }
                    }
                }, 5000)
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                bannerAdView.visibility = View.GONE
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            override fun onAdClicked() {
                bannerAdView.visibility = View.GONE
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        }

    }

    private fun playOtherAdIfFails() {
        var foundAd = false

//        if (currentPublisherAd != null) {
//            for (i in 0 until marketPlaceAdsArray!!.size) {
//                if (currentPublisherAd!!.time == marketPlaceAdsArray!![i].time) {
//                    foundAd = true
//                    currentMarketPlaceAd = marketPlaceAdsArray!![i]
//                    trackAndPlayVideoAds(false)
//                }
//            }
//        } else {
//            for (i in 0 until publisherAdsArray!!.size) {
//                if (currentMarketPlaceAd!!.time == publisherAdsArray!![i].time) {
//                    foundAd = true
//                    currentPublisherAd = publisherAdsArray!![i]
//                    trackAndPlayVideoAds(true)
//                }
//            }
//        }

        currentMOWAds = if (currentMedia.ads == null) {
            if (mowConfig.ads != null) {
                mowConfig.ads!!.getAdsFor("0", "m")
            } else {
                null
            }
        } else {
            currentMedia.ads!!.getAdsFor("0", "m")
        }

        if (currentMOWAds != null) {
            foundAd = true
        }

        if (!foundAd) {
            if (!isAdTester) {
                trackProgress()
            }
            if (videoController == null) {
                progressVisible()
                playVideo(currentPlayingIndex)
            } else {
                videoController!!.doPauseResume()
            }
        } else {
            setupIMAAds()
            requestIMAAds(currentMOWAds!!.url, currentMOWAds!!.loadFrom)
        }
    }

//    private fun trackAds(trackingParameterModel: HashMap<String, String>, authorizzation: String?, randomNumber: Int) {
//        MowApiMethods.trackAds(mTrackAdsCallback, mMowApiInterface, authorizzation!!,
//                currentMarketPlaceAd!!.id.toString(), randomNumber.toString(), code, trackingParameterModel)
//    }

    override fun onTrackAdResponse(response: Response<ResponseBody>) {}

    override fun onTrackAdFailure() {}

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return false
    }

    override fun setFullScreen(fullScreen: Boolean) {
        val screenOrientation = if (fullScreen)
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        else
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setFullscreen(fullScreen, screenOrientation)
    }

    override fun playList() {
        if (rvPlayList.visibility == View.VISIBLE) {
            rvPlayList.visibility = View.GONE
        } else {
            rvPlayList.visibility = View.VISIBLE
        }
    }

    override fun playNext() {
        if (currentPlayingIndex < (mediaList!!.size - 1)) {
            currentPlayingIndex += 1
            playVideo(currentPlayingIndex)
        } else {
            currentPlayingIndex = 0
            playVideo(currentPlayingIndex)
        }
    }

    override fun playPrevious() {
        if (currentPlayingIndex > 0) {
            currentPlayingIndex -= 1
            playVideo(currentPlayingIndex)
        } else {
            currentPlayingIndex = mediaList!!.size - 1
            playVideo(currentPlayingIndex)
        }
    }

    override fun go10SecForward() {
        videoController!!.seekForward(videoView)
    }

    override fun go10SecPrevious() {
        videoController!!.seekBackward(videoView)
    }

    override fun onAudioClickListener(media: Media?, position: Int) {
        currentPlayingIndex = position
        playVideo(position)
        rvPlayList.visibility = View.GONE
    }

    override fun setFullscreen(fullscreen: Boolean, screenOrientation: Int) {
        // Activity Need to be set to: android:configChanges="keyboardHidden|orientation|screenSize"
//        isLandscape = fullscreen
//        if (fullscreen) {
//            if (mVideoViewLayoutWidth == 0 && mVideoViewLayoutHeight == 0) {
//                val params = videoContainer!!.layoutParams
//                mVideoViewLayoutWidth = params.width//Save parameters before full screen
//                mVideoViewLayoutHeight = params.height
//            }
//        } else {
//            val params = videoContainer!!.layoutParams
//            params.width = mVideoViewLayoutWidth//Parameters before using full screen
//            params.height = mVideoViewLayoutHeight
//            videoContainer!!.layoutParams = params
//        }
//            requestedOrientation = resources.configuration.orientation
//        videoController!!.toggleFullScreen()
//        onConfigurationChanged(resources.configuration)
    }

//    override fun onConfigurationChanged(newConfig: Configuration) {
//        super.onConfigurationChanged(newConfig)
//        if (mVideoWidth > 0 && mVideoHeight > 0) videoView.adjustSize(getDeviceWidth(mContext), getDeviceHeight(mContext), videoView.width, videoView.height)

//        if (mVideoWidth > 0 && mVideoHeight > 0) {
//            isLandscape = if (isLandscape) {  //if isLandscape true
//                controller!!.fullScreenIcon(false)
//                // videoView!!.adjustSize(getDeviceWidth(this), getDeviceHeight(this), videoView!!.width, videoView!!.height)
////                videoPlayerWithAdPlayback!!.adjustSize(getDeviceWidth(mContext), getDeviceHeight(mContext), llVideoArticleContainer!!.width, llVideoArticleContainer!!.height)
//
//                false
//            } else { //false
//                controller!!.fullScreenIcon(true)
//                // videoView!!.adjustSize(getDeviceWidth(this), getDeviceHeight(this), getDeviceWidth(this), getDeviceHeight(this))
////                videoPlayerWithAdPlayback!!.adjustSize(getDeviceWidth(mContext), getDeviceHeight(mContext), getDeviceWidth(mContext), getDeviceHeight(mContext))
//                true
//            }
//        } else {
//            Log.d("check else", "onConfiguration")
//            if (isLandscape) {  //if isLandscape true
////                videoPlayerWithAdPlayback!!.adjustSize(getDeviceWidth(mContext), getDeviceHeight(mContext), llVideoArticleContainer!!.width, llVideoArticleContainer!!.height)
//                controller!!.fullScreenIcon(false)
//            } else { //false
////                videoPlayerWithAdPlayback!!.adjustSize(getDeviceWidth(mContext), getDeviceHeight(mContext), getDeviceWidth(mContext), getDeviceHeight(mContext))
//                controller!!.fullScreenIcon(true)
//            }
//        }
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            llVideoArticleContainer.orientation = LinearLayout.HORIZONTAL
//
//            llArticleView.layoutParams = paramArticle
//            videoView.layoutParams = param
//
////            videoPlayerWithAdPlayback!!.adjustSize(getDeviceWidth(mContext), getDeviceHeight(mContext), getDeviceWidth(mContext), getDeviceHeight(mContext))
//            controller!!.fullScreenIcon(true)
//            isLandscape = true
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            llVideoArticleContainer.orientation = LinearLayout.VERTICAL
//
//            llArticleView.layoutParams = paramArticle
//            videoView.layoutParams = param
//
////            videoPlayerWithAdPlayback!!.adjustSize(getDeviceWidth(mContext), getDeviceHeight(mContext), llVideoArticleContainer!!.width, llVideoArticleContainer!!.height)
//            controller!!.fullScreenIcon(false)
//            isLandscape = false
//        }

//        (videoPlayerWithAdPlayback.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio = mowConfig.ratio

//        resetVideoContainerRatio(mowConfig.ratio)
//
//        val videoParams = videoView.layoutParams
//        videoParams.height = getDeviceHeight(mContext)
//        videoParams.width = getDeviceWidth(mContext)
//        constraintVideoContainer.layoutParams = videoParams
//
//        val params = llVideoArticleContainer.layoutParams
//        params.height = getDeviceHeight(mContext)
//        params.width = getDeviceWidth(mContext)
//        llVideoArticleContainer.layoutParams = params
//
//        val paramArticle = LinearLayout.LayoutParams(
//                LayoutParams.MATCH_PARENT,
//                LayoutParams.MATCH_PARENT,
//                0.6f
//        )
//        val param = LinearLayout.LayoutParams(
//                LayoutParams.MATCH_PARENT,
//                LayoutParams.MATCH_PARENT,
//                0.4f
//        )
//
//        llArticleView.layoutParams = paramArticle
//        constraintVideo.layoutParams = param
//
//        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            llVideoArticleContainer.orientation = LinearLayout.VERTICAL
//
//            if (isFullscreen) {
////                val params = llVideoArticleContainer.layoutParams
////                params.height = getDeviceHeight(mContext)
////                params.width = getDeviceWidth(mContext)
////                llVideoArticleContainer.layoutParams = params
//            } else {
//
//            }
//        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            llVideoArticleContainer.orientation = LinearLayout.HORIZONTAL
//
//        }
//    }

    override fun toggleFullScreen() {
//        isFullscreen = mIsFullScreen
//        videoController!!.toggleFullScreen()
        if (isFullScreen) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            Handler().postDelayed({
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            }, 2000)
        } else {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            Handler().postDelayed({
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            }, 2000)
        }
    }

    fun resetPlayer() {
//        if (mMediaPlayer != null) {
//            mMediaPlayer.reset()
//            mMediaPlayer.release()
//            mMediaPlayer = null
//        }
    }

    /**
     * Implement VideoMediaController.MediaPlayerControl
     */
    override fun getBufferPercentage(): Int {
        return 0
    }

    override fun getCurrentPosition(): Int {
        return if (null != videoView)
            videoView!!.currentPosition
        else
            0
    }

    override fun getDuration(): Int {
        return if (null != videoView)
            videoView!!.duration
        else
            0
    }

    override fun isPlaying(): Boolean {
        return if (null != videoView)
            videoView!!.isPlaying
        else
            false
    }

    override fun isComplete(): Boolean {
        return mIsComplete
    }

    override fun pause() {
        if (null != videoView) {
            videoView!!.pause()
        }
    }

    override fun seekTo(i: Int) {
        if (null != videoView) {
            videoView!!.seekTo(i)
        }
    }

    override fun start() {
        if (null != videoView) {
            videoView!!.start()
            mIsComplete = false
        }
    }

    override fun isFullScreen(): Boolean {
        return activity.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    }

    override fun exit() {
        resetPlayer()
//        finish()
    }

    fun onActivityPause() {
        Log.d(TAG, "PAUSE")
        if (mAdsManager != null && mIsAdDisplayed) {
            mAdsManager!!.pause()
        } else {
            if (videoController != null) {
                if (videoController!!.mMediaPlayerControlListener.isPlaying) {
                    mSeekPosition = mMediaPlayer!!.currentPosition
//                    surfaceHolder = videoView!!.holder
//                    surfaceHolder!!.addCallback(this)
//                    surfaceHolder!!.setFormat(PixelFormat.TRANSPARENT)
//                    surfaceHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
                    isPaused = true
                    mMediaPlayer.pause()
                }
            }
        }
    }

    fun resume() {
        Log.d(TAG, "onResume called")
        if (mAdsManager != null && mIsAdDisplayed) {
            mAdsManager!!.resume()
        }
//        if (videoError != null) {
//            try {
//                if (isPaused) {
//                    videoPlayerWithAdPlayback!!.visibility = View.VISIBLE
//                    mMediaPlayer!!.seekTo(mSeekPosition)
//                    mMediaPlayer.start()
//
////                    surfaceHolder = videoView!!.holder
////                    surfaceHolder!!.addCallback(this)
////                    surfaceHolder!!.setFormat(PixelFormat.TRANSPARENT)
////                    surfaceHolder!!.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
//
//                    Log.d("RESUME-POS", mSeekPosition.toString())
//                    // mMediaPlayer!!.start()
//                    isPaused = false
//                } else {
//                    mMediaPlayer!!.start()
//                }
//
//            } catch (e: Exception) {
//                Log.d(TAG, "onResume VideoView Exception" + e.localizedMessage)
//            }
//        }
    }

    /*override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        // Store current position.
        Log.d(TAG, "onSaveInstanceState Position=" + mMediaPlayer!!.currentPosition)
        outState!!.putInt(SEEK_POSITION_KEY, mSeekPosition)

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        // Get saved position.
        mSeekPosition = savedInstanceState!!.getInt(SEEK_POSITION_KEY)
        mMediaPlayer!!.seekTo(mSeekPosition)

    }*/

//    override fun onBackPressed() {
//
//        if (isFull) {
//
//            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//
//        } else {
//            super.onBackPressed()
//
//        }
//
//    }

    /**
     * This function is use for set the light theme of player
     */
    private fun setLightTheme() {
//        llMainBackground.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_light))

//        tvDescription.setTextColor(ContextCompat.getColor(context, R.color.text_dark))
//        tvAdsDuration.setTextColor(ContextCompat.getColor(context, R.color.text_dark))
//        tvAdsCurrentTime.setTextColor(ContextCompat.getColor(context, R.color.text_dark))
//        tvDuration.setTextColor(ContextCompat.getColor(context, R.color.text_dark))
//        tvCurrentTime.setTextColor(ContextCompat.getColor(context, R.color.text_dark))
//        ivShuffle.setColorFilter(ContextCompat.getColor(context, R.color.text_dark))
//        iv10SecPrevious.setColorFilter(ContextCompat.getColor(context, R.color.text_dark))
//        ivPrevious.setColorFilter(ContextCompat.getColor(context, R.color.text_dark))
//        ivPlayPause.setColorFilter(ContextCompat.getColor(context, R.color.text_dark))
//        ivNext.setColorFilter(ContextCompat.getColor(context, R.color.text_dark))
//        iv10SecForward.setColorFilter(ContextCompat.getColor(context, R.color.text_dark))
//        ivSetting.setColorFilter(ContextCompat.getColor(context, R.color.text_dark))
//        ivMute.setColorFilter(ContextCompat.getColor(context, R.color.text_dark))

//        tvTrack.setTextColor(ContextCompat.getColor(context, R.color.text_dark))
//        tvDurations.setTextColor(ContextCompat.getColor(context, R.color.text_dark))

        rvPlayList.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_light))
    }

    /**
     * This function is use for set the dark theme of player
     */
    private fun setDarkTheme() {
//        llMainBackground.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_dark))

//        tvDescription.setTextColor(ContextCompat.getColor(context, R.color.text_white))
//        tvAdsDuration.setTextColor(ContextCompat.getColor(context, R.color.text_white))
//        tvAdsCurrentTime.setTextColor(ContextCompat.getColor(context, R.color.text_white))
//        tvDuration.setTextColor(ContextCompat.getColor(context, R.color.text_white))
//        tvCurrentTime.setTextColor(ContextCompat.getColor(context, R.color.text_white))
//        ivShuffle.setColorFilter(ContextCompat.getColor(context, R.color.text_white))
//        iv10SecPrevious.setColorFilter(ContextCompat.getColor(context, R.color.text_white))
//        ivPrevious.setColorFilter(ContextCompat.getColor(context, R.color.text_white))
//        ivPlayPause.setColorFilter(ContextCompat.getColor(context, R.color.text_white))
//        ivNext.setColorFilter(ContextCompat.getColor(context, R.color.text_white))
//        iv10SecForward.setColorFilter(ContextCompat.getColor(context, R.color.text_white))
//        ivSetting.setColorFilter(ContextCompat.getColor(context, R.color.text_white))
//        ivMute.setColorFilter(ContextCompat.getColor(context, R.color.text_white))

//        tvTrack.setTextColor(ContextCompat.getColor(context, R.color.text_white))
//        tvDurations.setTextColor(ContextCompat.getColor(context, R.color.text_white))

        rvPlayList.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_dark))
    }

    private fun progressVisible() {
        progressBar.visibility = View.VISIBLE
        if (!videoView.isPlaying)
            ivThumbnail.visibility = View.VISIBLE
    }

    private fun progressHide() {
        progressBar.visibility = View.GONE
        ivThumbnail.visibility = View.GONE
    }

    // IMA Ads
    private fun requestIMAAds(adTagUrl: String, isPublisher: String) {
        currentMOWAds!!.isUsedOrExpired = true
        // Create the ads request.
        val request = mSdkFactory!!.createAdsRequest()
        request.adTagUrl = adTagUrl
        // Request the ad. After the ad is loaded, onAdsManagerLoaded() will be called.
        mAdsLoader!!.requestAds(request)
    }

//    fun showIMAAds() {
////        isAdsShowing = true
//        onConfigurationChanged(resources.configuration)
//
//        TrackerManager.updateCurrentAds(currentMOWAds!!)
//        TrackerManager.trackAdRequest(currentMOWAds!!.id, "marketplace", "m")
////        llAdsViewLiveAudio.visibility = View.VISIBLE
////
////        val params = llAdsViewLiveAudio.layoutParams
////        params.width = 1
////        params.height = 1
////        llAdsViewLiveAudio.layoutParams = params
////
//
////        if (!firstTime)
////            TrackerManager.stopTracking()
//    }

    override fun onVideoSizeChanged(mp: MediaPlayer?, width: Int, height: Int) {
//        resetVideoContainerRatio(mowConfig.ratio)
//        mVideoHeight = mp!!.videoHeight
//        mVideoWidth = mp.videoWidth
//        if (mVideoHeight > 0 && mVideoWidth > 0) videoView.adjustSize(constraintVideo.width, constraintVideo.height, mMediaPlayer!!.videoWidth, mMediaPlayer.videoHeight)
    }
}

