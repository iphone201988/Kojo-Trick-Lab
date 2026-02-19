package com.tech.kojo.ui.dashboard.library

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.LibrarySery
import com.tech.kojo.data.model.LibraryVideoResponse
import com.tech.kojo.data.model.LibraryVideoX
import com.tech.kojo.databinding.FragmentLibraryBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.ui.dashboard.library.adapter.LibrarySection
import com.tech.kojo.ui.dashboard.library.adapter.SectionAdapter
import com.tech.kojo.ui.dashboard.library.adapter.SectionAdapter.OnSectionClickListener
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LibraryFragment : BaseFragment<FragmentLibraryBinding>() {
    private val viewModel: LibraryVM by viewModels()
    private lateinit var sectionAdapter: SectionAdapter
    private var currentPage = 1

    override fun getLayoutResource(): Int {
        return R.layout.fragment_library
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // adapter
        initAdapter()
        // api call
        val data = HashMap<String, Any>()
        data["page"] = currentPage
        viewModel.getLibraryVideoApi(data, Constants.VIDEO_LIBRARY)
        // observer
        initObserver()
    }


    /** api response observer ***/
    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()
                }

                Status.SUCCESS -> {
                    when (it.message) {
                        "getLibraryVideoApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: LibraryVideoResponse? = BindingUtils.parseJson(jsonData)
                                val library = model?.data?.topics
                                if (!library.isNullOrEmpty()) {
                                    val sections = model.let { buildSections(it) }
                                    if (sections.isNotEmpty()) {
                                        sectionAdapter.setList(sections)
                                        binding.clEmpty.visibility = View.GONE
                                    } else {
                                        binding.clEmpty.visibility = View.VISIBLE
                                    }
                                } else {
                                    binding.clEmpty.visibility = View.VISIBLE
                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.orEmpty())
                            }.also {
                                hideLoading()
                            }
                        }


                    }
                }

                Status.ERROR -> {
                    hideLoading()
                    showErrorToast(it.message.toString())
                }

                else -> {

                }
            }
        }
    }

    /**
     * Initialize adapter
     */

    private fun initAdapter() {
        sectionAdapter = SectionAdapter(object : OnSectionClickListener {
            override fun onVideoItemClick(
                section: LibrarySection.Topic,
                sectionPosition: Int,
                video: LibraryVideoX,
                childPosition: Int
            ) {
                val intent = Intent(requireContext(), CommonActivity::class.java)
                intent.putExtra("videoId", video._id)
                intent.putExtra("topicId", video.topicId)
                intent.putExtra("categoryId", video.categoryId)
                intent.putExtra("fromWhere", "videoPlayer")
                startActivity(intent)
            }

            override fun onSeriesItemClick(
                section: LibrarySection.SeriesRow,
                sectionPosition: Int,
                series: LibrarySery,
                childPosition: Int
            ) {
                val intent = Intent(requireContext(), CommonActivity::class.java)
                intent.putExtra("fromWhere", "allVideo")
                intent.putExtra("videoTopicId", series._id)
                intent.putExtra("videoTitle", series.title)
                startActivity(intent)
            }

            override fun onSeeAllTopicClick(
                section: LibrarySection.Topic, sectionPosition: Int
            ) {
                val intent = Intent(requireContext(), CommonActivity::class.java)
                intent.putExtra("fromWhere", "fragmentSeeAll")
                intent.putExtra("topicId", section.id)
                intent.putExtra("title", section.title)
                startActivity(intent)
            }

            override fun onSeeAllSeriesClick(
                section: LibrarySection.SeriesRow, sectionPosition: Int
            ) {
                val intent = Intent(requireContext(), CommonActivity::class.java)
                intent.putExtra("fromWhere", "allVideo")
                intent.putExtra("videoTopicId", section.id)
                intent.putExtra("videoTitle", section.title)
                startActivity(intent)
            }
        })




        binding.rvRecently.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = sectionAdapter
        }
    }

    private fun buildSections(model: LibraryVideoResponse): List<LibrarySection> {
        val list = mutableListOf<LibrarySection>()
        model.data?.topics?.forEach { topic ->
            list.add(
                LibrarySection.Topic(
                    id = topic._id, title = topic.title, videos = topic.videos.orEmpty()
                )
            )
        }

        model.data?.series?.let { series ->
            if (series.isNotEmpty()) {

                series.forEach { item ->
                    list.add(
                        LibrarySection.SeriesRow(
                            id = item._id,
                            title = item.title,
                            seriesList = listOf(item)
                        )
                    )
                }
            }
        }

        return list
    }


}