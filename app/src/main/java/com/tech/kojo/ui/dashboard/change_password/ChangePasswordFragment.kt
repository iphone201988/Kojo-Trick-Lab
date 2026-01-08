package com.tech.kojo.ui.dashboard.change_password

import android.text.InputType
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.CommonApiResponse
import com.tech.kojo.databinding.FragmentChangePasswordBinding
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import com.tech.kojo.utils.showInfoToast
import com.tech.kojo.utils.showSuccessToast
import dagger.hilt.android.AndroidEntryPoint
import kotlin.toString

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
        // click
        initOnClick()
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

                R.id.btnLogin -> {
                    if (validate()) {
                        val oldPassword = binding.etOldPassword.text.toString().trim()
                        val newPassword = binding.etNewPassword.text.toString().trim()
                        val data = HashMap<String, Any>()
                        data["oldPassword"] = oldPassword
                        data["newPassword"] = newPassword
                        viewModel.changePasswordApi(data, Constants.CHANGE_PASSWORD)

                    }

                }


                // show or hide password click
                R.id.ivHideOldPassword -> {
                    if (binding.etOldPassword.text.toString().trim().isNotEmpty()) {
                        showOrHidePassword()
                    }
                }

                R.id.ivHideNewPassword -> {
                    if (binding.etNewPassword.text.toString().trim().isNotEmpty()) {
                        showOrHideNewPassword()
                    }
                }

                // show or hide confirm password click
                R.id.ivHideConfirmPassword -> {
                    if (binding.etConfirmPassword.text.toString().trim().isNotEmpty()) {
                        showOrHideConfirmPassword()
                    }
                }



            }
        }
    }

    /** api response observer ***/
    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()
                }

                Status.SUCCESS -> {
                    when (it.message) {
                        "changePasswordApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: CommonApiResponse? = BindingUtils.parseJson(jsonData)
                                if (model?.success == true) {
                                    showSuccessToast(model.message.toString())
                                    requireActivity().finish()
                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
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

                else -> {

                }
            }
        }
    }



    /*** show or confirm hide old password **/
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

    /*** show or hide new password **/
    private fun showOrHideNewPassword() {
        // Save the current typeface
        val typeface = binding.etNewPassword.typeface
        if (binding.etNewPassword.inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            binding.ivHideNewPassword.setImageResource(R.drawable.show_password)
            binding.etNewPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            binding.ivHideNewPassword.setImageResource(R.drawable.hide_password)
            binding.etNewPassword.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        // Reapply the saved typeface to maintain the font style
        binding.etNewPassword.typeface = typeface
        binding.etNewPassword.setSelection(binding.etNewPassword.length())
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

    /*** add validation ***/
    private fun validate(): Boolean {
        val oldPassword = binding.etOldPassword.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        if (oldPassword.isEmpty()) {
            showInfoToast("Please enter old password")
            return false
        } else if (oldPassword.length < 6) {
            showInfoToast("Password must be at least 6 characters")
            return false
        } else if (!oldPassword.any { it.isUpperCase() }) {
            showInfoToast("Password must contain at least one uppercase letter")
            return false
        } else if (newPassword.isEmpty()) {
            showInfoToast("Please enter new password")
            return false
        } else if (newPassword.length < 6) {
            showInfoToast("Password must be at least 6 characters")
            return false
        } else if (!newPassword.any { it.isUpperCase() }) {
            showInfoToast("Password must contain at least one uppercase letter")
            return false
        } else if (confirmPassword.isEmpty()) {
            showInfoToast("Please enter confirm password")
            return false
        } else if (newPassword != confirmPassword) {
            showInfoToast("New Password and Confirm password do not match")
            return false
        }

        return true
    }



}