package com.example.newbase_2025.ui.dashboard.library.user_profile

import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.data.api.Constants
import com.example.newbase_2025.data.model.GetOtherUserProfile
import com.example.newbase_2025.data.model.LoginApiResponse
import com.example.newbase_2025.databinding.FragmentUserProfileBinding
import com.example.newbase_2025.utils.BindingUtils
import com.example.newbase_2025.utils.Status
import com.example.newbase_2025.utils.showErrorToast
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