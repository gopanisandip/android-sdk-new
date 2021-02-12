package com.mowplayer.models.audio

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import org.json.JSONArray
import org.json.JSONObject

class Media(mediaObject: JSONObject) {

    var ads: AdsType? = null

    @SerializedName("callback_ads")
    var callbackAds: CallbackAds? = null

    @SerializedName("mowplay_ads")
    var mowPlayAds: AdsType? = null

    var description: String? = ""
    var duration: Int? = 0
    var file: String? = ""
    var frequency: String? = ""
    var id: Int? = 0
    var live: Boolean? = false
    var thumbnail: String? = ""
    var title: String? = ""
    var _nv: NV? = null
    var reader: Reader? = null

    var playState: Int? = 0

    init {
        if (mediaObject.has("ads")) {
            //val mediaList: ArrayList<AdsType> = ArrayList()
            when (val mediaData = mediaObject.get("ads")) {
                is JSONObject -> {
                    ads = AdsType(mediaData)
                }
                is JSONArray -> {
                    val newjson = JSONObject()
                    newjson.putOpt("marketplace", mediaData)
                    ads = AdsType(newjson)
                }
                else -> {

                }
            }
        }

        if (mediaObject.has("mowplay_ads")) {
            //val mediaList: ArrayList<AdsType> = ArrayList()
            when (val mediaData = mediaObject.get("mowplay_ads")) {
                is JSONObject -> {
                    mowPlayAds = AdsType(mediaData)
                }
                is JSONArray -> {
                    val newJson = JSONObject()
                    newJson.putOpt("marketplace", mediaData)
                    mowPlayAds = AdsType(newJson)
                }
                else -> {

                }
            }
        }

        if (mediaObject.has("callback_ads")) {
            when (val mowPlayAds = mediaObject.get("callback_ads")) {
                is JSONObject -> {
                    callbackAds = Gson().fromJson(mediaObject.getJSONObject("callback_ads").toString(), CallbackAds::class.java)
                }
                is JSONArray -> {
                    val newJson = JSONObject()
                    newJson.putOpt("marketplace", mowPlayAds)
                    callbackAds = CallbackAds(newJson)
                }
                else -> {

                }
            }
        }

        if (mediaObject.has("description")) {
            description = mediaObject.getString("description")
        }

        if (mediaObject.has("duration")) {
            duration = mediaObject.getInt("duration")
        }

        if (mediaObject.has("file")) {
            file = mediaObject.getString("file")
        }

        if (mediaObject.has("frequency")) {
            frequency = mediaObject.getString("frequency")
        }

        if (mediaObject.has("id")) {
            id = mediaObject.getInt("id")
        }

        if (mediaObject.has("live")) {
            live = mediaObject.getBoolean("live")
        }

        if (mediaObject.has("thumbnail")) {
            thumbnail = mediaObject.getString("thumbnail")
        }

        if (mediaObject.has("title")) {
            title = mediaObject.getString("title")
        }

        if (mediaObject.has("_nv")) {
            _nv = Gson().fromJson(mediaObject.getJSONObject("_nv").toString(), NV::class.java)
        }

        if (mediaObject.has("reader")) {
            reader = Gson().fromJson(mediaObject.getJSONObject("reader").toString(), Reader::class.java)
        }
    }

}
