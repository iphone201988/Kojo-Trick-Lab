package com.example.newbase_2025.ui.dashboard.tracker.progression_details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.newbase_2025.databinding.HolderUserImageBinding

class UserImagePagerAdapter(
    private val images: List<Int>, private val onImageClick: (Int) -> Unit
) : RecyclerView.Adapter<UserImagePagerAdapter.ImageViewHolder>() {

    inner class ImageViewHolder(val binding: HolderUserImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = HolderUserImageBinding.inflate(inflater, parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = images[position]
        holder.binding.bean = imageUrl
        holder.binding.executePendingBindings()

        // Handle click
        holder.binding.root.setOnClickListener {
            onImageClick(imageUrl)
        }
    }

    override fun getItemCount(): Int = images.size
}
