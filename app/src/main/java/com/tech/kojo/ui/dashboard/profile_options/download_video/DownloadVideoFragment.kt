package com.tech.kojo.ui.dashboard.profile_options.download_video

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.tech.kojo.BR
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.base.SimpleRecyclerViewAdapter
import com.tech.kojo.data.room_module.DownloadVideoData
import com.tech.kojo.databinding.FragmentDownloadVideoBinding
import com.tech.kojo.databinding.RvDownloadItemBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DownloadVideoFragment : BaseFragment<FragmentDownloadVideoBinding>() {
    private val viewModel: DownloadVideoVM by viewModels()
    private lateinit var downloadAdapter: SimpleRecyclerViewAdapter<DownloadVideoData, RvDownloadItemBinding>

    companion object {
        var data = 0
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_download_video
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // view
        initView()
        // click
        initOnClick()
        // adapter
        initOnDownloadAdapter()

        viewModel.getAllVideos()
        // observer
        initObserver()
    }

    private fun initView() {
        binding.clCommon.tvHeader.text = "Downloads"
    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer {
            when (it?.id) {
                R.id.ivBack -> {
                    if (data == 2) {
                        requireActivity().finishAffinity()
                        requireActivity().finishAndRemoveTask()
//                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    } else {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })
    }

    /** api response observer ***/
    private fun initObserver() {
        viewModel.observeVideo.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()
                }

                Status.SUCCESS -> {
                    when (it.message) {
                        "getDownloadVideo" -> {
                            runCatching {
                                downloadAdapter.list = it.data
                                if (downloadAdapter.list.isNotEmpty()) {
                                    binding.clEmpty.visibility = View.GONE
                                } else {
                                    binding.clEmpty.visibility = View.VISIBLE
                                }

                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                binding.clEmpty.visibility = View.VISIBLE
                                showErrorToast(
                                    e.localizedMessage ?: getString(R.string.something_went_wrong)
                                )
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
     * download video adapter
     */
    private fun initOnDownloadAdapter() {
        downloadAdapter =
            SimpleRecyclerViewAdapter(R.layout.rv_download_item, BR.bean) { v, m, pos ->
                when (v?.id) {
                    R.id.ivVideo, R.id.cardView -> {
                        if (!m.localPath.isNullOrEmpty()) {
                            val intent = Intent(requireContext(), CommonActivity::class.java)
                            //intent.putExtra("fromWhere", "videoVimeo")
                            intent.putExtra("fromWhere", "video")
                            intent.putExtra("videoPath", m.localPath)
                            startActivity(intent)
                        }
                    }

                    R.id.ivDelete -> {
                        Log.e("kldkd", "initOnDownloadAdapter: $m")
                        viewModel.deleteVideo(m.id.toString())

                        val index = downloadAdapter.list.indexOfFirst { it._id == m._id }
                        if (index != -1) {
                            downloadAdapter.list.removeAt(index)
                            downloadAdapter.notifyItemRemoved(index)
                        }

                        binding.clEmpty.visibility =
                            if (downloadAdapter.list.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
            }

        binding.rvDownload.adapter = downloadAdapter
    }

//    override fun onResume() {
//        super.onResume()
//        viewModel.notificationCount.value = sharedPrefManager.getNotificationCount()
//    }

}