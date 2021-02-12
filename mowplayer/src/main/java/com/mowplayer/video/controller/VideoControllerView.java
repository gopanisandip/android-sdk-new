package com.mowplayer.video.controller;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.mowplayer.R;
import com.mowplayer.utils.PlayerType;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class VideoControllerView extends FrameLayout implements VideoGestureListener {

    private static final String TAG = "VideoControllerView";

    private boolean isPause = false;

    private int seekForwardTime = 10000;
    private int seekBackwardTime = 10000;

    private static final int HANDLER_ANIMATE_OUT = 1;// out animate
    private static final int HANDLER_UPDATE_PROGRESS = 2;//cycle update progress
    private static final int SHOW_COMPLETE = 3;//S
    private static final long PROGRESS_SEEK = 500;
    private static final long ANIMATE_TIME = 300;

    public ImageView ivPlayerLogo; // root view of this
    private View mRootView; // root view of this
    public SeekBar mSeekBar; //seek bar for video
    public static TextView mEndTime, mCurrentTime, time_seprator_text;
    private boolean mIsShowing;//controller view showing
    private boolean mIsDragging; //is dragging seekBar
    private StringBuilder mFormatBuilder;
    private Formatter mFormatter;
    private GestureDetector mGestureDetector;//gesture detector


    public Context mContext;
    private boolean mCanSeekVideo;
    private boolean mCanControlVolume;
    private boolean mCanControlBrightness;
    private String mVideoTitle;
    public MediaPlayerControlListener mMediaPlayerControlListener;
    private ViewGroup mAnchorView;
    private SurfaceView mSurfaceView;

    private boolean isMute = true;

    @DrawableRes
    private int mPauseIcon;
    @DrawableRes
    private int mPlayIcon;
    @DrawableRes
    private int mShrinkIcon;
    @DrawableRes
    private int mStretchIcon;

    //top layout
    private View mTopLayout;
    private ImageView mBackButton;
    private TextView mTitleText;

    //center layout
    private View mCenterLayout;
    private ImageView mCenterImage;
    private ProgressBar mCenterProgress;
    private float mCurBrightness = -1;
    private int mCurVolume = -1;
    private AudioManager mAudioManager;
    private int mMaxVolume;

    //bottom layout
    private View mBottomLayout;
    public ImageView muteButton, shareButton;
    public ImageView mPauseButton;
    public ImageView mFullscreenButton;
    public ImageView ivPlayList;
    public ImageView ivNext;
    public ImageView ivPrevious;
    public ImageView iv10SecForward;
    public ImageView iv10SecPrevious;
    public SeekBar seekBarAudio;
    public LinearLayout llLive;
    private boolean mIsFullScreen = false;


    private Handler handler = new Handler();

    private Handler mHandler = new ControllerViewHandler(this);

    private OrientationDetector mOrientationDetector;
    private int volume = 0;


    public VideoControllerView(Builder builder) {
        super(builder.context);
        this.mContext = builder.context;
        this.mMediaPlayerControlListener = builder.mediaPlayerControlListener;

        this.mVideoTitle = builder.videoTitle;
        this.mCanSeekVideo = builder.canSeekVideo;
        this.mCanControlVolume = builder.canControlVolume;
        this.mCanControlBrightness = builder.canControlBrightness;
        this.mPauseIcon = builder.pauseIcon;
        this.mPlayIcon = builder.playIcon;
        this.mStretchIcon = builder.stretchIcon;
        this.mShrinkIcon = builder.shrinkIcon;
        this.mSurfaceView = builder.surfaceView;

        setAnchorView(builder.anchorView);

        this.mSurfaceView.setOnTouchListener((v, event) -> {
            toggleControllerView();
            return false;
        });
    }


    public static class Builder {
        private Context context;
        private boolean canSeekVideo = true;
        private boolean canControlVolume = true;
        private boolean canControlBrightness = true;
        private String videoTitle = "";
        private MediaPlayerControlListener mediaPlayerControlListener;


        private ViewGroup anchorView;
        private SurfaceView surfaceView;
        @DrawableRes
        private int pauseIcon = R.drawable.ic_pause;
        @DrawableRes
        private int playIcon = R.drawable.ic_play;
        @DrawableRes
        private int shrinkIcon = R.drawable.ic_media_fullscreen_shrink;
        @DrawableRes
        private int stretchIcon = R.drawable.ic_maximize;

        //Required
        public Builder(@Nullable Context context, @Nullable MediaPlayerControlListener mediaControlListener) {
            this.context = context;
            this.mediaPlayerControlListener = mediaControlListener;
        }


        public Builder with(@Nullable Activity context) {
            this.context = context;
            return this;
        }

        public Builder withMediaControlListener(@Nullable MediaPlayerControlListener mediaControlListener) {
            this.mediaPlayerControlListener = mediaControlListener;
            return this;
        }

        //Options
        public Builder withVideoTitle(String videoTitle) {
            this.videoTitle = videoTitle;
            return this;
        }

        public Builder withVideoSurfaceView(@Nullable SurfaceView surfaceView) {
            this.surfaceView = surfaceView;
            return this;
        }

//        public Builder exitIcon(@DrawableRes int exitIcon) {
//            this.exitIcon = exitIcon;
//            return this;
//        }

        public Builder pauseIcon(@DrawableRes int pauseIcon) {
            this.pauseIcon = pauseIcon;
            return this;
        }

        public Builder playIcon(@DrawableRes int playIcon) {
            this.playIcon = playIcon;
            return this;
        }

        public Builder shrinkIcon(@DrawableRes int shrinkIcon) {
            this.shrinkIcon = shrinkIcon;
            return this;

        }

        public Builder stretchIcon(@DrawableRes int stretchIcon) {
            this.stretchIcon = stretchIcon;
            return this;
        }

        public Builder canSeekVideo(boolean canSeekVideo) {
            this.canSeekVideo = canSeekVideo;
            return this;
        }

        public Builder canControlVolume(boolean canControlVolume) {
            this.canControlVolume = canControlVolume;
            return this;
        }

        public Builder canControlBrightness(boolean canControlBrightness) {
            this.canControlBrightness = canControlBrightness;
            return this;
        }

        public VideoControllerView build(@Nullable ViewGroup anchorView) {
            this.anchorView = anchorView;
            return new VideoControllerView(this);
        }
    }

    /**
     * Handler prevent leak memory.
     */
    private static class ControllerViewHandler extends Handler {
        private final WeakReference<VideoControllerView> mView;

        ControllerViewHandler(VideoControllerView view) {
            mView = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            VideoControllerView view = mView.get();
            if (view == null || view.mMediaPlayerControlListener == null) {
                return;
            }

            int pos;
            switch (msg.what) {
                case HANDLER_ANIMATE_OUT:
                    view.hide();
                    break;
                case HANDLER_UPDATE_PROGRESS://cycle update seek bar progress
//                    if (!MowPlayerActivity.Companion.getMIsComplete()) {
                    pos = view.setSeekProgress("HANDLER_UPDATE_PROGRESS");

                    if (!view.mIsDragging && view.mIsShowing && view.mMediaPlayerControlListener.isPlaying()) {//just in case
                        //cycle update
                        msg = obtainMessage(HANDLER_UPDATE_PROGRESS);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
//                    }
                    break;

                case SHOW_COMPLETE:

            }
        }
    }


    /**
     * Inflate view from exit xml layout
     *
     * @return the root view of {@link VideoControllerView}
     */
    private View makeControllerView() {
        LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mRootView = inflate.inflate(R.layout.video_controller_view, null);
        initControllerView();

        // showLogo("https://mowplayer.nyc3.digitaloceanspaces.com/thumbnails/9tOTugMQZEkIvjON.png");

        return mRootView;
    }

    /**
     * find all views inside {@link VideoControllerView}
     * and init params
     */
    private void initControllerView() {
        shareButton = mRootView.findViewById(R.id.ivShare);
        shareButton.setOnClickListener(view -> {
            shareDialog("");
        });

        muteButton = mRootView.findViewById(R.id.ivMute);
        muteButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_volume_on));
        muteButton.setOnClickListener(view -> {
            if (isMute) {
                volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                muteButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_volume_mute));
                isMute = false;
            } else {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
                muteButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_volume_on));
                isMute = true;
            }
        });

        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);

        // Full screen
        mFullscreenButton = mRootView.findViewById(R.id.ivFullScreen);
        mFullscreenButton.requestFocus();
        mFullscreenButton.setOnClickListener(mFullscreenListener);

        //top layout
        mTopLayout = mRootView.findViewById(R.id.layoutTop);
        mBackButton = mRootView.findViewById(R.id.ivBack);
        ivPlayerLogo = mRootView.findViewById(R.id.ivPlayerLogo);

        ivPlayerLogo.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://mowplayer.com"));
            getContext().startActivity(browserIntent);
        });

//        mBackButton.setImageResource(mExitIcon);
        if (mBackButton != null) {
            mBackButton.requestFocus();
            mBackButton.setOnClickListener(mBackListener);
        }


        mTitleText = mRootView.findViewById(R.id.tvTitle);

        //center layout
        mCenterLayout = mRootView.findViewById(R.id.layoutCenter);
        mCenterLayout.setVisibility(GONE);
        mCenterImage = mRootView.findViewById(R.id.image_center_bg);
        mCenterProgress = mRootView.findViewById(R.id.progress_center);

        //bottom layout
        mBottomLayout = mRootView.findViewById(R.id.layoutBottom);
        mPauseButton = mRootView.findViewById(R.id.ivPausePlay);
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
            mPauseButton.setOnClickListener(mPauseListener);
        }

        ivPlayList = mRootView.findViewById(R.id.ivPlayList);
        if (ivPlayList != null) {
            ivPlayList.requestFocus();
            ivPlayList.setOnClickListener(mPlayListListener);
        }

        ivNext = mRootView.findViewById(R.id.ivNext);
        if (ivNext != null) {
            ivNext.requestFocus();
            ivNext.setOnClickListener(mPlayNextListener);
        }

        ivPrevious = mRootView.findViewById(R.id.ivPrevious);
        if (ivPrevious != null) {
            ivPrevious.requestFocus();
            ivPrevious.setOnClickListener(mPlayPreviousListener);
        }

        iv10SecForward = mRootView.findViewById(R.id.iv10SecForward);
        if (iv10SecForward != null) {
            iv10SecForward.requestFocus();
            iv10SecForward.setOnClickListener(go10SecForwardListener);
        }

        seekBarAudio = mRootView.findViewById(R.id.seekBarAudio);
        iv10SecPrevious = mRootView.findViewById(R.id.iv10SecPrevious);
        if (iv10SecPrevious != null) {
            iv10SecPrevious.requestFocus();
            iv10SecPrevious.setOnClickListener(go10SecPreviousListener);
        }

        mSeekBar = mRootView.findViewById(R.id.seekBar);
        if (mSeekBar != null) {
            mSeekBar.setOnSeekBarChangeListener(mSeekListener);
            mSeekBar.setMax(1000);
        }

        llLive = mRootView.findViewById(R.id.llLive);
        time_seprator_text = mRootView.findViewById(R.id.tvTimeSeparator);

        mEndTime = mRootView.findViewById(R.id.tvTotalTime);
        mCurrentTime = mRootView.findViewById(R.id.tvCurrentTime);

        //init formatter
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

    }

    public void visibleNextPrevious() {
        ivPlayList.setVisibility(VISIBLE);
        ivNext.setVisibility(VISIBLE);
        ivPrevious.setVisibility(VISIBLE);
    }

    public void hideNextPrevious() {
        ivPlayList.setVisibility(GONE);
        ivNext.setVisibility(GONE);
        ivPrevious.setVisibility(GONE);
    }

    public void visibleForwardBackward() {
        iv10SecForward.setVisibility(VISIBLE);
        iv10SecPrevious.setVisibility(VISIBLE);
    }

    public void hideForwardBackward() {
        iv10SecForward.setVisibility(GONE);
        iv10SecPrevious.setVisibility(GONE);
    }

    public void visibleLiveText() {
        llLive.setVisibility(VISIBLE);
        mCurrentTime.setVisibility(GONE);
        time_seprator_text.setVisibility(GONE);
        mEndTime.setVisibility(GONE);
    }

    public void hideLiveText() {
        llLive.setVisibility(GONE);
        mCurrentTime.setVisibility(VISIBLE);
        time_seprator_text.setVisibility(VISIBLE);
        mEndTime.setVisibility(VISIBLE);
    }

    public PlayerType playerType(String code) {
        PlayerType playerType;
        if (code.startsWith("als-")) {
            playerType = PlayerType.Live;
            setupView(playerType);
            return PlayerType.Live;
        } else if (code.startsWith("a-")) {
            playerType = PlayerType.Single;
            setupView(playerType);
            return PlayerType.Single;
        } else if (code.startsWith("v-")) {
            playerType = PlayerType.Single;
            setupView(playerType);
            return PlayerType.Single;
        } else if (code.startsWith("apl-")) {
            playerType = PlayerType.Playlist;
            setupView(playerType);
            return PlayerType.Playlist;
        } else if (code.startsWith("p-")) {
            playerType = PlayerType.Playlist;
            setupView(playerType);
            return PlayerType.Playlist;
        } else if (code.startsWith("pst-")) {
            playerType = PlayerType.PlaylistPosition;
            setupView(playerType);
            return PlayerType.PlaylistPosition;
        } else if (code.startsWith("nv-")) {
            playerType = PlayerType.Article;
            setupView(playerType);
            return PlayerType.Article;
        } else if (code.startsWith("ar-")) {
            playerType = PlayerType.Single;
            setupView(playerType);
            return PlayerType.Single;
        } else if (code.startsWith("vls-")) {
            playerType = PlayerType.Live;
            setupView(playerType);
            return PlayerType.Live;
        } else {
            playerType = PlayerType.Unknown;
            setupView(playerType);
            return PlayerType.Unknown;
        }
    }

    public void setupView(PlayerType playerType) {
        if (playerType == PlayerType.Live) {
            hideForwardBackward();
            hideNextPrevious();
            visibleLiveText();
            mSeekBar.setVisibility(GONE);
            seekBarAudio.setVisibility(VISIBLE);
        } else if (playerType == PlayerType.Single) {
            hideNextPrevious();
            visibleForwardBackward();
            hideLiveText();
            mSeekBar.setVisibility(VISIBLE);
            seekBarAudio.setVisibility(GONE);
        } else if (playerType == PlayerType.Playlist || playerType == PlayerType.Article) {
            visibleNextPrevious();
            visibleForwardBackward();
            hideLiveText();
            mSeekBar.setVisibility(VISIBLE);
            seekBarAudio.setVisibility(GONE);
        } else if (playerType == PlayerType.PlaylistPosition) {
            hideNextPrevious();
            visibleForwardBackward();
            hideLiveText();
            mSeekBar.setVisibility(VISIBLE);
            seekBarAudio.setVisibility(GONE);
        }
    }

    public void show() {

        if (!mIsShowing && mAnchorView != null) {
            //add controller view to bottom of the AnchorView
            LayoutParams tlp = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            mAnchorView.addView(VideoControllerView.this, mSurfaceView.getWidth(), mSurfaceView.getHeight());

            ViewAnimator.putOn(mTopLayout)
                    .waitForSize(new ViewAnimator.Listeners.Size() {
                        @Override
                        public void onSize(ViewAnimator viewAnimator) {
                            viewAnimator.animate()
                                    .translationY(-mTopLayout.getHeight(), 0)
                                    .duration(ANIMATE_TIME)
                                    .andAnimate(mBottomLayout)
                                    .translationY(mBottomLayout.getHeight(), 0)
                                    .duration(ANIMATE_TIME)
                                    .start(new ViewAnimator.Listeners.Start() {
                                        @Override
                                        public void onStart() {
                                            mIsShowing = true;
                                            mHandler.sendEmptyMessage(HANDLER_UPDATE_PROGRESS);
                                        }
                                    });
                        }
                    });

            mIsShowing = true;
        }

//        setSeekProgress();
        if (mPauseButton != null) {
            mPauseButton.requestFocus();
        }
        togglePausePlay();
        toggleFullScreen();

        mHandler.sendEmptyMessage(HANDLER_UPDATE_PROGRESS);

    }

    /**
     * toggle {@link VideoControllerView} show or not
     * this can be called when {@link View#onTouchEvent(MotionEvent)} happened
     */
    public void toggleControllerView() {
        if (!isShowing()) {
            show();
        } else {
            //animate out controller view
            Message msg = mHandler.obtainMessage(HANDLER_ANIMATE_OUT);
            //remove exist one first
            mHandler.removeMessages(HANDLER_ANIMATE_OUT);
            mHandler.sendMessageDelayed(msg, 100);
        }
        handler.removeCallbacks(runnable);
        handler.postDelayed(runnable, 3000);
    }

    public void togglePauseButtonView(Boolean value) {
        if (value) {
            mPauseButton.setVisibility(VISIBLE);
        } else {
            mPauseButton.setVisibility(GONE);
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!mIsDragging && isShowing()) {
                Message msg = mHandler.obtainMessage(HANDLER_ANIMATE_OUT);
                //remove exist one first
                mHandler.removeMessages(HANDLER_ANIMATE_OUT);
                mHandler.sendMessageDelayed(msg, 100);

            }
        }
    };


    /**
     * if {@link VideoControllerView} is visible
     *
     * @return showing or not
     */
    public boolean isShowing() {
        return mIsShowing;
    }

    /**
     * hide controller view with animation
     * With custom animation
     */
    private void hide() {
        if (mAnchorView == null) {
            return;
        }

        ViewAnimator.putOn(mTopLayout)
                .animate()
                .translationY(-mTopLayout.getHeight())
                .duration(ANIMATE_TIME)
                .andAnimate(mBottomLayout)
                .translationY(mBottomLayout.getHeight())
                .duration(ANIMATE_TIME)
                .end(() -> {
                    mAnchorView.removeView(VideoControllerView.this);
                    mHandler.removeMessages(HANDLER_UPDATE_PROGRESS);
                    mIsShowing = false;
                });
    }

    /**
     * convert string to time
     *
     * @param timeMs time to be formatted
     * @return 00:00:00
     */
    private String stringToTime(int timeMs, String method) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    /**
     * set {@link #mSeekBar} progress
     * and video play time {@link #mCurrentTime}
     *
     * @return current play position
     */
    private int setSeekProgress(String method) {
        if (mMediaPlayerControlListener == null || mIsDragging) {
            return 0;
        }

        int position = mMediaPlayerControlListener.getCurrentPosition();
        int duration = mMediaPlayerControlListener.getDuration();
        Log.e(TAG, " -> duration:" + duration);

        if (mSeekBar != null) {
            if (duration > 0) {
                // use long to avoid overflow
                long pos = 1000L * position / duration;
                mSeekBar.setProgress((int) pos);
            }
            //get buffer percentage
            int percent = mMediaPlayerControlListener.getBufferPercentage();
            //set buffer progress
            mSeekBar.setSecondaryProgress(percent * 10);
        }


        if (mEndTime != null)
            mEndTime.setText(stringToTime(duration, method));
        if (mCurrentTime != null) {
            mCurrentTime.setText(stringToTime(position, method));
            if (mMediaPlayerControlListener.isComplete()) {
                mCurrentTime.setText(stringToTime(duration, method));
            }
        }

        mTitleText.setText(mVideoTitle);
        return position;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                mCurVolume = -1;
                mCurBrightness = -1;
                mCenterLayout.setVisibility(GONE);
//                break;// do need bread,should let gestureDetector to handle event
            default://gestureDetector handle other MotionEvent
                if (mGestureDetector != null)
                    mGestureDetector.onTouchEvent(event);
        }
        return true;

    }

    /**
     * toggle pause or play
     */
    public void togglePausePlay() {
        if (mRootView == null || mPauseButton == null || mMediaPlayerControlListener == null) {
            return;
        }

        if (mMediaPlayerControlListener.isPlaying()) {
            isPause = true;
            mPauseButton.setImageResource(mPauseIcon);
        } else {
            isPause = false;
            mPauseButton.setImageResource(mPlayIcon);
        }
    }

    public void seekForward(VideoView videoView) {
        int currentPosition = videoView.getCurrentPosition();
        if (currentPosition + seekForwardTime <= videoView.getDuration()) {
            // forward song
            videoView.seekTo(currentPosition + seekForwardTime);
        } else {
            // forward to end position
            videoView.seekTo(videoView.getDuration());
        }
        videoView.seekTo(videoView.getCurrentPosition() + seekForwardTime);
    }

    public void seekBackward(VideoView videoView) {
        int currentPosition = videoView.getCurrentPosition();
        // check if seekBackward time is greater than 0 sec
        if (currentPosition - seekBackwardTime >= 0) {
            // forward song
            videoView.seekTo(currentPosition - seekBackwardTime);
        } else {
            // backward to starting position
            videoView.seekTo(0);
        }
    }

    public void fullScreenIcon(boolean b) {
        if (b) { //show shrink icon
            mFullscreenButton.setImageResource(R.drawable.ic_media_fullscreen_shrink);
        } else {
            mFullscreenButton.setImageResource(R.drawable.ic_maximize);
        }
    }

    /**
     * toggle full screen or not
     */
    public void toggleFullScreen() {
        if (mRootView == null || mFullscreenButton == null || mMediaPlayerControlListener == null) {
            return;
        }
        if (mMediaPlayerControlListener.isFullScreen()) {
            mFullscreenButton.setImageResource(mShrinkIcon);
        } else {
            mFullscreenButton.setImageResource(mStretchIcon);
        }
    }

    public void doPauseResume() {
        if (mMediaPlayerControlListener == null) {
            return;
        }

        if (mMediaPlayerControlListener.isPlaying()) {
            mMediaPlayerControlListener.pause();
        } else {
            mMediaPlayerControlListener.start();
        }
        togglePausePlay();
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

    private void doToggleFullscreen() {
        if (mMediaPlayerControlListener == null) {
            return;
        }

        mMediaPlayerControlListener.toggleFullScreen();
    }


    /**
     * Seek bar drag listener
     */
    private SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {

        int newPosition = 0;

        boolean change = false;


        public void onStartTrackingTouch(SeekBar bar) {
            show();
            mIsDragging = true;
            mHandler.removeMessages(HANDLER_UPDATE_PROGRESS);
        }

        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
            if (mMediaPlayerControlListener == null || !fromuser) {
                return;
            }


            long duration = mMediaPlayerControlListener.getDuration();
            long newposition = (duration * progress) / 1000L;

            newPosition = (int) newposition;
            change = true;

            mMediaPlayerControlListener.seekTo(newPosition);
            if (mCurrentTime != null)
                mCurrentTime.setText(stringToTime(newPosition, "onProgressChanged"));

        }

        public void onStopTrackingTouch(SeekBar bar) {
            if (mMediaPlayerControlListener == null) {
                return;
            }

            if (change) {
                mMediaPlayerControlListener.seekTo(newPosition);
                if (mCurrentTime != null) {
                    mCurrentTime.setText(stringToTime(newPosition, "onStopTrackingTouch"));
                }
            }

            mIsDragging = false;
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, 3000);
            setSeekProgress("onStopTrackingTouch setSeek");
            togglePausePlay();
            show();
            mHandler.sendEmptyMessage(HANDLER_UPDATE_PROGRESS);
        }
    };


    public void showComplete() {
        mHandler.sendEmptyMessage(SHOW_COMPLETE);
    }


    @Override
    public void setEnabled(boolean enabled) {
        if (mPauseButton != null) {
            mPauseButton.setEnabled(enabled);
        }
        if (mSeekBar != null) {
            mSeekBar.setEnabled(enabled);
        }
        super.setEnabled(enabled);
    }


    /**
     * set top back click listener
     */
    private OnClickListener mBackListener = new OnClickListener() {
        public void onClick(View v) {
            mMediaPlayerControlListener.exit();
        }
    };


    /**
     * set pause click listener
     */
    private OnClickListener mPauseListener = new OnClickListener() {
        public void onClick(View v) {
            doPauseResume();
            show();
        }
    };


    /**
     * set full screen click listener
     */
    private OnClickListener mFullscreenListener = new OnClickListener() {
        public void onClick(View v) {
//            mIsFullScreen = !mIsFullScreen;

            doToggleFullscreen();
//            mMediaPlayerControlListener.setFullScreen(mIsFullScreen);
            show();
        }
    };

    /**
     * PlayList click
     */
    private OnClickListener mPlayListListener = new OnClickListener() {
        public void onClick(View v) {
            mMediaPlayerControlListener.playList();
        }
    };


    /**
     * Play Next click
     */
    private OnClickListener mPlayNextListener = new OnClickListener() {
        public void onClick(View v) {
            mMediaPlayerControlListener.playNext();
        }
    };


    /**
     * Play Previous click
     */
    private OnClickListener mPlayPreviousListener = new OnClickListener() {
        public void onClick(View v) {
            mMediaPlayerControlListener.playPrevious();
        }
    };

    /**
     * Play Next 10 Sec click
     */
    private OnClickListener go10SecForwardListener = new OnClickListener() {
        public void onClick(View v) {
            mMediaPlayerControlListener.go10SecForward();
        }
    };

    /**
     * Play Previous 10 Sec click
     */
    private OnClickListener go10SecPreviousListener = new OnClickListener() {
        public void onClick(View v) {
            mMediaPlayerControlListener.go10SecPrevious();
        }
    };

    /**
     * setMediaPlayerControlListener update play state
     *
     * @param mediaPlayerListener self
     */
    public void setMediaPlayerControlListener(MediaPlayerControlListener mediaPlayerListener) {
        mMediaPlayerControlListener = mediaPlayerListener;
        togglePausePlay();
        toggleFullScreen();
    }

    /**
     * set anchor view
     *
     * @param view view that hold controller view
     */
    private void setAnchorView(ViewGroup view) {
        mAnchorView = view;
        LayoutParams frameParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        //remove all before add view
        removeAllViews();
        View v = makeControllerView();
        addView(v, frameParams);

        setGestureListener();
    }

    /**
     * set gesture listen to control media mediaPlayer
     * include screen brightness and volume of video
     * and seek video play
     */
    private void setGestureListener() {

        if (mCanControlVolume) {
            mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        }

        mGestureDetector = new GestureDetector(mContext, new ViewGestureListener(mContext, this));
    }


    @Override
    public void onSingleTap() {
//        if (!MowPlayerActivity.Companion.getMIsComplete() && isShowing()) {
        toggleControllerView();
//        }
    }

    @Override
    public void onHorizontalScroll(boolean seekForward) {
        if (mCanSeekVideo) {
            if (seekForward) {// seek forward
                seekForWard();
            } else {  //seek backward
                seekBackWard();
            }
        }
    }

    private void seekBackWard() {
        if (mMediaPlayerControlListener == null) {
            return;
        }

        int pos = mMediaPlayerControlListener.getCurrentPosition();
        pos -= PROGRESS_SEEK;
        mMediaPlayerControlListener.seekTo(pos);
        setSeekProgress("seekBackWard");

        show();
    }

    private void seekForWard() {
        if (mMediaPlayerControlListener == null) {
            return;
        }

        int pos = mMediaPlayerControlListener.getCurrentPosition();
        pos += PROGRESS_SEEK;
        mMediaPlayerControlListener.seekTo(pos);
        setSeekProgress("seekForWard");

        show();
    }

    @Override
    public void onVerticalScroll(float percent, int direction) {
        if (direction == ViewGestureListener.Companion.getSWIPE_LEFT()) {
            if (mCanControlBrightness) {
                mCenterImage.setImageResource(R.drawable.video_bright_bg);
                updateBrightness(percent);
            }
        } else {
            if (mCanControlVolume) {
                mCenterImage.setImageResource(R.drawable.video_volume_bg);
                updateVolume(percent);
            }
        }
    }

    /**
     * update volume by seek percent
     *
     * @param percent seek percent
     */
    private void updateVolume(float percent) {

        mCenterLayout.setVisibility(VISIBLE);

        if (mCurVolume == -1) {
            mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mCurVolume < 0) {
                mCurVolume = 0;
            }
        }

        int volume = (int) (percent * mMaxVolume) + mCurVolume;
        if (volume > mMaxVolume) {
            volume = mMaxVolume;
        }

        if (volume < 0) {
            volume = 0;
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);

        int progress = volume * 100 / mMaxVolume;
        mCenterProgress.setProgress(progress);


    }

    /**
     * update volume from API
     *
     * @param isMute
     */
    public void setMuteVolume(Boolean isMute) {
        if (isMute) {
            volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            muteButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_volume_mute));
        } else {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
            muteButton.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_volume_on));
        }
    }

    /**
     * update brightness by seek percent
     *
     * @param percent seek percent
     */
    private void updateBrightness(float percent) {

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

    public interface VideoViewCallback {

        void onScaleChange(boolean isFullscreen);


    }

    /**
     * Interface of Media Controller View Which can be callBack
     * when {@link android.media.MediaPlayer} or some other media
     * players work
     */
    public interface MediaPlayerControlListener {

        /**
         * start play video
         */
        void start();

        /**
         * pause video
         */
        void pause();

        /**
         * get video total time
         *
         * @return total time
         */
        int getDuration();

        /**
         * get video current position
         *
         * @return current position
         */
        int getCurrentPosition();

        /**
         * seek video to exactly position
         *
         * @param position position
         */
        void seekTo(int position);

        /**
         * video is playing state
         *
         * @return is video playing
         */
        boolean isPlaying();

        /**
         * video is complete
         *
         * @return complete or not
         */
        boolean isComplete();

        /**
         * get buffer percent
         *
         * @return percent
         */
        int getBufferPercentage();

        /**
         * video is full screen
         * in order to control image src...
         *
         * @return fullScreen
         */
        boolean isFullScreen();

        /**
         * toggle fullScreen
         */
        void toggleFullScreen();

        /**
         * exit media mediaPlayer
         */
        void exit();

        void setFullScreen(boolean fullScreen);

        void setFullscreen(boolean fullscreen, int screenOrientation);

        void playList();

        void go10SecPrevious();

        void go10SecForward();

        void playNext();

        void playPrevious();
    }

    public void shareDialog(final String videoUrl) {
        final List<String> packages = new ArrayList<String>();
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        final List<ResolveInfo> resInfosNew = new ArrayList<ResolveInfo>();


        final List<ResolveInfo> resInfos = mContext.getPackageManager().queryIntentActivities(shareIntent, 0);
        resInfosNew.addAll(resInfos);
        if (!resInfos.isEmpty()) {
            System.out.println("Have package");

            if (!resInfos.isEmpty()) {
                System.out.println("Have package");
                int count = 0;
                for (ResolveInfo resInfo : resInfos) {
                    String packageName = resInfo.activityInfo.packageName;
                    if (packageName.contains("com.facebook.katana") || packages.contains(packageName)) {
                        try {
                            resInfosNew.remove(count);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        packages.add(packageName);
                    }
                    count++;
                }
            }
            if (packages.size() > 1) {
                ArrayAdapter<String> adapter = new ChooserArrayAdapter(mContext, R.layout.share_dialog, packages);
                new AlertDialog.Builder(mContext).setTitle(R.string.share_via).setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        invokeApplication(packages.get(item), resInfosNew.get(item), videoUrl);
                    }
                }).show();
            } else if (packages.size() == 1) {
                invokeApplication(packages.get(0), resInfos.get(0), videoUrl);
            }
        }
    }

    private void invokeApplication(String packageName, ResolveInfo resolveInfo, String videoUrl) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, resolveInfo.activityInfo.name));
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, videoUrl);
        intent.setPackage(packageName);
        mContext.startActivity(intent);
    }

    public class ChooserArrayAdapter extends ArrayAdapter<String> {
        private List<String> userList;
        private int ilayout;

        PackageManager mPm;
        List<String> mPackages;

        public ChooserArrayAdapter(Context context, int layout, List<String> packages) {
            super(mContext, layout, packages);
            mPm = mContext.getPackageManager();
            mPackages = packages;
            this.ilayout = layout;
        }

        public long getItemId(int position) {
            return position;
        }

        class ViewHolder {
            private TextView appName;
            private ImageView appIcon;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            View rowView = convertView;
            if (rowView == null) {

                rowView = LayoutInflater.from(mContext).inflate(ilayout, null);
                viewHolder = new ViewHolder();
                viewHolder.appName = rowView.findViewById(R.id.appName);
                viewHolder.appIcon = rowView.findViewById(R.id.appIcon);
                rowView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) rowView.getTag();
            }

            String pkg = mPackages.get(position);

            try {
                ApplicationInfo ai = mPm.getApplicationInfo(pkg, 0);
                CharSequence appName = mPm.getApplicationLabel(ai);
                Drawable appIcon = mPm.getApplicationIcon(pkg);
                viewHolder.appName.setText(appName);
                viewHolder.appIcon.setImageDrawable(appIcon);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return rowView;
        }
    }

}