package com.tech.kojo.ui.video

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.OptIn
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.ui.PlayerView
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.UpdateVideoCountModel
import com.tech.kojo.databinding.FragmentVideoBinding
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import com.tech.kojo.utils.showInfoToast
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import kotlin.math.max

@AndroidEntryPoint
class VideoFragment : BaseFragment<FragmentVideoBinding>() {

    private val viewModel: VideoFragmentVM by viewModels()
    private var player: ExoPlayer? = null
    private var isFullscreen = false
    private var playWhenReady = true
    private var playbackPosition = 0L
    private var videoWidth: Int = 0
    private var videoHeight: Int = 0
    private var isVideoHorizontal = false
    private var mediaUri: Uri? = null
    private var isUsingSoftwareDecoder = false
    private var retryCount = 0
    private val MAX_RETRY_COUNT = 2

    override fun getLayoutResource(): Int = R.layout.fragment_video

    override fun getViewModel(): BaseViewModel = viewModel

    override fun onCreateView(view: View) {
        initOnClick()
        initializePlayer()
        val videoId = arguments?.getString("videoId")
        if (videoId != null) {
            val request = HashMap<String, Any>()
            request["timeTaken"] = 200
            request["isViewed"] = true
            viewModel.updateVideoViewStatus("${Constants.UPDATE_VIDEO_VIEW}/$videoId", request)
        }
        initObserver()
    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.ivBack -> requireActivity().finish()
                R.id.ivFullscreen -> {
                    toggleFullscreen()
                }
            }
        }
    }

    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.LOADING -> {}
                Status.SUCCESS -> {
                    when (it.message) {
                        "updateVideoViewStatus" -> {
                            runCatching {
                                val model =
                                    BindingUtils.parseJson<UpdateVideoCountModel>(it.data.toString())
                                if (model?.success == true && model.data != null) {
                                    Log.d("video count updated", "Video Count Updated")
                                }
                            }.onFailure { e ->
                                showErrorToast(e.message.toString())
                            }.also {}
                        }
                    }
                }

                Status.ERROR -> {
                    showErrorToast(it.message.toString())
                }

                else -> {

                }
            }
        }
    }

    /**
     * Initialize ExoPlayer with universal resolution support
     * Automatically falls back to software decoding for high-resolution videos
     */
    @OptIn(UnstableApi::class)
    private fun initializePlayer() {
        val videoUrl = arguments?.getString("videoUrl")
        val videoPath = arguments?.getString("videoPath")

        val uri = when {
            !videoUrl.isNullOrEmpty() -> {
                Uri.parse(Constants.BASE_URL_IMAGE + videoUrl)
            }

            !videoPath.isNullOrEmpty() -> {
                val file = File(videoPath)
                if (file.exists()) {
                    Uri.fromFile(file)
                } else {
                    Log.e("VideoFragment", "File does not exist: $videoPath")
                    Uri.parse(videoPath)
                }
            }

            else -> null
        }

        mediaUri = uri

        if (uri == null) return

        // Create track selector with adaptive resolution
        val trackSelector = DefaultTrackSelector(requireContext()).apply {
            setParameters(
                parameters
                    .buildUpon()
                    // Don't limit resolution initially, let it auto-adapt
                    .setAllowVideoMixedMimeTypeAdaptiveness(true)
                    .setAllowAudioMixedMimeTypeAdaptiveness(true)
                    .build()
            )
        }

        // Create renderers factory with decoder fallback enabled
        val renderersFactory = DefaultRenderersFactory(requireContext())
            .setEnableDecoderFallback(true) // CRITICAL: Enables software decoding fallback
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)

        player = ExoPlayer.Builder(requireContext())
            .setRenderersFactory(renderersFactory)
            .setTrackSelector(trackSelector)
            .build()
            .also { exoPlayer ->

                binding.playerView.player = exoPlayer
                binding.playerView.useController = true
                binding.playerView.hideController()
                binding.playerView.controllerShowTimeoutMs = 2000

                // Configure HTTP client with better timeout settings
                val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                    .setAllowCrossProtocolRedirects(true)
                    .setConnectTimeoutMs(30000)
                    .setReadTimeoutMs(30000)
                    .setUserAgent("KojoVideoPlayer/1.0")

                val dataSourceFactory = DefaultDataSource.Factory(requireContext(), httpDataSourceFactory)

                val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(uri))

                exoPlayer.setMediaSource(mediaSource)
                exoPlayer.prepare()
                exoPlayer.seekTo(playbackPosition)
                exoPlayer.playWhenReady = this@VideoFragment.playWhenReady
                setupControllerVisibility()

                exoPlayer.addListener(object : Player.Listener {

                    override fun onVideoSizeChanged(videoSize: VideoSize) {
                        super.onVideoSizeChanged(videoSize)
                        videoWidth = videoSize.width
                        videoHeight = videoSize.height
                        isVideoHorizontal = videoWidth > videoHeight

                        Log.d("VideoFragment", "Video size: ${videoWidth}x${videoHeight}, " +
                                "isHorizontal: $isVideoHorizontal, " +
                                "usingSoftwareDecoder: $isUsingSoftwareDecoder")

                        adjustFullscreenButton()
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_BUFFERING -> {
                                Log.d("VideoFragment", "Buffering...")
                            }
                            Player.STATE_READY -> {
                                val durationMs = exoPlayer.duration
                                val formatted = formatDuration(durationMs)
                                Log.d("VideoFragment", "Video duration: $formatted")
                            }
                            Player.STATE_ENDED -> {
                                Log.d("VideoFragment", "Playback completed")
                            }
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        if (isPlaying) {
                            binding.playerView.postDelayed({
                                binding.playerView.hideController()
                            }, 500)
                        } else {
                            binding.playerView.showController()
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        Log.e("ExoPlayer", "Playback error: ${error.message}", error)

                        // Comprehensive error handling with retry logic
                        when {
                            // Hardware decoder failure (high resolution videos)
                            error.errorCode == PlaybackException.ERROR_CODE_DECODER_INIT_FAILED ||
                                    error.message?.contains("exceeds capabilities") == true ||
                                    error.message?.contains("NO_EXCEEDS_CAPABILITIES") == true ||
                                    error.message?.contains("MediaCodecVideoRenderer") == true -> {

                                if (!isUsingSoftwareDecoder && retryCount < MAX_RETRY_COUNT) {
                                    retryCount++
                                    Log.w("VideoFragment", "Hardware decoder failed, retrying with software decoding (Attempt $retryCount)")
//                                    showInfoToast("Optimizing video playback for your device...")
                                    retryWithSoftwareDecoding(uri)
                                } else if (retryCount >= MAX_RETRY_COUNT) {
                                    showErrorToast("Unable to play this video. The resolution may be too high for your device.")
                                }
                            }

                            // Network errors
                            error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> {
                                showErrorToast("Network connection failed. Please check your internet connection.")
                            }

                            // File/Format errors
                            error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> {
                                showErrorToast("Video file not found.")
                            }
                            error.errorCode == PlaybackException.ERROR_CODE_DECODING_FAILED -> {
                                if (!isUsingSoftwareDecoder && retryCount < MAX_RETRY_COUNT) {
                                    retryCount++
                                    retryWithSoftwareDecoding(uri)
                                } else {
                                    showErrorToast("Video format not supported on this device.")
                                }
                            }

                            // Other errors
                            else -> {
                                if (!isUsingSoftwareDecoder && retryCount < MAX_RETRY_COUNT) {
                                    retryCount++
                                    retryWithSoftwareDecoding(uri)
                                } else {
                                    showErrorToast("Failed to play video. Error: ${error.message}")
                                }
                            }
                        }
                    }
                })
            }
    }

    /**
     * Retry playback using software decoding for high-resolution videos
     */
    @OptIn(UnstableApi::class)
    private fun retryWithSoftwareDecoding(uri: Uri) {
        isUsingSoftwareDecoder = true

        try {
            // Release existing player
            player?.release()

            // Create track selector with resolution limits for better performance
            val trackSelector = DefaultTrackSelector(requireContext()).apply {
                setParameters(
                    parameters
                        .buildUpon()
                        // Limit resolution to 1080p for software decoding
                        .setMaxVideoSize(1920, 1920)
                        .setMaxVideoBitrate(10_000_000) // 10 Mbps limit
                        .setAllowVideoMixedMimeTypeAdaptiveness(true)
                        .build()
                )
            }

            // Force software decoding by preferring software decoders
            val renderersFactory = DefaultRenderersFactory(requireContext())
                .setEnableDecoderFallback(true)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)

            // Create new player with software decoding configuration
            player = ExoPlayer.Builder(requireContext())
                .setRenderersFactory(renderersFactory)
                .setTrackSelector(trackSelector)
                .build()
                .also { exoPlayer ->

                    binding.playerView.player = exoPlayer
                    binding.playerView.useController = true
                    binding.playerView.controllerShowTimeoutMs = 2000

                    val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                        .setAllowCrossProtocolRedirects(true)
                        .setConnectTimeoutMs(30000)
                        .setReadTimeoutMs(30000)
                        .setUserAgent("KojoVideoPlayer/1.0-Software")

                    val dataSourceFactory = DefaultDataSource.Factory(requireContext(), httpDataSourceFactory)
                    val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(uri))

                    exoPlayer.setMediaSource(mediaSource)
                    exoPlayer.prepare()
                    exoPlayer.seekTo(playbackPosition)
                    exoPlayer.playWhenReady = true

                    // Add listener for software decoding playback
                    exoPlayer.addListener(object : Player.Listener {
                        override fun onPlayerError(error: PlaybackException) {
                            Log.e("ExoPlayer", "Software decoding failed: ${error.message}", error)

                            if (retryCount < MAX_RETRY_COUNT) {
                                retryCount++
                             //   showInfoToast("Trying alternative playback method...")
                                retryWithAlternativeDecoder(uri)
                            } else {
                                showErrorToast("This video cannot be played on your device")
                            }
                        }

                        override fun onVideoSizeChanged(videoSize: VideoSize) {
                            videoWidth = videoSize.width
                            videoHeight = videoSize.height
                            isVideoHorizontal = videoWidth > videoHeight
                            adjustFullscreenButton()

                            Log.d("VideoFragment", "Software decoder - Video size: ${videoWidth}x${videoHeight}")
                        }

                        override fun onPlaybackStateChanged(state: Int) {
                            if (state == Player.STATE_READY) {
                                Log.d("VideoFragment", "Software decoder - Playback ready")
                            }
                        }
                    })

                    setupControllerVisibility()
                }

        } catch (e: Exception) {
            Log.e("VideoFragment", "Failed to initialize software decoding", e)
            if (retryCount < MAX_RETRY_COUNT) {
                retryCount++
                retryWithAlternativeDecoder(uri)
            } else {
                showErrorToast("Cannot play this video on your device")
            }
        }
    }

    /**
     * Last resort: Use basic decoder with minimal settings
     */
    @OptIn(UnstableApi::class)
    private fun retryWithAlternativeDecoder(uri: Uri) {
        try {
            player?.release()

            // Most conservative settings
            val trackSelector = DefaultTrackSelector(requireContext()).apply {
                setParameters(
                    parameters
                        .buildUpon()
                        .setMaxVideoSize(854, 480) // Force 480p max
                        .setMaxVideoBitrate(2_000_000) // 2 Mbps max
                        .setAllowVideoMixedMimeTypeAdaptiveness(false)
                        .build()
                )
            }

            val renderersFactory = DefaultRenderersFactory(requireContext())
                .setEnableDecoderFallback(true)
                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)

            player = ExoPlayer.Builder(requireContext())
                .setRenderersFactory(renderersFactory)
                .setTrackSelector(trackSelector)
                .build()
                .also { exoPlayer ->
                    binding.playerView.player = exoPlayer

                    val dataSourceFactory = DefaultDataSource.Factory(requireContext())
                    val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(uri))

                    exoPlayer.setMediaSource(mediaSource)
                    exoPlayer.prepare()
                    exoPlayer.seekTo(playbackPosition)
                    exoPlayer.playWhenReady = true

                    exoPlayer.addListener(object : Player.Listener {
                        override fun onPlayerError(error: PlaybackException) {
                            showErrorToast("Video playback failed. Please try a different video.")
                        }
                    })
                }
        } catch (e: Exception) {
            showErrorToast("Unable to play this video")
        }
    }

    private fun adjustFullscreenButton() {
        binding.ivFullscreen.visibility = View.VISIBLE
        binding.ivFullscreen.alpha = 1f
        binding.ivFullscreen.setImageResource(
            if (isFullscreen) R.drawable.ic_minimise else R.drawable.ic_maximise
        )
    }

    private fun setupControllerVisibility() {
        binding.playerView.setControllerVisibilityListener(
            PlayerView.ControllerVisibilityListener { visibility ->
                val show = visibility == View.VISIBLE

                if (show) {
                    binding.ivFullscreen.fadeInSlideUp()
                } else {
                    binding.ivFullscreen.fadeOutSlideDown()
                }
            })
    }

    private fun formatDuration(durationMs: Long): String {
        if (durationMs <= 0) return "00:00"

        val totalSeconds = durationMs / 1000
        val seconds = (totalSeconds % 60).toInt()
        val minutes = ((totalSeconds / 60) % 60).toInt()
        val hours = (totalSeconds / 3600).toInt()

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    override fun onPause() {
        super.onPause()
        savePlayerState()
        player?.pause()
    }

    private fun savePlayerState() {
        player?.let {
            playWhenReady = it.playWhenReady
            playbackPosition = it.currentPosition
        }
    }

    private fun releasePlayer() {
        player?.release()
        player = null
        if (isVideoHorizontal && isFullscreen) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    private fun toggleFullscreen() {
        val wasPlaying = player?.isPlaying == true
        val currentPosition = player?.currentPosition ?: 0

        if (!isVideoHorizontal) {
            toggleFullscreenWithoutRotation()
            binding.videoContainer.postDelayed({
                player?.seekTo(currentPosition)
                if (wasPlaying) {
                    player?.play()
                }
            }, 100)
            return
        }

        isFullscreen = !isFullscreen

        if (isFullscreen) {
            enterFullscreen()
        } else {
            exitFullscreen()
        }

        binding.videoContainer.postDelayed({
            player?.seekTo(currentPosition)
            if (wasPlaying) {
                player?.play()
            }
        }, 100)
    }

    private fun toggleFullscreenWithoutRotation() {
        isFullscreen = !isFullscreen

        if (isFullscreen) {
            hideSystemUI()

            val params = binding.videoContainer.layoutParams as ConstraintLayout.LayoutParams
            params.topToBottom = ConstraintLayout.LayoutParams.UNSET
            params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
            params.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
            binding.videoContainer.layoutParams = params

            binding.playerView.resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
            binding.ivFullscreen.setImageResource(R.drawable.ic_minimise)
        } else {
            showSystemUI()

            val params = binding.videoContainer.layoutParams as ConstraintLayout.LayoutParams
            params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            params.topToBottom = ConstraintLayout.LayoutParams.UNSET
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
            params.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
            binding.videoContainer.layoutParams = params

            binding.ivFullscreen.setImageResource(R.drawable.ic_maximise)
        }
    }

    private fun enterFullscreen() {
        isFullscreen = true

        if (isVideoHorizontal) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        hideSystemUI()

        val params = binding.videoContainer.layoutParams as ConstraintLayout.LayoutParams
        params.topToBottom = ConstraintLayout.LayoutParams.UNSET
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
        params.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
        binding.videoContainer.layoutParams = params

        binding.ivFullscreen.setImageResource(R.drawable.ic_minimise)
    }

    private fun exitFullscreen() {
        isFullscreen = false

        if (isVideoHorizontal) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        showSystemUI()

        val params = binding.videoContainer.layoutParams as ConstraintLayout.LayoutParams
        params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
        params.topToBottom = ConstraintLayout.LayoutParams.UNSET
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
        params.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
        binding.videoContainer.layoutParams = params

        binding.ivFullscreen.setImageResource(R.drawable.ic_maximise)
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, false)
        WindowInsetsControllerCompat(
            requireActivity().window, requireActivity().window.decorView
        ).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun showSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(requireActivity().window, true)
        WindowInsetsControllerCompat(
            requireActivity().window, requireActivity().window.decorView
        ).show(WindowInsetsCompat.Type.systemBars())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        requireActivity().window.statusBarColor = Color.TRANSPARENT
        savePlayerState()
        releasePlayer()
    }

    private fun View.fadeInSlideUp() {
        if (visibility == View.VISIBLE) return

        alpha = 0f
        translationY = height * 0.25f
        visibility = View.VISIBLE

        animate().alpha(1f).translationY(0f).setDuration(1)
            .setInterpolator(android.view.animation.DecelerateInterpolator()).start()
    }

    private fun View.fadeOutSlideDown() {
        if (visibility != View.VISIBLE) return

        animate().alpha(0f).translationY(height * 0.25f).setDuration(1)
            .setInterpolator(android.view.animation.AccelerateInterpolator()).withEndAction {
                visibility = View.GONE
                alpha = 1f
                translationY = 0f
            }.start()
    }
}