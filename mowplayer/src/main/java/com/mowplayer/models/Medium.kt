package com.mowplayer.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Medium {

    @SerializedName("id")
    @Expose
    var id: Long = 0
    @SerializedName("title")
    @Expose
    var title: String = ""
    @SerializedName("thumbnail")
    @Expose
    var thumbnail: String = ""
    @SerializedName("duration")
    @Expose
    var duration: Int = 0
    @SerializedName("file")
    @Expose
    var file: String = ""
    @SerializedName("ads")
    @Expose
    var ads: Ads = Ads()
    @SerializedName("description")
    @Expose
    var description: String = ""

}
