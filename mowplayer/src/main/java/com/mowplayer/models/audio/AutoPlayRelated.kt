package com.mowplayer.models.audio

import org.json.JSONObject

class AutoPlayRelated(stickyObject: JSONObject) {

    var enabled: Boolean? = false
    var related_timeout: Int? = 0
    var timeout: Int? = 0

    init {
        if (stickyObject.has("enabled")) {
            enabled = stickyObject.getBoolean("enabled")
        }
        if (stickyObject.has("related_timeout")) {
            related_timeout = stickyObject.getInt("related_timeout")
        }
        if (stickyObject.has("timeout")) {
            timeout = stickyObject.getInt("timeout")
        }
    }
}
