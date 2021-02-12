package com.mowplayer.models.audio

import com.google.gson.Gson
import com.mowplayer.models.Playlist
import org.json.JSONObject

class Audio(audioObject: JSONObject) {

    var description: String? = ""
    var id: Int? = 0
    var live: Boolean? = false
    var name: String? = ""
    var playlist: Playlist? = null
    var theme: String? = ""
    var thumbnail: Boolean? = false
    var timezone: Int? = 0
    var reader: Reader? = null

    init {
        if (audioObject.has("description")) {
            description = audioObject.getString("description")
        }
        if (audioObject.has("id")) {
            id = audioObject.getInt("id")
        }
        if (audioObject.has("live")) {
            live = audioObject.getBoolean("live")
        }
        if (audioObject.has("name")) {
            name = audioObject.getString("name")
        }
        if (audioObject.has("playlist")) {
//            playlist = audioObject.getString("playlist")
        }
        if (audioObject.has("theme")) {
            theme = audioObject.getString("theme")
        }
        if (audioObject.has("thumbnail")) {
            thumbnail = audioObject.getBoolean("thumbnail")
        }
        if (audioObject.has("timezone")) {
            timezone = audioObject.getInt("timezone")
        }
        if (audioObject.has("reader")) {
            reader = Gson().fromJson(audioObject.getJSONObject("reader").toString(), Reader::class.java)
        }
    }

}
