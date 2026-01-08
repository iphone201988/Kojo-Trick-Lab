package com.tech.kojo.ui.dashboard.library.user_profile

import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.GetOtherUserProfile
import com.tech.kojo.databinding.FragmentUserProfileBinding
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserProfileFragment : BaseFragment<FragmentUserProfileBinding>() {
    private val viewModel: UserProfileFragmentVM by viewModels()
    private val args: UserProfileFragmentArgs by navArgs()
    override fun getLayoutResource(): Int {

        return R.layout.fragment_user_profile
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        initOnClick()
        // api call
        if (args.userId.isNotEmpty()) {
            viewModel.getUserProfile(Constants.AUTH_GET_USER+"/${args.userId}")
        }
        // observer
        initObserver()
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
            }
        }
    }

    /**
     * Method to initialize observer
     */
    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.SUCCESS -> {
                    when (it.message) {
                        "getUserProfile" -> {
                            runCatching {
                                val model =
                                    BindingUtils.parseJson<GetOtherUserProfile>(it.data.toString())
                                if (model?.success == true&& model.data!=null) {
                                    binding.bean = model.data
                                    binding.ivCircle1.visibility = View.GONE
                                    binding.ivCircle.visibility = View.VISIBLE
                                    binding.ivProfile.visibility = View.VISIBLE
                                } else {
                                    showErrorToast("Something went wrong")
                                }
                            }.onFailure { e ->
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
                Status.LOADING -> showLoading()
                else -> {

                }
            }
        }
    }


}