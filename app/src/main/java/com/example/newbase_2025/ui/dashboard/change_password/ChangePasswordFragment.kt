package com.example.newbase_2025.ui.dashboard.change_password

import android.content.Intent
import android.text.InputType
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.databinding.FragmentChangePasswordBinding
import com.example.newbase_2025.utils.BindingUtils
import com.example.newbase_2025.databinding.FragmentLoginBinding
import com.example.newbase_2025.ui.auth.AuthCommonVM
import com.example.newbase_2025.ui.dashboard.DashBoardActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChangePasswordFragment : BaseFragment<FragmentChangePasswordBinding>() {
    private val viewModel: ChangePasswordVM by viewModels()
    override fun getLayoutResource(): Int {
        return R.layout.fragment_change_password
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        binding.clCommon.tvHeader.text = "Change Password"
        initOnClick()
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

                R.id.ivHidePassword -> {
                    if (binding.etOldPassword.text.toString().trim().isNotEmpty()) {
                        showOrHidePassword()
                    }

                }

                R.id.btnLogin -> {
                    val intent = Intent(requireContext(), DashBoardActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }


            }
        }
    }


    /*** show or confirm hide password **/
    private fun showOrHidePassword() {
        // Save the current typeface
        val typeface = binding.etOldPassword.typeface
        if (binding.etOldPassword.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            binding.ivHideOldPassword.setImageResource(R.drawable.show_password)
            binding.etOldPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            binding.ivHideOldPassword.setImageResource(R.drawable.hide_password)
            binding.etOldPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }


        // Reapply the saved typeface to maintain the font style
        binding.etOldPassword.typeface = typeface
        binding.etOldPassword.setSelection(binding.etOldPassword.length())
    }

}