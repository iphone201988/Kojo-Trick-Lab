package com.tech.kojo.ui.video

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
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

    /**
     * Handle clicks
     */
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
     * Initialize ExoPlayer with URL or local path safety
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

        player = ExoPlayer.Builder(requireContext()).build().also { exoPlayer ->

            binding.playerView.player = exoPlayer

            // Enable controller
            binding.playerView.useController = true

            // Hide initially
            binding.playerView.hideController()

            // Auto hide duration
            binding.playerView.controllerShowTimeoutMs = 2000

            val httpDataSourceFactory =
                DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)

            val dataSourceFactory =
                DefaultDataSource.Factory(requireContext(), httpDataSourceFactory)

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

                    // Check if video is horizontal (width > height)
                    isVideoHorizontal = videoWidth > videoHeight

                    Log.d("VideoFragment", "Video size: ${videoWidth}x${videoHeight}, isHorizontal: $isVideoHorizontal")

                    // Adjust fullscreen button based on video orientation
                    adjustFullscreenButton()
                }

                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        val durationMs = exoPlayer.duration
                        val formatted = formatDuration(durationMs)
                        Log.d("VideoFragment", "Video duration: $formatted")
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {

                    if (isPlaying) {
                        // Playing → hide controller smoothly
                        binding.playerView.postDelayed({
                            binding.playerView.hideController()
                        }, 500)
                    } else {
                        // Paused → show controller
                        binding.playerView.showController()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e("ExoPlayer", "Playback error: ${error.message}", error)
                    showInfoToast("Video failed to load.")
                }
            })
        }
    }

    private fun adjustFullscreenButton() {
        if (!isVideoHorizontal) {
            // For vertical videos, keep button enabled with fullscreen functionality
            binding.ivFullscreen.visibility = View.VISIBLE
            binding.ivFullscreen.setImageResource(R.drawable.ic_maximise)
            binding.ivFullscreen.alpha = 1f
        } else {
            binding.ivFullscreen.visibility = View.VISIBLE
            binding.ivFullscreen.alpha = 1f
        }
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
        // Only reset orientation if it was changed for horizontal video
        if (isVideoHorizontal && isFullscreen) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    /**
     * Fullscreen + rotation - Only for horizontal videos
     * For vertical videos, just expand without rotation
     */
    private fun toggleFullscreen() {
        // Save current playback state
        val wasPlaying = player?.isPlaying == true
        val currentPosition = player?.currentPosition ?: 0

        if (!isVideoHorizontal) {
            // For vertical videos: expand without rotation
            toggleFullscreenWithoutRotation()
            // Restore playback state after layout change
            binding.videoContainer.postDelayed({
                player?.seekTo(currentPosition)
                if (wasPlaying) {
                    player?.play()
                }
            }, 100)
            return
        }

        // For horizontal videos: toggle with rotation
        isFullscreen = !isFullscreen

        if (isFullscreen) {
            enterFullscreen()
        } else {
            exitFullscreen()
        }

        // Restore playback state after layout change
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
            // Expand to fullscreen without rotation
            hideSystemUI()

            val params = binding.videoContainer.layoutParams as ConstraintLayout.LayoutParams
            params.topToBottom = ConstraintLayout.LayoutParams.UNSET
            params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID
            params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            params.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
            params.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
            binding.videoContainer.layoutParams = params

            // Set resize mode to fit for vertical videos
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

        // Change orientation for horizontal videos
        if (isVideoHorizontal) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        hideSystemUI()

        val params = binding.videoContainer.layoutParams as ConstraintLayout.LayoutParams

        // Clear previous constraints
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

        // Only reset orientation if it was changed (horizontal video)
        if (isVideoHorizontal) {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        showSystemUI()

        val params = binding.videoContainer.layoutParams as ConstraintLayout.LayoutParams

        // Reset constraints
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
        // Reset system UI
        requireActivity().window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        requireActivity().window.statusBarColor = Color.TRANSPARENT
        savePlayerState()
        releasePlayer()
    }

    private fun View.fadeInSlideUp() {
        if (visibility == View.VISIBLE) return

        // start slightly below
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