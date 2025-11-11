package com.example.newbase_2025.ui.auth.forgot

import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.databinding.FragmentPasswordChangeSuccessfullyBinding
import com.example.newbase_2025.ui.auth.AuthActivity
import com.example.newbase_2025.ui.auth.AuthCommonVM
import com.example.newbase_2025.ui.dashboard.DashBoardActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PasswordChangeSuccessfullyFragment :
    BaseFragment<FragmentPasswordChangeSuccessfullyBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    private val args: PasswordChangeSuccessfullyFragmentArgs by navArgs()
    private var successfulType: String? = null
    override fun getLayoutResource(): Int {
        return R.layout.fragment_password_change_successfully
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        initOnClick()
        successfulType = args.successfulType
        if (successfulType.equals("passwordChange")){
            binding.tvChange.text = getString(R.string.your_account_npassword_changed_nsuccessfully)
            binding.btnLogin.text = getString(R.string.login)
        }else{
            binding.tvChange.text = getString(R.string.your_account_was_successfully_created)
            binding.btnLogin.text = getString(R.string.done)
        }

    }

    /**
     * Method to initialize click
     */
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {

                R.id.btnLogin -> {
                    if (successfulType.equals("passwordChange")){
                        val intent = Intent(requireContext(), AuthActivity::class.java)
                        startActivity(intent)
                        requireActivity().finishAffinity()
                    }else{
                        val intent = Intent(requireContext(), DashBoardActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }

                }
            }
        }
    }

}