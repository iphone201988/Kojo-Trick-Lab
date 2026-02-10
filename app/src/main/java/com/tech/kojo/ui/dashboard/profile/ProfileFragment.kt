package com.tech.kojo.ui.dashboard.profile

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.data.api.Constants
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
        viewModel.getProfileApi(Constants.GET_PROFILE)
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
                                    sharedPrefManager.setProfileData(profile)
                                    binding.bean = profile
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

}