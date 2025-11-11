package com.example.newbase_2025.ui.auth.verify

import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.utils.BindingUtils
import com.example.newbase_2025.databinding.FragmentVerifyBinding
import com.example.newbase_2025.ui.auth.AuthCommonVM
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue


@AndroidEntryPoint
class VerifyFragment : BaseFragment<FragmentVerifyBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    private lateinit var otpETs: Array<AppCompatEditText?>
    private val args: VerifyFragmentArgs by navArgs()
    private lateinit var otpTimer: CountDownTimer
    private var isOtpComplete = false
    private var otpType : String?=null
    override fun getLayoutResource(): Int {
        return R.layout.fragment_verify
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        binding.clCommon.tvHeader.text = "Verify"
         otpType = args.otpType
        // click
        initOnClick()

        // view
        initView()
        // start timer
        startOtpTimer()
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
                    if (otpType.equals("Forgot")){
                        val action = VerifyFragmentDirections.navigateToResetPasswordFragment()
                        BindingUtils.navigateWithSlide(findNavController(), action)
                    }else{
                        val action = VerifyFragmentDirections.navigateToAddProfileFragment()
                        BindingUtils.navigateWithSlide(findNavController(), action)
                    }

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
    /** start timer ***/
    private fun startOtpTimer() {
        val totalTime = 1 * 60 * 1000L

        otpTimer = object : CountDownTimer(totalTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.tvResendCode.isEnabled = false
                binding.tvResendCode.isFocusable = false
                binding.tvResendCode.isClickable = false
                val minutes = millisUntilFinished / 1000 / 60
                val seconds = millisUntilFinished / 1000 % 60
                binding.tvResendCode.text = "Resend Code in " + String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                binding.tvResendCode.text = "Resend Code in 00:00"
                binding.tvResendCode.isEnabled = true
                binding.tvResendCode.isFocusable = true
                binding.tvResendCode.isClickable = true
                otpTimer.cancel()
                // Handle OTP expiration if needed...
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