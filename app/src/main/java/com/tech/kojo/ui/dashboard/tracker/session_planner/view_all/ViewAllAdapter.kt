package com.tech.kojo.ui.dashboard.tracker.session_planner.view_all


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.tech.kojo.R
import com.tech.kojo.data.model.PastSessionData
import com.tech.kojo.databinding.ItemLoadingBinding
import com.tech.kojo.databinding.ViewAllRvItemBinding

class ViewAllAdapter(
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val listItem: MutableList<ViewItem> = mutableListOf()

    companion object {
        private const val TYPE_TEXT = 0
        private const val TYPE_LOADER = 1
    }

    override fun getItemCount() = listItem.size

    override fun getItemViewType(position: Int): Int {
        return when (listItem[position]) {
            is ViewItem.Post -> TYPE_TEXT
            is ViewItem.Loader -> TYPE_LOADER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {

            TYPE_TEXT -> {
                val binding: ViewAllRvItemBinding = DataBindingUtil.inflate(
                    inflater, R.layout.view_all_rv_item, parent, false
                )
                TextPostViewHolder(binding)
            }

            TYPE_LOADER -> {
                val binding: ItemLoadingBinding = DataBindingUtil.inflate(
                    inflater, R.layout.item_loading, parent, false
                )
                LoaderViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = listItem[position]) {

            is ViewItem.Post -> {
                if (holder is TextPostViewHolder) {
                    holder.bind(item.post, listener, position)
                }
            }

            is ViewItem.Loader -> Unit
        }
    }

    fun setList(newList: List<ViewItem>) {
        listItem.clear()
        listItem.addAll(newList)
        notifyDataSetChanged()
    }

    fun addToList(newList: List<ViewItem>) {
        hideLoader()

        val start = listItem.size
        listItem.addAll(newList)
        notifyItemRangeInserted(start, newList.size)
    }

    fun showLoader() {
        if (listItem.none { it is ViewItem.Loader }) {
            listItem.add(ViewItem.Loader)
            notifyItemInserted(listItem.size - 1)
        }
    }

    fun hideLoader() {
        val index = listItem.indexOfFirst { it is ViewItem.Loader }
        if (index != -1) {
            listItem.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun getPureList(): List<PastSessionData> {
        return listItem.filterIsInstance<ViewItem.Post>().map { it.post }
    }

    fun getList(): MutableList<ViewItem> = listItem

    // --------------------- ViewHolders -----------------------------

    class LoaderViewHolder(val binding: ItemLoadingBinding) :
        RecyclerView.ViewHolder(binding.root)

    inner class TextPostViewHolder(private val binding: ViewAllRvItemBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(
            item: PastSessionData?,
            listener: OnItemClickListener,
            position: Int
        ) {
            binding.bean = item
            binding.position = position
            binding.adapter = this@ViewAllAdapter

            binding.clSession.setOnClickListener {
                listener.onItemClick(item, binding.clSession.id, position)
            }

            binding.executePendingBindings()
        }

    }

    interface OnItemClickListener {
        fun onItemClick(item: PastSessionData?, clickedViewId: Int, position: Int)
    }
}

