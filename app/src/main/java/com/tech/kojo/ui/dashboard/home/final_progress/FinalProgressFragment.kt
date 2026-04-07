package com.tech.kojo.ui.dashboard.home.final_progress

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.tech.kojo.BR
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.base.SimpleRecyclerViewAdapter
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.HomeKeypoint
import com.tech.kojo.data.model.HomeProgressStep
import com.tech.kojo.data.model.UserProgressApiResponse
import com.tech.kojo.data.model.VideoLink
import com.tech.kojo.databinding.FragmentFinalProgressBinding
import com.tech.kojo.databinding.StepRvItemBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.ui.dashboard.tracker.my_trick.progression_details.UserImagePagerAdapter
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FinalProgressFragment : BaseFragment<FragmentFinalProgressBinding>() {
    private val viewModel: FinalProgressFragmentVM by viewModels()
    private lateinit var stepAdapter: SimpleRecyclerViewAdapter<HomeKeypoint, StepRvItemBinding>
    val repsCount = MutableLiveData(0)
    private var diveName: String? = null
    private var trackDetailId: String? = null
    private var stepId: String? = null
    private var allVideos: List<VideoLink> = emptyList()
    private var reorderedVideos: List<VideoLink> = emptyList()
    private var videoPagerAdapter: UserImagePagerAdapter? = null
    private var screenStartTime: Long = 0L
    private var pagerRecyclerView: RecyclerView? = null

    override fun getLayoutResource(): Int {
        return R.layout.fragment_final_progress
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

    override fun onResume() {
        super.onResume()
        screenStartTime = System.currentTimeMillis()
        // Resume video playback when fragment becomes visible
        pagerRecyclerView?.post {
            videoPagerAdapter?.playVideoAt(binding.viewpager.currentItem, pagerRecyclerView!!)
        }
    }

    override fun onPause() {
        super.onPause()
        // Pause and release all videos when fragment is not visible
        pagerRecyclerView?.let {
            videoPagerAdapter?.releaseAllPlayers(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Release all players to prevent memory leaks
        pagerRecyclerView?.let {
            videoPagerAdapter?.releaseAllPlayers(it)
        }
    }

    /**
     * Method to initialize view
     */
    private fun initView() {
        // adapter
        initStepAdapter()
        // get data bundle
        val progressData = arguments?.getParcelable<HomeProgressStep>("progressData")
        trackDetailId = arguments?.getString("trackDetailId")
        diveName = arguments?.getString("diveName")
        // set data
        progressData?.let { data ->
            stepId = data._id
            binding.tvDive.text = diveName.orEmpty()
            binding.tvSkip.text = diveName.orEmpty()
            (data.keypoints ?: emptyList()).also { list ->
                stepAdapter.list = list

                val isVisible = list.isNotEmpty()
                binding.rvStep.visibility = if (isVisible) View.VISIBLE else View.GONE
                binding.tvKeyPoints.visibility = if (isVisible) View.VISIBLE else View.GONE
            }
            // SAFE REPS
            val reps = data.progress?.repsCount ?: 0
            binding.etCount.setText(reps.toString())
            repsCount.value = reps

            // SAFE VIDEO LINKS (very important)
            allVideos = data.videoLinks?.filterNotNull()?.toList() ?: emptyList()

            // DYNAMIC REORDERING: Last -> First -> Second -> ... -> Last
            reorderedVideos = reorderVideosDynamically(allVideos)

            // Setup ViewPager with reordered videos
            setupVideoViewPager()
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
     * Setup ViewPager with reordered videos and auto-play
     */
    private fun setupVideoViewPager() {
        // Create adapter with reordered videos
        videoPagerAdapter = UserImagePagerAdapter(
            displayVideos = reorderedVideos,
            originalVideos = allVideos,
            onImageClick = { videoItem ->
                // Handle image click - navigate to full screen
                navigateToFullScreenVideo(videoItem.link)
            },
            onFullScreenClick = { videoItem ->
                // Handle full screen click
                navigateToFullScreenVideo(videoItem.link)
            },
            onPlaybackStateChanged = { isPlaying ->
//                // Update UI based on playback state
//                updateUIBasedOnPlayback(isPlaying)
            }
        )

        binding.viewpager.adapter = videoPagerAdapter
        binding.viewpager.setCurrentItem(0, false)

        // 🔥 IMPORTANT: Get internal RecyclerView for adapter to find ViewHolders
        pagerRecyclerView = binding.viewpager.getChildAt(0) as? RecyclerView

        // Setup dot indicators
        setupViewPager()

        // 🔥 Auto-play FIRST video
        pagerRecyclerView?.post {
            videoPagerAdapter?.playVideoAt(0, pagerRecyclerView!!)
        }

        // Log the order for debugging
        logVideoOrder()
    }

    /**
     * Update UI based on video playback state
     */
//    private fun updateUIBasedOnPlayback(isPlaying: Boolean) {
//        when {
//            isPlaying -> {
//                // Video is playing - disable buttons to prevent conflicts
//                binding.btnCompleted.isEnabled = false
//                binding.btnAttempted.isEnabled = false
//                binding.ivPlus.isEnabled = false
//                binding.ivMinus.isEnabled = false
//                binding.etCount.isEnabled = false
//            }
//            else -> {
//                // Video is stopped - enable all controls
//                binding.btnCompleted.isEnabled = true
//                binding.btnAttempted.isEnabled = true
//                binding.ivPlus.isEnabled = true
//                binding.ivMinus.isEnabled = true
//                binding.etCount.isEnabled = true
//            }
//        }
//    }

    /**
     * Navigate to full screen video
     */
    private fun navigateToFullScreenVideo(videoUrl: String?) {
        if (!videoUrl.isNullOrEmpty()) {
            val intent = Intent(requireContext(), CommonActivity::class.java).apply {
                putExtra("fromWhere", "video")
                putExtra("videoUrl", videoUrl)
            }
            startActivity(intent)
        } else {
            showErrorToast("Video URL not found")
        }
    }

    /**
     * Log the video order for debugging
     */
    private fun logVideoOrder() {
        Log.d("FinalProgress", "Original videos (${allVideos.size}):")
        allVideos.forEachIndexed { index, video ->
            Log.d("FinalProgress", "  [$index]: ${video.link}")
        }

        Log.d("FinalProgress", "Reordered videos (${reorderedVideos.size}):")
        reorderedVideos.forEachIndexed { index, video ->
            val originalIndex = allVideos.indexOf(video)
            Log.d("FinalProgress", "  [$index] -> original[$originalIndex]: ${video.link}")
        }
    }

    /**
     * Method to initialize click
     */
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.ivProgress, R.id.ivClosePlayer -> {
                    requireActivity().finish()
                }
                R.id.ivFullscreen->{
                    val currentPosition = binding.viewpager.currentItem
                    reorderedVideos.getOrNull(currentPosition)?.let {  videoItem->
                        videoItem.link?.let { url ->
                            val intent = Intent(requireContext(), CommonActivity::class.java).apply {
                                putExtra("fromWhere", "video")
                                putExtra("videoUrl", url)
                            }
                            startActivity(intent)
                        }}

                }
                R.id.ivPlus -> {
                    val current = repsCount.value ?: 0
                    val newCount = current + 1
                    repsCount.value = newCount
                    binding.etCount.setText(newCount.toString())
                }

                R.id.ivMinus -> {
                    val current = repsCount.value ?: 0
                    val newCount = if (current > 0) current - 1 else 0
                    repsCount.value = newCount
                    binding.etCount.setText(newCount.toString())
                }

                R.id.btnCompleted -> {
                    if (binding.etCount.text.toString().trim().toInt() <= 0) {
                        showErrorToast("Please add at least 1 rep to continue.")
                        return@observe
                    }
                    apiCall("completed")
                }

                R.id.btnAttempted -> {
                    if (binding.etCount.text.toString().trim().toInt() <= 0) {
                        showErrorToast("Please add at least 1 rep to continue.")
                        return@observe
                    }
                    apiCall("attempted")
                }
            }
        }
    }

    /**
     * Method to api call
     */
    private fun apiCall(status: String) {
        val data = HashMap<String, Any>()
        if (!trackDetailId.isNullOrEmpty() && !stepId.isNullOrEmpty()) {
            val timeSpentMillis = System.currentTimeMillis() - screenStartTime
            val timeSpentSeconds = timeSpentMillis / 1000
            data["trickDataId"] = trackDetailId!!
            data["stepId"] = stepId!!
            data["isSaved"] = true
            data["repsCount"] = binding.etCount.text.toString().trim()
            data["status"] = status
            data["timeTaken"] = timeSpentSeconds
            viewModel.userProgressApi(Constants.POST_PROGRESS, data)
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
                        "userProgressApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: UserProgressApiResponse? =
                                    BindingUtils.parseJson(jsonData)
                                if (model?.success == true) requireActivity().finish()
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

    /*** setup view pager ***/
    private fun setupViewPager() {
        val pageCount = binding.viewpager.adapter?.itemCount ?: 0

        if (pageCount <= 1) {
            // Single video - hide indicators completely
            binding.staticDot.visibility = View.GONE
            binding.dotsIndicator.visibility = View.GONE
        } else {
            // Multiple videos - show animated indicator
            binding.staticDot.visibility = View.GONE
            binding.dotsIndicator.visibility = View.VISIBLE

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
        }

        // Add page change callback for auto-play on swipe
        binding.viewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                // Auto-play video when page changes
                pagerRecyclerView?.post {
                    videoPagerAdapter?.playVideoAt(position, pagerRecyclerView!!)
                }

                val currentVideo = reorderedVideos.getOrNull(position)
                val originalIndex = allVideos.indexOf(currentVideo)
                Log.d("FinalProgress", "Position $position → original index $originalIndex")
            }
        })
    }

    /**
     * Initialize adapter
     */
    private fun initStepAdapter() {
        stepAdapter = SimpleRecyclerViewAdapter(R.layout.step_rv_item, BR.bean) { v, m, _ ->
            when (v?.id) {
                R.id.clProgress -> {
                    // Handle step item click if needed
                }
            }
        }
        binding.rvStep.adapter = stepAdapter
    }
}