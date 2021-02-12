package com.mowplayer.models.audio

import android.util.Log
import org.json.JSONObject

class Tracking(trackingObject: JSONObject) {

    var throttle: Long = 0
    var readyDelay: Int = 0
    var url: String = ""
    var events: ArrayList<String> = arrayListOf()
    var ignoreEvents: ArrayList<String> = arrayListOf()

    init {
        if (trackingObject.has("throttle")) {
            throttle = trackingObject.getLong("throttle")
        }
        if (trackingObject.has("readyDelay")) {
            readyDelay = trackingObject.getInt("readyDelay")
        }
        if (trackingObject.has("url")) {
            url = trackingObject.getString("url")
        }
        if (trackingObject.has("events")) {
            val array = trackingObject.getJSONArray("events")
            for (i in 0 until array.length()) {
                events.add(array.getString(i))
            }

            Log.e("Array ---- ", "" + events)
        }
        if (trackingObject.has("throttle")) {
//            ignoreEvents = trackingObject.getInt("ignoreEvents")
        }
    }
}
