package com.mowplayer.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Ads {

    @SerializedName("marketplace")
    @Expose
    var marketplace: List<Marketplace> = emptyList()
    @SerializedName("publisher")
    @Expose
    var publisher: List<Publisher> = emptyList()

}
