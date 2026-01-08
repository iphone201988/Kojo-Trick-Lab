package com.tech.kojo.ui.auth.add_profile

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.tech.kojo.BR
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.base.SimpleRecyclerViewAdapter
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.GetProfileResponse
import com.tech.kojo.data.model.UploadProfileApiResponse
import com.tech.kojo.databinding.FragmentAddProfileBinding
import com.tech.kojo.databinding.PersonalDialogItemBinding
import com.tech.kojo.databinding.UnPinLayoutBinding
import com.tech.kojo.databinding.VideoImagePickerDialogBoxBinding
import com.tech.kojo.ui.auth.AuthCommonVM
import com.tech.kojo.ui.auth.forgot.ForgotEmailFragmentDirections
import com.tech.kojo.utils.AppUtils
import com.tech.kojo.utils.BaseCustomDialog
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import com.tech.kojo.utils.showInfoToast
import com.github.dhaval2404.imagepicker.util.FileUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


@AndroidEntryPoint
class AddProfileFragment : BaseFragment<FragmentAddProfileBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    private var imageDialog: BaseCustomDialog<VideoImagePickerDialogBoxBinding>? = null
    private var photoFile2: File? = null
    private var photoURI: Uri? = null
    private var multipartPart: MultipartBody.Part? = null
    private var profileImage: String? = null
    private var skin: String? = null

    private var commonDialog: BaseCustomDialog<PersonalDialogItemBinding>? = null
    private lateinit var commonAdapter: SimpleRecyclerViewAdapter<String, UnPinLayoutBinding>
    override fun getLayoutResource(): Int {
        return R.layout.fragment_add_profile
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        binding.clCommon.tvHeader.text = "Add Profile Picture"
        // click
        initOnClick()
        // observer
        initObserver()
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
                        "uploadProfile" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: UploadProfileApiResponse? =
                                    BindingUtils.parseJson(jsonData)
                                if (model?.success == true) {
                                    profileImage = model.url
                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }

                        "profileApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: GetProfileResponse? = BindingUtils.parseJson(jsonData)
                                if (model != null) {
                                    val action =
                                        ForgotEmailFragmentDirections.navigateToSetupFragment()
                                    BindingUtils.navigateWithSlide(findNavController(), action)
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


    /**
     * Method to initialize click
     */
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.ivBack -> {
                    findNavController().popBackStack()
                }


                R.id.clUpload -> {
                    imageDialog()
                }

                R.id.btnContinue -> {
                    val data = HashMap<String, Any>()
                    if (profileImage?.isNotEmpty() == true) {
                        data["profilePicture"] = profileImage!!
                    }
                    if (skin?.isNotEmpty() == true) {
                        data["skin"] = skin!!
                    }
                    viewModel.profileApi(Constants.UPDATE_PROFILE, data)
                }

                R.id.clChoose -> {
                    commonDialog()
                }

            }
        }
    }

    /**** image pick dialog  handel ***/
    private fun imageDialog() {
        imageDialog = BaseCustomDialog(requireActivity(), R.layout.video_image_picker_dialog_box) {
            when (it.id) {
                R.id.tvCamera, R.id.imageCamera -> {
                    if (!BindingUtils.hasPermissions(
                            requireActivity(), BindingUtils.permissions
                        )
                    ) {
                        permissionResultLauncher1.launch(BindingUtils.permissions)
                    } else {
                        // camera
                        openCameraIntent()
                    }
                    imageDialog!!.dismiss()
                }

                R.id.imageGallery, R.id.tvGallery -> {
                    if (!BindingUtils.hasPermissions(
                            requireActivity(), BindingUtils.permissions
                        )
                    ) {
                        permissionResultLauncher.launch(BindingUtils.permissions)

                    } else {
                        galleryImagePicker()

                    }
                    imageDialog!!.dismiss()
                }

            }
        }
        imageDialog!!.create()
        imageDialog!!.show()

    }

    /**** Gallery permission  ***/
    private var allGranted = false
    private val permissionResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            for (it in permissions.entries) {
                it.key
                val isGranted = it.value
                allGranted = isGranted
            }
            when {
                allGranted -> {
                    galleryImagePicker()
                }

                else -> {
                    showInfoToast("Permission Denied")
                }
            }
        }

    /*** open gallery ***/
    private fun galleryImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
            action = Intent.ACTION_GET_CONTENT
        }
        resultLauncherGallery.launch(Intent.createChooser(intent, "Select Picture"))
    }


    /*** gallery launcher ***/
    private var resultLauncherGallery =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                val imageUri = data?.data
                imageUri?.let { uri ->
                    try {
                        binding.ivCircle1.visibility = View.GONE
                        binding.ivCircle.visibility = View.VISIBLE
                        binding.ivProfile.visibility = View.VISIBLE
                        Glide.with(requireActivity()).load(imageUri).into(binding.ivProfile)
                        multipartPart = convertMultipartPartGal(uri)


                        viewModel.uploadProfile(Constants.UPLOAD, multipartPart)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        showErrorToast("Image compression failed")
                    }

                }
            }
        }


    private val permissionResultLauncher1: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                openCameraIntent()
            } else {
                showInfoToast("Permission Denied")
            }
        }

    /**** open camera ***/
    private fun openCameraIntent() {
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (pictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            try {
                photoFile2 = AppUtils.createImageFile1(requireActivity())
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            if (photoFile2 != null) {
                photoURI = FileProvider.getUriForFile(
                    requireActivity(), "com.example.newbase_2025.fileProvider", photoFile2!!
                )
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                resultLauncherCamera.launch(pictureIntent)
            } else {
                Log.d("TAG", "openCameraIntent: ")
            }
        } else {
            Log.d("TAG", "openCameraIntent: ")
        }
    }

    /*** camera launcher ***/
    private val resultLauncherCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                if (photoFile2?.exists() == true) {
                    photoURI?.let { uri ->
                        lifecycleScope.launch {
                            try {
                                binding.ivCircle1.visibility = View.GONE
                                binding.ivCircle.visibility = View.VISIBLE
                                binding.ivProfile.visibility = View.VISIBLE
                                Glide.with(requireActivity()).load(uri).into(binding.ivProfile)

                                multipartPart = convertMultipartPart(requireActivity(), uri)

                                viewModel.uploadProfile(Constants.UPLOAD, multipartPart)

                            } catch (e: Exception) {
                                e.printStackTrace()
                                showErrorToast("Compression fail")
                            }
                        }
                    }
                }
            }
        }

    /*** convert image in multipart ***/
    private fun convertMultipartPart(context: Context, imageUri: Uri): MultipartBody.Part? {
        return try {
            // Open input stream from content resolver
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return null

            // Create a temp file to copy data
            val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()

            // Create multipart from the temp file
            val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun convertMultipartPartGal(imageUri: Uri): MultipartBody.Part {
        val file = FileUtil.getTempFile(requireActivity(), imageUri)
        val fileName =
            "${file!!.nameWithoutExtension}_${System.currentTimeMillis()}.${file.extension}"
        val newFile = File(file.parent, fileName)
        file.renameTo(newFile)
        return MultipartBody.Part.createFormData(
            "file", newFile.name, newFile.asRequestBody("image/*".toMediaTypeOrNull())
        )
    }


    /** common dialog  handel ***/
    private fun commonDialog() {
        commonDialog = BaseCustomDialog(requireActivity(), R.layout.personal_dialog_item) {

        }
        commonDialog!!.create()
        commonDialog!!.show()
        // adapter
        initCommonAdapter()
    }

    /** handle adapter **/
    private fun initCommonAdapter() {
        commonAdapter = SimpleRecyclerViewAdapter(R.layout.un_pin_layout, BR.bean) { v, m, pos ->
            when (v?.id) {
                R.id.consMainUnPin -> {
                    skin = m.toString()
                    binding.tvChoose.text = m.toString()
                    commonDialog?.dismiss()
                }
            }
        }
        commonAdapter.list = commonList()

        commonDialog?.binding?.rvCommon?.adapter = commonAdapter
    }

    private fun commonList(): ArrayList<String> {
        return arrayListOf(
            "Skin 1",
            "Skin 2",
            "Skin 3",
            "Skin 4",
            "Skin 5",
            "Skin 6",
        )
    }
}