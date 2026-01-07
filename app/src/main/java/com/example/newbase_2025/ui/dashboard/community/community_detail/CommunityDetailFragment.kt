package com.example.newbase_2025.ui.dashboard.community.community_detail

import android.content.res.ColorStateList
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.api.Constants
import com.example.newbase_2025.data.model.AddCommentApiResponse
import com.example.newbase_2025.data.model.GetCommentData
import com.example.newbase_2025.data.model.GetCommentsApiResponse
import com.example.newbase_2025.data.model.LikedApiResponse
import com.example.newbase_2025.data.model.PinnedApiResponse
import com.example.newbase_2025.data.model.PostData
import com.example.newbase_2025.databinding.FragmentCommunityDetailBinding
import com.example.newbase_2025.databinding.ItemLayoutCommentsBinding
import com.example.newbase_2025.utils.BindingUtils
import com.example.newbase_2025.utils.Status
import com.example.newbase_2025.utils.showErrorToast
import com.example.newbase_2025.utils.showSuccessToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommunityDetailFragment : BaseFragment<FragmentCommunityDetailBinding>() {
    private lateinit var commentAdapter: SimpleRecyclerViewAdapter<GetCommentData, ItemLayoutCommentsBinding>
    private val viewModel: CommunityDetailVm by viewModels()
    private var postId: String? = null
    private var isPinned: Boolean? = null
    private var communityID: String? = null
    private var videoLink: String? = null
    private val currentPage = 1
    private var player: ExoPlayer? = null
    private var comments = ArrayList<GetCommentData>()

    override fun getLayoutResource(): Int {
        return R.layout.fragment_community_detail
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        initOnClick()
        // view
        initView()
        // observer
        initObserver()
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

                R.id.ivLike -> {
                    val data = HashMap<String, Any>()
                    if (postId != null) {
                        data["postId"] = postId!!
                        viewModel.postLikeApi(Constants.POST_LIKE, data)
                    }

                }

                R.id.ivPinIcon -> {
                    if (postId != null) {
                        val data = HashMap<String, Any>()
                        data["postId"] = postId!!
                        viewModel.postPinApi(Constants.POST_PIN, data)
                    }

                }

                R.id.sendChat -> {
                    if (binding.etChat.text.toString().trim().isNotEmpty()) {
                        val data = HashMap<String, Any>()
                        data["postId"] = postId!!
                        data["message"] = binding.etChat.text.toString().trim()
                        viewModel.postCommentApi(Constants.POST_COMMENT, data)
                        binding.etChat.setText("")
                    } else {
                        showSuccessToast("Please enter your response")
                    }

                }

                R.id.ivVideoPlay -> {
                    binding.cardPostVideoPlayer.visibility = View.VISIBLE
                    binding.ivVideo.visibility = View.INVISIBLE
                    binding.ivVideoPlay.visibility = View.GONE
                    if (videoLink != null) {
                        showVideo(Constants.BASE_URL_IMAGE + videoLink)
                    } else {
                        showSuccessToast("Video not found")
                    }
                }
            }
        }
    }

    /**
     * play video if video compressed
     */
    private fun showVideo(path: String) {
        player = ExoPlayer.Builder(requireActivity()).build().also {
            binding.playerView.player = it
            val mediaItem = MediaItem.fromUri(path)
            it.setMediaItem(mediaItem)
            it.prepare()
            it.playWhenReady = true
        }
    }

    /**
     * Method to initialize view
     */
    private fun initView() {
        // get data
        val communityData = arguments?.getParcelable<PostData>("communityData")
        if (communityData != null) {
            binding.tvLikes.text = "${communityData.totalLikes}"
            binding.bean = communityData
            isPinned = communityData.isPinned
            postId = communityData._id
            binding.type = if (communityData.postType == "text") 1 else 2

            if (isPinned == true) {
                binding.ivPinIcon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.blue)
                )
            } else {
                binding.ivPinIcon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.blue_40)
                )
            }
            communityID = communityData._id
            videoLink = communityData.videoLink
            // api call
            val data = HashMap<String, Any>()
            data["page"] = currentPage
            viewModel.getCommentsApi(Constants.GET_COMMENTS + "/${communityID}", data)
        }
        // adapter
        initAdapter()
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
                        "postLikeApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: LikedApiResponse? = BindingUtils.parseJson(jsonData)
                                var like = model?.likesCount
                                if (model?.success == true) {
                                    binding.tvLikes.text = "$like"
                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }

                        "postPinApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: PinnedApiResponse? = BindingUtils.parseJson(jsonData)
                                val pinned = model?.isPinned
                                if (model?.success == true) {
                                    // Toggle logic
                                    isPinned = pinned
                                    if (isPinned == true) {
                                        binding.ivPinIcon.imageTintList = ColorStateList.valueOf(
                                            ContextCompat.getColor(requireContext(), R.color.blue)
                                        )
                                    } else {
                                        binding.ivPinIcon.imageTintList = ColorStateList.valueOf(
                                            ContextCompat.getColor(
                                                requireContext(), R.color.blue_40
                                            )
                                        )
                                    }
                                }

                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }

                        "postCommentApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: AddCommentApiResponse? = BindingUtils.parseJson(jsonData)
                                if (model?.success == true) {
                                    // api call
                                    val data = HashMap<String, Any>()
                                    data["page"] = currentPage
                                    viewModel.getCommentsApi(
                                        Constants.GET_COMMENTS + "/${communityID}", data
                                    )
                                }
                            }.onFailure { e ->
                                hideLoading()
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
                            }.also {

                            }
                        }

                        "getCommentsApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: GetCommentsApiResponse? =
                                    BindingUtils.parseJson(jsonData)
                                val comment = model?.comments
                                if (comment?.isNotEmpty() == true) {
                                    comments = comment as ArrayList<GetCommentData>
                                    commentAdapter.list = comments
                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
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
     * Method to initialize click
     */
    private fun initAdapter() {
        commentAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_layout_comments, BR.bean) { v, m, pos ->

            }
        binding.rvComments.adapter = commentAdapter

    }


    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }


}