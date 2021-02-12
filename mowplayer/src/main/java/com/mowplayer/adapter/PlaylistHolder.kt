package com.mowplayer.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.mowplayer.R


class PlaylistHolder(v: View) : RecyclerView.ViewHolder(v) {

    var videoName: TextView
    var img_Poster: ImageView
    var mainRelative: ImageView
    var videoDuration: TextView

    init {

        videoName = v.findViewById<View>(R.id.txt_videoName) as TextView
        img_Poster = v.findViewById<View>(R.id.poster1) as ImageView
        mainRelative = v.findViewById<View>(R.id.poster1) as ImageView
        videoDuration = v.findViewById<View>(R.id.txt_Time) as TextView

    }
}