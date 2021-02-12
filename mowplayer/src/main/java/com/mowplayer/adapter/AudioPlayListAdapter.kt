package com.mowplayer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mowplayer.R
import com.mowplayer.models.audio.Media
import com.mowplayer.utils.MowUtils
import com.mowplayer.utils.OnAudioClickListener

class AudioPlayListAdapter(var mContext: Context, private var mediaList: List<Media>?,
                           var currentPlaying: Int, var theme: String, var onAudioListener: OnAudioClickListener?)
    : RecyclerView.Adapter<AudioPlayListAdapter.AudioViewHolder>() {

//    private var onAudioClickListener: OnAudioClickListener? = null
//
//    fun setOnAudioItemClick(onAudioClickListener: OnAudioClickListener) {
//        this.onAudioClickListener = onAudioClickListener
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_audio_play_list, null)
        return AudioViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val event = mediaList!![position]

        holder.tvNumber.text = (position + 1).toString()
        holder.tvTitle.text = event.title
        holder.tvDuration.text = MowUtils.milliSecondsToTimer(event.duration!!.toLong())

        holder.tvTitle.setOnClickListener {
            onAudioListener!!.onAudioClickListener(event, position)
        }

        if (theme == "light") {
            holder.ivPlay.setColorFilter(ContextCompat.getColor(mContext, R.color.text_dark))
            holder.tvNumber.setTextColor(ContextCompat.getColor(mContext, R.color.text_dark))
            holder.tvTitle.setTextColor(ContextCompat.getColor(mContext, R.color.text_dark))
            holder.tvDuration.setTextColor(ContextCompat.getColor(mContext, R.color.text_dark))
        } else {
            holder.ivPlay.setColorFilter(ContextCompat.getColor(mContext, R.color.text_white))
            holder.tvNumber.setTextColor(ContextCompat.getColor(mContext, R.color.text_white))
            holder.tvTitle.setTextColor(ContextCompat.getColor(mContext, R.color.text_white))
            holder.tvDuration.setTextColor(ContextCompat.getColor(mContext, R.color.text_white))
        }

        if (currentPlaying == position) {
            holder.ivPlay.visibility = View.VISIBLE
            holder.tvNumber.visibility = View.INVISIBLE
            holder.ivPlay.setColorFilter(ContextCompat.getColor(mContext, R.color.progress_end_color))
            holder.tvNumber.setTextColor(ContextCompat.getColor(mContext, R.color.progress_end_color))
            holder.tvTitle.setTextColor(ContextCompat.getColor(mContext, R.color.progress_end_color))
            holder.tvDuration.setTextColor(ContextCompat.getColor(mContext, R.color.progress_end_color))
        } else {
            holder.ivPlay.visibility = View.INVISIBLE
            holder.tvNumber.visibility = View.VISIBLE
        }
    }

    fun update(currentPlaying: Int) {
        this.currentPlaying = currentPlaying
    }

    override fun getItemCount(): Int {
        return mediaList!!.size
    }

    class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var ivPlay: ImageView = itemView.findViewById<View>(R.id.ivPlay) as ImageView
        internal var tvNumber: TextView = itemView.findViewById<View>(R.id.tvNumber) as TextView
        internal var tvTitle: TextView = itemView.findViewById<View>(R.id.tvTitle) as TextView
        internal var tvDuration: TextView = itemView.findViewById<View>(R.id.tvDuration) as TextView
    }
}