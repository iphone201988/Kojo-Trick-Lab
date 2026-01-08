package com.tech.kojo.ui.dashboard.tracker.progression_details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tech.kojo.data.model.VideoLink
import com.tech.kojo.databinding.HolderUserImageBinding

class UserImagePagerAdapter(
    private val images: List<VideoLink>,
    private val onImageClick: (VideoLink) -> Unit
) : RecyclerView.Adapter<UserImagePagerAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: HolderUserImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = HolderUserImageBinding.inflate(inflater, parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val item = images[position]

        // assuming VideoLink has a field "url: String?"
        holder.binding.bean = item
        holder.binding.executePendingBindings()

        holder.binding.root.setOnClickListener {
            onImageClick(item)
        }
    }

    override fun getItemCount(): Int = images.size
}

