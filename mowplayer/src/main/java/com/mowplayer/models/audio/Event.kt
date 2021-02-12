package com.mowplayer.models.audio

import java.util.ArrayList

class Event {
    var video_progress: Double? = null
    var first_play: FirstPlay? = null
    var ad_impression: ArrayList<AdImpression>? = null
    var ad_request: ArrayList<AdRequest>? = null
}
