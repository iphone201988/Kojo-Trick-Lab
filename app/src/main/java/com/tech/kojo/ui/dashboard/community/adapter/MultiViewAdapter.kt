package com.tech.kojo.ui.dashboard.community.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.tech.kojo.R
import com.tech.kojo.data.model.PostData
import com.tech.kojo.databinding.ItemLoadingBinding
import com.tech.kojo.databinding.ItemTextPostBinding
import com.tech.kojo.databinding.ItemVideoPostBinding

class MultiViewAdapter(
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val listItem: MutableList<FeedItem> = mutableListOf()

    companion object {
        private const val TYPE_VIDEO = 1
        private const val TYPE_TEXT = 0
        private const val TYPE_LOADER = 2
    }

    override fun getItemCount() = listItem.size

    override fun getItemViewType(position: Int): Int {
        return when (val item = listItem[position]) {
            is FeedItem.Post -> when (item.post.postType) {
                "video" -> TYPE_VIDEO
                "text" -> TYPE_TEXT
                else -> TYPE_TEXT
            }
            is FeedItem.Loader -> TYPE_LOADER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {

            TYPE_TEXT -> {
                val binding: ItemTextPostBinding = DataBindingUtil.inflate(
                    inflater,
                    R.layout.item_text_post,
                    parent,
                    false
                )
                TextPostViewHolder(binding)
            }

            TYPE_VIDEO -> {
                val binding: ItemVideoPostBinding = DataBindingUtil.inflate(
                    inflater,
                    R.layout.item_video_post,
                    parent,
                    false
                )
                VideoPostViewHolder(binding)
            }

            TYPE_LOADER -> {
                val binding: ItemLoadingBinding = DataBindingUtil.inflate(
                    inflater,
                    R.layout.item_loading,
                    parent,
                    false
                )
                LoaderViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = listItem[position]) {

            is FeedItem.Post -> {
                when (holder) {
                    is TextPostViewHolder -> holder.bind(item.post, listener, position)
                    is VideoPostViewHolder -> holder.bind(item.post, listener, position)
                }
            }

            is FeedItem.Loader -> Unit
        }
    }

    fun setList(newList: List<FeedItem>) {
        listItem.clear()
        listItem.addAll(newList)
        notifyDataSetChanged()
    }

    fun addToList(list: List<FeedItem>?) {
        val newDataList: List<FeedItem>? = list
        if (newDataList != null) {
            val initialSize = listItem.size
            listItem.addAll(newDataList)
            notifyItemRangeInserted(initialSize, newDataList.size)
        }
    }


    fun showLoader() {
        if (listItem.none { it is FeedItem.Loader }) {
            listItem.add(FeedItem.Loader)
            notifyItemInserted(listItem.size - 1)
        }
    }

    fun hideLoader() {
        val index = listItem.indexOfFirst { it is FeedItem.Loader }
        if (index != -1) {
            listItem.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun getList(): MutableList<FeedItem> = listItem
    // --------------------- ViewHolders -----------------------------

    class LoaderViewHolder(val binding: ItemLoadingBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class TextPostViewHolder(private val binding: ItemTextPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PostData?, listener: OnItemClickListener, position: Int) {

            binding.bean = item

            binding.cardView.setOnClickListener {
                listener.onItemClick(item, binding.cardView.id, position)
            }

            binding.ivLike.setOnClickListener {
                listener.onItemClick(item, binding.ivLike.id, position)
            }
            binding.ivPinIcon.setOnClickListener {
                listener.onItemClick(item, binding.ivPinIcon.id, position)
            }


            binding.executePendingBindings()
        }
    }

    inner class VideoPostViewHolder(private val binding: ItemVideoPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PostData?, listener: OnItemClickListener, position: Int) {

            binding.bean = item

            binding.ivVideo.setOnClickListener {
                listener.onItemClick(item, binding.ivVideo.id, position)
            }

            binding.ivLike.setOnClickListener {
                listener.onItemClick(item, binding.ivLike.id, position)
            }

            binding.ivPinIcon.setOnClickListener {
                listener.onItemClick(item, binding.ivPinIcon.id, position)
            }


            binding.cardView.setOnClickListener {
                listener.onItemClick(item, binding.cardView.id, position)
            }

            binding.executePendingBindings()
        }
    }

    interface OnItemClickListener {
        fun onItemClick(item: PostData?, clickedViewId: Int, position: Int)
    }
}