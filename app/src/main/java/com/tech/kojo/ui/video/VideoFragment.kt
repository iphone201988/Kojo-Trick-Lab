package com.tech.kojo.ui.video

import android.net.Uri
import android.util.Log
import android.view.View
import androidx.annotation.OptIn
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
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

    override fun getLayoutResource(): Int = R.layout.fragment_video

    override fun getViewModel(): BaseViewModel = viewModel

    override fun onCreateView(view: View) {
        initOnClick()
        initializePlayer()
        val videoId = arguments?.getString("videoId")
        if (videoId!=null){
            val request = HashMap<String, Any>()
            request["timeTaken"]=200
            request["isViewed"]=true
            viewModel.updateVideoViewStatus("${Constants.UPDATE_VIDEO_VIEW}/$videoId",request)
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
            }
        }
    }

    private fun initObserver(){
        viewModel.observeCommon.observe(viewLifecycleOwner){
            when(it?.status){
                Status.LOADING -> {}
                Status.SUCCESS -> {
                    when(it.message){
                        "updateVideoViewStatus"->{
                            runCatching {
                                val model =
                                    BindingUtils.parseJson<UpdateVideoCountModel>(it.data.toString())
                                if (model?.success == true && model.data != null) {
                                    Log.d("video count updated","Video Count Updated")
                                }
                            }.onFailure { e ->
                                showErrorToast(e.message.toString())
                            }.also {
                            }
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

        val mediaUri: Uri? = when {
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

        if (mediaUri == null) return

        player = ExoPlayer.Builder(requireContext()).build().also { exoPlayer ->

            binding.playerView.player = exoPlayer

            // ✅ Enable controller
            binding.playerView.useController = true

            // ✅ Hide initially
            binding.playerView.hideController()

            // ✅ Auto hide duration
            binding.playerView.controllerShowTimeoutMs = 2000

            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true)

            val dataSourceFactory =
                DefaultDataSource.Factory(requireContext(), httpDataSourceFactory)

            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(mediaUri))

            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true

            exoPlayer.addListener(object : Player.Listener {

                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        val durationMs = exoPlayer.duration
                        val formatted = formatDuration(durationMs)
                        Log.d("VideoFragment", "Video duration: $formatted")
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {

                    if (isPlaying) {
                        // ▶️ Playing → hide controller smoothly
                        binding.playerView.postDelayed({
                            binding.playerView.hideController()
                        }, 500)
                    } else {
                        // ⏸ Paused → show controller
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
        player?.pause()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }
}
