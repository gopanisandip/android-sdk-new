package com.mowplayer.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class RelatedVideoModel {

    @SerializedName("id")
    @Expose
    val id: Long = 0
    @SerializedName("title")
    @Expose
    val title: String = ""
    @SerializedName("thumbnail")
    @Expose
    val thumbnail: String = ""
    @SerializedName("duration")
    @Expose
    val duration: Int = 0
    @SerializedName("file")
    @Expose
    val file: String = ""
    @SerializedName("ads")
    @Expose
    val ads: Ads = Ads()
    @SerializedName("description")
    @Expose
    val description: String = ""

    inner class Ads{
        @SerializedName("marketplace")
        @Expose
        var marketplace: List<Marketplace> = emptyList()
        @SerializedName("publisher")
        @Expose
        var publisher: List<Publisher> = emptyList()
    }
}
