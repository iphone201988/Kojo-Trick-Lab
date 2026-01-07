package com.example.newbase_2025.ui.dashboard.community

import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.data.api.Constants
import com.example.newbase_2025.data.model.GetPostApiResponse
import com.example.newbase_2025.data.model.LikedApiResponse
import com.example.newbase_2025.data.model.PinnedApiResponse
import com.example.newbase_2025.data.model.PostData
import com.example.newbase_2025.databinding.FragmentCommunityBinding
import com.example.newbase_2025.ui.common.CommonActivity
import com.example.newbase_2025.ui.dashboard.community.adapter.FeedItem
import com.example.newbase_2025.ui.dashboard.community.adapter.MultiViewAdapter
import com.example.newbase_2025.utils.BindingUtils
import com.example.newbase_2025.utils.Status
import com.example.newbase_2025.utils.showErrorToast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CommunityFragment : BaseFragment<FragmentCommunityBinding>() {
    private val viewModel: CommunityVm by viewModels()
    private var currentPage = 1
    private var isProgress = false
    private var postPosition = -1
    private var isLoading = false
    private var isLastPage = false

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
                    when (it.message) {
                        "getPostApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: GetPostApiResponse? = BindingUtils.parseJson(jsonData)
                                if (model != null) {
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

                                    if (communityAdapter.getList().isNotEmpty()){
                                        binding.clEmpty.visibility = View.GONE
                                    }
                                    else{
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
                                    val list = communityAdapter.getList()
                                    val item = list[postPosition]
                                    if (item is FeedItem.Post) {
                                        item.post.totalLikes = updatedLikes
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
        val data = HashMap<String, Any>()
        data["page"] = currentPage
        viewModel.getPostApi(data, Constants.GET_POST)

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
                // api call
                // api call
                val data = HashMap<String, Any>()
                data["page"] = 1
                viewModel.getPostApi(data, Constants.GET_POST)
            }, 2000)
        }
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
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //   playCenterVideoRvAdapter(recyclerView)
                }
            }
        })
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
                    val data = HashMap<String, Any>()
                    data["page"] = currentPage
                    viewModel.getPostApi(data, Constants.GET_POST)
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
                    val data = HashMap<String, Any>()
                    data["page"] = currentPage
                    viewModel.getPostApi(data, Constants.GET_POST)
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
        communityAdapter = MultiViewAdapter(object : MultiViewAdapter.OnItemClickListener {
            override fun onItemClick(item: PostData?, clickedViewId: Int, position: Int) {
                postPosition = position
                when (clickedViewId) {
                    R.id.cardView -> {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "communityDetail")
                        intent.putExtra("communityData", item)
                        startActivity(intent)
                    }

                    R.id.ivVideo -> {
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
                }
            }
        })

        binding.rvCommunity.adapter = communityAdapter
    }


}