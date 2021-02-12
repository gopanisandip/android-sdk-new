package com.mowplayer.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Playlist {

    @SerializedName("id")
    @Expose
    var id: Int = 0
    @SerializedName("name")
    @Expose
    var name: String = ""
    @SerializedName("description")
    @Expose
    var description: String = ""
    @SerializedName("show")
    @Expose
    var show: Boolean = false
    @SerializedName("position")
    @Expose
    var position: String = ""
    @SerializedName("repeat")
    @Expose
    var repeat: Boolean = false
    @SerializedName("theme")
    @Expose
    var theme: String = ""

}
