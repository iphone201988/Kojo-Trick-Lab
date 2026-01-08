package com.tech.kojo.ui.dashboard.library.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tech.kojo.R
import com.tech.kojo.data.model.LibrarySery
import com.tech.kojo.data.model.LibraryVideoX

class SectionAdapter(
    private val listener: OnSectionClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val sections = ArrayList<LibrarySection>()

    companion object {
        private const val TYPE_TOPIC = 0
        private const val TYPE_SERIES = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (sections[position]) {
            is LibrarySection.Topic -> TYPE_TOPIC
            is LibrarySection.SeriesRow -> TYPE_SERIES
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TOPIC -> TopicVH(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_section_topic, parent, false)
            )

            else -> SeriesRowVH(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_section_series_row, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val section = sections[position]) {
            is LibrarySection.Topic -> (holder as TopicVH).bind(section)

            is LibrarySection.SeriesRow -> (holder as SeriesRowVH).bind(section)
        }
    }


    override fun getItemCount(): Int = sections.size

    fun setList(data: List<LibrarySection>) {
        sections.clear()
        sections.addAll(data)
        notifyDataSetChanged()
    }


    inner class TopicVH(view: View) : RecyclerView.ViewHolder(view),
        VideoAdapter.OnVideoClickListener {

        private val title: TextView = view.findViewById(R.id.tvTitle)
        private val rvClick: RecyclerView = view.findViewById(R.id.rvSeries)
        private val tvSeeAll: AppCompatTextView = view.findViewById(R.id.tvSeeAll)

        private val adapter = VideoAdapter(this)
        private lateinit var currentSection: LibrarySection.Topic

        init {
            rvClick.layoutManager =
                LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
            rvClick.adapter = adapter
        }

        fun bind(section: LibrarySection.Topic) {
            currentSection = section
            title.text = section.title
            adapter.setList(section.videos)

            val pos = bindingAdapterPosition
            tvSeeAll.visibility =
                if (pos == RecyclerView.NO_POSITION || pos == 0) View.INVISIBLE else View.VISIBLE

            tvSeeAll.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    listener.onSeeAllTopicClick(
                        section = currentSection,
                        sectionPosition = bindingAdapterPosition
                    )
                }
            }
        }

        override fun onVideoClick(video: LibraryVideoX, childPosition: Int) {
            listener.onVideoItemClick(
                section = currentSection,
                sectionPosition = bindingAdapterPosition,
                video = video,
                childPosition = childPosition
            )
        }
    }




    inner class SeriesRowVH(view: View) : RecyclerView.ViewHolder(view),
        SeriesAdapter.OnSeriesClickListener {

        private val title: TextView = view.findViewById(R.id.tvTitle)
        private val rvClick: RecyclerView = view.findViewById(R.id.rvSeries)
        private val tvSeeAll: AppCompatTextView = view.findViewById(R.id.tvSeeAll)

        private val adapter = SeriesAdapter(this)
        private lateinit var currentSection: LibrarySection.SeriesRow

        init {
            rvClick.layoutManager =
                LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
            rvClick.adapter = adapter
        }

        fun bind(section: LibrarySection.SeriesRow) {
            currentSection = section
            title.text = section.title
            adapter.setList(section.seriesList)

            tvSeeAll.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    listener.onSeeAllSeriesClick(
                        section = currentSection,
                        sectionPosition = bindingAdapterPosition
                    )
                }
            }
        }

        override fun onSeriesClick(series: LibrarySery, childPosition: Int) {
            listener.onSeriesItemClick(
                section = currentSection,
                sectionPosition = bindingAdapterPosition,
                series = series,
                childPosition = childPosition
            )
        }
    }


    interface OnSectionClickListener {

        fun onVideoItemClick(
            section: LibrarySection.Topic,
            sectionPosition: Int,
            video: LibraryVideoX,
            childPosition: Int
        )

        fun onSeriesItemClick(
            section: LibrarySection.SeriesRow,
            sectionPosition: Int,
            series: LibrarySery,
            childPosition: Int
        )

        fun onSeeAllTopicClick(
            section: LibrarySection.Topic,
            sectionPosition: Int
        )

        fun onSeeAllSeriesClick(
            section: LibrarySection.SeriesRow,
            sectionPosition: Int
        )
    }



}

