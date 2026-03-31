package com.tech.kojo.ui.dashboard.profile.edit

import android.Manifest
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
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.util.FileUtil
import com.google.android.material.imageview.ShapeableImageView
import com.tech.kojo.BR
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.base.SimpleRecyclerViewAdapter
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.GetProfileResponse
import com.tech.kojo.data.model.UploadProfileApiResponse
import com.tech.kojo.databinding.FragmentEditProfileBinding
import com.tech.kojo.databinding.PersonalDialogItemBinding
import com.tech.kojo.databinding.UnPinLayoutBinding
import com.tech.kojo.databinding.VideoImagePickerDialogBoxBinding
import com.tech.kojo.ui.dashboard.DashBoardActivity
import com.tech.kojo.utils.AppUtils
import com.tech.kojo.utils.BaseCustomDialog
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.BindingUtils.compressImage
import com.tech.kojo.utils.BindingUtils.hasCameraPermission
import com.tech.kojo.utils.BindingUtils.setBgSkin
import com.tech.kojo.utils.Resource
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import com.tech.kojo.utils.showInfoToast
import com.tech.kojo.utils.showSuccessToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@AndroidEntryPoint
class EditProfileFragment : BaseFragment<FragmentEditProfileBinding>() {
    private val viewModel: EditProfileFragmentVM by viewModels()
    private var imageDialog: BaseCustomDialog<VideoImagePickerDialogBoxBinding>? = null
    private var photoURI: Uri? = null
    private var multipartPart: MultipartBody.Part? = null
    private var photoFile: File? = null
    private var profileImage: String? = null
    private var skin: String? = null

    private var commonDialog: BaseCustomDialog<PersonalDialogItemBinding>? = null
    private lateinit var commonAdapter: SimpleRecyclerViewAdapter<String, UnPinLayoutBinding>

    override fun getLayoutResource(): Int {
        return R.layout.fragment_edit_profile
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        binding.clCommon.tvHeader.text = "Edit Profile Picture"
        // click
        initOnClick()

        // observer
        initObserver()
        // data set
        val data = sharedPrefManager.getLoginData()
        if (data != null) {
            if (!data.skin.isNullOrEmpty()){
                binding.tvChoose.text = data.skin
                skin = data.skin
            }
            binding.ivCircle1.visibility = View.GONE
            binding.ivCircle.visibility = View.VISIBLE
            binding.ivProfile.visibility = View.VISIBLE
            setBgSkin(binding.ivBgProfile,data.skin)
            Glide.with(requireContext()).load(Constants.BASE_URL_IMAGE + data.profilePicture)
                .placeholder(R.drawable.holder_dummy).error(R.drawable.holder_dummy)
                .into(binding.ivProfile)
        }

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

                R.id.btnUpdate -> {
                    val data = HashMap<String, Any>()
                    if (profileImage?.isNotEmpty() == true) {
                        data["profilePicture"] = profileImage!!
                    }
                    if (skin?.isNotEmpty() == true) {
                        data["skin"] = skin!!
                    }

                    viewModel.editProfileApi(Constants.UPDATE_PROFILE, data)
                }

                R.id.clUpload,R.id.ivCircle1,R.id.ivCircle,R.id.ivProfile -> {
                    imageDialog()
                }

                R.id.clChoose -> {
                    commonDialog()
                }
            }
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

                        "editProfileApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: GetProfileResponse? = BindingUtils.parseJson(jsonData)
                                if (model != null) {
                                    val profile = model.user
                                    sharedPrefManager.setLoginData(profile)
                                    profileImage = null
                                    skin = null
                                    showSuccessToast(model.message.toString())
                                    DashBoardActivity.changeImage.postValue(
                                        Resource.success(
                                            "changeImage", true
                                        )
                                    )
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

    /**** image pick dialog  handel ***/
    private fun imageDialog() {

        imageDialog = BaseCustomDialog(requireActivity(), R.layout.video_image_picker_dialog_box) {
            when (it.id) {
                R.id.tvCamera, R.id.imageCamera -> {
                    if (!hasCameraPermission(requireContext())) {
                        permissionResultLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                    } else {
                        openCamera()
                    }
                    imageDialog?.dismiss()
                }

                R.id.imageGallery, R.id.tvGallery -> {
                    openGallery()
                    imageDialog?.dismiss()
                }
            }
        }

        imageDialog?.create()
        imageDialog?.show()
    }


    private val permissionResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                openCamera()
            } else {
                showInfoToast("Permission Denied")
            }
        }


    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    /**
     * Method to handle selected image
     */


    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                bindImage(it, binding.ivProfile) { multipartPart = it }
                val request = HashMap<String, RequestBody>()
                viewModel.uploadProfile(Constants.UPLOAD, multipartPart)
            }
        }


    /**
     * Method to open camera
     */
    private fun openCamera() {
        photoFile = BindingUtils.createImageFile(requireContext())
        photoURI = FileProvider.getUriForFile(
            requireContext(), "${requireContext().packageName}.fileProvider", photoFile!!
        )
        cameraLauncher.launch(photoURI!!)
    }


    /**
     * Method to handle selected image
     */
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoURI != null) {
                bindImage(photoURI!!, binding.ivProfile) { multipartPart = it }
                val request = HashMap<String, RequestBody>()
                viewModel.uploadProfile(Constants.UPLOAD, multipartPart)
            }
        }

    private fun bindImage(
        uri: Uri, imageView: ShapeableImageView, onMultipartReady: (MultipartBody.Part?) -> Unit
    ) {
        binding.ivCircle1.visibility = View.GONE
        binding.ivCircle.visibility = View.VISIBLE
        binding.ivProfile.visibility = View.VISIBLE
        val compressedUri = compressImage(uri,requireContext() )
        Glide.with(requireActivity()).load(uri).into(binding.ivProfile)
        onMultipartReady(uriToMultipart(compressedUri!!))
    }

    private fun uriToMultipart(uri: Uri): MultipartBody.Part? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val file = File(requireContext().cacheDir, "img_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            MultipartBody.Part.createFormData(
                "file", file.name, file.asRequestBody("image/*".toMediaTypeOrNull())
            )
        } catch (e: Exception) {
            Log.d("error", "uriToMultipart:$e: ")
            null
        }
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
                    setBgSkin(binding.ivBgProfile,m.toString())
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