package com.tech.kojo.ui.auth.signup

import android.app.Activity
import android.content.Intent
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.LoginApiResponse
import com.tech.kojo.databinding.FragmentSignupBinding
import com.tech.kojo.ui.auth.AuthCommonVM
import com.tech.kojo.ui.dashboard.DashBoardActivity
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import com.tech.kojo.utils.showInfoToast
import com.tech.kojo.utils.showSuccessToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignupFragment : BaseFragment<FragmentSignupBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    private var token = "1234567890"
    private var agePermission = false
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private var termAndCondition = false
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
        // observer
        initObserver()

        // get token firebase
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                return@addOnCompleteListener
            }
            token = it.result
        }
    }

    /**
     * Method to initialize click
     */
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.ivCheckBox -> {
                    if (agePermission) {
                        agePermission = false
                        binding.type = false
                    } else {
                        agePermission = true
                        binding.type = true
                    }
                }

                R.id.ivAcceptCheckBox -> {
                    if (termAndCondition) {
                        termAndCondition = false
                        binding.acceptType = false
                    } else {
                        termAndCondition = true
                        binding.acceptType = true
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

                R.id.btnSignUp -> {
                    val name = binding.etName.text.toString().trim()
                    val email = binding.etEmail.text.toString().trim()
                    val password = binding.etPassword.text.toString().trim()
                    if (validate()) {
                        val data = HashMap<String, Any>()
                        data["name"] = name
                        data["email"] = email
                        data["password"] = password
                        data["deviceToken"] = token
                        data["deviceType"] = "2" // android

                        viewModel.createAccount(Constants.SIGNUP, data)
                    }

                }

                R.id.clGoogle -> {
                    mGoogleSignInClient.signOut()
                    signIn()
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
                        "createAccount" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: LoginApiResponse? = BindingUtils.parseJson(jsonData)
                                val loginData = model?.user
                                if (loginData != null) {
                                    showSuccessToast(model.message.toString())
                                    loginData.let {
                                        sharedPrefManager.setLoginData(it)
                                    }
                                    val email = binding.etEmail.text.toString().trim()
                                    val action = SignupFragmentDirections.navigateToVerifyFragment(
                                        otpType = "Signup",
                                        userEmail = email
                                    )
                                    BindingUtils.navigateWithSlide(findNavController(), action)

                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }


                        "socialLogin" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: LoginApiResponse? = BindingUtils.parseJson(jsonData)
                                val loginData = model?.user
                                if (loginData != null) {
                                    showSuccessToast(model.message.toString())
                                    loginData.let {
                                        sharedPrefManager.setLoginData(it)
                                    }
                                    val intent =
                                        Intent(requireContext(), DashBoardActivity::class.java)
                                    startActivity(intent)
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


    /** google sign in **/
    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }


    /** google launcher **/
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!


                    // Prepare data for API
                    val apiData = hashMapOf<String, Any>(
                        "socialId" to account.id.toString(),
                        "name" to account.givenName.toString(),
                        "surname" to account.familyName.toString(),
                        "email" to account.email.toString(),
                        "avatar" to account.photoUrl.toString(),
                        "provider" to "2",
                        "deviceType" to "2",
                        "deviceToken" to token,
                    )

                    viewModel.socialLogin(Constants.SOCIAL_LOGIN, apiData)

                } catch (e: ApiException) {
                    showErrorToast("Google sign-in failed $e")
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

    /*** add validation ***/
    private fun validate(): Boolean {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (name.isEmpty()) {
            showInfoToast("Please enter  name")
            return false
        } else if (email.isEmpty()) {
            showInfoToast("Please enter email")
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showInfoToast("Please enter a valid email")
            return false
        } else if (password.isEmpty()) {
            showInfoToast("Please enter password")
            return false
        } else if (password.length < 6) {
            showInfoToast("Password must be at least 6 characters")
            return false
        } else if (!password.any { it.isUpperCase() }) {
            showInfoToast("Password must contain at least one uppercase letter")
            return false
        } else if (agePermission == false) {
            showInfoToast("Please accept age permission")
            return false
        } else if (termAndCondition == false) {
            showInfoToast("Please accept term and condition")
            return false
        }
        return true
    }

}