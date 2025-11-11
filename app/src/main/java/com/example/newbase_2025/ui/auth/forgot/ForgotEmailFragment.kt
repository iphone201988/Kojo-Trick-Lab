package com.example.newbase_2025.ui.auth.forgot

import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.utils.BindingUtils
import com.example.newbase_2025.databinding.FragmentForgotEmailBinding
import com.example.newbase_2025.ui.auth.AuthCommonVM
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ForgotEmailFragment : BaseFragment<FragmentForgotEmailBinding>() {
    private val viewModel: AuthCommonVM by viewModels()

    override fun getLayoutResource(): Int {
        return R.layout.fragment_forgot_email
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        binding.clCommon.tvHeader.text = "Forgot Password"
        // click
        initOnClick()

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

                R.id.btnVerify -> {
                    val action =
                        ForgotEmailFragmentDirections.navigateToVerifyFragment(otpType = "Forgot")
                    BindingUtils.navigateWithSlide(findNavController(), action)
                }
            }
        }
    }

}