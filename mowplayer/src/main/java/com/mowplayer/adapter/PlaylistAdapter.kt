package com.mowplayer.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView
import com.mowplayer.R
import com.mowplayer.models.RelatedVideoModel

import com.squareup.picasso.Picasso
import java.util.*


class PlaylistAdapter(internal var mContext: Context, private var relatedVideosList: List<RelatedVideoModel>?) : RecyclerView.Adapter<PlaylistHolder>() {
    private var mItemClickListener: onRecyclerViewItemClickListener? = null
    internal lateinit var mFormatBuilder: StringBuilder
    private var mFormatter: Formatter? = null

    interface onRecyclerViewItemClickListener {
        fun onItemClickListener(view: View, position: Int)
    }


    fun setOnItemClickListener(mItemClickListener: onRecyclerViewItemClickListener) {
        this.mItemClickListener = mItemClickListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): PlaylistHolder {

        val v = LayoutInflater.from(viewGroup.context).inflate(
                R.layout.row_playlist_video, null)
        return PlaylistHolder(v)
    }

    override fun onBindViewHolder(holder: PlaylistHolder,
                                  i: Int) {
        val relatedVideos = relatedVideosList!![i]


        //init formatter
        mFormatBuilder = StringBuilder()
        mFormatter = Formatter(mFormatBuilder, Locale.getDefault())


        // var title = lists!![i].getTitle()
        //  title = if (title.isEmpty() || title == "") "No title" else title
        //holder.videoName.setText(title)


        var title = relatedVideos.title

        title = if (title.isEmpty() || title == "") "No title" else title


        holder.videoName.text = title


        /*  // if (!lists.getJSONObject(i).getString("image").equals("")) {
          if (!lists[i].getThumbnail().equals("")) {
              Picasso.get()
                      .load(lists[i].getThumbnail())
                      .into(holder.img_Poster)
          }*/


        if (relatedVideos.thumbnail != "") {
            Picasso.get()
                    .load(relatedVideos.thumbnail)
                    .into(holder.img_Poster)
        }


        //int t = Integer.parseInt(lists.getJSONObject(i).optString("duration","0"));
        //String time= Utility.timeConversion(t);
        //holder.videoDuration.setText(time);


        //val time = String.valueOf(lists[i].getDuration())
        //holder.videoDuration.text = stringToTime(1900)

        holder.videoDuration.text = stringToTime(relatedVideos.duration)

        holder.mainRelative.setOnClickListener { v ->
            /////- MToast.makeText(mContext, "clicked at position : "+i, Toast.LENGTH_SHORT).show();
            if (mItemClickListener != null) {
                mItemClickListener!!.onItemClickListener(v, i)
            }
        }

    }

    private fun stringToTime(timeMs: Int): String {


        val seconds = timeMs % 60
        val minutes = timeMs / 60
        val hours = timeMs / 3600

        mFormatBuilder.setLength(0)
        return if (hours > 0) {
            mFormatter!!.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter!!.format("%02d:%02d", minutes, seconds).toString()
        }
    }


    override fun getItemCount(): Int {

        return if (null != relatedVideosList) relatedVideosList!!.size else 0

    }


}

