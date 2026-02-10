package com.tech.kojo.ui.dashboard.library.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tech.kojo.R
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.LibrarySery
import com.google.android.material.imageview.ShapeableImageView

class SeriesAdapter(
    private val listener: OnSeriesClickListener
) : RecyclerView.Adapter<SeriesAdapter.VH>() {

    private val list = ArrayList<LibrarySery>()

    fun setList(data: List<LibrarySery>) {
        list.clear()
        list.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_section_series, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = list[position]

        holder.title.text = item.title
        holder.count.text = "${item.videoCount} videos"

        Glide.with(holder.image.context)
            .load(Constants.BASE_URL_IMAGE + item.imageUrl)
            .placeholder(R.drawable.progress_animation_small)
            .error(R.drawable.blank_pofile)
            .into(holder.image)


        holder.image.setOnClickListener {
            listener.onSeriesClick(item, position)
        }
    }

    interface OnSeriesClickListener {
        fun onSeriesClick(series: LibrarySery, position: Int)
    }
    override fun getItemCount(): Int = list.size

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val image: ShapeableImageView = view.findViewById(R.id.ivVideoImage)
        val title: TextView = view.findViewById(R.id.tvDesc)
        val count: TextView = view.findViewById(R.id.tvVideoCount)
    }
}
