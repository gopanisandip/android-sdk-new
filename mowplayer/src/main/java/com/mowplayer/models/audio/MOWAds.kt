package com.mowplayer.models.audio

import com.google.gson.annotations.SerializedName
import org.json.JSONObject
import java.util.*

class MOWAds(mowAds: JSONObject, loadFrom: String) {

    var client: String = ""
    var duration: Int? = 0
    var id: Int = 0
    var startTime: String? = ""
    var time: String = ""
    var timezone: Int? = 0
    var url: String = ""

    @SerializedName("callback_ads")
    var callbackAds: String? = ""

    var startDateTime: Date? = Date()
    var isUsedOrExpired: Boolean = false
    var loadFrom = "marketplace"
    var isTritonAds: Boolean = false

    init {
        if (mowAds.has("client")) {
            client = if (mowAds.getString("client") != null) {
                mowAds.getString("client")
            } else {
                "ima"
            }

            isTritonAds = client == "triton"
        }

        if (mowAds.has("duration")) {
            duration = mowAds.getInt("duration")
        }

        if (mowAds.has("callback_type")) {
            callbackAds = mowAds.getString("callback_type")
        }

        if (mowAds.has("id")) {
            id = mowAds.getInt("id")
        }
        if (mowAds.has("startTime")) {
            startTime = mowAds.getString("startTime")
        }
        if (mowAds.has("time")) {
            time = mowAds.getString("time")
        }
        if (mowAds.has("url")) {
            url = mowAds.getString("url")
        }
        if (mowAds.has("timezone")) {
            timezone = mowAds.getInt("timezone")
        }

        this.loadFrom = loadFrom
    }
}
