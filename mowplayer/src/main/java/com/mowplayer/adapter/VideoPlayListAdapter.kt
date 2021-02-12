package com.mowplayer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mowplayer.R
import com.mowplayer.models.audio.Media
import com.mowplayer.utils.MowUtils
import com.mowplayer.utils.OnAudioClickListener

class VideoPlayListAdapter(var mContext: Context, private var mediaList: List<Media>?,
                           var currentPlaying: Int, var theme: String, var onAudioListener: OnAudioClickListener?)
    : RecyclerView.Adapter<VideoPlayListAdapter.VideoViewHolder>() {

//    private var onAudioClickListener: OnAudioClickListener? = null
//
//    fun setOnAudioItemClick(onAudioClickListener: OnAudioClickListener) {
//        this.onAudioClickListener = onAudioClickListener
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_video_play_list, null)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val event = mediaList!![position]

        holder.tvNumber.text = (position + 1).toString()
        if (event.thumbnail!!.isNotEmpty()) {
            Glide.with(mContext)
                    .load(event.thumbnail)
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.ivPoster)
        }
        holder.tvDuration.text = MowUtils.milliSecondsToTimer(event.duration!!.toLong())
        holder.tvTitle.text = event.title
        holder.tvDescription.text = event.description

        holder.ivPoster.setOnClickListener {
            onAudioListener!!.onAudioClickListener(event, position)
        }

        if (theme == "light") {
            holder.ivPlay.setColorFilter(ContextCompat.getColor(mContext, R.color.text_dark))
            holder.tvNumber.setTextColor(ContextCompat.getColor(mContext, R.color.text_dark))
            holder.tvTitle.setTextColor(ContextCompat.getColor(mContext, R.color.text_dark))
            holder.tvDescription.setTextColor(ContextCompat.getColor(mContext, R.color.text_dark))
        } else {
            holder.ivPlay.setColorFilter(ContextCompat.getColor(mContext, R.color.text_white))
            holder.tvNumber.setTextColor(ContextCompat.getColor(mContext, R.color.text_white))
            holder.tvTitle.setTextColor(ContextCompat.getColor(mContext, R.color.text_white))
            holder.tvDescription.setTextColor(ContextCompat.getColor(mContext, R.color.text_white))
        }

        if (currentPlaying == position) {
            holder.ivPlay.visibility = View.VISIBLE
            holder.tvNumber.visibility = View.GONE
        } else {
            holder.ivPlay.visibility = View.GONE
            holder.tvNumber.visibility = View.VISIBLE
        }
    }

    fun update(currentPlaying: Int) {
        this.currentPlaying = currentPlaying
    }

    override fun getItemCount(): Int {
        return mediaList!!.size
    }

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var ivPlay: ImageView = itemView.findViewById<View>(R.id.ivPlay) as ImageView
        internal var tvNumber: TextView = itemView.findViewById<View>(R.id.tvNumber) as TextView
        internal var ivPoster: ImageView = itemView.findViewById<View>(R.id.ivPoster) as ImageView
        internal var tvDuration: TextView = itemView.findViewById<View>(R.id.tvDuration) as TextView
        internal var tvTitle: TextView = itemView.findViewById<View>(R.id.tvTitle) as TextView
        internal var tvDescription: TextView = itemView.findViewById<View>(R.id.tvDescription) as TextView
    }
}