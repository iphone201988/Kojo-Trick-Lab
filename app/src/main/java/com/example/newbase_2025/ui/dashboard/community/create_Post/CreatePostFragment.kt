package com.example.newbase_2025.ui.dashboard.community.create_Post

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.data.api.Constants
import com.example.newbase_2025.data.model.GetPostApiResponse
import com.example.newbase_2025.data.model.UploadProfileApiResponse
import com.example.newbase_2025.databinding.FragmentCreatePostBinding
import com.example.newbase_2025.databinding.WarningDialogItemBinding
import com.example.newbase_2025.utils.BaseCustomDialog
import com.example.newbase_2025.utils.BindingUtils
import com.example.newbase_2025.utils.Status
import com.example.newbase_2025.utils.showErrorToast
import com.example.newbase_2025.utils.showInfoToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


@AndroidEntryPoint
class CreatePostFragment : BaseFragment<FragmentCreatePostBinding>() {
    private val viewModel: CreatePostVm by viewModels()
    private var multipartVideo: MultipartBody.Part? = null
    private var videoLink: String? = null
    private var player: ExoPlayer? = null
    override fun getLayoutResource(): Int {
        return R.layout.fragment_create_post
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


    /**
     * Method to initialize view
     */

    private fun initView() {
        // text color change
        val text = "Upload Video Clip (20mb max)"
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

    /**
     * Method to initialize click
     */
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {

                R.id.ivBack -> {
                    requireActivity().finish()
                }

                R.id.btnPost -> {
                    val email = binding.etTitle.text.toString().trim()
                    val description = binding.etDescription.text.toString().trim()
                    val data = HashMap<String, Any>()
                    if (validate()) {
                        data["title"] = email
                        data["description"] = description
                        data["description"] = description
                        data["videoLink"] = videoLink.toString()
                        data["postType"] = if (videoLink != null) "video" else "text"
                        viewModel.cretePostApi(Constants.POST_CREATE, data)
                    }

                }

                R.id.consVideoClip, R.id.ivVideo -> {
                    // icAddImg
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "video/*"
                    resultLauncherVideo.launch(intent)
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


    /**
     * play video if video compressed
     */
    private fun showVideo(path: String) {
        player = ExoPlayer.Builder(requireActivity()).build().also {
            binding.playerView.player = it
            val mediaItem = MediaItem.fromUri(path)
            it.setMediaItem(mediaItem)
            it.prepare()
            it.playWhenReady = true
        }
    }

    /**
     * Video launcher with 10s duration + 20MB size validation
     */
    private val resultLauncherVideo =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {

                val fileUri = result.data?.data ?: run {
                    showInfoToast("No video selected")
                    return@registerForActivityResult
                }

                val documentFile = DocumentFile.fromSingleUri(requireActivity(), fileUri)
                if (documentFile == null) {
                    showInfoToast("Invalid video file")
                    return@registerForActivityResult
                }

                runCatching {

                    // ----------- Check video duration ------------
                    requireActivity().contentResolver.openFileDescriptor(fileUri, "r")?.use { afd ->
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(afd.fileDescriptor)

                        val durationMs =
                            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                ?.toLongOrNull() ?: 0L

                        retriever.release()

                        if (durationMs > 10_000) {
                            warningDialogItem(2)  // your alert popup
                            return@registerForActivityResult
                        }
                    }

                    // ----------- Check video size ------------
                    val fileSizeBytes = documentFile.length()
                    val maxSizeBytes = 20 * 1024 * 1024L // 20MB

                    if (fileSizeBytes > maxSizeBytes) {
                        warningDialogItem(1)  // your alert popup
                        return@registerForActivityResult
                    }


                    // ---------- Copy video to cache -------------
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            val inputStream =
                                requireActivity().contentResolver.openInputStream(fileUri)
                            if (inputStream == null) {
                                withContext(Dispatchers.Main) { showInfoToast("Error reading video") }
                                return@launch
                            }

                            val targetFile =
                                File(requireActivity().cacheDir, documentFile.name ?: "video.mp4")

                            inputStream.use { input ->
                                targetFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }

                            withContext(Dispatchers.Main) {
                                val requestFile =
                                    targetFile.asRequestBody("video/*".toMediaTypeOrNull())
                                multipartVideo = MultipartBody.Part.createFormData(
                                    "file", targetFile.name, requestFile
                                )

                                if (multipartVideo != null) {
                                    viewModel.uploadVideoApi(Constants.UPLOAD, multipartVideo)
                                } else {
                                    showErrorToast("Something went wrong, try again")
                                }


                                viewModel.uploadVideoApi(Constants.UPLOAD, multipartVideo)
                            }

                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                showErrorToast("Error copying video: ${e.message}")
                            }
                        }
                    }

                }.onFailure {
                    showErrorToast("Error processing video")
                }
            }
        }


    /**
     * dialog bix initialize and handel
     */
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

        if (type == 1) {
            warningItem.binding.text.text = "Video too long"
            warningItem.binding.tvSure.text =
                "Your video exceeds the allowed duration (Max 10 seconds)."
        } else {
            warningItem.binding.text.text = "File  too Long"
            warningItem.binding.tvSure.text = "Your video exceeds the size limit (Max 20 MB)."
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
                        "cretePostApi" -> {
                            runCatching {
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
                                    // ----------- Show thumbnail preview ------------
                                    val fullUrl = Constants.BASE_URL_IMAGE + videoLink
                                    binding.ivVideo.visibility = View.VISIBLE
                                    binding.consVideoClip.visibility = View.INVISIBLE
                                    binding.ivVideoPlay.visibility = View.VISIBLE

                                    Glide.with(requireContext()).asBitmap().load(fullUrl)
                                        .apply(RequestOptions().frame(1_000_000))
                                        .into(binding.ivVideo)

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


    /*** add validation ***/
    private fun validate(): Boolean {
        val email = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        if (email.isEmpty()) {
            showInfoToast("Please enter title")
            return false
        } else if (description.isEmpty()) {
            showInfoToast("Please enter description")
            return false
        }

        return true
    }


    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }

}