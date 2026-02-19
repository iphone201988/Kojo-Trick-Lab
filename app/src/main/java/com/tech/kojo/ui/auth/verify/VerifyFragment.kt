package com.tech.kojo.ui.auth.verify

import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.CommonApiResponse
import com.tech.kojo.data.model.LoginApiResponse
import com.tech.kojo.databinding.FragmentVerifyBinding
import com.tech.kojo.ui.auth.AuthCommonVM
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import com.tech.kojo.utils.showInfoToast
import com.tech.kojo.utils.showSuccessToast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class VerifyFragment : BaseFragment<FragmentVerifyBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    private lateinit var otpETs: Array<AppCompatEditText?>
    private val args: VerifyFragmentArgs by navArgs()
    private lateinit var otpTimer: CountDownTimer
    private var isOtpComplete = false
    private var otpType: String? = null
    private var userEmail: String? = null
    override fun getLayoutResource(): Int {
        return R.layout.fragment_verify
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        binding.clCommon.tvHeader.text = "Verify"
        otpType = args.otpType
        userEmail = args.userEmail
        // click
        initOnClick()
        // view
        initView()
        // start timer
        startOtpTimer()

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
                    findNavController().popBackStack()
                }

                R.id.btnVerify -> {
                    if (validate()) {
                        verifyAccountApi()
                    }

                }

                R.id.tvResendCode -> {
                    // get opt api call
                    if (!userEmail.isNullOrEmpty()) {
                        val data = HashMap<String, Any>()
                        if (!userEmail.isNullOrEmpty()) {
                            data["email"] = userEmail.toString()
                        }
                        if (otpType.equals("Forgot")) {
                            data["type"] = 2
                        } else {
                            data["type"] = 1
                        }
                        viewModel.resendOtpApi(Constants.RESEND_OTP, data)
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
                        "codeVerificationApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: LoginApiResponse? = BindingUtils.parseJson(jsonData)
                                val loginData = model?.user
                                if (loginData != null) {
                                    showSuccessToast(model.message.toString())
                                    loginData.token.let {
                                        sharedPrefManager.setToken(it.toString())
                                    }
                                    if (otpType.equals("Forgot")) {
                                        val action =
                                            VerifyFragmentDirections.navigateToResetPasswordFragment(userEmail = userEmail.toString())
                                        BindingUtils.navigateWithSlide(findNavController(), action)
                                    } else {
                                        val action = VerifyFragmentDirections.navigateToAddProfileFragment()
                                        BindingUtils.navigateWithSlide(findNavController(), action)
                                    }
                                } else {
                                    showErrorToast("Something went wrong")
                                }
                            }.onFailure { e ->
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }

                        "resendOtpApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: CommonApiResponse? = BindingUtils.parseJson(jsonData)
                                if (model?.success == true) {
                                    showSuccessToast(model.message.toString())
                                    binding.edtOtp1.setText("")
                                    binding.edtOtp2.setText("")
                                    binding.edtOtp3.setText("")
                                    binding.edtOtp4.setText("")
                                    startOtpTimer()
                                }

                            }.onFailure { e ->
                                Log.e("error", "verifyAccount: $e")
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

    /*** view ***/
    private fun initView() {
        otpETs = arrayOf(
            binding.edtOtp1,
            binding.edtOtp2,
            binding.edtOtp3,
            binding.edtOtp4,
        )
        otpETs.forEachIndexed { index, editText ->
            editText?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int,
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    if (!s.isNullOrEmpty() && index != otpETs.size - 1) {
                        otpETs[index + 1]?.requestFocus()
                    }

                    // Check if all OTP fields are filled
                    isOtpComplete = otpETs.all { it!!.text?.isNotEmpty() == true }

                }
            })

            editText?.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (editText.text?.isEmpty() == true && index != 0) {
                        otpETs[index - 1]?.apply {
                            text?.clear()
                            requestFocus()
                        }
                    }
                }
                // Check if all OTP fields are filled
                isOtpComplete = otpETs.all { it!!.text?.isNotEmpty() == true }

                false
            }
        }
    }

    /*** add validation ***/
    private fun validate(): Boolean {
        val first = binding.edtOtp1.text.toString().trim()
        val second = binding.edtOtp2.text.toString().trim()
        val third = binding.edtOtp3.text.toString().trim()
        val four = binding.edtOtp4.text.toString().trim()

        if (first.isEmpty()) {
            showInfoToast("Please enter valid otp")
            return false
        } else if (second.isEmpty()) {
            showInfoToast("Please enter valid otp")
            return false
        } else if (third.isEmpty()) {
            showInfoToast("Please enter valid otp")
            return false
        } else if (four.isEmpty()) {
            showInfoToast("Please enter valid otp")
            return false
        }
        return true
    }


    /** verifyAccount api call **/
    private fun verifyAccountApi() {
        try {
            val otpData =
                "${binding.edtOtp1.text}" + "${binding.edtOtp2.text}" + "${binding.edtOtp3.text}" + "${binding.edtOtp4.text}"
            val data = HashMap<String, Any>()
            if (otpData.isNotEmpty()) {
                data["email"] = userEmail.toString()
                data["otp"] = otpData.toString()
                if (otpType.equals("Forgot")) {
                    data["type"] = 2
                } else {
                    data["type"] = 1
                }
                viewModel.codeVerificationApi(Constants.VERIFY_OTP, data)
            }

        } catch (e: Exception) {
            Log.e("error", "verifyAccount: $e")
        }
    }

    /** start timer ***/
    private fun startOtpTimer() {
        val totalTime = 60 * 1000L

        otpTimer = object : CountDownTimer(totalTime, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                binding.tvResendCode.isEnabled = false
                binding.tvResendCode.isFocusable = false
                binding.tvResendCode.isClickable = false
                val seconds = millisUntilFinished / 1000

                binding.tvResendCode.text = "Resend Code in ${seconds}s"

                binding.tvResendCode.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.blue)
                )
            }

            override fun onFinish() {
                binding.tvResendCode.text = "Resend OTP"
                binding.tvResendCode.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.colorPrimary)
                )
                binding.tvResendCode.isEnabled = true
                binding.tvResendCode.isFocusable = true
                binding.tvResendCode.isClickable = true
                otpTimer.cancel()
            }
        }

        otpTimer.start()
    }


    override fun onDestroy() {
        super.onDestroy()
        otpTimer.cancel()
    }

    override fun onPause() {
        super.onPause()
        otpTimer.cancel()
    }

}