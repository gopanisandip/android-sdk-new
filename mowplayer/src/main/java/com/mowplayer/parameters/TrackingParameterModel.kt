package com.mowplayer.parameters

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TrackingParameterModel {


    @SerializedName("events")
    @Expose
    var events: Events? = null

    inner class AdImpression {

        @SerializedName("id")
        @Expose
        var id: Int? = null
        @SerializedName("data2")
        @Expose
        var data2: Int? = null
        @SerializedName("ad_from")
        @Expose
        var adFrom: String? = null
        @SerializedName("ad_priority")
        @Expose
        var adPriority: String? = null

    }

    inner class AdRequest {

        @SerializedName("id")
        @Expose
        var id: Int? = null
        @SerializedName("data2")
        @Expose
        var data2: Int? = null
        @SerializedName("ad_from")
        @Expose
        var adFrom: String? = null
        @SerializedName("ad_priority")
        @Expose
        var adPriority: String? = null

    }

    inner class Events {

        @SerializedName("ad_request")
        @Expose
        var adRequest: List<AdRequest>? = null
        @SerializedName("first_play")
        @Expose
        var firstPlay: FirstPlay? = null
        @SerializedName("ad_impression")
        @Expose
        var adImpression: List<AdImpression>? = null
        @SerializedName("video_progress")
        @Expose
        var videoProgress: Double? = null

    }

    inner class FirstPlay {

        @SerializedName("type")
        @Expose
        var type: String? = null
        @SerializedName("live")
        @Expose
        var live: Any? = null
        @SerializedName("stream")
        @Expose
        var stream: Any? = null

    }
}
