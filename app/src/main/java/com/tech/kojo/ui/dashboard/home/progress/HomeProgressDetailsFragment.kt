package com.tech.kojo.ui.dashboard.home.progress

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.JsonObject
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
import com.tech.kojo.data.room_module.DownloadVideoData
import com.tech.kojo.databinding.FragmentHomeProgressDetailsBinding
import com.tech.kojo.databinding.HomeProgressItemBinding
import com.tech.kojo.databinding.PrerequistesRvItemBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.ui.dashboard.tracker.my_trick.progression_details.UserImagePagerAdapter
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Resource
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import com.tech.kojo.utils.showSuccessToast
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

@AndroidEntryPoint
class HomeProgressDetailsFragment : BaseFragment<FragmentHomeProgressDetailsBinding>() {
    private val viewModel: HomeProgressDetailsVM by viewModels()
    private lateinit var homeProgressionDetailsAdapter: SimpleRecyclerViewAdapter<HomeProgressStep, HomeProgressItemBinding>
    private var trackDetailId: String? = null
    private var diveName: String? = null
    private var allVideos: List<VideoLink> = emptyList()
    private var reorderedVideos: List<VideoLink> = emptyList()
    private var videoPagerAdapter: UserImagePagerAdapter? = null

    private var pagerRecyclerView: RecyclerView? = null
    private lateinit var stepAdapter: SimpleRecyclerViewAdapter<Prerequisites, PrerequistesRvItemBinding>

    private var downloadedVideos = ArrayList<DownloadVideoData>()

    override fun getLayoutResource(): Int = R.layout.fragment_home_progress_details

    override fun getViewModel(): BaseViewModel = viewModel

    override fun onCreateView(view: View) {
        initProgressionDetailsAdapter()
        initStepAdapter()
        viewModel.getAllVideos()
        observeDownloadedVideo()
        initOnClick()
        initObserver()
      //  setupSwipeGestures()

    }

    override fun onResume() {
        super.onResume()
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

    private fun initView() {
        trackDetailId = arguments?.getString("trackDetailId")
        trackDetailId?.let { id ->
            val data = HashMap<String, Any>()
            viewModel.getTrickDataByIdApi(data, Constants.TRICKS_DATA + "/$id")
        } ?: showErrorToast("Something went wrong!")
    }

    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.LOADING -> showLoading()
                Status.SUCCESS -> handleSuccessResponse(it)
                Status.ERROR -> handleErrorResponse(it)
                else -> {}
            }
        }
    }

    private fun handleSuccessResponse(it: Resource<JsonObject>) {
        when (it.message) {
            "getTrickDataByIdApi" -> {
                runCatching {
                    val jsonData = it.data?.toString().orEmpty()
                    val model: HomeProgressApiResponse? = BindingUtils.parseJson(jsonData)
                    model?.data?.let { homeData ->
                        diveName = homeData.name
                        homeData.description?.takeIf { it.isNotBlank() }?.let {
                            binding.tvTrack.text = it
                            binding.tvTrack.visibility = View.VISIBLE
                        } ?: run {
                            binding.tvTrack.visibility = View.GONE
                        }
                        binding.tvSkip.text = homeData.name.orEmpty()
                        (homeData.prerequisites ?: emptyList()).also { list ->
                            stepAdapter.list = list

                            val isVisible = list.isNotEmpty()
                            binding.rvStep.visibility = if (isVisible) View.VISIBLE else View.GONE
                            binding.tvTricker.visibility = if (isVisible) View.VISIBLE else View.GONE
                        }
                        homeProgressionDetailsAdapter.list = homeData.steps ?: emptyList()

                        allVideos = homeData.steps?.flatMap { it?.videoLinks ?: emptyList() }
                            ?.filterNotNull() ?: emptyList()

                        reorderedVideos = reorderVideosDynamically(allVideos)
                        setupVideoViewPager()
                    }
                }.onFailure { e ->
                    Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                    showErrorToast(e.message.toString())
                }.also { hideLoading() }
            }
        }
    }

    private fun handleErrorResponse(it: Resource<JsonObject>) {
        hideLoading()
        showErrorToast(it.message.toString())
    }

    private fun reorderVideosDynamically(originalVideos: List<VideoLink>): List<VideoLink> {
        if (originalVideos.isEmpty()) return emptyList()
        if (originalVideos.size == 1) return listOf(originalVideos[0])

        val reorderedList = mutableListOf<VideoLink>()
        reorderedList.add(originalVideos.last())
        reorderedList.addAll(originalVideos.dropLast(1))
        reorderedList.add(originalVideos.last())
        return reorderedList
    }

    private fun setupVideoViewPager() {
        videoPagerAdapter = UserImagePagerAdapter(
            displayVideos = reorderedVideos,
            originalVideos = allVideos,
            onImageClick = {},
            onFullScreenClick = { videoItem ->
                videoItem.link?.let { url ->
                    val intent = Intent(requireContext(), CommonActivity::class.java).apply {
                        putExtra("fromWhere", "video")
                        putExtra("videoUrl", url)
                    }
                    startActivity(intent)
                }
            },
            onPlaybackStateChanged = { isPlaying ->
//                binding.btnDownload.isEnabled = !isPlaying
            })

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

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.ivProgress, R.id.ivClosePlayer -> requireActivity().finish()
                R.id.btnDownload,R.id.ivDownload ->
                    downloadCurrentVideo()
//                R.id.ivMAx -> openCurrentVideoFullScreen()
            }
        }
    }

    private fun downloadCurrentVideo() {
        val currentPosition = binding.viewpager.currentItem
        val video = reorderedVideos.getOrNull(currentPosition)

        if (video != null) {
            if (downloadedVideos.any { it.thumbnailUrl == video.thumbnail }) {
                showErrorToast("Video already downloaded")
                return
            }
            downloadAndSaveVideo(video, currentPosition)
        } else {
            showErrorToast("Video URL not found")
        }
    }


//    private fun openCurrentVideoFullScreen() {
//        val currentPosition = binding.viewpager.currentItem
//        val videoUrl = reorderedVideos.getOrNull(currentPosition)?.link
//
//        if (!videoUrl.isNullOrEmpty()) {
//            val intent = Intent(requireContext(), CommonActivity::class.java).apply {
//                putExtra("fromWhere", "video")
//                putExtra(
//                    "videoUrl",
//                    if (videoUrl.startsWith("http")) videoUrl else Constants.BASE_URL_IMAGE + videoUrl
//                )
//            }
//            startActivity(intent)
//        } else {
//            showErrorToast("Video URL not found")
//        }
//    }

    //    private fun downloadVideo(url: String) {
//        try {
//            val fullUrl = if (url.startsWith("http")) url else Constants.BASE_URL_IMAGE + url
//            val request = DownloadManager.Request(fullUrl.toUri()).apply {
//                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
//                setTitle("Downloading Video")
//                setDescription("Downloading video file...")
//                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//                setDestinationInExternalPublicDir(
//                    Environment.DIRECTORY_DOWNLOADS, "${System.currentTimeMillis()}.mp4"
//                )
//            }
//
//            val downloadManager =
//                requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//            downloadManager.enqueue(request)
//            showSuccessToast("Download started...")
//        } catch (e: Exception) {
//            Log.e("DownloadError", "Error: ${e.message}", e)
//            showErrorToast("Download failed: ${e.message}")
//        }
//    }
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
                // Update download button state for current video
                updateDownloadButtonState(currentVideo)

                Log.d("ViewPager", "Position $position → original index $originalIndex")
            }
        })
    }

    private fun setupSwipeGestures() {
        val gestureDetector = GestureDetectorCompat(
            requireContext(), object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean = true

                override fun onFling(
                    e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
                ): Boolean {
                    try {
                        val x1 = e1?.x ?: 0f
                        val x2 = e2.x
                        val y1 = e1?.y ?: 0f
                        val y2 = e2.y

                        val diffX = x2 - x1
                        val diffY = y2 - y1

                        if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > 100 && Math.abs(
                                velocityX
                            ) > 100
                        ) {
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
            if (event.action == MotionEvent.ACTION_UP) v.performClick()
            handled
        }

        binding.rvHomeProgressionDetails.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
    }

    private fun handleRightSwipe() {
        if (homeProgressionDetailsAdapter.list.isNotEmpty()) {
            navigateToItem(homeProgressionDetailsAdapter.list.first())
        } else {
            showErrorToast("No items available")
        }
    }

    private fun navigateToItem(item: HomeProgressStep) {
        val intent = Intent(requireContext(), CommonActivity::class.java).apply {
            putExtra("fromWhere", "finalProgress")
            putExtra("progressData", item)
            putExtra("trackDetailId", trackDetailId)
            putExtra("diveName", diveName)
        }
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun initProgressionDetailsAdapter() {
        homeProgressionDetailsAdapter = SimpleRecyclerViewAdapter(
            R.layout.home_progress_item, BR.bean
        ) { v, m, _ ->
            when (v?.id) {
                R.id.clProgress -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java).apply {
                        putExtra("fromWhere", "finalProgress")
                        putExtra("progressData", m)
                        putExtra("trackDetailId", trackDetailId)
                        putExtra("diveName", diveName)
                    }
                    startActivity(intent)
                }
            }
        }
        binding.rvHomeProgressionDetails.adapter = homeProgressionDetailsAdapter
    }

    /**
     * Initialize adapter
     */
    private fun initStepAdapter() {
        stepAdapter = SimpleRecyclerViewAdapter(R.layout.prerequistes_rv_item, BR.bean) { v, m, _ ->
            when (v?.id) {
                R.id.tvJump -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "homeProgress")
                    intent.putExtra("trackDetailId", m._id)
                    startActivity(intent)
                }
            }
        }
        binding.rvStep.adapter = stepAdapter
    }

    ///// download video
    private fun downloadAndSaveVideo(video: VideoLink, position: Int) {
        showLoading()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val videoUrl = if (video.link?.startsWith("http") == true) video.link else Constants.BASE_URL_IMAGE + video.link
                Log.d("VideoDownload", "Downloading from: $videoUrl")

                val localPath = requireContext().downloadVideo(
                    url = videoUrl ?: "",
                    fileName = "${video._id ?: System.currentTimeMillis()}.mp4"
                )

                if (localPath == null) {
                    throw Exception("Download failed, localPath is null")
                }

                // Find step title for this video link
                val stepTitle = homeProgressionDetailsAdapter.list.find { step ->
                    step.videoLinks?.any { it?.thumbnail == video.thumbnail } == true
                }?.title

                val downloadVideo = DownloadVideoData(
                    _id = video._id,
                    createdAt = System.currentTimeMillis().toString(),
                    thumbnailUrl = video.thumbnail,
                    title = stepTitle ?: "Video $position",
                    videoDownload = true,
                    localPath = localPath
                )

                viewModel.insertVideo(downloadVideo)

                launch(Dispatchers.Main) {
                    hideLoading()
                    showSuccessToast("Video saved to download")
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
                if (status == HttpURLConnection.HTTP_MOVED_TEMP ||
                    status == HttpURLConnection.HTTP_MOVED_PERM ||
                    status == HttpURLConnection.HTTP_SEE_OTHER ||
                    status == 307 || status == 308) {

                    var newUrl = connection.getHeaderField("Location")
                    if (newUrl == null) break

                    if (newUrl.startsWith("/")) {
                        newUrl = urlObj.protocol + "://" + urlObj.host + (if (urlObj.port != -1) ":" + urlObj.port else "") + newUrl
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
                Status.LOADING -> {
//                    showLoading()
                }

                Status.SUCCESS -> {
                    when (it.message) {
                        "getDownloadVideo" -> {
                            runCatching {
                                downloadedVideos = it.data as ArrayList<DownloadVideoData>
                                // Update download button state for current video
                                val currentPosition = binding.viewpager.currentItem
                                val currentVideo = reorderedVideos.getOrNull(currentPosition)
                                updateDownloadButtonState(currentVideo)

                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(
                                    e.localizedMessage ?: getString(R.string.something_went_wrong)
                                )
                            }.also {
//                                hideLoading()
                            }
                        }

                    }
                }

                Status.ERROR -> {
//                    hideLoading()
                    showErrorToast(it.message.toString())
                }

                else -> {
                }
            }
        }
    }

    private fun updateDownloadButtonState(currentVideo: VideoLink?) {
        val isDownloaded = currentVideo != null && downloadedVideos.any { it.thumbnailUrl == currentVideo.thumbnail }
        // Optionally change the icon/background to indicate download status
        if (isDownloaded) {
            binding.ivLoad.setImageResource(R.drawable.ic_download_complete) // Add a downloaded icon
            // Or show a checkmark, disable the button, etc.
        } else {
            binding.ivLoad.setImageResource(R.drawable.ic_cloud_download) // Your default download icon
        }
    }
}
