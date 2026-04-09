package com.tech.kojo.ui.dashboard.tracker.my_trick.progression_details

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
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

    inner class ImageViewHolder(val binding: HolderUserImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var player: ExoPlayer? = null
        private var isPlaying = false
        private var isUsingSoftwareDecoder = false
        private var retryCount = 0
        private val MAX_RETRY_COUNT = 2
        private var currentVideoUrl: String? = null

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
        }

        fun bind(item: VideoLink, position: Int) {
            binding.bean = item
            binding.executePendingBindings()

            if (position != currentPlayingPosition) {
                resetUI()
            } else {
                if (isPlaying && player != null) {
                    showPlayerView()
                }
            }
        }

        private fun resetUI() {
            binding.localPlayerView.visibility = View.GONE
            binding.ivUser.visibility = View.VISIBLE
            binding.ivVideoPlay.visibility = View.VISIBLE
            isPlaying = false
            isUsingSoftwareDecoder = false
            retryCount = 0
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
        }

        fun autoPlayVideo() {
            if (!isPlaying) {
                playVideo()
            }
        }

        @androidx.annotation.OptIn(UnstableApi::class)
        private fun playVideo() {
            val videoUrl = displayVideos.getOrNull(adapterPosition)?.link ?: run {
                Log.e("VideoPlayer", "Video URL not found at position $adapterPosition")
                return
            }

            currentVideoUrl = videoUrl

            // Stop any currently playing video first
            stopVideo()

            val fullUrl = if (videoUrl.startsWith("http")) videoUrl
            else Constants.BASE_URL_IMAGE + videoUrl

            try {
                // Create track selector with adaptive resolution
                val trackSelector = DefaultTrackSelector(binding.root.context).apply {
                    setParameters(
                        parameters.buildUpon().setAllowVideoMixedMimeTypeAdaptiveness(true)
                            .setAllowAudioMixedMimeTypeAdaptiveness(true).build()
                    )
                }

                // Create renderers factory with decoder fallback
                val renderersFactory =
                    DefaultRenderersFactory(binding.root.context).setEnableDecoderFallback(true) // CRITICAL for high-res videos
                        .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER)

                player =
                    ExoPlayer.Builder(binding.root.context).setRenderersFactory(renderersFactory)
                        .setTrackSelector(trackSelector).build().apply {
                            val httpDataSourceFactory =
                                DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
                                    .setConnectTimeoutMs(30000).setReadTimeoutMs(30000)
                                    .setUserAgent("KojoVideoPlayer/1.0")

                            val dataSourceFactory = DefaultDataSource.Factory(
                                binding.root.context, httpDataSourceFactory
                            )

                            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                                .createMediaSource(MediaItem.fromUri(fullUrl))

                            setMediaSource(mediaSource)
                            prepare()
                            playWhenReady = true
                            repeatMode = Player.REPEAT_MODE_ONE

                            addListener(object : Player.Listener {
                                override fun onVideoSizeChanged(videoSize: VideoSize) {
                                    super.onVideoSizeChanged(videoSize)
                                    Log.d(
                                        "VideoPlayer",
                                        "Video size: ${videoSize.width}x${videoSize.height}, " + "usingSoftwareDecoder: $isUsingSoftwareDecoder"
                                    )
                                }

                                override fun onPlaybackStateChanged(state: Int) {
                                    when (state) {
                                        Player.STATE_READY -> {
                                            onPlaybackStateChanged?.invoke(true)
                                            Log.d(
                                                "VideoPlayer",
                                                "Video ready at position $adapterPosition"
                                            )
                                        }

                                        Player.STATE_ENDED -> {
                                            Log.d(
                                                "VideoPlayer",
                                                "Video ended at position $adapterPosition"
                                            )
                                            stopVideo()
                                        }
                                    }
                                }

                                override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                                    if (isPlayingNow) {
                                        binding.localPlayerView.postDelayed({
                                            binding.localPlayerView.hideController()
                                        }, 500)
                                    } else {
                                        binding.localPlayerView.showController()
                                    }
                                }

                                override fun onPlayerError(error: PlaybackException) {
                                    Log.e("VideoPlayer", "Playback error: ${error.message}", error)

                                    // Handle decoder errors with retry logic
                                    when {
                                        error.errorCode == PlaybackException.ERROR_CODE_DECODER_INIT_FAILED || error.message?.contains(
                                            "exceeds capabilities"
                                        ) == true || error.message?.contains("NO_EXCEEDS_CAPABILITIES") == true || error.message?.contains(
                                            "MediaCodecVideoRenderer"
                                        ) == true -> {

                                            if (!isUsingSoftwareDecoder && retryCount < MAX_RETRY_COUNT) {
                                                retryCount++
                                                Log.w(
                                                    "VideoPlayer",
                                                    "Hardware decoder failed, retrying with software decoding (Attempt $retryCount)"
                                                )
                                                retryWithSoftwareDecoding(fullUrl)
                                            } else if (retryCount >= MAX_RETRY_COUNT) {
                                                Log.e(
                                                    "VideoPlayer",
                                                    "Max retries reached, cannot play video"
                                                )
                                                stopVideo()
                                            }
                                        }

                                        error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> {
                                            Log.e("VideoPlayer", "Network connection failed")
                                            stopVideo()
                                        }

                                        else -> {
                                            if (!isUsingSoftwareDecoder && retryCount < MAX_RETRY_COUNT) {
                                                retryCount++
                                                retryWithSoftwareDecoding(fullUrl)
                                            } else {
                                                stopVideo()
                                            }
                                        }
                                    }
                                }
                            })
                        }

                // Setup UI
                binding.localPlayerView.player = player
                binding.localPlayerView.visibility = View.VISIBLE
                binding.localPlayerView.setShowFastForwardButton(false)
                binding.localPlayerView.setShowNextButton(false)
                binding.localPlayerView.setShowRewindButton(false)
                binding.localPlayerView.setShowPreviousButton(false)
                binding.ivUser.visibility = View.INVISIBLE
                binding.ivVideoPlay.visibility = View.GONE

                isPlaying = true
                currentPlayingPosition = adapterPosition

                Log.d("VideoPlayer", "Started playing video at position $adapterPosition")

            } catch (e: Exception) {
                Log.e("VideoPlayer", "Error playing video: ${e.message}", e)
                stopVideo()
            }
        }

        @androidx.annotation.OptIn(UnstableApi::class)
        private fun retryWithSoftwareDecoding(videoUrl: String) {
            isUsingSoftwareDecoder = true

            try {
                // Release existing player
                player?.release()

                // Create track selector with resolution limits for software decoding
                val trackSelector = DefaultTrackSelector(binding.root.context).apply {
                    setParameters(
                        parameters.buildUpon()
                            .setMaxVideoSize(1920, 1920) // Limit to 1080p for software decoding
                            .setMaxVideoBitrate(10_000_000) // 10 Mbps limit
                            .setAllowVideoMixedMimeTypeAdaptiveness(true).build()
                    )
                }

                // Force software decoding
                val renderersFactory =
                    DefaultRenderersFactory(binding.root.context).setEnableDecoderFallback(true)
                        .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)

                player =
                    ExoPlayer.Builder(binding.root.context).setRenderersFactory(renderersFactory)
                        .setTrackSelector(trackSelector).build().apply {
                            val httpDataSourceFactory =
                                DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
                                    .setConnectTimeoutMs(30000).setReadTimeoutMs(30000)
                                    .setUserAgent("KojoVideoPlayer/1.0-Software")

                            val dataSourceFactory = DefaultDataSource.Factory(
                                binding.root.context, httpDataSourceFactory
                            )

                            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                                .createMediaSource(MediaItem.fromUri(videoUrl))

                            setMediaSource(mediaSource)
                            prepare()
                            playWhenReady = true
                            repeatMode = Player.REPEAT_MODE_ONE

                            addListener(object : Player.Listener {
                                override fun onVideoSizeChanged(videoSize: VideoSize) {
                                    Log.d(
                                        "VideoPlayer",
                                        "Software decoder - Video size: ${videoSize.width}x${videoSize.height}"
                                    )
                                }

                                override fun onPlaybackStateChanged(state: Int) {
                                    when (state) {
                                        Player.STATE_READY -> {
                                            onPlaybackStateChanged?.invoke(true)
                                            Log.d(
                                                "VideoPlayer",
                                                "Software decoder - Video ready at position $adapterPosition"
                                            )
                                        }

                                        Player.STATE_ENDED -> {
                                            Log.d(
                                                "VideoPlayer",
                                                "Video ended at position $adapterPosition"
                                            )
                                            stopVideo()
                                        }
                                    }
                                }

                                override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                                    if (isPlayingNow) {
                                        binding.localPlayerView.postDelayed({
                                            binding.localPlayerView.hideController()
                                        }, 500)
                                    } else {
                                        binding.localPlayerView.showController()
                                    }
                                }

                                override fun onPlayerError(error: PlaybackException) {
                                    Log.e(
                                        "VideoPlayer",
                                        "Software decoding failed: ${error.message}",
                                        error
                                    )

                                    if (retryCount < MAX_RETRY_COUNT) {
                                        retryCount++
                                        retryWithAlternativeDecoder(videoUrl)
                                    } else {
                                        stopVideo()
                                    }
                                }
                            })
                        }

                binding.localPlayerView.player = player
                isPlaying = true
                currentPlayingPosition = adapterPosition

                Log.d("VideoPlayer", "Started software decoding at position $adapterPosition")

            } catch (e: Exception) {
                Log.e("VideoPlayer", "Failed to initialize software decoding", e)
                if (retryCount < MAX_RETRY_COUNT) {
                    retryCount++
                    retryWithAlternativeDecoder(videoUrl)
                } else {
                    stopVideo()
                }
            }
        }

        @androidx.annotation.OptIn(UnstableApi::class)
        private fun retryWithAlternativeDecoder(videoUrl: String) {
            try {
                player?.release()

                // Most conservative settings - force 480p
                val trackSelector = DefaultTrackSelector(binding.root.context).apply {
                    setParameters(
                        parameters.buildUpon().setMaxVideoSize(854, 480) // Force 480p max
                            .setMaxVideoBitrate(2_000_000) // 2 Mbps limit
                            .setAllowVideoMixedMimeTypeAdaptiveness(false).build()
                    )
                }

                val renderersFactory =
                    DefaultRenderersFactory(binding.root.context).setEnableDecoderFallback(true)
                        .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)

                player =
                    ExoPlayer.Builder(binding.root.context).setRenderersFactory(renderersFactory)
                        .setTrackSelector(trackSelector).build().apply {
                            val dataSourceFactory = DefaultDataSource.Factory(binding.root.context)
                            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                                .createMediaSource(MediaItem.fromUri(videoUrl))

                            setMediaSource(mediaSource)
                            prepare()
                            playWhenReady = true
                            repeatMode = Player.REPEAT_MODE_ONE

                            addListener(object : Player.Listener {
                                override fun onPlayerError(error: PlaybackException) {
                                    Log.e("VideoPlayer", "Alternative decoder also failed", error)
                                    stopVideo()
                                }
                            })
                        }

                binding.localPlayerView.player = player
                isPlaying = true
                currentPlayingPosition = adapterPosition

            } catch (e: Exception) {
                Log.e("VideoPlayer", "Alternative decoder failed", e)
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
            isUsingSoftwareDecoder = false
            retryCount = 0

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
            isUsingSoftwareDecoder = false
            retryCount = 0
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

        val originalIndex = originalVideos.indexOf(displayVideos[position])
        Log.d("ViewPagerAdapter", "Position $position shows original video at index $originalIndex")
    }

    override fun getItemCount(): Int = displayVideos.size

    override fun onViewRecycled(holder: ImageViewHolder) {
        holder.releasePlayer()
    }

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

    fun pauseAllVideos() {
        currentPlayingPosition = -1
        onPlaybackStateChanged?.invoke(false)
    }

    fun resumeCurrentVideo(recyclerView: RecyclerView) {
        if (currentPlayingPosition != -1) {
            val holder =
                recyclerView.findViewHolderForAdapterPosition(currentPlayingPosition) as? ImageViewHolder
            holder?.resumeVideo()
        }
    }

    fun releaseAllPlayers(recyclerView: RecyclerView) {
        for (i in 0 until itemCount) {
            val holder = recyclerView.findViewHolderForAdapterPosition(i) as? ImageViewHolder
            holder?.stopVideo()
            holder?.releasePlayer()
        }
        currentPlayingPosition = -1
    }

    fun getOriginalIndex(displayPosition: Int): Int {
        return if (displayPosition in displayVideos.indices) {
            originalVideos.indexOf(displayVideos[displayPosition])
        } else {
            -1
        }
    }
}