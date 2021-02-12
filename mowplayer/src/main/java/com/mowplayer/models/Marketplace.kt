package com.mowplayer.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Marketplace {

    @SerializedName("id")
    @Expose
    var id: Int = 0
    @SerializedName("url")
    @Expose
    var url: String = ""
    @SerializedName("client")
    @Expose
    var client: String = ""

    @SerializedName("time")
    @Expose
    var time: Int = 0

    @SerializedName("type")
    @Expose
    var type: String = ""
    @SerializedName("fallback")
    @Expose
    var fallback: Boolean = false

}
