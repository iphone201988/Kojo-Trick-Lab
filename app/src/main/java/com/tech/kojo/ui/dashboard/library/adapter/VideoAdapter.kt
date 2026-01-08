package com.tech.kojo.ui.dashboard.library.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tech.kojo.R
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.LibraryVideoX
import com.google.android.material.imageview.ShapeableImageView

class VideoAdapter (
    private val listener: OnVideoClickListener
): RecyclerView.Adapter<VideoAdapter.VideoVH>() {
    private val list = ArrayList<LibraryVideoX>()

    inner class VideoVH(view: View) : RecyclerView.ViewHolder(view) {
        val img: ShapeableImageView = view.findViewById(R.id.ivVideoImage)
        val title: AppCompatTextView = view.findViewById(R.id.tvDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoVH {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_video_rv_item, parent, false)
        return VideoVH(view)
    }

    override fun onBindViewHolder(holder: VideoVH, position: Int) {
        val item = list[position]
        holder.title.text = item.title

        Glide.with(holder.img.context).asBitmap().load(Constants.BASE_URL_IMAGE + item.videoUrl)
            .into(holder.img)


        holder.img.setOnClickListener {
            listener.onVideoClick(item, position)
        }
    }

    interface OnVideoClickListener {
        fun onVideoClick(video: LibraryVideoX, position: Int)
    }

    override fun getItemCount(): Int = list.size

    fun setList(data: List<LibraryVideoX>?) {
        list.clear()
        list.addAll(data.orEmpty())
        notifyDataSetChanged()
    }

}
