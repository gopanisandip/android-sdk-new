package com.mowplayer.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Logo {

    @SerializedName("img")
    @Expose
    var img: String = ""
    @SerializedName("url")
    @Expose
    var url: String = ""

}
