package com.tech.kojo.ui.dashboard.community

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.GetPostApiResponse
import com.tech.kojo.data.model.LikedApiResponse
import com.tech.kojo.data.model.PinnedApiResponse
import com.tech.kojo.data.model.PostData
import com.tech.kojo.databinding.FragmentCommunityBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.ui.dashboard.DashBoardActivity
import com.tech.kojo.ui.dashboard.community.adapter.FeedItem
import com.tech.kojo.ui.dashboard.community.adapter.MultiViewAdapter
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Resource
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import com.tech.kojo.utils.showInfoToast
import com.tech.kojo.utils.showSuccessToast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CommunityFragment : BaseFragment<FragmentCommunityBinding>() {
    private val viewModel: CommunityVm by viewModels()
    private var currentPage = 1
    private var isProgress = false
    private var postPosition = -1
    private var isLoading = false
    private var isLastPage = false
    private var isPopular = false
    private var player: ExoPlayer? = null

    private lateinit var communityAdapter: MultiViewAdapter
    override fun getLayoutResource(): Int {
        return R.layout.fragment_community
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // view
        initView()
        // click
        initOnClick()
        // observer
        initObserver()

        // data set
        val loginData = sharedPrefManager.getLoginData()
        val imageUrl = when {
            loginData?.profilePicture.isNullOrEmpty() -> null
            loginData?.profilePicture?.startsWith("http") == true -> loginData.profilePicture
            else -> Constants.BASE_URL_IMAGE + loginData?.profilePicture
        }

        if (imageUrl != null) {
            Glide.with(requireContext())
                .load(imageUrl)
                .placeholder(R.drawable.holder_dummy)
                .error(R.drawable.holder_dummy)
                .into(binding.profileImage)
        } else {
            binding.profileImage.setImageResource(R.drawable.holder_dummy)
        }
    }


    /** api response observer ***/
    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.LOADING -> {
                    if (!isProgress) {
                        showLoading()
                    }
                }

                Status.SUCCESS -> {
                    hideLoading()
                    when (it.message) {
                        "getPostApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: GetPostApiResponse? = BindingUtils.parseJson(jsonData)
                                if (model != null) {
                                    sharedPrefManager.setNotificationCount(model?.notifications)
                                    DashBoardActivity.notificationObserver.postValue(
                                        Resource.success(
                                            "notificationObserver", true
                                        )
                                    )
                                    val posts = model.posts
                                    // Convert PostData → FeedItem.Post
                                    val feedItems: List<FeedItem> =
                                        posts?.filterNotNull()?.map { FeedItem.Post(it) }
                                            ?: emptyList()
                                    isLoading = false
                                    isLastPage = false
                                    isProgress = true
                                    if (currentPage == 1) {
                                        communityAdapter.setList(feedItems)
                                    } else {
                                        communityAdapter.addToList(feedItems)
                                    }
                                    isLastPage = currentPage == model.totalPages

                                    if (communityAdapter.getList().isNotEmpty()) {
                                        binding.clEmpty.visibility = View.GONE
                                    } else {
                                        binding.clEmpty.visibility = View.VISIBLE
                                    }
                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())

                            }.also {
                                communityAdapter.hideLoader()
                                hideLoading()
                            }
                        }

                        "postLikeApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: LikedApiResponse? = BindingUtils.parseJson(jsonData)
                                if (model?.success == true) {
                                    val updatedLikes = model.likesCount ?: 0
                                    var isLiked = false
                                    // Toggle logic
                                    if (model.message!!.contains("Post unliked")){
                                        isLiked = false
                                    }
                                    else{
                                        isLiked = true
                                    }
                                    val list = communityAdapter.getList()
                                    val item = list[postPosition]
                                    if (item is FeedItem.Post) {
                                        item.post.totalLikes = updatedLikes
                                        item.post.isLiked = isLiked
                                        communityAdapter.notifyItemChanged(postPosition)
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
                                if (model?.success == true) {
                                    val item = communityAdapter.getList()[postPosition]
                                    if (item is FeedItem.Post) {
                                        item.post.isPinned = model.isPinned == true
                                        communityAdapter.notifyItemChanged(postPosition)
                                    }
                                }

                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())

                            }.also {
                                hideLoading()
                            }
                        }
                        "postDeleteApi" -> {
                            hideLoading()
                            showSuccessToast("Post deleted successfully")
                            if (postPosition != -1 && postPosition < communityAdapter.getList().size) {
                                communityAdapter.getList().removeAt(postPosition)
                                communityAdapter.notifyItemRemoved(postPosition)
                                communityAdapter.notifyItemRangeChanged(postPosition, communityAdapter.itemCount)
                                if (communityAdapter.getList().isEmpty()) {
                                    binding.clEmpty.visibility = View.VISIBLE
                                }
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
     * Method to initialize view
     */

    private fun initView() {
        binding.check = 1
        // adapter
        initAdapter()
        // api call
        getApi(isPopular)

        // pagination
        pagination()

        // refresh
        binding.ssPullRefresh.setColorSchemeResources(
            ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        )
        binding.ssPullRefresh.setOnRefreshListener {
            Handler().postDelayed({
                binding.ssPullRefresh.isRefreshing = false
                isProgress = true
                if (binding.check==3){
                    val data = HashMap<String, Any>()
                    data["pinned"] = true
                    viewModel.getPostApi(data, Constants.GET_POST)
                }
                else{
                    getApi(isPopular)
                }

            }, 2000)
        }
    }

    private fun getApi(isPopular: Boolean) {
        val data = HashMap<String, Any>()
        data["page"] = currentPage
        data["isPopular"] = isPopular
        viewModel.getPostApi(data, Constants.GET_POST)
    }

    /**
     * home adapter handel pagination
     */
    private fun pagination() {
        binding.rvCommunity.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        loadMoreItems()
                    }
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    stopInLinePlayback()
                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //   playCenterVideoRvAdapter(recyclerView)
                }
            }
        })
    }

    private fun stopInLinePlayback() {
        if (player != null) {
            player?.stop()
            player?.release()
            player = null
            communityAdapter.setPlayingPosition(-1, null)
        }
    }

    /**
     *  load more function call
     **/
    private fun loadMoreItems() {
        communityAdapter.showLoader()
        isProgress = true
        isLoading = true
        currentPage++
        val data = HashMap<String, Any>()
        data["page"] = currentPage
        viewModel.getPostApi(data, Constants.GET_POST)
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

                R.id.tvNewest -> {
                    binding.check = 1
                    isPopular = false
                    getApi(false)
                    showLoading()
                }

                R.id.consPinned -> {
                    binding.check = 3
                    val data = HashMap<String, Any>()
                    data["page"] = currentPage
                    data["pinned"] = true
                    viewModel.getPostApi(data, Constants.GET_POST)
                }

                R.id.tvCompleteTask -> {
                    binding.check = 2
                    isPopular = true
                    getApi(true)
                    showLoading()
                }

                R.id.createPost -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "createPost")
                    startActivity(intent)
                }

            }
        }
    }

    /**
     * Method to initialize adapter
     */

    private fun initAdapter() {
        val myId = sharedPrefManager.getLoginData()?._id
        communityAdapter = MultiViewAdapter(myId,object : MultiViewAdapter.OnItemClickListener {
            override fun onItemClick(item: PostData?, clickedViewId: Int, position: Int) {
                postPosition = position
                when (clickedViewId) {
                    R.id.cardView -> {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "communityDetail")
                        intent.putExtra("communityData", item)
                        startActivity(intent)
                    }
                        R.id.profileImage->{
                            val loggedInUserId = sharedPrefManager.getLoginData()?._id
                            val clickedUserId = item?.userData?._id

                            if (clickedUserId.isNullOrEmpty() || item?.userData?._id==null) {
                                showInfoToast("User not available")
                                return
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

                    R.id.ivVideo -> {
                        playInLine(item, position)
                    }

                    R.id.ivMaximize -> {
                        player?.pause()
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "video")
                        intent.putExtra("videoUrl", item?.videoLink)
                        startActivity(intent)
                    }

                    R.id.ivLike -> {
                        val data = HashMap<String, Any>()
                        data["postId"] = item?._id.toString()
                        viewModel.postLikeApi(Constants.POST_LIKE, data)
                    }

                    R.id.ivPinIcon -> {
                        val data = HashMap<String, Any>()
                        data["postId"] = item?._id.toString()
                        viewModel.postPinApi(Constants.POST_PIN, data)
                    }
                    R.id.tvdelete->{
                        val loginData = sharedPrefManager.getLoginData()
                        if (item?.userData?._id == loginData?._id) {
                            viewModel.postDeleteApi("${Constants.DELETE_POST}/${item?._id}")
                        } else {
                            showInfoToast("You can only delete your own posts")
                        }
                    }
                }
            }
        })

        binding.rvCommunity.adapter = communityAdapter
    }

    @OptIn(UnstableApi::class)
    private fun playInLine(item: PostData?, position: Int) {
        if (item?.videoLink == null) return

        // Stop previous player
        player?.stop()
        player?.release()

        val videoUrl = if (item.videoLink.startsWith("http")) {
            item.videoLink
        } else {
            Constants.BASE_URL_IMAGE + item.videoLink
        }

        Log.d("CommunityFragment", "Playing video: $videoUrl")

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)

        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(videoUrl))

        player = ExoPlayer.Builder(requireContext()).build().apply {

            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true

            addListener(object : Player.Listener {

                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING ->
                            Log.d("CommunityFragment", "Buffering at pos $position")

                        Player.STATE_READY ->
                            Log.d("CommunityFragment", "Ready to play at pos $position")

                        Player.STATE_ENDED ->
                            Log.d("CommunityFragment", "Video ended at pos $position")

                        Player.STATE_IDLE ->
                            Log.d("CommunityFragment", "Player idle at pos $position")
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {

                    // 🔥 Get current visible ViewHolder
                    val viewHolder = binding.rvCommunity
                        .findViewHolderForAdapterPosition(position)
                            as? MultiViewAdapter.VideoPostViewHolder

                    val playerView = viewHolder?.binding?.playerView ?: return

                    // Enable controller
                    playerView.useController = true
                    playerView.controllerShowTimeoutMs = 2000

                    if (isPlaying) {
                        // ▶️ Playing → hide controller smoothly
                        playerView.postDelayed({
                            playerView.hideController()
                        }, 500)
                    } else {
                        // ⏸ Paused → show controller
                        playerView.showController()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e(
                        "CommunityFragment",
                        "Player error at pos $position: ${error.message}",
                        error
                    )
                    showErrorToast("Playback error: ${error.localizedMessage}")
                }
            })
        }

        // Attach player to adapter
        communityAdapter.setPlayingPosition(position, player)
    }

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }


}
