package com.mowplayer.utils

import com.mowplayer.models.audio.Media

interface OnAudioClickListener {
    fun onAudioClickListener(media: Media?, position: Int)
}
