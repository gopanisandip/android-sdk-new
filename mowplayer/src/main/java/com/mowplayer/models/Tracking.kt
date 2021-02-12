package com.mowplayer.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Tracking {

    @SerializedName("throttle")
    @Expose
    var throttle: Int = 0
    @SerializedName("readyDelay")
    @Expose
    var readyDelay: Int = 0
    @SerializedName("url")
    @Expose
    var url: String = ""
    @SerializedName("events")
    @Expose
    var events: List<String> = emptyList()
    @SerializedName("ignoreEvents")
    @Expose
    var ignoreEvents: List<Any> = emptyList()

}
