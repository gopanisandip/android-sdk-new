package com.mowplayer.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class MediaModel {

    @SerializedName("autoplay")
    @Expose
    var autoplay: Boolean = false
    @SerializedName("media")
    @Expose
    var media: List<Medium> = emptyList()
    @SerializedName("playlist")
    @Expose
    var playlist: Playlist = Playlist()
    @SerializedName("ad_priority")
    @Expose
    var adPriority: String = ""
    @SerializedName("auth")
    @Expose
    var auth: String = ""
    @SerializedName("mute")
    @Expose
    var mute: Boolean = false
    @SerializedName("volume")
    @Expose
    var volume: String = ""
    @SerializedName("tracking")
    @Expose
    var tracking: Tracking = Tracking()
    @SerializedName("related")
    @Expose
    var related: String = ""

    @SerializedName("logo")
    @Expose
    var logo: Logo = Logo()

    @SerializedName("sharing")
    @Expose
    var sharing: Boolean = false

}
