package com.example.newbase_2025.ui.auth.forgot

import android.text.InputType
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.utils.BindingUtils
import com.example.newbase_2025.databinding.FragmentResetPasswordBinding
import com.example.newbase_2025.ui.auth.AuthCommonVM
import com.example.newbase_2025.ui.auth.login.LoginFragmentDirections
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ResetPasswordFragment : BaseFragment<FragmentResetPasswordBinding>() {
    private val viewModel: AuthCommonVM by viewModels()

    override fun getLayoutResource(): Int {
        return R.layout.fragment_reset_password
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        binding.clCommon.tvHeader.text = "Create Password"
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

                R.id.btnConfirm -> {
                    val action = ResetPasswordFragmentDirections.navigateToSuccessfullyFragment(successfulType = "passwordChange")
                    BindingUtils.navigateWithSlide(findNavController(), action)
                }

                R.id.ivHidePassword -> {
                    if (binding.etPassword.text.toString().trim().isNotEmpty()) {
                        showOrHidePassword()
                    }

                }

                R.id.ivHideConfirmPassword -> {
                    if (binding.etConfirmPassword.text.toString().trim().isNotEmpty()) {
                        showOrHideConfirmPassword()
                    }

                }
            }
        }
    }

    /*** show or  hide password **/
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

    /*** show or hide confirm password **/
    private fun showOrHideConfirmPassword() {
        // Save the current typeface
        val typeface = binding.etConfirmPassword.typeface
        if (binding.etConfirmPassword.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            binding.ivHideConfirmPassword.setImageResource(R.drawable.show_password)
            binding.etConfirmPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            binding.ivHideConfirmPassword.setImageResource(R.drawable.hide_password)
            binding.etConfirmPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }


        // Reapply the saved typeface to maintain the font style
        binding.etConfirmPassword.typeface = typeface
        binding.etConfirmPassword.setSelection(binding.etConfirmPassword.length())
    }
}