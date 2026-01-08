package com.tech.kojo.ui.dashboard.library.all_video

import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.tech.kojo.BR
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.base.SimpleRecyclerViewAdapter
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.GetRelatedVideoData
import com.tech.kojo.data.model.RelatedVideoData
import com.tech.kojo.databinding.AllVideosRvItemBinding
import com.tech.kojo.databinding.FragmentAllVideoBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllVideoFragment : BaseFragment<FragmentAllVideoBinding>() {
    private val viewModel: AllVideoFragmentVM by viewModels()
    private val args: AllVideoFragmentArgs by navArgs()

    private lateinit var allVideoAdapter: SimpleRecyclerViewAdapter<RelatedVideoData, AllVideosRvItemBinding>
    override fun getLayoutResource(): Int {
        return R.layout.fragment_all_video
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // adapter
        initAdapter()
        // api call
        if (args.videoTopicId.isNotEmpty()) {
            viewModel.getVideoTopic(Constants.VIDEO_RELATED + "/${args.videoTopicId}")
        }
        if (args.videoTitle.isNotEmpty()) {
            binding.tvLorem.text = args.videoTitle
        }
        // observer
        initObserver()
        // click
        initOnClick()
    }


    /**
     * Method to initialize click
     */
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().finish()
                }

            }
        }
    }


    /**
     * Method to initialize observer
     */
    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.SUCCESS -> {
                    when (it.message) {
                        "getVideoTopic" -> {
                            runCatching {
                                val model = BindingUtils.parseJson<GetRelatedVideoData>(
                                    it.data?.toString().orEmpty()
                                )
                                if (model?.success == true && model.data?.isNotEmpty() == true) {
                                    val safeList = model.data
                                    allVideoAdapter.list = safeList
                                    binding.tvVideoCount.text = "(${safeList.size} videos)"
                                    binding.clEmpty.visibility = View.GONE

                                } else {
                                    binding.clEmpty.visibility = View.VISIBLE
                                    binding.tvVideoCount.text = "(0 videos)"
                                }

                            }.onFailure { e ->
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

                Status.LOADING -> showLoading()
                else -> {

                }
            }
        }
    }

    /**
     * Initialize adapter
     */
    private fun initAdapter() {
        allVideoAdapter =
            SimpleRecyclerViewAdapter(R.layout.all_videos_rv_item, BR.bean) { v, m, _ ->
                when (v?.id) {
                    R.id.clForward -> {
                        // api call
                        if (m._id?.isNotEmpty() == true && m.topicId?._id?.isNotEmpty() == true && m.categoryId?._id?.isNotEmpty() == true) {
                            val intent = Intent(requireContext(), CommonActivity::class.java)
                            intent.putExtra("videoId", m._id)
                            intent.putExtra("topicId", m.topicId._id)
                            intent.putExtra("categoryId", m.categoryId._id)
                            intent.putExtra("fromWhere", "videoPlayer")
                            startActivity(intent)
                        }
                    }
                }

            }
        binding.rvAllVideo.adapter = allVideoAdapter

    }


}
