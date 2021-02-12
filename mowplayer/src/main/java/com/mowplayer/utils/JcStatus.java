package com.mowplayer.utils;

import com.mowplayer.models.audio.Media;

/**
 * Created by rio on 02 January 2017.
 */
public class JcStatus {
    private Media jcAudio;
    private long duration;
    private long currentPosition;
    private PlayState playState;

    public JcStatus() {
        this(null, 0, 0, PlayState.PREPARING);
    }

    public JcStatus(Media jcAudio, long duration, long currentPosition, PlayState playState) {
        this.jcAudio = jcAudio;
        this.duration = duration;
        this.currentPosition = currentPosition;
        this.playState = playState;
    }

    public Media getMedia() {
        return jcAudio;
    }

    public void setMedia(Media jcAudio) {
        this.jcAudio = jcAudio;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(long currentPosition) {
        this.currentPosition = currentPosition;
    }

    public PlayState getPlayState() {
        return playState;
    }

    public void setPlayState(PlayState playState) {
        this.playState = playState;
    }

    public enum PlayState {
        PLAY, PAUSE, STOP, CONTINUE, PREPARING, PLAYING
    }
}
