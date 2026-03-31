package com.tech.kojo.ui.dashboard.community.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.databinding.DataBindingUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.RecyclerView
import com.tech.kojo.R
import com.tech.kojo.data.model.PostData
import com.tech.kojo.databinding.ItemImagePostBinding
import com.tech.kojo.databinding.ItemLoadingBinding
import com.tech.kojo.databinding.ItemTextPostBinding
import com.tech.kojo.databinding.ItemVideoPostBinding
import com.zerobranch.layout.SwipeLayout

class MultiViewAdapter(
    private var myId: String?,private val listener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val listItem: MutableList<FeedItem> = mutableListOf()
    private var playingPosition: Int = -1
    private var player: ExoPlayer? = null
    var swipeLayout: SwipeLayout? = null
    companion object {
        private const val TYPE_VIDEO = 1
        private const val TYPE_IMAGE = 3
        private const val TYPE_TEXT = 0
        private const val TYPE_LOADER = 2
    }

    override fun getItemCount() = listItem.size

    override fun getItemViewType(position: Int): Int {
        return when (val item = listItem[position]) {
            is FeedItem.Post -> when (item.post.postType) {
                "video" -> TYPE_VIDEO
                "image"->TYPE_IMAGE
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

            TYPE_IMAGE -> {
                val binding: ItemImagePostBinding = DataBindingUtil.inflate(
                    inflater,
                    R.layout.item_image_post,
                    parent,
                    false
                )
                ImagePostViewHolder(binding)
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
                    is VideoPostViewHolder -> holder.bind(item.post, listener, position, position == playingPosition, player)
                    is ImagePostViewHolder -> holder.bind(item.post, listener, position)
                }
            }

            is FeedItem.Loader -> Unit
        }
    }

    fun setList(newList: List<FeedItem>) {
        listItem.clear()
        listItem.addAll(newList)
        playingPosition = -1
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

    fun setPlayingPosition(position: Int, exoPlayer: ExoPlayer?) {
        val oldPosition = playingPosition
        playingPosition = position
        player = exoPlayer
        if (oldPosition != -1) {
            notifyItemChanged(oldPosition)
        }
        if (playingPosition != -1) {
            notifyItemChanged(playingPosition)
        }
    }

    fun getPlayingPosition() = playingPosition

    // --------------------- ViewHolders -----------------------------

    class LoaderViewHolder(val binding: ItemLoadingBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class TextPostViewHolder(private val binding: ItemTextPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PostData?, listener: OnItemClickListener, position: Int) {

            binding.bean = item
            val isMyPost = item?.userData?._id == myId

            if (isMyPost) {
                binding.swipeLayout.isEnabled = true
                binding.swipeLayout.isEnabledSwipe = true

                binding.swipeLayout.setOnActionsListener(object :
                    SwipeLayout.SwipeActionsListener {

                    override fun onOpen(direction: Int, isContinuous: Boolean) {
                        if (direction == SwipeLayout.LEFT) {
                            if (swipeLayout != null && swipeLayout != binding.swipeLayout) {
                                swipeLayout?.close(true)
                            }
                            swipeLayout = binding.swipeLayout
                        }
                    }

                    override fun onClose() {
                        if (swipeLayout == binding.swipeLayout) swipeLayout = null
                    }
                })

            } else {
                binding.swipeLayout.close(true)
                binding.swipeLayout.isEnabledSwipe = false
                binding.swipeLayout.setOnActionsListener(null)
            }
            binding.cardView.setOnClickListener {
                listener.onItemClick(item, binding.cardView.id, position)
            }
            binding.profileImage.setOnClickListener {
                listener.onItemClick(item,binding.profileImage.id,position)
            }

            binding.ivLike.setOnClickListener {
                listener.onItemClick(item, binding.ivLike.id, position)
            }
            binding.ivPinIcon.setOnClickListener {
                listener.onItemClick(item, binding.ivPinIcon.id, position)
            }
            binding.tvdelete.setOnClickListener {
                listener.onItemClick(item, binding.tvdelete.id, position)
            }


            binding.executePendingBindings()
        }
    }

    inner class VideoPostViewHolder(val binding: ItemVideoPostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @OptIn(UnstableApi::class)
        fun bind(item: PostData?, listener: OnItemClickListener, position: Int, isPlaying: Boolean, player: ExoPlayer?) {

            binding.bean = item
            val isMyPost = item?.userData?._id == myId

            if (isMyPost) {
                binding.swipeLayout.isEnabled = true
                binding.swipeLayout.isEnabledSwipe = true

                binding.swipeLayout.setOnActionsListener(object :
                    SwipeLayout.SwipeActionsListener {

                    override fun onOpen(direction: Int, isContinuous: Boolean) {
                        if (direction == SwipeLayout.LEFT) {
                            if (swipeLayout != null && swipeLayout != binding.swipeLayout) {
                                swipeLayout?.close(true)
                            }
                            swipeLayout = binding.swipeLayout
                        }
                    }

                    override fun onClose() {
                        if (swipeLayout == binding.swipeLayout) swipeLayout = null
                    }
                })

            } else {
                binding.swipeLayout.close(true)
                binding.swipeLayout.isEnabledSwipe = false
                binding.swipeLayout.setOnActionsListener(null)
            }
            if (isPlaying && player != null) {
                binding.cardVideoPlayer.visibility = View.VISIBLE
                binding.ivVideo.visibility = View.GONE
                binding.iv1.visibility = View.INVISIBLE
                binding.playerView.player = player
                binding.playerView.setShowFastForwardButton(false)
                binding.playerView.setShowNextButton(false)
                binding.playerView.setShowRewindButton(false)
                binding.playerView.setShowPreviousButton(false)
            } else {
                binding.cardVideoPlayer.visibility = View.GONE
                binding.ivVideo.visibility = View.VISIBLE
                binding.iv1.visibility = View.VISIBLE
                binding.playerView.player = null
            }

            binding.ivVideo.setOnClickListener {
                listener.onItemClick(item, binding.ivVideo.id, position)
            }
            binding.profileImage.setOnClickListener {
                listener.onItemClick(item,binding.profileImage.id,position)
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

            binding.ivMaximize.setOnClickListener {
                listener.onItemClick(item, binding.ivMaximize.id, position)
            }
            binding.tvdelete.setOnClickListener {
                listener.onItemClick(item, binding.tvdelete.id, position)
            }

            binding.executePendingBindings()
        }
    }


    inner class ImagePostViewHolder(val binding: ItemImagePostBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PostData?, listener: OnItemClickListener, position: Int) {

            binding.bean = item
            val isMyPost = item?.userData?._id == myId

            if (isMyPost) {
                binding.swipeLayout.isEnabled = true
                binding.swipeLayout.isEnabledSwipe = true

                binding.swipeLayout.setOnActionsListener(object :
                    SwipeLayout.SwipeActionsListener {

                    override fun onOpen(direction: Int, isContinuous: Boolean) {
                        if (direction == SwipeLayout.LEFT) {
                            if (swipeLayout != null && swipeLayout != binding.swipeLayout) {
                                swipeLayout?.close(true)
                            }
                            swipeLayout = binding.swipeLayout
                        }
                    }

                    override fun onClose() {
                        if (swipeLayout == binding.swipeLayout) swipeLayout = null
                    }
                })

            } else {
                binding.swipeLayout.close(true)
                binding.swipeLayout.isEnabledSwipe = false
                binding.swipeLayout.setOnActionsListener(null)
            }

            binding.ivLike.setOnClickListener {
                listener.onItemClick(item, binding.ivLike.id, position)
            }

            binding.ivPinIcon.setOnClickListener {
                listener.onItemClick(item, binding.ivPinIcon.id, position)
            }
            binding.profileImage.setOnClickListener {
                listener.onItemClick(item,binding.profileImage.id,position)
            }

            binding.cardView.setOnClickListener {
                listener.onItemClick(item, binding.cardView.id, position)
            }
            binding.tvdelete.setOnClickListener {
                listener.onItemClick(item, binding.tvdelete.id, position)
            }

            binding.executePendingBindings()
        }
    }

    interface OnItemClickListener {
        fun onItemClick(item: PostData?, clickedViewId: Int, position: Int)
    }
}