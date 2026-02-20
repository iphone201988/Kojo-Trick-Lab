package com.tech.kojo.ui.dashboard.library.video_player

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.tech.kojo.BR
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.base.SimpleRecyclerViewAdapter
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.CommentsData
import com.tech.kojo.data.model.GetRelatedVideoData
import com.tech.kojo.data.model.GetUserCommentsData
import com.tech.kojo.data.model.GetVideoByIdResponse
import com.tech.kojo.data.model.RelatedVideoData
import com.tech.kojo.data.model.UserIdProfile
import com.tech.kojo.databinding.CommentBottomSheetItemBinding
import com.tech.kojo.databinding.FragmentVideoPlayerBinding
import com.tech.kojo.databinding.ItemSectionRvItemBinding
import com.tech.kojo.databinding.MessageRvItemBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.utils.BaseCustomBottomSheet
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import com.tech.kojo.utils.showInfoToast
import com.tech.kojo.utils.showSuccessToast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import androidx.core.net.toUri
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.common.images.WebImage
import com.tech.kojo.utils.showToast

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
    private var videoTitle: String? = null

    private var mCastContext: CastContext? = null
    private var mCastSession: CastSession? = null
    private var mSessionManagerListener: SessionManagerListener<CastSession>? = null

    private val args: VideoPlayerFragmentArgs by navArgs()

    override fun getLayoutResource(): Int {
        return R.layout.fragment_video_player
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        mCastContext = CastContext.getSharedInstance(requireContext())
        setupCastListener()

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

    private fun setupCastListener() {
        mSessionManagerListener = object : SessionManagerListener<CastSession> {
            override fun onSessionEnded(session: CastSession, error: Int) {
                onApplicationDisconnected()
            }

            override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
                onApplicationConnected(session)
            }

            override fun onSessionResumeFailed(session: CastSession, error: Int) {
                onApplicationDisconnected()
            }

            override fun onSessionStarted(session: CastSession, sessionId: String) {
                onApplicationConnected(session)
            }

            override fun onSessionStartFailed(session: CastSession, error: Int) {
                onApplicationDisconnected()
            }

            override fun onSessionStarting(session: CastSession) {}
            override fun onSessionEnding(session: CastSession) {}
            override fun onSessionResuming(session: CastSession, sessionId: String) {}
            override fun onSessionSuspended(session: CastSession, reason: Int) {}

            private fun onApplicationConnected(session: CastSession) {
                mCastSession = session
                if (!videoUrl.isNullOrEmpty()) {
                    loadRemoteMedia()
                }
            }

            private fun onApplicationDisconnected() {
                mCastSession = null
            }
        }
    }

    private fun loadRemoteMedia() {
        if (mCastSession == null) {
            return
        }
        val remoteMediaClient = mCastSession!!.remoteMediaClient ?: return

        val movieMetadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE)
        movieMetadata.putString(MediaMetadata.KEY_TITLE, videoTitle ?: "Video")
        
        val mediaInfo = MediaInfo.Builder(videoUrl!!)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType("video/mp4")
            .setMetadata(movieMetadata)
            .build()

        remoteMediaClient.load(MediaLoadRequestData.Builder()
            .setMediaInfo(mediaInfo)
            .setAutoplay(true)
            .build())
    }

    override fun onResume() {
        super.onResume()
        mSessionManagerListener?.let {
            mCastContext?.sessionManager?.addSessionManagerListener(it, CastSession::class.java)
        }
    }

    override fun onPause() {
        super.onPause()
        mSessionManagerListener?.let {
            mCastContext?.sessionManager?.removeSessionManagerListener(it, CastSession::class.java)
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        initBottomSheet()
                    }
                }

                R.id.ivVideo -> {
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

                R.id.clDownload -> {
                    if (!videoUrl.isNullOrEmpty()) {
                        downloadVideo(videoUrl!!)
                    } else {
                        showErrorToast("Video URL not found")
                    }
                }

                R.id.clCast -> {
                    if (videoUrl.isNullOrEmpty()) {
                        showErrorToast("Video URL not found")
                        return@observe
                    }
                    val castSession = mCastContext?.sessionManager?.currentCastSession
                    if (castSession != null && castSession.isConnected) {
                        loadRemoteMedia()
                    } else {
                        showToast("Please connect to a Cast device first")
                    }
                }
            }
        }
    }

    private fun downloadVideo(url: String) {
        try {
            val fullUrl = if (url.startsWith("http")) url else Constants.BASE_URL_IMAGE + url
            val request = DownloadManager.Request(fullUrl.toUri())
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setTitle("Downloading Video")
            request.setDescription("Downloading video file...")
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${System.currentTimeMillis()}.mp4")

            val downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            showSuccessToast("Download started...")
        } catch (e: Exception) {
            Log.e("DownloadError", "Error: ${e.message}", e)
            showErrorToast("Download failed: ${e.message}")
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
                                    videoTitle = model.data.title
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

                                        val imageUrl = when {
                                            url?.startsWith("http") == true -> url
                                            url != null -> Constants.BASE_URL_IMAGE + url
                                            else -> null
                                        }
                                        imageUrl?.let { url ->
                                            Glide.with(requireContext())
                                                .load(url)
                                                .placeholder(R.drawable.progress_animation_small)
                                                .error(R.drawable.holder_dummy)
                                                .into(binding.ivPerson)
                                        }

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
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initBottomSheet() {
        commentsBottomSheet = BaseCustomBottomSheet(
            requireContext(), R.layout.comment_bottom_sheet_item
        ) { view ->
            when (view?.id) {
                R.id.ivSend -> {
                    val profile = sharedPrefManager.getLoginData()
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
                            createdAt = Instant.now().toString(),
                            updatedAt = Instant.now().toString(),
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
//                    val loggedInUserId = sharedPrefManager.getLoginData()?._id
                        showInfoToast("You can't open your own profile")
//                    val intent = Intent(requireContext(), CommonActivity::class.java)
//                    intent.putExtra("fromWhere", "userProfile")
//                    intent.putExtra("userId",loggedInUserId)
//                    startActivity(intent)
                }
            }
        }
        commentsBottomSheet.behavior.isDraggable = true
        commentsBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        commentsBottomSheet.show()
        commentsBottomSheet.binding.check = 1
        val profileUser = sharedPrefManager.getLoginData()?.profilePicture

        val imageUrl = when {
            profileUser?.startsWith("http") == true -> profileUser
            profileUser != null -> Constants.BASE_URL_IMAGE + profileUser
            else -> null
        }
        imageUrl?.let { url ->
            Glide.with(requireContext())
                .load(url)
                .placeholder(R.drawable.progress_animation_small)
                .error(R.drawable.holder_dummy)
                .into(commentsBottomSheet.binding.ivPerson)
        }


        // adapter
        initCommentAdapter()

        commentsBottomSheet.setOnDismissListener {
            if (commentsAdapter.list.isNotEmpty()) {
                binding.tvCommentsCounts.text = commentsAdapter.list.size.toString()
                binding.tvMessage.text = commentsAdapter.list[0].comment.toString()
                val url = commentsAdapter.list[0].userId?.profilePicture

                val imageUrl = when {
                    url?.startsWith("http") == true -> url
                    url != null -> Constants.BASE_URL_IMAGE + url
                    else -> null
                }
                imageUrl?.let { url ->
                    Glide.with(requireContext())
                        .load(url)
                        .placeholder(R.drawable.progress_animation_small)
                        .error(R.drawable.holder_dummy)
                        .into(binding.ivPerson)
                }

            } else {
                binding.tvCommentsCounts.text = "0"
                binding.tvMessage.text = "-"
                binding.ivPerson.setImageResource(R.drawable.holder_dummy)
            }
        }
        commentsBottomSheet.binding.etComments.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
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