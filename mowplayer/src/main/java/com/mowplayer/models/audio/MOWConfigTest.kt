package com.mowplayer.models.audio

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject

class MOWConfigTest(responseBody: String) {

    var autoplay: Boolean? = false
    var _mute: Int? = 0
    var _auto_permalink: String? = "m"
    var media: List<Media>? = emptyList()
    var auth: String? = null
    var mute: Boolean? = null
    var volume: Double? = 0.0
    var tracking: Tracking? = null
    var sharing: Boolean? = null
    var related: String? = null
    var sticky: Sticky? = null
    var mobile_full_width: String? = null
    var ratio: String? = "16:9"
    var ads: AdsType? = null
    @SerializedName("autoplay_related")
    var autoPlayRelated: AutoPlayRelated? = null
    var responsive: Boolean? = false
    var error: String? = null

    // Old API keys
    var adPriority: String? = "m"
    var audio: Audio? = null
    var playlist: Audio? = null
    var layout: String? = null

    init {
        val responseObject = JSONObject(responseBody)

        if (responseObject.has("autoplay")) {
            autoplay = responseObject.getBoolean("autoplay")
        }

        if (responseObject.has("_mute")) {
            _mute = responseObject.getInt("_mute")
        }

        if (responseObject.has("_auto_permalink")) {
            _auto_permalink = responseObject.getString("_auto_permalink")
        }

        if (responseObject.has("media")) {
            val mediaList: ArrayList<Media> = ArrayList()
            when (val mediaData = responseObject.get("media")) {
                is JSONObject -> {
                    mediaList.add(Media(mediaData))
                }
                is JSONArray -> {
                    for (i in 0 until mediaData.length()) {
                        val item = Media(mediaData.getJSONObject(i))
                        mediaList.add(item)
                    }
                }
                else -> {

                }
            }
            media = mediaList.toList()
        } else {
            media = emptyList()
        }

        if (responseObject.has("auth")) {
            auth = responseObject.getString("auth")
        }

        if (responseObject.has("mute")) {
            mute = responseObject.getBoolean("mute")
        }

        if (responseObject.has("volume")) {
            volume = responseObject.getDouble("volume")
        }

        if (responseObject.has("tracking")) {
            tracking = Tracking(responseObject.getJSONObject("tracking"))
        }

        if (responseObject.has("sharing")) {
            sharing = responseObject.getBoolean("sharing")
        }

        if (responseObject.has("related")) {
            related = responseObject.getString("related")
        }

        if (responseObject.has("sticky")) {
            sticky = Sticky(responseObject.getJSONObject("sticky"))
        }

        if (responseObject.has("mobile_full_width")) {
            mobile_full_width = responseObject.getString("mobile_full_width")
        }

        if (responseObject.has("ratio")) {
            ratio = responseObject.getString("ratio")
        }

        if (responseObject.has("ads") && responseObject.getString("ads") != null) {
            ads = AdsType(responseObject.getJSONObject("ads"))
        }

        if (responseObject.has("autoplay_related")) {
            autoPlayRelated = AutoPlayRelated(responseObject.getJSONObject("autoplay_related"))
        }

        if (responseObject.has("responsive")) {
            responsive = responseObject.getBoolean("responsive")
        }

        if (responseObject.has("error")) {
            error = responseObject.getString("error")
        }

        // OLD api use
        if (responseObject.has("layout")) {
            layout = responseObject.getString("layout")
        }
        // ad_property
        if (responseObject.has("ad_priority")) {
            if (responseObject.getString("ad_priority") != null) {
                adPriority = responseObject.getString("ad_priority")
            }
        }
        // audio
        if (responseObject.has("audio") && responseObject.getString("audio") != null) {
            audio = Audio(responseObject.getJSONObject("audio"))
        }
        // playlist
        if (responseObject.has("playlist") && responseObject.getString("playlist") != null) {
            playlist = Audio(responseObject.getJSONObject("playlist"))
        }
    }

}

