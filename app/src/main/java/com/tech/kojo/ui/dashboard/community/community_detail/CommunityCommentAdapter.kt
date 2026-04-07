package com.tech.kojo.ui.dashboard.community.community_detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.tech.kojo.R
import com.tech.kojo.data.model.GetCommentData
import com.tech.kojo.databinding.ItemLayoutCommentsBinding
import com.zerobranch.layout.SwipeLayout

class CommunityCommentAdapter(
    private var myId: String?, private val listener: OnItemClickListener2
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val listItem: MutableList<GetCommentData> = mutableListOf()
    private var swipeLayout: SwipeLayout? = null

    companion object {
        private const val TYPE_COMMENTS = 0
    }

    override fun getItemCount() = listItem.size

    override fun getItemViewType(position: Int): Int {
        return TYPE_COMMENTS
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {

            TYPE_COMMENTS -> {
                val binding: ItemLayoutCommentsBinding = DataBindingUtil.inflate(
                    inflater, R.layout.item_layout_comments, parent, false
                )
                CommentsViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = listItem[position]
        when (holder) {
            is CommentsViewHolder -> holder.bind(item, listener, position)
        }


    }

    fun setList(newList: List<GetCommentData>) {
        listItem.clear()
        listItem.addAll(newList)
        notifyDataSetChanged()
    }

    fun addToList(list: List<GetCommentData>?) {
        val newDataList: List<GetCommentData>? = list
        if (newDataList != null) {
            val initialSize = listItem.size
            listItem.addAll(newDataList)
            notifyItemRangeInserted(initialSize, newDataList.size)
        }
    }

    fun getList(): MutableList<GetCommentData> = listItem

    // --------------------- ViewHolders -----------------------------


    inner class CommentsViewHolder(private val binding: ItemLayoutCommentsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GetCommentData?, listener: OnItemClickListener2, position: Int) {

            binding.bean = item
            val isMyPost = item?.user?._id == myId

            if (isMyPost) {
                binding.swipeLayout.isEnabled = true
                binding.swipeLayout.isEnabledSwipe = true

                binding.swipeLayout.setOnActionsListener(object : SwipeLayout.SwipeActionsListener {

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
                listener.onItemClick(item, binding.profileImage.id, position)
            }

            binding.tvdelete.setOnClickListener {
                listener.onItemClick(item, binding.tvdelete.id, position)
            }


            binding.executePendingBindings()
        }
    }

    fun clearList() {
        val size = listItem.size
        listItem.clear()
        notifyItemRangeRemoved(0, size)
    }

    interface OnItemClickListener2 {
        fun onItemClick(item: GetCommentData?, clickedViewId: Int, position: Int)
    }
}