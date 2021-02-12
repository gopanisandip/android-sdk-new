package com.mowplayer.audio

import android.media.AudioManager
import android.media.MediaPlayer

class MOWMediaPlayer {

    var mediaPlayer: MediaPlayer? = null
    var startTime = 0
    var finalTime = 0
    var seekForwardTime = 10000
    var seekBackwardTime = 10000

    init {

    }

    val getMediaPlayer: MediaPlayer?
        get() {
            return mediaPlayer
        }

    fun setURL(url: String) {
        mediaPlayer = MediaPlayer().apply {
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            setDataSource(url)
            setOnPreparedListener { mp ->
                mediaPlayer = mp
            }
            prepare()
        }
    }

    fun start() {
        if (mediaPlayer != null) {
            mediaPlayer!!.start()
        }
    }

    fun release() {
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
        }
    }

    fun pause() {
        if (mediaPlayer!!.isPlaying && mediaPlayer != null) {
            mediaPlayer!!.pause()
        }
    }

    fun stop() {
        if (mediaPlayer!!.isPlaying && mediaPlayer != null) {
            mediaPlayer!!.stop()
        }
    }

    fun seekTo(seek: Int) {
        mediaPlayer!!.seekTo(seek)
    }

    fun seekForward() {
        val currentPosition = currentMilliSeconds
        if (currentPosition + seekForwardTime <= totalMilliSeconds) {
            // forward song
            mediaPlayer!!.seekTo(currentPosition + seekForwardTime)
        } else {
            // forward to end position
            mediaPlayer!!.seekTo(totalMilliSeconds)
        }
        mediaPlayer!!.seekTo(mediaPlayer!!.currentPosition + seekForwardTime)
    }

    fun seekBackward() {
        val currentPosition = currentMilliSeconds
        // check if seekBackward time is greater than 0 sec
        if (currentPosition - seekBackwardTime >= 0) {
            // forward song
            mediaPlayer!!.seekTo(currentPosition - seekBackwardTime)
        } else {
            // backward to starting position
            mediaPlayer!!.seekTo(0)
        }
    }

    val totalMilliSeconds: Int
        get() {
            return mediaPlayer!!.duration
        }

    val currentMilliSeconds: Int
        get() {
            return if (mediaPlayer != null)
                mediaPlayer!!.currentPosition
            else
                0
        }
}
