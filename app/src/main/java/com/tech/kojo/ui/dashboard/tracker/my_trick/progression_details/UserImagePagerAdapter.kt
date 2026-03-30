package com.tech.kojo.ui.dashboard.tracker.my_trick.progression_details

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.recyclerview.widget.RecyclerView
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.VideoLink
import com.tech.kojo.databinding.HolderUserImageBinding

@OptIn(UnstableApi::class)
class UserImagePagerAdapter(
    private val displayVideos: List<VideoLink>,
    private val originalVideos: List<VideoLink>,
    private val onImageClick: (VideoLink) -> Unit,
    private val onFullScreenClick: (VideoLink) -> Unit = {},
    private val onPlaybackStateChanged: ((isPlaying: Boolean) -> Unit)? = null
) : RecyclerView.Adapter<UserImagePagerAdapter.ImageViewHolder>() {

    private var currentPlayingPosition: Int = -1
    private var isControllerVisible: Boolean = true

    inner class ImageViewHolder(val binding: HolderUserImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var player: ExoPlayer? = null
        private var isPlaying = false

        init {
            binding.root.setOnClickListener {
                onImageClick(displayVideos[adapterPosition])
            }

            binding.ivVideoPlay.setOnClickListener {
                if (isPlaying) {
                    stopVideo()
                } else {
                    playVideo()
                }
            }

            // Fullscreen button click handler - always available
            binding.ivFullscreen.setOnClickListener {
                displayVideos.getOrNull(adapterPosition)?.let { onFullScreenClick(it) }
            }

//            // Add click listener for player controller visibility toggle
//            binding.localPlayerView.setOnClickListener {
//                toggleControllerVisibility()
//            }
        }

        fun bind(item: VideoLink, position: Int) {
            binding.bean = item
            binding.executePendingBindings()

            if (position != currentPlayingPosition) {
                resetUI()
            } else {
                // If this is the current playing position, restore player view
                if (isPlaying && player != null) {
                    showPlayerView()
                }
            }
        }

        private fun resetUI() {
            binding.localPlayerView.visibility = View.GONE
            binding.ivUser.visibility = View.VISIBLE
            binding.ivVideoPlay.visibility = View.VISIBLE
            // Fullscreen button is always visible in thumbnail mode
            binding.ivFullscreen.visibility = View.VISIBLE
            isPlaying = false
        }

        @androidx.annotation.OptIn(UnstableApi::class)
        private fun showPlayerView() {
            binding.localPlayerView.player = player
            binding.localPlayerView.visibility = View.VISIBLE
            binding.localPlayerView.setShowFastForwardButton(false)
            binding.localPlayerView.setShowNextButton(false)
            binding.localPlayerView.setShowRewindButton(false)
            binding.localPlayerView.setShowPreviousButton(false)
            binding.ivUser.visibility = View.INVISIBLE
            binding.ivVideoPlay.visibility = View.GONE
            // Fullscreen button remains visible when video is playing
            binding.ivFullscreen.visibility = View.VISIBLE

            // Restore controller visibility state
//            updateControllerVisibility()
        }

//        private fun toggleControllerVisibility() {
//            if (isPlaying) {
//                isControllerVisible = !isControllerVisible
//                updateControllerVisibility()
//            }
//        }

//        private fun updateControllerVisibility() {
//            if (isControllerVisible) {
//                // Show controller
//                binding.localPlayerView.useController = true
//                // Auto-hide controller after 3 seconds
//                binding.localPlayerView.postDelayed({
//                    if (isControllerVisible && isPlaying) {
//                        binding.localPlayerView.useController = false
//                        isControllerVisible = false
//                    }
//                }, 3000)
//            } else {
//                // Hide controller but fullscreen button remains visible
//                binding.localPlayerView.useController = false
//            }
//            // Fullscreen button is always visible regardless of controller state
//        }

        fun autoPlayVideo() {
            if (!isPlaying) {
                playVideo()
            }
        }

        @androidx.annotation.OptIn(UnstableApi::class)
        private fun playVideo() {
            val videoUrl = displayVideos.getOrNull(adapterPosition)?.link ?: run {
//                showErrorToast("Video URL not found")
                return
            }

            // Stop any currently playing video first
            stopVideo()

            val fullUrl = if (videoUrl.startsWith("http")) videoUrl
            else Constants.BASE_URL_IMAGE + videoUrl

            try {
                val mediaSource = ProgressiveMediaSource.Factory(
                    DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
                ).createMediaSource(MediaItem.fromUri(fullUrl))

                player = ExoPlayer.Builder(binding.root.context).build().apply {
                    setMediaSource(mediaSource)
                    prepare()
                    playWhenReady = true
                    repeatMode = Player.REPEAT_MODE_ONE

                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(state: Int) {
                            when (state) {
                                Player.STATE_READY -> {
                                    onPlaybackStateChanged?.invoke(true)
                                    Log.d("VideoPlayer", "Video ready at position $adapterPosition")
                                }

                                Player.STATE_ENDED -> {
                                    Log.d("VideoPlayer", "Video ended at position $adapterPosition")
                                    stopVideo()
                                }
                            }
                        }

                        override fun onPlayerError(error: PlaybackException) {
                            Log.e("VideoPlayer", "Playback error: ${error.message}")
//                            showErrorToast("Playback error occurred")
                            stopVideo()
                        }
                    })
                }

                // Setup UI - fullscreen button is always visible
                binding.localPlayerView.player = player
                binding.localPlayerView.visibility = View.VISIBLE
                binding.localPlayerView.setShowFastForwardButton(false)
                binding.localPlayerView.setShowNextButton(false)
                binding.localPlayerView.setShowRewindButton(false)
                binding.localPlayerView.setShowPreviousButton(false)
                binding.ivUser.visibility = View.INVISIBLE
                binding.ivVideoPlay.visibility = View.GONE
                binding.ivFullscreen.visibility = View.VISIBLE  // Fullscreen always visible

                // Show controller initially, then auto-hide
                isControllerVisible = true
                binding.localPlayerView.useController = true

                // Auto-hide controller after 3 seconds
                binding.localPlayerView.postDelayed({
                    if (isPlaying && binding.localPlayerView.visibility == View.VISIBLE) {
                        binding.localPlayerView.useController = true
                        isControllerVisible = false
                    }
                }, 3000)

                isPlaying = true
                currentPlayingPosition = adapterPosition

                Log.d("VideoPlayer", "Started playing video at position $adapterPosition")

            } catch (e: Exception) {
                Log.e("VideoPlayer", "Error playing video: ${e.message}", e)
//                showErrorToast("Failed to play video")
                stopVideo()
            }
        }

        fun stopVideo() {
            if (!isPlaying && player == null) return

            player?.stop()
            player?.release()
            player = null
            binding.localPlayerView.player = null

            resetUI()
            isPlaying = false

            if (currentPlayingPosition == adapterPosition) {
                currentPlayingPosition = -1
            }

            onPlaybackStateChanged?.invoke(false)
            Log.d("VideoPlayer", "Stopped video at position $adapterPosition")
        }

        fun pauseVideo() {
            if (isPlaying) {
                player?.pause()
                onPlaybackStateChanged?.invoke(false)
                Log.d("VideoPlayer", "Paused video at position $adapterPosition")
            }
        }

        fun resumeVideo() {
            if (isPlaying && player != null) {
                player?.play()
                onPlaybackStateChanged?.invoke(true)
                Log.d("VideoPlayer", "Resumed video at position $adapterPosition")
            }
        }

        fun releasePlayer() {
            player?.release()
            player = null
            isPlaying = false
        }

        fun isVideoPlaying(): Boolean = isPlaying
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = HolderUserImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(displayVideos[position], position)

        // Log for debugging
        val originalIndex = originalVideos.indexOf(displayVideos[position])
        Log.d("ViewPagerAdapter", "Position $position shows original video at index $originalIndex")
    }

    override fun getItemCount(): Int = displayVideos.size

    override fun onViewRecycled(holder: ImageViewHolder) {
        holder.releasePlayer()
    }

    /**
     * Play video at specific position - call this when swiping
     */
    fun playVideoAt(position: Int, recyclerView: RecyclerView) {
        // Stop previous video if playing
        if (currentPlayingPosition != -1 && currentPlayingPosition != position) {
            val oldHolder = recyclerView.findViewHolderForAdapterPosition(
                currentPlayingPosition
            ) as? ImageViewHolder
            oldHolder?.stopVideo()
            Log.d("VideoPlayer", "Stopped video at position $currentPlayingPosition")
        }

        // Auto-play new video
        val newHolder = recyclerView.findViewHolderForAdapterPosition(position) as? ImageViewHolder
        newHolder?.autoPlayVideo()

        currentPlayingPosition = position
        Log.d("VideoPlayer", "Playing video at position $position")
    }

    /**
     * Pause all videos
     */
    fun pauseAllVideos() {
        currentPlayingPosition = -1
        onPlaybackStateChanged?.invoke(false)
    }

    /**
     * Resume current video if any
     */
    fun resumeCurrentVideo(recyclerView: RecyclerView) {
        if (currentPlayingPosition != -1) {
            val holder =
                recyclerView.findViewHolderForAdapterPosition(currentPlayingPosition) as? ImageViewHolder
            holder?.resumeVideo()
        }
    }

    /**
     * Release all players
     */
    fun releaseAllPlayers(recyclerView: RecyclerView) {
        for (i in 0 until itemCount) {
            val holder = recyclerView.findViewHolderForAdapterPosition(i) as? ImageViewHolder
            holder?.stopVideo()
            holder?.releasePlayer()
        }
        currentPlayingPosition = -1
    }

    /**
     * Get original index of video at display position
     */
    fun getOriginalIndex(displayPosition: Int): Int {
        return if (displayPosition in displayVideos.indices) {
            originalVideos.indexOf(displayVideos[displayPosition])
        } else {
            -1
        }
    }
}