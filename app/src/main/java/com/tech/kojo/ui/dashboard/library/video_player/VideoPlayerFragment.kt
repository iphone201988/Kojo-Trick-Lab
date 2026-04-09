package com.tech.kojo.ui.dashboard.library.video_player

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tech.kojo.BR
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.base.SimpleRecyclerViewAdapter
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.AddedCommentModel
import com.tech.kojo.data.model.CommentsData
import com.tech.kojo.data.model.GetRelatedVideoData
import com.tech.kojo.data.model.GetUserCommentsData
import com.tech.kojo.data.model.GetVideoByIdResponse
import com.tech.kojo.data.model.RelatedVideoData
import com.tech.kojo.data.room_module.DownloadVideoData
import com.tech.kojo.databinding.CommentBottomSheetItemBinding
import com.tech.kojo.databinding.FragmentVideoPlayerBinding
import com.tech.kojo.databinding.ItemSectionRvItemBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.utils.BaseCustomBottomSheet
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import com.tech.kojo.utils.showInfoToast
import com.tech.kojo.utils.showSuccessToast
import com.tech.kojo.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class VideoPlayerFragment : BaseFragment<FragmentVideoPlayerBinding>() {
    private val viewModel: VideoPlayerFragmentVM by viewModels()
    private lateinit var relatedAdapter: SimpleRecyclerViewAdapter<RelatedVideoData, ItemSectionRvItemBinding>
    private lateinit var commentsBottomSheet: BaseCustomBottomSheet<CommentBottomSheetItemBinding>
    private lateinit var commentsAdapter: VideoCommentsAdapter
    private var userVideoId: String? = null
    private var topicId: String? = null
    private var videoUrl: String? = null

    // Original unfiltered comment data
    private var originalCommentData = ArrayList<CommentsData>()
    private var filteredCommentData = ArrayList<CommentsData>()
    private var currentFilterType = FilterType.TOP

    private var videoTitle: String? = null
    private var thumbnailUrl: String? = null

    private var mCastContext: CastContext? = null
    private var mCastSession: CastSession? = null
    private var mSessionManagerListener: SessionManagerListener<CastSession>? = null

    private val args: VideoPlayerFragmentArgs by navArgs()

    private var downloadedVideos = ArrayList<DownloadVideoData>()
    private var deletePosition: Int? = null
    private var deletingCommentId: String? = null
    private var pendingCommentIndex: Int? = null

    enum class FilterType {
        TOP, MOST_RECENT
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_video_player
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        mCastContext = CastContext.getSharedInstance(requireContext())
        setupCastListener()
        viewModel.getAllVideos()
        observeDownloadedVideo()
        initAdapter()
        initOnClick()
        initObserver()
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

        val mediaInfo = MediaInfo.Builder(videoUrl!!).setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType("video/mp4").setMetadata(movieMetadata).build()

        remoteMediaClient.load(
            MediaLoadRequestData.Builder().setMediaInfo(mediaInfo).setAutoplay(true).build()
        )
    }

    override fun onResume() {
        super.onResume()
        val data = HashMap<String, Any>()
        if (args.topicId.isNotEmpty() && args.videoId.isNotEmpty()) {
            topicId = args.topicId
            userVideoId = args.videoId
            viewModel.getVideoById(Constants.GET_VIDEO_ID + "/${userVideoId}", data)
        }

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

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().finish()
                }

                R.id.clComments, R.id.tvNoCommentView -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        initBottomSheet()
                    }
                }

                R.id.ivVideo -> {
                    if (!videoUrl.isNullOrEmpty()) {
                        if (videoUrl!!.contains("vimeo")) {
                            val intent = Intent(requireContext(), CommonActivity::class.java)
                            intent.putExtra("fromWhere", "videoVimeo")
                            intent.putExtra("videoId", userVideoId)
                            intent.putExtra("videoUrl", videoUrl)
                            startActivity(intent)
                        } else {
                            val intent = Intent(requireContext(), CommonActivity::class.java)
                            intent.putExtra("fromWhere", "video")
                            intent.putExtra("videoId", userVideoId)
                            intent.putExtra("videoUrl", videoUrl)
                            startActivity(intent)
                        }
                    }
                }

                R.id.ivPerson -> {
                    val loggedInUserId = sharedPrefManager.getLoginData()?._id
                    val clickedUserId = getFirstValidUserId()

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
                        if (downloadedVideos.any { it.thumbnailUrl == thumbnailUrl }) {
                            showErrorToast("Video already downloaded")
                            return@observe
                        }
                        downloadAndSaveVideo()
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

    private fun getFirstValidUserId(): String? {
        return originalCommentData.firstOrNull { it.userId != null }?.userId?._id
    }

    private fun parseDate(dateString: String?): Date? {
        if (dateString.isNullOrEmpty()) return null
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            format.parse(dateString)
        } catch (e: Exception) {
            try {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                format.parse(dateString)
            } catch (e: Exception) {
                null
            }
        }
    }

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
                                    if (videoUrl!!.contains("vimeo")) {
                                        binding.clDownload.visibility = View.GONE
                                    } else {
                                        binding.clDownload.visibility = View.VISIBLE
                                    }
                                    videoTitle = model.data.title
                                    thumbnailUrl = model.data.thumbnailUrl
                                    if (downloadedVideos.any { it -> it.thumbnailUrl == thumbnailUrl }) {
                                        binding.tvDownload.text = "Downloaded"
                                        binding.tvDownload.setTextColor(
                                            ContextCompat.getColor(
                                                requireContext(), R.color.green_color
                                            )
                                        )
                                        binding.ivDownload.setImageResource(R.drawable.ic_download_video)
                                        binding.clDownload.backgroundTintList =
                                            ContextCompat.getColorStateList(
                                                requireContext(), R.color.green_color
                                            )
                                    } else {
                                        binding.tvDownload.setTextColor(
                                            ContextCompat.getColor(
                                                requireContext(), R.color.white
                                            )
                                        )
                                        binding.tvDownload.text = "Download"
                                        binding.ivDownload.setImageResource(R.drawable.download_icon)
                                        binding.clDownload.backgroundTintList =
                                            ContextCompat.getColorStateList(
                                                requireContext(), R.color.white
                                            )
                                    }
                                }
                            }.onFailure { e ->
                                showErrorToast(e.message.toString())
                            }.also {
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
                                    originalCommentData = model.data as ArrayList<CommentsData>
                                    filterComments(currentFilterType)
                                    updateMainCommentDisplay()
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
                                        model.data?.filterNot { it -> it?._id == userVideoId }
                                            .orEmpty()
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

                        "deletePostVideoComments" -> {
                            runCatching {
                                showSuccessToast("Comment deleted")
                                deleteCommentFromData(deletingCommentId)
                            }.onFailure { e ->
                                showErrorToast(e.message.orEmpty())
                            }.also {
                                hideLoading()
                                deletingCommentId = null
                                deletePosition = null
                            }
                        }

                        "postComments" -> {
                            runCatching {
                                val model = BindingUtils.parseJson<AddedCommentModel>(
                                    it.data?.toString().orEmpty()
                                )
                                if (model?.success == true) {
                                    val newComment = model.data
                                    if (newComment != null) {
                                        originalCommentData.add(0, newComment)
                                        filterComments(currentFilterType)
                                        commentsBottomSheet.binding.etComments.text?.clear()
                                        commentsBottomSheet.binding.rvMessage.scrollToPosition(0)
                                        showSuccessToast("Comment posted successfully")
                                    } else {
                                        showErrorToast("Failed to post comment: Invalid response")
                                    }
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
                    deletingCommentId = null
                    deletePosition = null
                    pendingCommentIndex = null
                }

                Status.LOADING -> showLoading()
                else -> {}
            }
        }
    }

    private fun deleteCommentFromData(commentId: String?) {
        if (commentId.isNullOrEmpty()) return

        // Find and remove from original data
        val originalPosition = originalCommentData.indexOfFirst { it._id == commentId }
        if (originalPosition != -1) {
            originalCommentData.removeAt(originalPosition)
        }

        // Find and remove from filtered data
        val filteredPosition = filteredCommentData.indexOfFirst { it._id == commentId }
        if (filteredPosition != -1) {
            filteredCommentData.removeAt(filteredPosition)

            if (::commentsAdapter.isInitialized) {
                commentsAdapter.setList(ArrayList(filteredCommentData))
                commentsAdapter.notifyItemRemoved(filteredPosition)
                commentsAdapter.notifyItemRangeChanged(
                    filteredPosition,
                    filteredCommentData.size - filteredPosition
                )
            }

            if (::commentsBottomSheet.isInitialized && commentsBottomSheet.isShowing) {
                commentsBottomSheet.binding.tvCommentsCounts.text =
                    filteredCommentData.size.toString()
            }
        }

        binding.tvCommentsCounts.text = originalCommentData.size.toString()
        updateMainCommentDisplay()
    }

    private fun updateMainCommentDisplay() {
        if (originalCommentData.isNotEmpty()) {
            binding.tvCommentsCounts.text = originalCommentData.size.toString()
            val firstValidComment = originalCommentData.firstOrNull { it.userId != null }

            if (firstValidComment != null) {
                binding.tvNoCommentView.visibility = View.GONE
                binding.tvMessage.visibility = View.VISIBLE
                binding.ivPerson.visibility = View.VISIBLE
                binding.tvMessage.text = firstValidComment.comment.toString()

                val url = firstValidComment.userId?.profilePicture
                val imageUrl = when {
                    url?.startsWith("http") == true -> url
                    url != null -> Constants.BASE_URL_IMAGE + url
                    else -> null
                }

                imageUrl?.let { imgUrl ->
                    Glide.with(requireContext()).load(imgUrl)
                        .placeholder(R.drawable.progress_animation_small)
                        .error(R.drawable.holder_dummy).into(binding.ivPerson)
                } ?: run {
                    binding.ivPerson.setImageResource(R.drawable.holder_dummy)
                }
            } else if (originalCommentData.isNotEmpty()) {
                binding.tvNoCommentView.visibility = View.GONE
                binding.tvMessage.visibility = View.VISIBLE
                binding.ivPerson.visibility = View.VISIBLE
                binding.tvMessage.text = originalCommentData[0].comment.toString()
                binding.ivPerson.setImageResource(R.drawable.holder_dummy)
            }
        } else {
            binding.tvNoCommentView.visibility = View.VISIBLE
            binding.tvMessage.visibility = View.INVISIBLE
            binding.ivPerson.visibility = View.INVISIBLE
            binding.tvCommentsCounts.text = "0"
            binding.tvMessage.text = "-"
            binding.ivPerson.setImageResource(R.drawable.holder_dummy)
        }
    }

    private fun filterComments(filterType: FilterType) {
        currentFilterType = filterType

        filteredCommentData = when (filterType) {
            FilterType.MOST_RECENT -> {
                originalCommentData.sortedByDescending { comment ->
                    parseDate(comment.createdAt) ?: Date(0)
                }
            }

            FilterType.TOP -> {
                originalCommentData.sortedBy { comment ->
                    parseDate(comment.createdAt) ?: Date(0)
                }
            }
        }.toCollection(ArrayList())

        if (::commentsBottomSheet.isInitialized && commentsBottomSheet.isShowing) {
            updateBottomSheetComments()
        }
    }

    private fun updateBottomSheetComments() {
        if (::commentsAdapter.isInitialized) {
            commentsAdapter.setList(ArrayList(filteredCommentData))
            commentsAdapter.notifyDataSetChanged()
            commentsBottomSheet.binding.tvCommentsCounts.text = filteredCommentData.size.toString()
        }
    }

    private fun initAdapter() {
        relatedAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_section_rv_item, BR.bean) { v, m, _ ->
                when (v?.id) {
                    R.id.cardView -> {
                        if (m._id?.isNotEmpty() == true && m.topicId?._id?.isNotEmpty() == true) {
                            val intent = Intent(requireContext(), CommonActivity::class.java)
                            intent.putExtra("videoId", m._id)
                            intent.putExtra("topicId", m.topicId._id)
                            intent.putExtra("categoryId", m.categoryId?._id)
                            intent.putExtra("fromWhere", "videoPlayer")
                            startActivity(intent)
                        }
                    }
                }
            }

        binding.rvRelated.adapter = relatedAdapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initBottomSheet() {
        commentsBottomSheet = BaseCustomBottomSheet(
            requireContext(), R.layout.comment_bottom_sheet_item
        ) { view ->
            when (view?.id) {
                R.id.ivSend -> {
                    val message =
                        commentsBottomSheet.binding.etComments.text?.toString()?.trim().orEmpty()

                    if (message.isNotEmpty()) {
                        userVideoId?.takeIf { it.isNotEmpty() }?.let {
                            val data = hashMapOf<String, Any>(
                                "comment" to message, "videoId" to it
                            )
                            viewModel.postComments(Constants.VIDEO_COMMENT, data)
                        }
                    } else {
                        showInfoToast("Please enter message")
                    }
                }

                R.id.tvTop -> {
                    commentsBottomSheet.binding.check = 1
                    filterComments(FilterType.TOP)
                }

                R.id.tvMost -> {
                    commentsBottomSheet.binding.check = 2
                    filterComments(FilterType.MOST_RECENT)
                }

                R.id.ivPerson -> {
                    // open my own profile
                }
            }
        }

        commentsBottomSheet.behavior.isDraggable = true
        commentsBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        commentsBottomSheet.show()
        commentsBottomSheet.binding.check = if (currentFilterType == FilterType.TOP) 1 else 2

        val profileUser = sharedPrefManager.getLoginData()?.profilePicture
        val imageUrl = when {
            profileUser?.startsWith("http") == true -> profileUser
            profileUser != null -> Constants.BASE_URL_IMAGE + profileUser
            else -> null
        }

        imageUrl?.let { url ->
            Glide.with(requireContext()).load(url).placeholder(R.drawable.progress_animation_small)
                .error(R.drawable.holder_dummy).into(commentsBottomSheet.binding.ivPerson2)
        }

        initCommentAdapter()

        commentsBottomSheet.setOnDismissListener {
            updateMainCommentDisplay()
        }

        commentsBottomSheet.binding.etComments.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initCommentAdapter() {
        val currentUserId = sharedPrefManager.getLoginData()?._id
        commentsAdapter =
            VideoCommentsAdapter(currentUserId, object : VideoCommentsAdapter.OnItemClickListener2 {
                override fun onItemClick(item: CommentsData?, clickedViewId: Int, position: Int) {
                    when (clickedViewId) {
                        R.id.ivPerson, R.id.tvComments -> {
                            val loggedInUserId = sharedPrefManager.getLoginData()?._id
                            val clickedUserId = item?.userId?._id

                            if (clickedUserId.isNullOrEmpty() || item?.userId == null) {
                                showInfoToast("User not available")
                                return
                            }

                            if (clickedUserId != loggedInUserId) {
                                val intent = Intent(requireContext(), CommonActivity::class.java)
                                intent.putExtra("userId", clickedUserId)
                                intent.putExtra("fromWhere", "userProfile")
                                startActivity(intent)
                                commentsBottomSheet.dismiss()
                            } else {
                                showInfoToast("You can't open your own profile")
                            }
                        }

                        R.id.tvdelete -> {
                            val commentId = item?._id

                            // Prevent deleting comments that haven't been saved yet
                            if (commentId.isNullOrEmpty()) {
                                showErrorToast("Please wait, comment is still being posted")
                                return
                            }
                            deletingCommentId = commentId
                            deletePosition = position
                            viewModel.deletePostVideoComments("${Constants.VIDEO_COMMENT_DELETE}/$commentId")
                        }
                    }
                }
            })

        commentsBottomSheet.binding.rvMessage.adapter = commentsAdapter

        if (filteredCommentData.isNotEmpty()) {
            commentsAdapter.setList(ArrayList(filteredCommentData))
            commentsBottomSheet.binding.tvCommentsCounts.text = filteredCommentData.size.toString()
        }
    }

    private fun downloadAndSaveVideo() {
        if (videoUrl.isNullOrEmpty()) return

        showLoading()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val fullUrl =
                    if (videoUrl!!.startsWith("http")) videoUrl!! else Constants.BASE_URL_IMAGE + videoUrl!!
                Log.d("VideoDownload", "Downloading from: $fullUrl")

                val localPath = requireContext().downloadVideo(
                    url = fullUrl, fileName = "${userVideoId ?: System.currentTimeMillis()}.mp4"
                )

                if (localPath == null) {
                    throw Exception("Download failed, localPath is null")
                }

                val downloadVideo = DownloadVideoData(
                    _id = userVideoId,
                    createdAt = System.currentTimeMillis().toString(),
                    thumbnailUrl = thumbnailUrl,
                    title = videoTitle ?: "Video",
                    videoDownload = true,
                    localPath = localPath
                )

                viewModel.insertVideo(downloadVideo)

                launch(Dispatchers.Main) {
                    hideLoading()
                    showSuccessToast("Video saved to download")
                    binding.tvDownload.text = "Downloaded"
                    binding.tvDownload.setTextColor(
                        ContextCompat.getColor(
                            requireContext(), R.color.green_color
                        )
                    )
                    binding.ivDownload.setImageResource(R.drawable.ic_download_video)
                    binding.clDownload.backgroundTintList =
                        ContextCompat.getColorStateList(requireContext(), R.color.green_color)
                    viewModel.getAllVideos()
                }

            } catch (e: Exception) {
                Log.e("VideoDownloadError", e.message ?: "Unknown error", e)

                launch(Dispatchers.Main) {
                    hideLoading()
                    showErrorToast("Video download failed. Please try again.")
                }
            }
        }
    }

    fun Context.downloadVideo(url: String, fileName: String): String? {
        var currentUrl = url
        var connection: HttpURLConnection? = null
        val file = File(filesDir, fileName)
        if (file.exists()) file.delete()

        try {
            var redirects = 0
            val maxRedirects = 5

            while (redirects < maxRedirects) {
                val urlObj = URL(currentUrl)
                connection = urlObj.openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.instanceFollowRedirects = true
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")

                val status = connection.responseCode
                if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER || status == 307 || status == 308) {

                    var newUrl = connection.getHeaderField("Location")
                    if (newUrl == null) break

                    if (newUrl.startsWith("/")) {
                        newUrl =
                            urlObj.protocol + "://" + urlObj.host + (if (urlObj.port != -1) ":" + urlObj.port else "") + newUrl
                    }

                    Log.d("VideoDownload", "Redirected ($status) to: $newUrl")
                    currentUrl = newUrl
                    redirects++
                    connection.disconnect()
                    continue
                }
                break
            }

            if (connection?.responseCode != HttpURLConnection.HTTP_OK) {
                Log.e("VideoDownload", "Server returned HTTP ${connection?.responseCode}")
                return null
            }

            val input = connection.inputStream
            val output = FileOutputStream(file)
            input.use { inp ->
                output.use { outp ->
                    inp.copyTo(outp)
                }
            }

            Log.d("VideoDownload", "Downloaded file size: ${file.length()} bytes")
            if (file.length() == 0L) {
                file.delete()
                return null
            }

            return file.absolutePath
        } catch (e: Exception) {
            Log.e("VideoDownload", "Error downloading: ${e.message}")
            if (file.exists()) file.delete()
            return null
        } finally {
            connection?.disconnect()
        }
    }

    private fun observeDownloadedVideo() {
        viewModel.observeVideo.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.LOADING -> {}
                Status.SUCCESS -> {
                    when (it.message) {
                        "getDownloadVideo" -> {
                            runCatching {
                                downloadedVideos = it.data as ArrayList<DownloadVideoData>
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(
                                    e.localizedMessage ?: getString(R.string.something_went_wrong)
                                )
                            }
                        }
                    }
                }

                Status.ERROR -> {
                    showErrorToast(it.message.toString())
                }

                else -> {}
            }
        }
    }
}