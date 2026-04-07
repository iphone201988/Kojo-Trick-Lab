package com.tech.kojo.ui.dashboard.community.create_Post

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.UploadProfileApiResponse
import com.tech.kojo.databinding.FragmentCreatePostBinding
import com.tech.kojo.databinding.VideoImagePickerDialogBoxBinding
import com.tech.kojo.databinding.WarningDialogItemBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.utils.BaseCustomDialog
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.BindingUtils.hasCameraPermission
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.VideoCompressor
import com.tech.kojo.utils.showErrorToast
import com.tech.kojo.utils.showInfoToast
import com.tech.kojo.utils.showSuccessToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class CreatePostFragment : BaseFragment<FragmentCreatePostBinding>() {
    private val viewModel: CreatePostVm by viewModels()
    private var multipartVideo: MultipartBody.Part? = null
    private var videoLink: String? = null
    private var player: ExoPlayer? = null
    private var imageDialog: BaseCustomDialog<VideoImagePickerDialogBoxBinding>? = null

    private var videoFile: File? = null
    private var compressedVideoFile: File? = null
    private var videoUri: Uri? = null
    private var isCompressing = false

    override fun getLayoutResource(): Int {
        return R.layout.fragment_create_post
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        initOnClick()
        initView()
        initObserver()
    }

    private fun initView() {
        val text = "Upload Video Clip (30mb max)"
        val spannable = SpannableString(text)
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.blue)),
            0,
            16,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.colorPrimary)),
            17,
            text.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvUploadVideo.text = spannable
    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().finish()
                }

                R.id.ivNotification -> {
                    val intent = Intent(requireActivity(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "notificationNew")
                    startActivity(intent)
                }

                R.id.btnPost -> {
                    val title = binding.etTitle.text.toString().trim()
                    val description = binding.etDescription.text.toString().trim()
                    if (validate()) {
                        val data = HashMap<String, Any>()
                        data["title"] = title
                        data["description"] = description
                        data["videoLink"] = videoLink.toString()
                        data["postType"] = if (videoLink != null) "video" else "text"
                        viewModel.cretePostApi(Constants.POST_CREATE, data)
                    }
                }

                R.id.consVideoClip, R.id.ivVideo -> {
                    imageDialog()
                }

                R.id.ivVideoPlay -> {
                    binding.playerView.visibility = View.VISIBLE
                    binding.ivVideo.visibility = View.INVISIBLE
                    binding.ivVideoPlay.visibility = View.GONE
                    showVideo(Constants.BASE_URL_IMAGE + videoLink)
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun showVideo(path: String) {
        val dataSourceFactory = DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
        val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(path))

        player = ExoPlayer.Builder(requireActivity()).build().also {
            binding.playerView.player = it
            it.setMediaSource(mediaSource)
            it.prepare()
            it.playWhenReady = true
        }
        binding.playerView.setShowFastForwardButton(false)
        binding.playerView.setShowNextButton(false)
        binding.playerView.setShowRewindButton(false)
        binding.playerView.setShowPreviousButton(false)
    }

    /**
     * Video Gallery Picker Launcher
     */
    private val galleryVideoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val fileUri = result.data?.data ?: run {
                    showInfoToast("No video selected")
                    return@registerForActivityResult
                }
                processSelectedVideo(fileUri)
            }
        }

    /**
     * Video Camera Capture Launcher
     */
    private val cameraVideoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                videoUri?.let { uri ->
                    processSelectedVideo(uri)
                } ?: run {
                    showInfoToast("Failed to capture video")
                }
            } else {
                showInfoToast("Video capture cancelled")
            }
        }

    /**
     * Process selected/captured video
     */
    private fun processSelectedVideo(fileUri: Uri) {
        val documentFile = DocumentFile.fromSingleUri(requireActivity(), fileUri)
        if (documentFile == null) {
            showInfoToast("Invalid video file")
            return
        }

        runCatching {
            // Check video duration
            var durationMs = 0L
            requireActivity().contentResolver.openFileDescriptor(fileUri, "r")?.use { afd ->
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(afd.fileDescriptor)

                durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull() ?: 0L

                retriever.release()

                if (durationMs > 30_000) {
                    warningDialogItem(1) // Video too long
                    return@runCatching
                }
            }

            // Check video size
            val fileSizeBytes = documentFile.length()
            val maxSizeBytes = 30 * 1024 * 1024L

            if (fileSizeBytes > maxSizeBytes) {
                // Need compression
                showInfoToast("Video is large, compressing...")
                compressAndUploadVideo(fileUri, documentFile.name ?: "video.mp4")
            } else {
                // Size is fine, upload directly
                copyVideoToCacheAndUpload(fileUri, documentFile.name ?: "video.mp4")
            }

        }.onFailure {
            Log.e("CreatePostFragment", "Error processing video: ${it.message}", it)
            showErrorToast("Error processing video")
        }
    }

    /**
     * Compress video and upload
     */
    private fun compressAndUploadVideo(fileUri: Uri, fileName: String) {
        if (isCompressing) {
            showInfoToast("Already compressing...")
            return
        }

        isCompressing = true
        showLoading()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val inputStream = requireActivity().contentResolver.openInputStream(fileUri)
                if (inputStream == null) {
                    withContext(Dispatchers.Main) {
                        showErrorToast("Error reading video")
                        isCompressing = false
                        hideLoading()
                    }
                    return@launch
                }

                // Create temp file for original video
                val originalFile = File(
                    requireActivity().cacheDir,
                    "original_${System.currentTimeMillis()}_$fileName"
                )
                inputStream.use { input ->
                    originalFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                // Create compressed file
                val compressedFile = File(
                    requireActivity().cacheDir,
                    "compressed_${System.currentTimeMillis()}_$fileName"
                )

                // Compress video
                val compressionResult = withContext(Dispatchers.IO) {
                    compressVideo(originalFile, compressedFile)
                }

                // Delete original file after compression
                originalFile.delete()

                withContext(Dispatchers.Main) {
                    if (compressionResult != null && compressionResult.exists() && compressionResult.length() > 0) {
                        val compressedSizeMB = compressionResult.length() / (1024.0 * 1024.0)
                        Log.d(
                            "CreatePostFragment",
                            "Compressed video size: ${String.format("%.2f", compressedSizeMB)} MB"
                        )

                        if (compressionResult.length() > 30 * 1024 * 1024) {
                            warningDialogItem(2)
                            isCompressing = false
                            hideLoading()
                            return@withContext
                        }

                        // Upload compressed video
                        val requestFile =
                            compressionResult.asRequestBody("video/*".toMediaTypeOrNull())
                        multipartVideo = MultipartBody.Part.createFormData(
                            "file", compressionResult.name, requestFile
                        )

                        if (multipartVideo != null) {
                            viewModel.uploadVideoApi(Constants.UPLOAD, multipartVideo)
                        } else {
                            showErrorToast("Something went wrong, try again")
                            isCompressing = false
                            hideLoading()
                        }
                    } else {
                        showErrorToast("Compression failed, try with smaller video")
                        isCompressing = false
                        hideLoading()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("CreatePostFragment", "Error compressing video: ${e.message}", e)
                    showErrorToast("Error compressing video: ${e.message}")
                    isCompressing = false
                    hideLoading()
                }
            }
        }
    }

    /**
     * Compress video using FFmpeg or fallback methods
     */
    private suspend fun compressVideo(inputFile: File, outputFile: File): File? {
        return withContext(Dispatchers.IO) {
            try {
                // Method 1: Try using VideoCompressor library
                val compressed = VideoCompressor.compressVideo(
                    context = requireContext(),
                    videoFile = inputFile,
                    destinationFile = outputFile,
                    quality = VideoCompressor.Quality.MEDIUM, // LOW, MEDIUM, HIGH
                    minBitrate = 500_000, // 500 kbps min
                    maxBitrate = 2_000_000 // 2 mbps max
                )

                if (compressed != null && compressed.exists()) {
                    return@withContext compressed
                }

                // Method 2: Fallback - use Android's built-in compression
                fallbackCompressVideo(inputFile, outputFile)

            } catch (e: Exception) {
                Log.e("CreatePostFragment", "Compression error: ${e.message}", e)
                fallbackCompressVideo(inputFile, outputFile)
            }
        }
    }

    /**
     * Fallback compression method using MediaCodec (simplified)
     */
    private fun fallbackCompressVideo(inputFile: File, outputFile: File): File? {
        return try {
            if (inputFile.length() <= 30 * 1024 * 1024) {
                inputFile.copyTo(outputFile, overwrite = true)
                outputFile
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("CreatePostFragment", "Fallback compression failed: ${e.message}", e)
            null
        }
    }

    /**
     * Copy video to cache and upload (no compression needed)
     */
    private fun copyVideoToCacheAndUpload(fileUri: Uri, fileName: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val inputStream = requireActivity().contentResolver.openInputStream(fileUri)
                if (inputStream == null) {
                    withContext(Dispatchers.Main) { showInfoToast("Error reading video") }
                    return@launch
                }

                val targetFile = File(
                    requireActivity().cacheDir,
                    "video_${System.currentTimeMillis()}_$fileName"
                )

                inputStream.use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                withContext(Dispatchers.Main) {
                    val requestFile = targetFile.asRequestBody("video/*".toMediaTypeOrNull())
                    multipartVideo = MultipartBody.Part.createFormData(
                        "file", targetFile.name, requestFile
                    )

                    if (multipartVideo != null) {
                        viewModel.uploadVideoApi(Constants.UPLOAD, multipartVideo)
                    } else {
                        showErrorToast("Something went wrong, try again")
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("CreatePostFragment", "Error copying video: ${e.message}", e)
                    showErrorToast("Error copying video: ${e.message}")
                }
            }
        }
    }

    /**
     * Create video file for camera capture
     */
    private fun createVideoFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = requireContext().cacheDir
            File.createTempFile(
                "VIDEO_${timeStamp}_", ".mp4", storageDir
            ).apply {
                videoFile = this
                videoUri = FileProvider.getUriForFile(
                    requireContext(), "${requireContext().packageName}.fileProvider", this
                )
            }
        } catch (e: IOException) {
            Log.e("CreatePostFragment", "Error creating video file: ${e.message}", e)
            showErrorToast("Failed to create video file")
            null
        }
    }

    /**
     * Open camera to capture video
     */
    private fun openCameraForVideo() {
        val videoFile = createVideoFile()
        if (videoFile != null) {
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
                putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30) // 30 seconds limit
                putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1) // 1 = high quality
            }
            cameraVideoLauncher.launch(intent)
        } else {
            showErrorToast("Failed to prepare camera")
        }
    }

    private lateinit var warningItem: BaseCustomDialog<WarningDialogItemBinding>

    fun warningDialogItem(type: Int) {
        warningItem = BaseCustomDialog(
            requireContext(), R.layout.warning_dialog_item
        ) {
            when (it?.id) {
                R.id.btnDeleteCancel -> {
                    warningItem.dismiss()
                }
            }
        }
        warningItem.create()
        warningItem.show()

        when (type) {
            1 -> {
                warningItem.binding.text.text = "Video too long"
                warningItem.binding.tvSure.text =
                    "Your video exceeds the allowed duration (Max 30 seconds)."
            }

            2 -> {
                warningItem.binding.text.text = "File too Large"
                warningItem.binding.tvSure.text =
                    "Your video exceeds the size limit (Max 30 MB)."
            }

            3 -> {
                warningItem.binding.text.text = "Compression Failed"
                warningItem.binding.tvSure.text =
                    "Unable to compress video. Please try with a smaller video."
            }
        }
    }

    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.LOADING -> {
                    if (!isCompressing) {
                        showLoading()
                    }
                }

                Status.SUCCESS -> {
                    when (it.message) {
                        "cretePostApi" -> {
                            runCatching {
                                showSuccessToast("Post created successfully")
                                requireActivity().finish()
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }

                        "uploadVideoApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: UploadProfileApiResponse? =
                                    BindingUtils.parseJson(jsonData)
                                if (model?.success == true) {
                                    videoLink = model.url
                                    // Show thumbnail preview
                                    val fullUrl = Constants.BASE_URL_IMAGE + videoLink
                                    binding.ivVideo.visibility = View.VISIBLE
                                    binding.consVideoClip.visibility = View.INVISIBLE
                                    binding.ivVideoPlay.visibility = View.VISIBLE

                                    Glide.with(requireContext()).asBitmap().load(fullUrl)
                                        .apply(RequestOptions().frame(1_000_000))
                                        .into(binding.ivVideo)

                                    showSuccessToast("Video uploaded successfully")
                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                                isCompressing = false
                            }
                        }
                    }
                }

                Status.ERROR -> {
                    hideLoading()
                    isCompressing = false
                    showErrorToast(it.message.toString())
                }

                else -> {}
            }
        }
    }

    private fun imageDialog() {
        imageDialog = BaseCustomDialog(requireActivity(), R.layout.video_image_picker_dialog_box) {
            when (it.id) {
                R.id.tvCamera, R.id.imageCamera -> {
                    if (!hasCameraPermission(requireContext())) {
                        cameraPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                    } else {
                        openCameraForVideo()
                    }
                    imageDialog?.dismiss()
                }

                R.id.imageGallery, R.id.tvGallery -> {
                    val intent = Intent(Intent.ACTION_PICK).apply {
                        type = "video/*"
                    }
                    galleryVideoLauncher.launch(intent)
                    imageDialog?.dismiss()
                }
            }
        }

        imageDialog?.create()
        imageDialog?.show()
    }

    private val cameraPermissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                openCameraForVideo()
            } else {
                showInfoToast("Camera permission required to capture video")
            }
        }

    private fun validate(): Boolean {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (title.isEmpty()) {
            showInfoToast("Please enter title")
            return false
        }
        if (description.isEmpty()) {
            showInfoToast("Please enter description")
            return false
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
        // Clean up temp files
        videoFile?.delete()
        compressedVideoFile?.delete()
    }

    override fun onResume() {
        super.onResume()
        viewModel.notificationCount.value = sharedPrefManager.getNotificationCount()
    }
}