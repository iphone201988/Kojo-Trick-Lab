package com.example.newbase_2025.ui.auth.add_profile

import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.utils.BaseCustomDialog
import com.example.newbase_2025.base.utils.BindingUtils
import com.example.newbase_2025.databinding.FragmentAddProfileBinding
import com.example.newbase_2025.databinding.VideoImagePickerDialogBoxBinding
import com.example.newbase_2025.ui.auth.AuthCommonVM
import com.example.newbase_2025.ui.auth.forgot.ForgotEmailFragmentDirections
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddProfileFragment : BaseFragment<FragmentAddProfileBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    private var imageDialog: BaseCustomDialog<VideoImagePickerDialogBoxBinding>? = null
    override fun getLayoutResource(): Int {
        return R.layout.fragment_add_profile
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        binding.clCommon.tvHeader.text = "Add Profile Picture"
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

                R.id.btnContinue -> {
                    val action = ForgotEmailFragmentDirections.navigateToSetupFragment()
                    BindingUtils.navigateWithSlide(findNavController(), action)
                }
            }
        }
    }

}