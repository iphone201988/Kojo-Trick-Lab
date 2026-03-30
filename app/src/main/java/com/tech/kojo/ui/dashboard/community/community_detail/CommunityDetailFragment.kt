package com.tech.kojo.ui.dashboard.community.community_detail

import android.content.Intent
import android.content.res.ColorStateList
import android.util.Log
import android.view.View
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tech.kojo.BR
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.base.SimpleRecyclerViewAdapter
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.AddCommentApiResponse
import com.tech.kojo.data.model.GetCommentData
import com.tech.kojo.data.model.GetCommentsApiResponse
import com.tech.kojo.data.model.LikedApiResponse
import com.tech.kojo.data.model.PinnedApiResponse
import com.tech.kojo.data.model.PostData
import com.tech.kojo.data.model.PostDetailModel
import com.tech.kojo.databinding.FragmentCommunityDetailBinding
import com.tech.kojo.databinding.ItemLayoutCommentsBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import com.tech.kojo.utils.showInfoToast
import com.tech.kojo.utils.showSuccessToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommunityDetailFragment : BaseFragment<FragmentCommunityDetailBinding>() {
    private lateinit var commentAdapter: SimpleRecyclerViewAdapter<GetCommentData, ItemLayoutCommentsBinding>
    private val viewModel: CommunityDetailVm by viewModels()
    private var postId: String? = null
    private var isPinned: Boolean? = null
    private var isLiked: Boolean? = null
    private var communityID: String? = null
    private var videoLink: String? = null
    private var currentPage = 1
    private var scroll: Int = 0
    private var player: ExoPlayer? = null
    private var comments = ArrayList<GetCommentData>()
    private var communityData: PostData? = null
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
        pagination()
        // data set
        val loginData = sharedPrefManager.getLoginData()
        val imageUrl = when {
            loginData?.profilePicture?.startsWith("http") == true -> loginData.profilePicture
            loginData?.profilePicture != null -> Constants.BASE_URL_IMAGE + loginData.profilePicture
            else -> null
        }
        imageUrl?.let { url ->
            Glide.with(requireActivity()).load(url).placeholder(R.drawable.holder_dummy)
                .error(R.drawable.holder_dummy).into(binding.chatProfileImage)
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
                R.id.profileImage->{
                    val loggedInUserId = sharedPrefManager.getLoginData()?._id
                    val clickedUserId = communityData?.userData?._id

                    if (clickedUserId.isNullOrEmpty() || communityData?.userData?._id==null) {
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

                R.id.ivMaximize -> {
                    player?.pause()
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "video")
                    intent.putExtra("videoUrl", videoLink)
                    startActivity(intent)
                }
            }
        }
    }

    /**
     * play video if video compressed
     */
    @OptIn(UnstableApi::class)
    private fun showVideo(path: String) {
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(path))

        player = ExoPlayer.Builder(requireActivity()).build().also {
            binding.playerView.player = it
            it.setMediaSource(mediaSource)
            it.prepare()
            it.playWhenReady = true
        }
    }

    /**
     * Method to initialize view
     */
    private fun initView() {
        // get data
        communityData = arguments?.getParcelable<PostData>("communityData")
        if (communityData != null) {
            binding.tvLikes.text = "${communityData?.totalLikes}"
            binding.bean = communityData
            isPinned = communityData?.isPinned
            isLiked = communityData?.isLiked
            postId = communityData?._id
            binding.type = when (communityData?.postType) {
                "text" -> 1
                "video" -> 2
                "image" -> 3
                else -> 0
            }

            if (isPinned == true) {
                binding.ivPinIcon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.blue)
                )
            } else {
                binding.ivPinIcon.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.blue_40)
                )
            }
            communityID = communityData?._id
            videoLink = communityData?.videoLink
            if (isLiked==true){
                binding.ivLike.setImageResource(R.drawable.iv_heart)
            }
            else{
                binding.ivLike.setImageResource(R.drawable.iv_heart_unfilled)
            }
            // api call
            val data = HashMap<String, Any>()
            data["page"] = currentPage
            data["limit"] = 10
            viewModel.getCommentsApi(Constants.GET_COMMENTS + "/${communityID}", data,true)
        }
        else{
            val postId = arguments?.getString("postId")
            if (postId!=null){
                val request = HashMap<String, Any>()
                request["postId"]=postId
                viewModel.getPostDetailApi(Constants.POST_DETAIL,request)
            }
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
                                    // Toggle logic
                                    if (model.message!!.contains("Post unliked")){
                                        isLiked = false
                                    }
                                    else{
                                        isLiked = true
                                    }
//                                    isLiked = pinned
                                    if (isLiked == true) {
                                        binding.ivLike.setImageResource(R.drawable.iv_heart)
                                    } else {
                                        binding.ivLike.setImageResource(R.drawable.iv_heart_unfilled)
                                    }
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
                                    data["limit"] = 10
                                    viewModel.getCommentsApi(
                                        Constants.GET_COMMENTS + "/${communityID}", data,false
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
                                val list = model?.comments as ArrayList<GetCommentData>
                                if (currentPage == 1) {
                                    comments.clear()
                                }
                                comments.addAll(list)
                                commentAdapter.list = comments
                                binding.count.text = model?.total.toString()
                                scroll = if ((model?.page
                                        ?: 0) < (model?.totalPages
                                        ?: 0)
                                ) {
                                    1
                                } else {
                                    0
                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }
                        "getPostDetailApi"->{
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model : PostDetailModel?=BindingUtils.parseJson(jsonData)
                                if (model!=null){
                                    if (model.data!=null){
                                        binding.tvLikes.text = "${model.data.totalLikes}"
                                        binding.bean = model.data
                                        isPinned = model.data.isPinned
                                        isLiked = model.data.isLiked
                                        postId = model.data._id
                                        binding.type = when (model.data.postType) {
                                            "text" -> 1
                                            "video" -> 2
                                            "image" -> 3
                                            else -> 0
                                        }

                                        if (isPinned == true) {
                                            binding.ivPinIcon.imageTintList = ColorStateList.valueOf(
                                                ContextCompat.getColor(requireContext(), R.color.blue)
                                            )
                                        } else {
                                            binding.ivPinIcon.imageTintList = ColorStateList.valueOf(
                                                ContextCompat.getColor(requireContext(), R.color.blue_40)
                                            )
                                        }
                                        if (isLiked==true){
                                            binding.ivLike.setImageResource(R.drawable.iv_heart)
                                        }
                                        else{
                                            binding.ivLike.setImageResource(R.drawable.iv_heart_unfilled)
                                        }
                                        communityID = model.data._id
                                        videoLink = model.data.videoLink
                                    }
                                }

                            }.onFailure {
                                Log.e("apiErrorOccurred", "Error: ${it.message}", it)
                                showErrorToast(it.message.toString())
                            }.also {
                                // api call
                                val data = HashMap<String, Any>()
                                data["page"] = currentPage
                                data["limit"] = 10
                                viewModel.getCommentsApi(Constants.GET_COMMENTS + "/${communityID}", data,false)
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
                when(v.id){
                    R.id.profileImage->{
                        val loggedInUserId = sharedPrefManager.getLoginData()?._id
                        val clickedUserId = communityData?.userData?._id

                        if (clickedUserId.isNullOrEmpty() || communityData?.userData?._id==null) {
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
        binding.rvComments.adapter = commentAdapter

    }

    // pagination
    private fun pagination() {
        binding.scrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            val tolerance = 50
            if (scrollY >= (v.getChildAt(0).measuredHeight - v.measuredHeight - tolerance)) {
                if (scroll == 1) {
                    currentPage++
                    val data = HashMap<String, Any>()
                    data["page"] = currentPage
                    data["limit"] =10
                    viewModel.getCommentsApi(Constants.GET_COMMENTS + "/${communityID}", data,true)
                    scroll = 0
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }


}
