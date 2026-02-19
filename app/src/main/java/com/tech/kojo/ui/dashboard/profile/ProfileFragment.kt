package com.tech.kojo.ui.dashboard.profile

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.ChipData
import com.tech.kojo.data.model.GetProfileResponse
import com.tech.kojo.databinding.FragmentProfileBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.ui.dashboard.DashBoardActivity
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Resource
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>() {
    private val viewModel: ProfileVM by viewModels()

    override fun getLayoutResource(): Int {
        return R.layout.fragment_profile
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        initOnClick()
        // api call
//        viewModel.getProfileApi(Constants.GET_PROFILE)
        // observer
        initObserver()
    }

    /**
     * Method to initialize click
     */
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer {
            when (it?.id) {
                R.id.tvEditProfilePic -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "editDataProfile")
                    startActivity(intent)
                }

                R.id.tvEditProfile -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "editProfileInfo")
                    startActivity(intent)
                }
            }
        })

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
                        "getProfileApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: GetProfileResponse? = BindingUtils.parseJson(jsonData)
                                var profile = model?.user
                                if (profile != null) {
                                    sharedPrefManager.setLoginData(profile)
//                                    binding.bean = profile
                                    binding.ivCircle1.visibility = View.GONE
                                    binding.ivCircle.visibility = View.VISIBLE
                                    binding.ivProfile.visibility = View.VISIBLE

                                    val imageUrl = when {
                                        profile.profilePicture?.startsWith("http") == true -> profile.profilePicture
                                        profile.profilePicture != null -> Constants.BASE_URL_IMAGE + profile.profilePicture
                                        else -> null
                                    }
                                    imageUrl?.let { url ->
                                        Glide.with(requireContext()).load(url)
                                            .placeholder(R.drawable.progress_animation_small)
                                            .error(R.drawable.holder_dummy).into(binding.ivProfile)
                                    }

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

    private fun chipStatus(list1: List<ChipData?>?) {

        if (list1.isNullOrEmpty()) return

        binding.chipGroupStatus.removeAllViews()

        for (element1 in list1) {

            val chip1 = layoutInflater.inflate(
                R.layout.single_chip_item, binding.chipGroupStatus, false
            ) as Chip

            element1?.let { item ->

                chip1.text = item.title
                chip1.isCheckable = false
                chip1.setChipIconResource(item.icon ?: 0)
                chip1.isChipIconVisible = true

                chip1.setOnClickListener {

                    item.link?.let { link ->

                        var finalUrl = link

                        // If link does not start with http, fix it
                        if (!finalUrl.startsWith("http")) {
                            finalUrl = "https://$finalUrl"
                        }

                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
                            startActivity(intent)
                        } catch (e: Exception) {
                            showErrorToast("Invalid link")
                        }
                    }
                }
            }

            binding.chipGroupStatus.addView(chip1)
        }
    }

    override fun onResume() {
        super.onResume()
        val currentUser = sharedPrefManager.getLoginData()
        // ✅ Create chip list dynamically
        val list = mutableListOf<ChipData>()
        if (currentUser != null) {
            binding.bean = currentUser
            binding.ivCircle1.visibility = View.GONE
            binding.ivCircle.visibility = View.VISIBLE
            binding.ivProfile.visibility = View.VISIBLE

            val imageUrl = when {
                currentUser.profilePicture?.startsWith("http") == true -> currentUser.profilePicture
                currentUser.profilePicture != null -> Constants.BASE_URL_IMAGE + currentUser.profilePicture
                else -> null
            }
            imageUrl?.let { url ->
                Glide.with(requireContext()).load(url)
                    .placeholder(R.drawable.progress_animation_small).error(R.drawable.holder_dummy)
                    .into(binding.ivProfile)
            }

            if (!currentUser.instagramLink.isNullOrEmpty()) {
                list.add(
                    ChipData(
                        title = "Instagram",
                        icon = R.drawable.ic_chip_insta,
                        link = currentUser.instagramLink
                    )
                )
            }

            if (!currentUser.youtubeLink.isNullOrEmpty()) {
                list.add(
                    ChipData(
                        title = "YouTube",
                        icon = R.drawable.ic_chip_youtube,
                        link = currentUser.youtubeLink
                    )
                )
            }
            if (!currentUser.tiktockLink.isNullOrEmpty()) {
                list.add(
                    ChipData(
                        title = "TikTok",
                        icon = R.drawable.ic_chip_tiktok,
                        link = currentUser.tiktockLink
                    )
                )
            }
            chipStatus(list)

            DashBoardActivity.changeImage.postValue(
                Resource.success(
                    "changeImage", true
                )
            )
        }
    }

}