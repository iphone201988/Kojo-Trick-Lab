package com.tech.kojo.ui.dashboard.tracker.progression_details

import android.content.Intent
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.tech.kojo.BR
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.base.SimpleRecyclerViewAdapter
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.HomeProgressApiResponse
import com.tech.kojo.data.model.HomeProgressStep
import com.tech.kojo.data.model.Prerequisites
import com.tech.kojo.data.model.VideoLink
import com.tech.kojo.databinding.FragmentProgressionDetailsBinding
import com.tech.kojo.databinding.PrerequistesRvItemBinding
import com.tech.kojo.databinding.ProgressionDetailsRvItemBinding
import com.tech.kojo.databinding.StepRvItemBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProgressionDetailsFragment : BaseFragment<FragmentProgressionDetailsBinding>() {
    private val viewModel: ProgressionDetailsFragmentVM by viewModels()
    private lateinit var progressionDetailsAdapter: SimpleRecyclerViewAdapter<HomeProgressStep, ProgressionDetailsRvItemBinding>
    private var trackDetailId: String? = null
    private var diveName: String? = null
    private var allVideos: List<VideoLink> = emptyList()
    private var reorderedVideos: List<VideoLink> = emptyList()
    private var videoPagerAdapter: UserImagePagerAdapter? = null

    private var pagerRecyclerView: RecyclerView? = null
    private lateinit var stepAdapter: SimpleRecyclerViewAdapter<Prerequisites, PrerequistesRvItemBinding>
    private var status :String?=null

    override fun getLayoutResource(): Int {
        return R.layout.fragment_progression_details
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // adapter
        initProgressionDetailsAdapter()
        initStepAdapter()
        status = arguments?.getString("status")
        if (status != null) {
            binding.status = status
        }

        // click
        initOnClick()

        // observer
        initObserver()

        setupSwipeGestures()
    }

    override fun onResume() {
        super.onResume()
        //view
        initView()
        pagerRecyclerView?.post {
            videoPagerAdapter?.playVideoAt(binding.viewpager.currentItem, pagerRecyclerView!!)
        }
    }

    override fun onPause() {
        super.onPause()
        videoPagerAdapter?.releaseAllPlayers(pagerRecyclerView!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        videoPagerAdapter?.releaseAllPlayers(pagerRecyclerView!!)
    }

    /**
     * Method to initialize view
     */
    private fun initView() {
        // get argument
        trackDetailId = arguments?.getString("trackDetailId")

        //  API call
        trackDetailId?.let { id ->
            val data = HashMap<String, Any>()
            viewModel.getTrickDataByIdApi(data, Constants.TRICKS_DATA + "/$id")
        } ?: run {
            showErrorToast("Something went wrong!")
        }
    }

    /**
     * Method to initialize click
     */
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.ivProgress,R.id.ivClosePlayer -> {
                    requireActivity().finish()
                }
            }
        }
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
                        "getTrickDataByIdApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: HomeProgressApiResponse? =
                                    BindingUtils.parseJson(jsonData)
                                if (model != null) {
                                    diveName = model.data?.name
                                    val home = model.data
                                    home?.description?.takeIf { it.isNotBlank() }?.let {
                                        binding.tvTrack.text = it
                                        binding.tvTrack.visibility = View.VISIBLE
                                    } ?: run {
                                        binding.tvTrack.visibility = View.GONE
                                    }
                                    binding.tvSkip.text = home?.name.orEmpty()
                                    (home?.prerequisites ?: emptyList()).also { list ->
                                        stepAdapter.list = list

                                        val isVisible = list.isNotEmpty()
                                        binding.rvStep.visibility = if (isVisible) View.VISIBLE else View.GONE
                                        binding.tvTricker.visibility = if (isVisible) View.VISIBLE else View.GONE
                                    }
                                    // Safe list
                                    val safeList = home?.steps ?: emptyList()
                                    progressionDetailsAdapter.list = safeList

                                    // Get all videos
                                    allVideos = home?.steps?.flatMap { it?.videoLinks ?: emptyList() }
                                        ?.filterNotNull() ?: emptyList()

                                    // DYNAMIC REORDERING: Last -> First -> Second -> ... -> Last
                                    reorderedVideos = reorderVideosDynamically(allVideos)

                                    // Setup ViewPager with reordered videos
                                    setupVideoViewPager()
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
     * Dynamically reorder videos: Last -> First -> Second -> ... -> Last
     * Works with ANY number of videos (1, 2, 3, 4, 5, 8, 10, etc.)
     */
    private fun reorderVideosDynamically(originalVideos: List<VideoLink>): List<VideoLink> {
        if (originalVideos.isEmpty()) return emptyList()

        // For single video, show it only once
        if (originalVideos.size == 1) {
            return listOf(originalVideos[0])
        }

        val reorderedList = mutableListOf<VideoLink>()

        // Add last video first
        val lastVideo = originalVideos.last()
        reorderedList.add(lastVideo)

        // Add all videos except last
        reorderedList.addAll(originalVideos.dropLast(1))

        // Add last video again at the end
        reorderedList.add(lastVideo)

        return reorderedList
    }

    /**
     * Setup ViewPager with reordered videos
     */
    private fun setupVideoViewPager() {
        videoPagerAdapter = UserImagePagerAdapter(
            displayVideos = reorderedVideos,
            originalVideos = allVideos,
            onImageClick = { videoItem ->
                // Optional: Handle image click - could open full screen
                videoItem.link?.let { url ->
                    val intent = Intent(requireContext(), CommonActivity::class.java).apply {
                        putExtra("fromWhere", "video")
                        putExtra("videoUrl", url)
                    }
                    startActivity(intent)
                }
            },
            onFullScreenClick = { videoItem ->
                // Handle full screen click
                videoItem.link?.let { url ->
                    val intent = Intent(requireContext(), CommonActivity::class.java).apply {
                        putExtra("fromWhere", "video")
                        putExtra("videoUrl", url)
                    }
                    startActivity(intent)
                }
            },
            onPlaybackStateChanged = { isPlaying ->
//                // Optional: Update UI based on playback state
//                // For example, disable download button while video is playing
//                binding.btnDownload?.isEnabled = !isPlaying
            }
        )

        binding.viewpager.adapter = videoPagerAdapter
        binding.viewpager.setCurrentItem(0, false)

        // 🔥 IMPORTANT: get internal RecyclerView
        pagerRecyclerView = binding.viewpager.getChildAt(0) as RecyclerView

        setupViewPager()

        // 🔥 Auto play FIRST video
        pagerRecyclerView?.post {
            videoPagerAdapter?.playVideoAt(0, pagerRecyclerView!!)
        }

        logVideoOrder()
    }

    /**
     * Log the video order for debugging
     */
    private fun logVideoOrder() {
        Log.d("VideoOrder", "Original videos (${allVideos.size}):")
        allVideos.forEachIndexed { index, video ->
            Log.d("VideoOrder", "  [$index]: ${video.link}")
        }

        Log.d("VideoOrder", "Reordered videos (${reorderedVideos.size}):")
        reorderedVideos.forEachIndexed { index, video ->
            val originalIndex = allVideos.indexOf(video)
            Log.d("VideoOrder", "  [$index] -> original[$originalIndex]: ${video.link}")
        }
    }

    private fun setupViewPager() {
        val pageCount = binding.viewpager.adapter?.itemCount ?: 0

        binding.dotsIndicator.apply {
            setSliderColor(
                ContextCompat.getColor(requireActivity(), R.color.colorPrimary),
                ContextCompat.getColor(requireActivity(), R.color.white)
            )
            setSliderWidth(
                resources.getDimension(com.intuit.sdp.R.dimen._8sdp),
                resources.getDimension(com.intuit.sdp.R.dimen._36sdp)
            )
            setSliderHeight(resources.getDimension(com.intuit.sdp.R.dimen._8sdp))
            setSlideMode(IndicatorSlideMode.SCALE)
            setIndicatorStyle(IndicatorStyle.ROUND_RECT)
            setPageSize(pageCount)
            notifyDataChanged()
            setupWithViewPager(binding.viewpager)
        }

        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                pagerRecyclerView?.post {
                    videoPagerAdapter?.playVideoAt(position, pagerRecyclerView!!)
                }

                val currentVideo = reorderedVideos.getOrNull(position)
                val originalIndex = allVideos.indexOf(currentVideo)

                Log.d("ViewPager", "Position $position → original index $originalIndex")
            }
        })
    }

    /**
     * Update UI based on current video (optional)
     */
    private fun updateUIBasedOnVideo(video: VideoLink?) {
        // Example: Update text or buttons based on which video is showing
        // binding.tvVideoTitle.text = video?.title ?: ""
    }

    /**
     * Initialize adapter
     */
    private fun initProgressionDetailsAdapter() {
        progressionDetailsAdapter =
            SimpleRecyclerViewAdapter(R.layout.progression_details_rv_item, BR.bean) { v, m, _ ->
                when (v?.id) {
                    R.id.clProgress -> {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "finalProgress")
                        intent.putExtra("progressData", m)
                        intent.putExtra("trackDetailId", trackDetailId)
                        intent.putExtra("diveName", diveName)
                        startActivity(intent)
                    }
                }
            }
        binding.rvProgressionDetails.adapter = progressionDetailsAdapter
    }

    /**
     * Setup swipe gesture detection
     */
    private fun setupSwipeGestures() {
        val gestureDetector = GestureDetectorCompat(
            requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean {
                    return true
                }

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    try {
                        val x1 = e1?.x ?: 0f
                        val x2 = e2.x
                        val y1 = e1?.y ?: 0f
                        val y2 = e2.y

                        val diffX = x2 - x1
                        val diffY = y2 - y1

                        if (Math.abs(diffX) > Math.abs(diffY) &&
                            Math.abs(diffX) > 100 &&
                            Math.abs(velocityX) > 100) {
                            if (diffX > 0) {
                                handleRightSwipe()
                                return true
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    return false
                }
            })

        binding.root.setOnTouchListener { v, event ->
            val handled = gestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_UP) {
                v.performClick()
            }
            handled
        }

        binding.rvProgressionDetails.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
    }

    private fun handleRightSwipe() {
        if (progressionDetailsAdapter.list.isNotEmpty()) {
            val firstItem = progressionDetailsAdapter.list.first()
            navigateToItem(firstItem)
        } else {
            showErrorToast("No items available")
        }
    }

    /**
     * Navigate to the specified item
     */
    private fun navigateToItem(item: HomeProgressStep) {
        val intent = Intent(requireContext(), CommonActivity::class.java)
        intent.putExtra("fromWhere", "finalProgress")
        intent.putExtra("progressData", item)
        intent.putExtra("trackDetailId", trackDetailId)
        intent.putExtra("diveName", diveName)
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    /**
     * Initialize adapter
     */
    private fun initStepAdapter() {
        stepAdapter = SimpleRecyclerViewAdapter(R.layout.prerequistes_rv_item, BR.bean) { v, m, _ ->
            when (v?.id) {
                R.id.tvJump -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "progressionDetails")
                    intent.putExtra("status",status)
                    intent.putExtra("progressId", trackDetailId)
                    startActivity(intent)
                }
            }
        }
        binding.rvStep.adapter = stepAdapter
    }
}
