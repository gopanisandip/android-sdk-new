package com.mowplayer.models.audio

import com.google.gson.Gson
import org.json.JSONObject

class CallbackAds(adsObject: JSONObject) {

    var linear: MOWAds? = null

    init {
        if (adsObject.has("linear") && adsObject.getString("linear") != null) {
            linear = Gson().fromJson(adsObject.getJSONObject("linear").toString(), MOWAds::class.java)
        }
    }
}
