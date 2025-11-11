package com.example.newbase_2025.ui.auth.signup

import android.text.InputType
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.utils.BindingUtils
import com.example.newbase_2025.databinding.FragmentSignupBinding
import com.example.newbase_2025.ui.auth.AuthCommonVM
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SignupFragment : BaseFragment<FragmentSignupBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    private var isCheck = false
    private var isAcceptCheckBox = false
    override fun getLayoutResource(): Int {
        return R.layout.fragment_signup
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        binding.type = false
        binding.acceptType = false
        binding.clCommon.tvHeader.text = "Create a account"

        // click
        initOnClick()
    }

    /**
     * Method to initialize click
     */
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.ivCheckBox -> {
                    if (isCheck) {
                        isCheck = false
                        binding.type = true
                    } else {
                        isCheck = true
                        binding.type = false
                    }
                }

                R.id.ivAcceptCheckBox -> {
                    if (isAcceptCheckBox) {
                        isAcceptCheckBox = false
                        binding.acceptType = true
                    } else {
                        isAcceptCheckBox = true
                        binding.acceptType = false
                    }
                }

                R.id.ivBack -> {
                    findNavController().popBackStack()
                }

                R.id.ivHidePassword -> {
                    if (binding.etPassword.text.toString().trim().isNotEmpty()) {
                        showOrHidePassword()
                    }

                }

                R.id.tvLogin -> {
                    val action = SignupFragmentDirections.navigateToLoginFragment()
                    BindingUtils.navigateWithSlide(findNavController(), action)
                }

                R.id.btnSignUp->{
                    val action = SignupFragmentDirections.navigateToVerifyFragment(otpType = "Signup")
                    BindingUtils.navigateWithSlide(findNavController(), action)
                }
            }
        }
    }


    /*** show or confirm hide password **/
    private fun showOrHidePassword() {
        // Save the current typeface
        val typeface = binding.etPassword.typeface
        if (binding.etPassword.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            binding.ivHidePassword.setImageResource(R.drawable.show_password)
            binding.etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            binding.ivHidePassword.setImageResource(R.drawable.hide_password)
            binding.etPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }


        // Reapply the saved typeface to maintain the font style
        binding.etPassword.typeface = typeface
        binding.etPassword.setSelection(binding.etPassword.length())
    }

}