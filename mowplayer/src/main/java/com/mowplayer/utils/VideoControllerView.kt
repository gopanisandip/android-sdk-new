package com.mowplayer.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.*
import android.view.View.OnClickListener
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.mowplayer.R
import com.mowplayer.video.controller.OrientationDetector
import com.mowplayer.video.controller.VideoGestureListener
import com.mowplayer.video.controller.ViewAnimator
import com.mowplayer.video.controller.ViewAnimator.Listeners
import com.mowplayer.video.controller.ViewGestureListener
import com.mowplayer.video.controller.ViewGestureListener.Companion.SWIPE_LEFT
import kotlinx.android.synthetic.main.video_controller_view.view.*
import java.lang.ref.WeakReference
import java.util.*

class VideoControllerView(builder: Builder) : FrameLayout(builder.context!!), VideoGestureListener {

    private var isPause = false
    private val seekForwardTime = 10000
    private val seekBackwardTime = 10000
    private var mRootView // root view of this
            : View? = null
    var mSeekBar //seek bar for video
            : SeekBar? = null

    /**
     * if [VideoControllerView] is visible
     *
     * @return showing or not
     */
    var isShowing //controller view showing
            = false
        private set
    private var mIsDragging //is dragging seekBar
            = false
    private var mFormatBuilder: StringBuilder? = null
    private var mFormatter: Formatter? = null
    private var mGestureDetector //gesture detector
            : GestureDetector? = null
    var mContext: Context?
    private val mCanSeekVideo: Boolean
    private val mCanControlVolume: Boolean
    private val mCanControlBrightness: Boolean
    private val mVideoTitle: String
    var mMediaPlayerControlListener: MediaPlayerControlListener
    private var mAnchorView: ViewGroup? = null
    private val mSurfaceView: SurfaceView?
    private var isMute = true

    @DrawableRes
    private val mPauseIcon: Int

    @DrawableRes
    private val mPlayIcon: Int

    @DrawableRes
    private val mShrinkIcon: Int

    @DrawableRes
    private val mStretchIcon: Int

    //top layout
    private var mTopLayout: View? = null
    private var mBackButton: ImageButton? = null

    //center layout
    private var mCenterImage: ImageView? = null
    private var mCenterProgress: ProgressBar? = null
    private var mCurBrightness = -1f
    private var mCurVolume = -1
    private var mAudioManager: AudioManager? = null
    private var mMaxVolume = 0

    //bottom layout
    private var mBottomLayout: View? = null
    var mPauseButton: ImageView? = null
    var ivPlayList: ImageView? = null
    var ivNext: ImageView? = null
    var ivPrevious: ImageView? = null
    var iv10SecForward: ImageView? = null
    var iv10SecPrevious: ImageView? = null
    var seekBarAudio: SeekBar? = null
    var llLive: LinearLayout? = null
    private val mIsFullScreen = false
//    private val handler: Handler = Handler()
    private val mHandler: Handler = ControllerViewHandler(this)
    private val mOrientationDetector: OrientationDetector? = null
    private var volume = 0

    init {
        mContext = builder.context
        mMediaPlayerControlListener = builder.mediaPlayerControlListener!!
        mVideoTitle = builder.videoTitle
        mCanSeekVideo = builder.canSeekVideo
        mCanControlVolume = builder.canControlVolume
        mCanControlBrightness = builder.canControlBrightness
        mPauseIcon = builder.pauseIcon
        mPlayIcon = builder.playIcon
        mStretchIcon = builder.stretchIcon
        mShrinkIcon = builder.shrinkIcon
        mSurfaceView = builder.surfaceView
        setAnchorView(builder.anchorView)
        mSurfaceView!!.setOnTouchListener { v, event ->
            toggleControllerView()
            false
        }
    }

    class Builder(var context: Context?, var mediaPlayerControlListener: MediaPlayerControlListener?) {
        var canSeekVideo = true
        var canControlVolume = true
        var canControlBrightness = true
        var videoTitle = ""
        var anchorView: ViewGroup? = null
        var surfaceView: SurfaceView? = null

        @DrawableRes
        var pauseIcon = R.drawable.ic_pause

        @DrawableRes
        var playIcon = R.drawable.ic_play

        @DrawableRes
        var shrinkIcon = R.drawable.ic_media_fullscreen_shrink

        @DrawableRes
        var stretchIcon = R.drawable.ic_maximize
        fun with(context: Activity?): Builder {
            this.context = context
            return this
        }

        fun withMediaControlListener(mediaControlListener: MediaPlayerControlListener?): Builder {
            mediaPlayerControlListener = mediaControlListener
            return this
        }

        //Options
        fun withVideoTitle(videoTitle: String): Builder {
            this.videoTitle = videoTitle
            return this
        }

        fun withVideoSurfaceView(surfaceView: SurfaceView?): Builder {
            this.surfaceView = surfaceView
            return this
        }

        //        public Builder exitIcon(@DrawableRes int exitIcon) {
        //            this.exitIcon = exitIcon;
        //            return this;
        //        }
        fun pauseIcon(@DrawableRes pauseIcon: Int): Builder {
            this.pauseIcon = pauseIcon
            return this
        }

        fun playIcon(@DrawableRes playIcon: Int): Builder {
            this.playIcon = playIcon
            return this
        }

        fun shrinkIcon(@DrawableRes shrinkIcon: Int): Builder {
            this.shrinkIcon = shrinkIcon
            return this
        }

        fun stretchIcon(@DrawableRes stretchIcon: Int): Builder {
            this.stretchIcon = stretchIcon
            return this
        }

        fun canSeekVideo(canSeekVideo: Boolean): Builder {
            this.canSeekVideo = canSeekVideo
            return this
        }

        fun canControlVolume(canControlVolume: Boolean): Builder {
            this.canControlVolume = canControlVolume
            return this
        }

        fun canControlBrightness(canControlBrightness: Boolean): Builder {
            this.canControlBrightness = canControlBrightness
            return this
        }

        fun build(anchorView: ViewGroup?): VideoControllerView {
            this.anchorView = anchorView
            return VideoControllerView(this)
        }
    }

    /**
     * Handler prevent leak memory.
     */
    private class ControllerViewHandler internal constructor(view: VideoControllerView) : Handler() {
        private val mView: WeakReference<VideoControllerView>
        override fun handleMessage(msg: Message) {
            var msg = msg
            val view = mView.get()
            if (view == null || view.mMediaPlayerControlListener == null) {
                return
            }
            val pos: Int
            when (msg.what) {
                HANDLER_ANIMATE_OUT -> view.hide()
                HANDLER_UPDATE_PROGRESS -> {
                    //                    if (!MowPlayerActivity.Companion.getMIsComplete()) {
                    pos = view.setSeekProgress("HANDLER_UPDATE_PROGRESS")
                    if (!view.mIsDragging && view.isShowing && view.mMediaPlayerControlListener!!.isPlaying) { //just in case
                        //cycle update
                        msg = obtainMessage(HANDLER_UPDATE_PROGRESS)
                        sendMessageDelayed(msg, 1000 - (pos % 1000).toLong())
                    }
                }
                SHOW_COMPLETE -> {
                }
            }
        }

        init {
            mView = WeakReference(view)
        }
    }

    /**
     * Inflate view from exit xml layout
     *
     * @return the root view of [VideoControllerView]
     */
    private fun makeControllerView(): View? {
        val inflate = mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mRootView = inflate.inflate(R.layout.video_controller_view, null)
        initControllerView()

        // showLogo("https://mowplayer.nyc3.digitaloceanspaces.com/thumbnails/9tOTugMQZEkIvjON.png");
        return mRootView
    }

    /**
     * find all views inside [VideoControllerView]
     * and init params
     */
    private fun initControllerView() {
        // Share
        ivShare.setOnClickListener {
            shareDialog("")
        }

        // Setup Sound
        ivMute.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_volume_on))
        ivMute.setOnClickListener(OnClickListener { view: View? ->
            if (isMute) {
                volume = mAudioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
                mAudioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
                ivMute.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_volume_mute))
                isMute = false
            } else {
                mAudioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
                ivMute.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_volume_on))
                isMute = true
            }
        })

        // Full screen
        ivFullScreen.requestFocus()
        ivFullScreen.setOnClickListener(mFullscreenListener)

        //top layout
        mTopLayout = mRootView!!.findViewById(R.id.layoutTop)
        mBackButton = mRootView!!.findViewById(R.id.ivBack)

        ivPlayerLogo.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mowplayer.com"))
            context.startActivity(browserIntent)
        }

//        mBackButton.setImageResource(mExitIcon);
        if (mBackButton != null) {
            mBackButton!!.requestFocus()
            mBackButton!!.setOnClickListener(mBackListener)
        }

        // Center Layout
        layoutCenter.visibility = GONE
        mCenterImage = mRootView!!.findViewById(R.id.image_center_bg)
        mCenterProgress = mRootView!!.findViewById(R.id.progress_center)

        //bottom layout
        mBottomLayout = mRootView!!.findViewById(R.id.layoutBottom)
        mPauseButton = mRootView!!.findViewById(R.id.ivPausePlay)
        if (mPauseButton != null) {
            mPauseButton!!.requestFocus()
            mPauseButton!!.setOnClickListener(mPauseListener)
        }
        ivPlayList = mRootView!!.findViewById(R.id.ivPlayList)
        if (ivPlayList != null) {
            ivPlayList!!.requestFocus()
            ivPlayList!!.setOnClickListener(mPlayListListener)
        }
        ivNext = mRootView!!.findViewById(R.id.ivNext)
        if (ivNext != null) {
            ivNext!!.requestFocus()
            ivNext!!.setOnClickListener(mPlayNextListener)
        }
        ivPrevious = mRootView!!.findViewById(R.id.ivPrevious)
        if (ivPrevious != null) {
            ivPrevious!!.requestFocus()
            ivPrevious!!.setOnClickListener(mPlayPreviousListener)
        }
        iv10SecForward = mRootView!!.findViewById(R.id.iv10SecForward)
        if (iv10SecForward != null) {
            iv10SecForward!!.requestFocus()
            iv10SecForward!!.setOnClickListener(go10SecForwardListener)
        }
        seekBarAudio = mRootView!!.findViewById(R.id.seekBarAudio)
        iv10SecPrevious = mRootView!!.findViewById(R.id.iv10SecPrevious)
        if (iv10SecPrevious != null) {
            iv10SecPrevious!!.requestFocus()
            iv10SecPrevious!!.setOnClickListener(go10SecPreviousListener)
        }
        mSeekBar = mRootView!!.findViewById(R.id.seekBar)
        if (mSeekBar != null) {
            mSeekBar!!.setOnSeekBarChangeListener(mSeekListener)
            mSeekBar!!.max = 1000
        }
        llLive = mRootView!!.findViewById(R.id.llLive)
        mEndTime = mRootView!!.findViewById(R.id.tvTotalTime)
        mCurrentTime = mRootView!!.findViewById(R.id.tvCurrentTime)

        //init formatter
        mFormatBuilder = StringBuilder()
        mFormatter = Formatter(mFormatBuilder, Locale.getDefault())
    }

    fun visibleNextPrevious() {
        ivPlayList!!.visibility = VISIBLE
        ivNext!!.visibility = VISIBLE
        ivPrevious!!.visibility = VISIBLE
    }

    fun hideNextPrevious() {
        ivPlayList!!.visibility = GONE
        ivNext!!.visibility = GONE
        ivPrevious!!.visibility = GONE
    }

    fun visibleForwardBackward() {
        iv10SecForward!!.visibility = VISIBLE
        iv10SecPrevious!!.visibility = VISIBLE
    }

    fun hideForwardBackward() {
        iv10SecForward!!.visibility = GONE
        iv10SecPrevious!!.visibility = GONE
    }

    fun visibleLiveText() {
        llLive!!.visibility = VISIBLE
        mCurrentTime!!.visibility = GONE
        tvTimeSeparator!!.visibility = GONE
        mEndTime!!.visibility = GONE
    }

    fun hideLiveText() {
        llLive!!.visibility = GONE
        mCurrentTime!!.visibility = VISIBLE
        tvTimeSeparator!!.visibility = VISIBLE
        mEndTime!!.visibility = VISIBLE
    }

    fun playerType(code: String): PlayerType {
        val playerType: PlayerType
        return if (code.startsWith("als-")) {
            playerType = PlayerType.Live
            setupView(playerType)
            PlayerType.Live
        } else if (code.startsWith("a-")) {
            playerType = PlayerType.Single
            setupView(playerType)
            PlayerType.Single
        } else if (code.startsWith("v-")) {
            playerType = PlayerType.Single
            setupView(playerType)
            PlayerType.Single
        } else if (code.startsWith("apl-")) {
            playerType = PlayerType.Playlist
            setupView(playerType)
            PlayerType.Playlist
        } else if (code.startsWith("p-")) {
            playerType = PlayerType.Playlist
            setupView(playerType)
            PlayerType.Playlist
        } else if (code.startsWith("pst-")) {
            playerType = PlayerType.PlaylistPosition
            setupView(playerType)
            PlayerType.PlaylistPosition
        } else if (code.startsWith("nv-")) {
            playerType = PlayerType.Article
            setupView(playerType)
            PlayerType.Article
        } else if (code.startsWith("ar-")) {
            playerType = PlayerType.Single
            setupView(playerType)
            PlayerType.Single
        } else if (code.startsWith("vls-")) {
            playerType = PlayerType.Live
            setupView(playerType)
            PlayerType.Live
        } else {
            playerType = PlayerType.Unknown
            setupView(playerType)
            PlayerType.Unknown
        }
    }

    fun setupView(playerType: PlayerType) {
        if (playerType === PlayerType.Live) {
            hideForwardBackward()
            hideNextPrevious()
            visibleLiveText()
            mSeekBar!!.visibility = GONE
            seekBarAudio!!.visibility = VISIBLE
        } else if (playerType === PlayerType.Single) {
            hideNextPrevious()
            visibleForwardBackward()
            hideLiveText()
            mSeekBar!!.visibility = VISIBLE
            seekBarAudio!!.visibility = GONE
        } else if (playerType === PlayerType.Playlist || playerType === PlayerType.Article) {
            visibleNextPrevious()
            visibleForwardBackward()
            hideLiveText()
            mSeekBar!!.visibility = VISIBLE
            seekBarAudio!!.visibility = GONE
        } else if (playerType === PlayerType.PlaylistPosition) {
            hideNextPrevious()
            visibleForwardBackward()
            hideLiveText()
            mSeekBar!!.visibility = VISIBLE
            seekBarAudio!!.visibility = GONE
        }
    }

    fun show() {
        if (!isShowing && mAnchorView != null) {
            //add controller view to bottom of the AnchorView
            val tlp = LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            mAnchorView!!.addView(this@VideoControllerView, mSurfaceView!!.width, mSurfaceView.height)
//            ViewAnimator.putOn(mTopLayout)
//                    .waitForSize { viewAnimator ->
//                        viewAnimator.animate()
//                                .translationY(-mTopLayout!!.height.toFloat(), 0f)
//                                .duration(ANIMATE_TIME)
//                                .andAnimate(mBottomLayout)
//                                .translationY(mBottomLayout!!.height.toFloat(), 0f)
//                                .duration(ANIMATE_TIME)
//                                .start(object : Listeners.Start {
//                                    override fun onStart() {
//                                        isShowing = true
//                                        mHandler.sendEmptyMessage(HANDLER_UPDATE_PROGRESS)
//                                    }
//                                })
//                    }
            isShowing = true
        }

//        setSeekProgress();
        if (mPauseButton != null) {
            mPauseButton!!.requestFocus()
        }
        togglePausePlay()
        toggleFullScreen()
        mHandler.sendEmptyMessage(HANDLER_UPDATE_PROGRESS)
    }

    /**
     * toggle [VideoControllerView] show or not
     * this can be called when [View.onTouchEvent] happened
     */
    fun toggleControllerView() {
        if (!isShowing) {
            show()
        } else {
            //animate out controller view
            val msg = mHandler.obtainMessage(HANDLER_ANIMATE_OUT)
            //remove exist one first
            mHandler.removeMessages(HANDLER_ANIMATE_OUT)
            mHandler.sendMessageDelayed(msg, 100)
        }
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, 3000)
    }

    fun togglePauseButtonView(value: Boolean) {
        if (value) {
            mPauseButton!!.visibility = VISIBLE
        } else {
            mPauseButton!!.visibility = GONE
        }
    }

    var runnable = Runnable {
        if (!mIsDragging && isShowing) {
            val msg = mHandler.obtainMessage(HANDLER_ANIMATE_OUT)
            //remove exist one first
            mHandler.removeMessages(HANDLER_ANIMATE_OUT)
            mHandler.sendMessageDelayed(msg, 100)
        }
    }

    /**
     * hide controller view with animation
     * With custom animation
     */
    private fun hide() {
        if (mAnchorView == null) {
            return
        }
//        ViewAnimator.putOn(mTopLayout)
//                .animate()
//                .translationY(-mTopLayout!!.height.toFloat())
//                .duration(ANIMATE_TIME)
//                .andAnimate(mBottomLayout)
//                .translationY(mBottomLayout!!.height.toFloat())
//                .duration(ANIMATE_TIME)
//                .end {
//                    mAnchorView!!.removeView(this@VideoControllerView)
//                    mHandler.removeMessages(HANDLER_UPDATE_PROGRESS)
//                    isShowing = false
//                }
    }

    /**
     * convert string to time
     *
     * @param timeMs time to be formatted
     * @return 00:00:00
     */
    private fun stringToTime(timeMs: Int, method: String): String {
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        mFormatBuilder!!.setLength(0)
        return if (hours > 0) {
            mFormatter!!.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter!!.format("%02d:%02d", minutes, seconds).toString()
        }
    }

    /**
     * set [.mSeekBar] progress
     * and video play time [.mCurrentTime]
     *
     * @return current play position
     */
    private fun setSeekProgress(method: String): Int {
        if (mMediaPlayerControlListener == null || mIsDragging) {
            return 0
        }
        val position = mMediaPlayerControlListener!!.currentPosition
        val duration = mMediaPlayerControlListener!!.duration
        Log.e(TAG, " -> duration:$duration")
        if (mSeekBar != null) {
            if (duration > 0) {
                // use long to avoid overflow
                val pos = 1000L * position / duration
                mSeekBar!!.progress = pos.toInt()
            }
            //get buffer percentage
            val percent = mMediaPlayerControlListener!!.bufferPercentage
            //set buffer progress
            mSeekBar!!.secondaryProgress = percent * 10
        }
        if (mEndTime != null) mEndTime!!.text = stringToTime(duration, method)
        if (mCurrentTime != null) {
            Log.e(TAG, "position:$position -> duration:$duration")
            mCurrentTime!!.text = stringToTime(position, method)
            if (mMediaPlayerControlListener!!.isComplete) {
                mCurrentTime!!.text = stringToTime(duration, method)
            }
        }

        tvTitle!!.text = mVideoTitle
        return position
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                mCurVolume = -1
                mCurBrightness = -1f
                layoutCenter!!.visibility = GONE
                if (mGestureDetector != null) mGestureDetector!!.onTouchEvent(event)
            }
            else -> if (mGestureDetector != null) mGestureDetector!!.onTouchEvent(event)
        }
        return true
    }

    /**
     * toggle pause or play
     */
    fun togglePausePlay() {
        if (mRootView == null || mPauseButton == null || mMediaPlayerControlListener == null) {
            return
        }
        if (mMediaPlayerControlListener!!.isPlaying) {
            isPause = true
            mPauseButton!!.setImageResource(mPauseIcon)
        } else {
            isPause = false
            mPauseButton!!.setImageResource(mPlayIcon)
        }
    }

    fun seekForward(videoView: VideoView) {
        val currentPosition = videoView.currentPosition
        if (currentPosition + seekForwardTime <= videoView.duration) {
            // forward song
            videoView.seekTo(currentPosition + seekForwardTime)
        } else {
            // forward to end position
            videoView.seekTo(videoView.duration)
        }
        videoView.seekTo(videoView.currentPosition + seekForwardTime)
    }

    fun seekBackward(videoView: VideoView) {
        val currentPosition = videoView.currentPosition
        // check if seekBackward time is greater than 0 sec
        if (currentPosition - seekBackwardTime >= 0) {
            // forward song
            videoView.seekTo(currentPosition - seekBackwardTime)
        } else {
            // backward to starting position
            videoView.seekTo(0)
        }
    }

    fun fullScreenIcon(b: Boolean) {
        if (b) { //show shrink icon
            ivFullScreen!!.setImageResource(R.drawable.ic_media_fullscreen_shrink)
        } else {
            ivFullScreen!!.setImageResource(R.drawable.ic_maximize)
        }
    }

    /**
     * toggle full screen or not
     */
    fun toggleFullScreen() {
        if (mRootView == null || ivFullScreen == null || mMediaPlayerControlListener == null) {
            return
        }
        if (mMediaPlayerControlListener!!.isFullScreen) {
            ivFullScreen!!.setImageResource(mShrinkIcon)
        } else {
            ivFullScreen!!.setImageResource(mStretchIcon)
        }
    }

    fun doPauseResume() {
        if (mMediaPlayerControlListener == null) {
            return
        }
        if (mMediaPlayerControlListener!!.isPlaying) {
            mMediaPlayerControlListener!!.pause()
        } else {
            mMediaPlayerControlListener!!.start()
        }
        togglePausePlay()
    }

    //    private void doToggleFullscreen(boolean mIsFullScreen) {
    //        if (mMediaPlayerControlListener == null) {
    //            return;
    //        }
    //
    ////        if (!mIsFullScreen)
    //            mMediaPlayerControlListener.toggleFullScreen(mIsFullScreen);
    //
    //    }
    private fun doToggleFullscreen() {
        if (mMediaPlayerControlListener == null) {
            return
        }
        mMediaPlayerControlListener!!.toggleFullScreen()
    }

    /**
     * Seek bar drag listener
     */
    private val mSeekListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        var newPosition = 0
        var change = false
        override fun onStartTrackingTouch(bar: SeekBar) {
            show()
            mIsDragging = true
            mHandler.removeMessages(HANDLER_UPDATE_PROGRESS)
        }

        override fun onProgressChanged(bar: SeekBar, progress: Int, fromuser: Boolean) {
            if (mMediaPlayerControlListener == null || !fromuser) {
                return
            }
            val duration = mMediaPlayerControlListener!!.duration.toLong()
            val newposition = duration * progress / 1000L
            newPosition = newposition.toInt()
            change = true
            mMediaPlayerControlListener!!.seekTo(newPosition)
            if (mCurrentTime != null) mCurrentTime!!.text = stringToTime(newPosition, "onProgressChanged")
        }

        override fun onStopTrackingTouch(bar: SeekBar) {
            if (mMediaPlayerControlListener == null) {
                return
            }
            if (change) {
                mMediaPlayerControlListener!!.seekTo(newPosition)
                if (mCurrentTime != null) {
                    mCurrentTime!!.text = stringToTime(newPosition, "onStopTrackingTouch")
                }
            }
            mIsDragging = false
            handler.removeCallbacks(runnable)
            handler.postDelayed(runnable, 3000)
            setSeekProgress("onStopTrackingTouch setSeek")
            togglePausePlay()
            show()
            mHandler.sendEmptyMessage(HANDLER_UPDATE_PROGRESS)
        }
    }

    fun showComplete() {
        mHandler.sendEmptyMessage(SHOW_COMPLETE)
    }

    override fun setEnabled(enabled: Boolean) {
        if (mPauseButton != null) {
            mPauseButton!!.isEnabled = enabled
        }
        if (mSeekBar != null) {
            mSeekBar!!.isEnabled = enabled
        }
        super.setEnabled(enabled)
    }

    /**
     * set top back click listener
     */
    private val mBackListener = OnClickListener { mMediaPlayerControlListener!!.exit() }

    /**
     * set pause click listener
     */
    private val mPauseListener = OnClickListener {
        doPauseResume()
        show()
    }

    /**
     * set full screen click listener
     */
    private val mFullscreenListener = OnClickListener { //            mIsFullScreen = !mIsFullScreen;
        doToggleFullscreen()
        //            mMediaPlayerControlListener.setFullScreen(mIsFullScreen);
        show()
    }

    /**
     * PlayList click
     */
    private val mPlayListListener = OnClickListener { mMediaPlayerControlListener!!.playList() }

    /**
     * Play Next click
     */
    private val mPlayNextListener = OnClickListener { mMediaPlayerControlListener!!.playNext() }

    /**
     * Play Previous click
     */
    private val mPlayPreviousListener = OnClickListener { mMediaPlayerControlListener!!.playPrevious() }

    /**
     * Play Next 10 Sec click
     */
    private val go10SecForwardListener = OnClickListener { mMediaPlayerControlListener!!.go10SecForward() }

    /**
     * Play Previous 10 Sec click
     */
    private val go10SecPreviousListener = OnClickListener { mMediaPlayerControlListener!!.go10SecPrevious() }

    /**
     * setMediaPlayerControlListener update play state
     *
     * @param mediaPlayerListener self
     */
    fun setMediaPlayerControlListener(mediaPlayerListener: MediaPlayerControlListener?) {
        mMediaPlayerControlListener = mediaPlayerListener!!
        togglePausePlay()
        toggleFullScreen()
    }

    /**
     * set anchor view
     *
     * @param view view that hold controller view
     */
    private fun setAnchorView(view: ViewGroup?) {
        mAnchorView = view
        val frameParams = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        )
        //remove all before add view
        removeAllViews()
        val v = makeControllerView()
        addView(v, frameParams)
        setGestureListener()
    }

    /**
     * set gesture listen to control media mediaPlayer
     * include screen brightness and volume of video
     * and seek video play
     */
    private fun setGestureListener() {
        if (mCanControlVolume) {
            mAudioManager = mContext!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            mMaxVolume = mAudioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        }
        mGestureDetector = GestureDetector(mContext, ViewGestureListener(mContext!!, this))
    }

    override fun onSingleTap() {
//        if (!MowPlayerActivity.Companion.getMIsComplete() && isShowing()) {
        toggleControllerView()
        //        }
    }

    override fun onHorizontalScroll(seekForward: Boolean) {
        if (mCanSeekVideo) {
            if (seekForward) { // seek forward
                seekForWard()
            } else {  //seek backward
                seekBackWard()
            }
        }
    }

    private fun seekBackWard() {
        if (mMediaPlayerControlListener == null) {
            return
        }
        var pos = mMediaPlayerControlListener!!.currentPosition
        pos -= PROGRESS_SEEK.toInt()
        mMediaPlayerControlListener!!.seekTo(pos)
        setSeekProgress("seekBackWard")
        show()
    }

    private fun seekForWard() {
        if (mMediaPlayerControlListener == null) {
            return
        }
        var pos = mMediaPlayerControlListener!!.currentPosition
        pos += PROGRESS_SEEK.toInt()
        mMediaPlayerControlListener!!.seekTo(pos)
        setSeekProgress("seekForWard")
        show()
    }

    override fun onVerticalScroll(percent: Float, direction: Int) {
        if (direction == SWIPE_LEFT) {
            if (mCanControlBrightness) {
                mCenterImage!!.setImageResource(R.drawable.video_bright_bg)
                updateBrightness(percent)
            }
        } else {
            if (mCanControlVolume) {
                mCenterImage!!.setImageResource(R.drawable.video_volume_bg)
                updateVolume(percent)
            }
        }
    }

    /**
     * update volume by seek percent
     *
     * @param percent seek percent
     */
    private fun updateVolume(percent: Float) {
        layoutCenter!!.visibility = VISIBLE
        if (mCurVolume == -1) {
            mCurVolume = mAudioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
            if (mCurVolume < 0) {
                mCurVolume = 0
            }
        }
        var volume = (percent * mMaxVolume).toInt() + mCurVolume
        if (volume > mMaxVolume) {
            volume = mMaxVolume
        }
        if (volume < 0) {
            volume = 0
        }
        mAudioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
        val progress = volume * 100 / mMaxVolume
        mCenterProgress!!.progress = progress
    }

    /**
     * update brightness by seek percent
     *
     * @param percent seek percent
     */
    private fun updateBrightness(percent: Float) {

//        if (mCurBrightness == -1) {
//            mCurBrightness = mContext.getWindow().getAttributes().screenBrightness;
//            if (mCurBrightness <= 0.01f) {
//                mCurBrightness = 0.01f;
//            }
//        }
//
//        mCenterLayout.setVisibility(VISIBLE);
//
//        WindowManager.LayoutParams attributes = mContext.getWindow().getAttributes();
//        attributes.screenBrightness = mCurBrightness + percent;
//        if (attributes.screenBrightness >= 1.0f) {
//            attributes.screenBrightness = 1.0f;
//        } else if (attributes.screenBrightness <= 0.01f) {
//            attributes.screenBrightness = 0.01f;
//        }
//        mContext.getWindow().setAttributes(attributes);
//
//        float p = attributes.screenBrightness * 100;
//        mCenterProgress.setProgress((int) p);
    }

    interface VideoViewCallback {
        fun onScaleChange(isFullscreen: Boolean)
    }

    /**
     * Interface of Media Controller View Which can be callBack
     * when [android.media.MediaPlayer] or some other media
     * players work
     */
    interface MediaPlayerControlListener {
        /**
         * start play video
         */
        fun start()

        /**
         * pause video
         */
        fun pause()

        /**
         * get video total time
         *
         * @return total time
         */
        val duration: Int

        /**
         * get video current position
         *
         * @return current position
         */
        val currentPosition: Int

        /**
         * seek video to exactly position
         *
         * @param position position
         */
        fun seekTo(position: Int)

        /**
         * video is playing state
         *
         * @return is video playing
         */
        val isPlaying: Boolean

        /**
         * video is complete
         *
         * @return complete or not
         */
        val isComplete: Boolean

        /**
         * get buffer percent
         *
         * @return percent
         */
        val bufferPercentage: Int

        /**
         * video is full screen
         * in order to control image src...
         *
         * @return fullScreen
         */
        var isFullScreen: Boolean

        /**
         * toggle fullScreen
         *
         */
        fun toggleFullScreen()

        /**
         * exit media mediaPlayer
         */
        fun exit()
        fun setFullscreen(fullscreen: Boolean, screenOrientation: Int)
        fun playList()
        fun go10SecPrevious()
        fun go10SecForward()
        fun playNext()
        fun playPrevious()
    }

    private fun shareDialog(videoUrl: String) {
//        val packages: MutableList<String> = ArrayList()
//        val shareIntent = Intent()
//        shareIntent.action = Intent.ACTION_SEND
//        shareIntent.type = "text/plain"
//        val resInfosNew: MutableList<ResolveInfo> = ArrayList()
//        val resInfos = mContext!!.packageManager.queryIntentActivities(shareIntent, 0)
//        resInfosNew.addAll(resInfos)
//        if (!resInfos.isEmpty()) {
//            println("Have package")
//            if (!resInfos.isEmpty()) {
//                println("Have package")
//                var count = 0
//                for (resInfo in resInfos) {
//                    val packageName = resInfo.activityInfo.packageName
//                    if (packageName.contains("com.facebook.katana") || packages.contains(packageName)) {
//                        try {
//                            resInfosNew.removeAt(count)
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                    } else {
//                        packages.add(packageName)
//                    }
//                    count++
//                }
//            }
//            if (packages.size > 1) {
//                val adapter = ChooserArrayAdapter(mContext, R.layout.share_dialog, packages)
//                AlertDialog.Builder(mContext!!).setTitle(R.string.share_via).setAdapter(adapter) { dialog, item -> invokeApplication(packages[item], resInfosNew[item], videoUrl) }.show()
//            } else if (packages.size == 1) {
//                invokeApplication(packages[0], resInfos[0], videoUrl)
//            }
//        }
    }

//    private fun invokeApplication(packageName: String, resolveInfo: ResolveInfo, videoUrl: String) {
//        val intent = Intent()
//        intent.component = ComponentName(packageName, resolveInfo.activityInfo.name)
//        intent.action = Intent.ACTION_SEND
//        intent.type = "text/plain"
//        intent.putExtra(Intent.EXTRA_TEXT, videoUrl)
//        intent.setPackage(packageName)
//        mContext!!.startActivity(intent)
//    }

//    inner class ChooserArrayAdapter(context: Context?, layout: Int, packages: List<String>) : ArrayAdapter<String?>(mContext!!, layout, packages) {
//        private val userList: List<String>? = null
//        private val ilayout: Int
//        var mPm: PackageManager
//        var mPackages: List<String>
//        override fun getItemId(position: Int): Long {
//            return position.toLong()
//        }
//
//        internal inner class ViewHolder {
//            val appName: TextView? = null
//            val appIcon: ImageView? = null
//        }
//
//        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//            val viewHolder: ViewHolder
//            var rowView = convertView
//            if (rowView == null) {
//                rowView = LayoutInflater.from(mContext).inflate(ilayout, null)
//                viewHolder = ViewHolder()
//                viewHolder.appName = rowView.findViewById(R.id.appName)
//                viewHolder.appIcon = rowView.findViewById(R.id.appIcon)
//                rowView.tag = viewHolder
//            } else {
//                viewHolder = rowView.tag as ViewHolder
//            }
//            val pkg = mPackages[position]
//            try {
//                val ai = mPm.getApplicationInfo(pkg, 0)
//                val appName = mPm.getApplicationLabel(ai)
//                val appIcon = mPm.getApplicationIcon(pkg)
//                viewHolder.appName!!.text = appName
//                viewHolder.appIcon!!.setImageDrawable(appIcon)
//            } catch (e: PackageManager.NameNotFoundException) {
//                e.printStackTrace()
//            }
//            return rowView!!
//        }
//
//        init {
//            mPm = mContext!!.packageManager
//            mPackages = packages
//            ilayout = layout
//        }
//    }

    companion object {
        private const val TAG = "VideoControllerView"
        private const val HANDLER_ANIMATE_OUT = 1 // out animate
        private const val HANDLER_UPDATE_PROGRESS = 2 //cycle update progress
        private const val SHOW_COMPLETE = 3 //S
        private const val PROGRESS_SEEK: Long = 500
        private const val ANIMATE_TIME: Long = 300
        var mEndTime: TextView? = null
        var mCurrentTime: TextView? = null
        var time_seprator_text: TextView? = null
    }
}