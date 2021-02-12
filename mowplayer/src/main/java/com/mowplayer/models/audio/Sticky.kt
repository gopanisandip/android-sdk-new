package com.mowplayer.models.audio

import org.json.JSONObject

class Sticky(stickyObject: JSONObject) {

    var enabled: Boolean? = false
    var position: String? = null

    init {
        if (stickyObject.has("enabled")) {
            enabled = stickyObject.getBoolean("enabled")
        }
        if (stickyObject.has("position")) {
            position = stickyObject.getString("position")
        }
    }
}
