package com.example.newbase_2025.ui.auth.add_profile

import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.utils.BindingUtils
import com.example.newbase_2025.databinding.FragmentSetupBinding
import com.example.newbase_2025.ui.auth.AuthCommonVM
import com.example.newbase_2025.ui.auth.forgot.ForgotEmailFragmentDirections
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SetupFragment : BaseFragment<FragmentSetupBinding>() {
    private val viewModel: AuthCommonVM by viewModels()

    override fun getLayoutResource(): Int {
        return R.layout.fragment_setup
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        binding.clCommon.tvHeader.text = "Set Up Your Profile"
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
                    val action = ForgotEmailFragmentDirections.navigateToSuccessfullyFragment(successfulType = "signup")
                    BindingUtils.navigateWithSlide(findNavController(), action)
                }


            }
        }
    }

}