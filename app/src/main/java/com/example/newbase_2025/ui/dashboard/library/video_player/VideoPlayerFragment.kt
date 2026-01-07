package com.example.newbase_2025.ui.dashboard.library.video_player

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.api.Constants
import com.example.newbase_2025.data.model.CommentsData
import com.example.newbase_2025.data.model.GetRelatedVideoData
import com.example.newbase_2025.data.model.GetUserCommentsData
import com.example.newbase_2025.data.model.GetVideoByIdResponse
import com.example.newbase_2025.data.model.RelatedVideoData
import com.example.newbase_2025.data.model.UserIdProfile
import com.example.newbase_2025.databinding.CommentBottomSheetItemBinding
import com.example.newbase_2025.databinding.FragmentVideoPlayerBinding
import com.example.newbase_2025.databinding.ItemSectionRvItemBinding
import com.example.newbase_2025.databinding.MessageRvItemBinding
import com.example.newbase_2025.ui.common.CommonActivity
import com.example.newbase_2025.utils.BaseCustomBottomSheet
import com.example.newbase_2025.utils.BindingUtils
import com.example.newbase_2025.utils.Status
import com.example.newbase_2025.utils.showErrorToast
import com.example.newbase_2025.utils.showInfoToast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideoPlayerFragment : BaseFragment<FragmentVideoPlayerBinding>() {
    private val viewModel: VideoPlayerFragmentVM by viewModels()
    private lateinit var relatedAdapter: SimpleRecyclerViewAdapter<RelatedVideoData, ItemSectionRvItemBinding>
    private lateinit var commentsBottomSheet: BaseCustomBottomSheet<CommentBottomSheetItemBinding>
    private lateinit var commentsAdapter: SimpleRecyclerViewAdapter<CommentsData, MessageRvItemBinding>
    private var userVideoId: String? = null
    private var topicId: String? = null
    private var videoUrl: String? = null
    private var commentData = ArrayList<CommentsData>()

    private val args: VideoPlayerFragmentArgs by navArgs()

    override fun getLayoutResource(): Int {
        return R.layout.fragment_video_player
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // adapter
        initAdapter()
        // click
        initOnClick()
        // observer
        initObserver()
        // api call
        val data = HashMap<String, Any>()
        if (args.topicId.isNotEmpty() && args.videoId.isNotEmpty()) {
            topicId = args.topicId
            userVideoId = args.videoId
            viewModel.getVideoById(Constants.GET_VIDEO_ID + "/${userVideoId}", data)
        }


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

                R.id.clComments -> {
                    initBottomSheet()
                }

                R.id.ivUser -> {
                    if (!videoUrl.isNullOrEmpty()) {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "video")
                        intent.putExtra("videoUrl", videoUrl)
                        startActivity(intent)
                    }
                }

                R.id.ivPerson -> {
                    val loggedInUserId = sharedPrefManager.getLoginData()?._id
                    val clickedUserId = commentData.firstOrNull()?.userId?._id
                    if (clickedUserId.isNullOrEmpty()) {
                        showInfoToast("User not available")
                        return@observe
                    }
                    if (clickedUserId != loggedInUserId) {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("userId", clickedUserId)
                        intent.putExtra("fromWhere", "userProfile")
                        startActivity(intent)
                    } else {
                        showInfoToast("You can't open your own profile")
                    }
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
                        "getVideoById" -> {
                            runCatching {
                                val model =
                                    BindingUtils.parseJson<GetVideoByIdResponse>(it.data.toString())
                                if (model?.success == true && model.data != null) {
                                    binding.bean = model.data
                                    videoUrl = model.data.videoUrl
                                }
                            }.onFailure { e ->
                                showErrorToast(e.message.toString())
                            }.also {
                                // api call
                                val data = HashMap<String, Any>()
                                viewModel.getUserRelated(
                                    Constants.VIDEO_RELATED + "/${args.topicId}", data
                                )
                            }
                        }

                        "getUserComment" -> {
                            runCatching {
                                val model =
                                    BindingUtils.parseJson<GetUserCommentsData>(it.data.toString())
                                if (model?.success == true && model.data != null) {
                                    commentData = model.data as ArrayList<CommentsData>
                                    if (commentData.isNotEmpty()) {
                                        binding.tvCommentsCounts.text = commentData.size.toString()
                                        binding.tvMessage.text = commentData[0].comment.toString()
                                        val url = commentData[0].userId?.profilePicture
                                        Glide.with(requireContext())
                                            .load(Constants.BASE_URL_IMAGE + url)
                                            .placeholder(R.drawable.progress_animation_small)
                                            .error(R.drawable.progress_animation_small)
                                            .into(binding.ivPerson)
                                    } else {
                                        binding.tvCommentsCounts.text = "0"
                                        binding.tvMessage.text = "-"
                                        binding.ivPerson.setImageResource(R.drawable.holder_dummy)
                                    }
                                }
                            }.onFailure { e ->
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }

                        "getUserRelated" -> {
                            runCatching {
                                val model = BindingUtils.parseJson<GetRelatedVideoData>(
                                    it.data?.toString().orEmpty()
                                )
                                if (model?.success == true) {
                                    val safeList =
                                        model.data?.filterNot { it?._id == userVideoId }.orEmpty()
                                    relatedAdapter.list = safeList
                                }
                            }.onFailure { e ->
                                showErrorToast(e.message.orEmpty())
                            }.also {
                                val data = HashMap<String, Any>()
                                viewModel.getUserComment(
                                    Constants.VIDEO_COMMENT + "/${userVideoId}", data
                                )
                            }
                        }

                        "postComments" -> {
                            runCatching {


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
        relatedAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_section_rv_item, BR.bean) { v, m, _ ->
                when (v?.id) {
                    R.id.cardView -> {
                        // api call
                        if (m._id?.isNotEmpty() == true && m.topicId?._id?.isNotEmpty() == true) {
                            topicId = m.topicId._id
                            userVideoId = m._id
                            val data = HashMap<String, Any>()
                            viewModel.getVideoById(Constants.GET_VIDEO_ID + "/${m._id}", data)
                        }
                    }
                }

            }

        binding.rvRelated.adapter = relatedAdapter
    }

    /**
     * Initialize bottom sheet
     */
    private fun initBottomSheet() {
        commentsBottomSheet = BaseCustomBottomSheet(
            requireContext(), R.layout.comment_bottom_sheet_item
        ) { view ->
            when (view?.id) {
                R.id.ivSend -> {
                    val profile = sharedPrefManager.getProfileData()
                    val message =
                        commentsBottomSheet.binding.etComments.text?.toString()?.trim().orEmpty()
                    if (message.isNotEmpty()) {
                        val userProfileData = profile?.let {
                            UserIdProfile(
                                _id = it._id, name = it.name, profilePicture = it.profilePicture
                            )
                        }
                        val newComment = CommentsData(
                            __v = null,
                            _id = null,
                            comment = message,
                            createdAt = "Just now",
                            updatedAt = null,
                            userId = userProfileData,
                            videoId = userVideoId
                        )
                        commentData.add(0, newComment)
                        commentsAdapter.list = ArrayList(commentData)
                        commentsAdapter.notifyItemInserted(0)
                        commentsBottomSheet.binding.etComments.text?.clear()
                        commentsBottomSheet.binding.tvCommentsCounts.text =
                            commentsAdapter.list.size.toString()
                        commentsBottomSheet.binding.rvMessage.scrollToPosition(0)
                        userVideoId?.takeIf { it.isNotEmpty() }?.let {
                            val data = hashMapOf<String, Any>(
                                "comment" to message, "videoId" to it
                            )
                            viewModel.postComments(Constants.VIDEO_COMMENTS, data)
                        }
                    } else {
                        showInfoToast("Please enter message")
                    }
                }

                R.id.tvTop -> {
                    commentsBottomSheet.binding.check = 1
                }

                R.id.tvMost -> {
                    commentsBottomSheet.binding.check = 2
                }

                R.id.ivPerson -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "userProfile")
                    startActivity(intent)
                }
            }
        }
        commentsBottomSheet.behavior.isDraggable = true
        commentsBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        commentsBottomSheet.show()
        commentsBottomSheet.binding.check = 1
        val profileUser = sharedPrefManager.getProfileData()?.profilePicture
        if (!profileUser.isNullOrEmpty()) {
            Glide.with(requireContext()).load(Constants.BASE_URL_IMAGE + profileUser)
                .placeholder(R.drawable.progress_animation_small).error(R.drawable.holder_dummy)
                .into(commentsBottomSheet.binding.ivPerson)
        }
        // adapter
        initCommentAdapter()

        commentsBottomSheet.setOnDismissListener {
            if (commentsAdapter.list.isNotEmpty()) {
                binding.tvCommentsCounts.text = commentsAdapter.list.size.toString()
                binding.tvMessage.text = commentsAdapter.list[0].comment.toString()
                val url = commentsAdapter.list[0].userId?.profilePicture
                Glide.with(requireContext()).load(Constants.BASE_URL_IMAGE + url)
                    .placeholder(R.drawable.progress_animation_small)
                    .error(R.drawable.progress_animation_small).into(binding.ivPerson)
            } else {
                binding.tvCommentsCounts.text = "0"
                binding.tvMessage.text = "-"
                binding.ivPerson.setImageResource(R.drawable.holder_dummy)
            }
        }
    }

    /**
     * Initialize comment adapter
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun initCommentAdapter() {
        commentsAdapter = SimpleRecyclerViewAdapter(R.layout.message_rv_item, BR.bean) { v, m, _ ->
            when (v?.id) {
                R.id.ivPerson -> {
                    val loggedInUserId = sharedPrefManager.getLoginData()?._id
                    val clickedUserId = m.userId?._id

                    if (clickedUserId.isNullOrEmpty()) {
                        showInfoToast("User not available")
                        return@SimpleRecyclerViewAdapter
                    }

                    if (clickedUserId != loggedInUserId) {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("userId", clickedUserId)
                        intent.putExtra("fromWhere", "userProfile")
                        startActivity(intent)
                    } else {
                        showInfoToast("You can't open your own profile")
                    }
                }

            }
        }
        commentsBottomSheet.binding.rvMessage.adapter = commentsAdapter
        if (commentData.isNotEmpty()) {
            commentsAdapter.list = commentData
            commentsAdapter.notifyDataSetChanged()
            commentsBottomSheet.binding.tvCommentsCounts.text = commentsAdapter.list.size.toString()

        }
    }
}