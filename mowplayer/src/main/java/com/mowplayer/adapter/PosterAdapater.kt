package com.mowplayer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

import com.mowplayer.models.RelatedVideoModel
import com.squareup.picasso.Picasso

import androidx.recyclerview.widget.RecyclerView
import com.mowplayer.R

class PosterAdapater(internal var mContext: Context, private var relatedVideosList: List<RelatedVideoModel>?) : RecyclerView.Adapter<PosterAdapater.PosterHolder>() {
    private var mItemClickListener: onRecyclerViewItemClickListener? = null
    internal lateinit var mFormatBuilder: StringBuilder

    interface onRecyclerViewItemClickListener {
        fun onItemClickListener(view: View, position: Int)
    }

    fun setOnItemClickListener(mItemClickListener: onRecyclerViewItemClickListener) {
        this.mItemClickListener = mItemClickListener
    }

    fun setVideos(lists: List<RelatedVideoModel>) {
        this.relatedVideosList = lists
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): PosterHolder {

        val v = LayoutInflater.from(viewGroup.context).inflate(
                R.layout.row_poster, null)
        return PosterHolder(v)
    }

    override fun onBindViewHolder(holder: PosterHolder, i: Int) {

        val relatedVideos = relatedVideosList!![i]


        if (relatedVideos.thumbnail != "") {
            Picasso.get()
                    .load(relatedVideos.thumbnail)
                    .into(holder.img_Poster1)
        }

        val title = relatedVideos.title
        holder.txtInfo.text = title

        val time = relatedVideos.duration.toString()
        holder.txtTime.text = time

        holder.relativeLinear.setOnClickListener { v ->
            if (mItemClickListener != null) {
                mItemClickListener!!.onItemClickListener(v, i)
            }
        }
    }



    override fun getItemCount(): Int {
        return if (null != relatedVideosList) relatedVideosList!!.size else 0
    }

    inner class PosterHolder(v: View) : RecyclerView.ViewHolder(v) {

        val img_Poster1: ImageView
        val txtInfo: TextView
        val txtTime: TextView
        val relativeLinear: RelativeLayout

        init {

            relativeLinear = v.findViewById(R.id.liner)

            img_Poster1 = v.findViewById(R.id.poster1)
            txtInfo = v.findViewById(R.id.txt_Info)
            txtTime = v.findViewById(R.id.txt_Time)
        }
    }
}
