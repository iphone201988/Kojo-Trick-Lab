package com.example.newbase_2025.ui.auth.login

import android.content.Intent
import android.text.InputType
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.utils.BindingUtils
import com.example.newbase_2025.databinding.FragmentLoginBinding
import com.example.newbase_2025.ui.auth.AuthCommonVM
import com.example.newbase_2025.ui.dashboard.DashBoardActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    private var isCheck = false
    override fun getLayoutResource(): Int {
        return R.layout.fragment_login
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        binding.type = false

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

                R.id.ivBack -> {
                    requireActivity().finish()
                }

                R.id.ivHidePassword -> {
                    if (binding.etPassword.text.toString().trim().isNotEmpty()) {
                        showOrHidePassword()
                    }

                }

                R.id.btnLogin -> {
                    val intent = Intent(requireContext(), DashBoardActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }

                R.id.tvSignup -> {
                    val action = LoginFragmentDirections.navigateToSignUpFragment()
                    BindingUtils.navigateWithSlide(findNavController(), action)
                }

                R.id.tvForgot -> {
                    val action = LoginFragmentDirections.navigateToForgotEmailFragment()
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