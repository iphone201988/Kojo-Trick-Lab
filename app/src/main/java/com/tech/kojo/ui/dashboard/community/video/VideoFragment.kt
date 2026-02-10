package com.tech.kojo.ui.dashboard.community.video

import android.net.Uri
import android.util.Log
import android.view.View
import androidx.annotation.OptIn
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.data.api.Constants
import com.tech.kojo.databinding.FragmentVideoBinding
import com.tech.kojo.utils.showInfoToast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class VideoFragment : BaseFragment<FragmentVideoBinding>() {

    private val viewModel: VideoFragmentVM by viewModels()
    private var player: ExoPlayer? = null

    override fun getLayoutResource(): Int = R.layout.fragment_video

    override fun getViewModel(): BaseViewModel = viewModel

    override fun onCreateView(view: View) {
        initOnClick()
        initializePlayer()
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

    /**
     * Initialize ExoPlayer with URL safety
     */


    @OptIn(UnstableApi::class)
    private fun initializePlayer() {
        val videoPath = arguments?.getString("videoUrl")

        if (videoPath.isNullOrEmpty()) {
            showInfoToast("Video URL not found")
            return
        }

        val fullUrl = Constants.BASE_URL_IMAGE + videoPath

        // Create ExoPlayer instance
        player = ExoPlayer.Builder(requireContext()).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer

            // Create a DataSource factory that allows redirects
            val dataSourceFactory = DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true) // Important for 302 redirects

            // Build MediaSource with the factory
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(fullUrl)))

            // Prepare and start playback
            exoPlayer.setMediaSource(mediaSource)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true

            // Error listener
            exoPlayer.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    Log.e("ExoPlayer", "Playback error: ${error.message}", error)
                    showInfoToast("Video failed to load.")
                }
            })
        }
    }


    /* private fun initializePlayer() {
         val videoPath = arguments?.getString("videoUrl")

         if (videoPath.isNullOrEmpty()) {
             showInfoToast("Video URL not found")
             return
         }

         val fullUrl = Constants.BASE_URL_IMAGE + videoPath

         player = ExoPlayer.Builder(requireContext()).build().apply {
             binding.playerView.player = this

             val mediaItem = MediaItem.fromUri(Uri.parse(fullUrl))
             setMediaItem(mediaItem)

             prepare()
             playWhenReady = true

             addListener(object : Player.Listener {
                 override fun onPlayerError(error: PlaybackException) {
                         Log.e("ExoPlayer", "Playback error: ${error.message}", error)
                     showInfoToast("Video failed to load.")
                 }
             })
         }
     }*/

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
