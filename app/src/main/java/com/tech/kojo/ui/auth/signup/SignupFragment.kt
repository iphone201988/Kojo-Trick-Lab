package com.tech.kojo.ui.auth.signup

import android.content.Intent
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
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
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignupFragment : BaseFragment<FragmentSignupBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    private var token = "1234567890"
    private var agePermission = false
    private lateinit var credentialManager: CredentialManager
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
        credentialManager = CredentialManager.create(requireContext())
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
                    googleLogin()
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
                                        otpType = "Signup", userEmail = email
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
                                    loginData.token.let {
                                        sharedPrefManager.setToken(it.toString())
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

    /**
     * Create a Google Sign-In request.
     */
    private fun getGoogleRequest(): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder().setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id)).setAutoSelectEnabled(true)
            .build()

        return GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
    }

    /**
     * Perform a Google Sign-In request using the provided request.
     */
    private fun googleLogin() {
        lifecycleScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = requireContext(), request = getGoogleRequest()
                )

                handleGoogleResult(result)

            } catch (e: Exception) {
                Log.e("GoogleLogin", "Error: ${e.message}", e)
                showErrorToast("Google Sign-In failed")
            }
        }
    }

    /**
     * Handle the result of the Google Sign-In request.
     */
    private fun handleGoogleResult(result: GetCredentialResponse) {
        val credential = result.credential

        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)


            val idToken = googleCredential.idToken

            val apiData = hashMapOf<String, Any>(
                "socialId" to getGoogleUid(idToken),
                "email" to getGoogleEmail(idToken),
                "name" to googleCredential.displayName.orEmpty(),
                "avatar" to googleCredential.profilePictureUri?.toString().orEmpty(),
                "provider" to "2",
                "deviceType" to "2",
                "deviceToken" to token
            )

            viewModel.socialLogin(Constants.SOCIAL_LOGIN, apiData)
        }
    }

    /**
     * Extract the email from the Google ID token.
     */
    private fun getGoogleEmail(idToken: String): String {
        val payload = idToken.split(".")[1]
        val decoded = String(
            android.util.Base64.decode(payload, android.util.Base64.URL_SAFE)
        )
        return org.json.JSONObject(decoded).optString("email", "")
    }

    /**
     * Extract the UID from the Google ID token.
     */
    private fun getGoogleUid(idToken: String): String {
        val payload = idToken.split(".")[1]
        val decoded = String(
            android.util.Base64.decode(payload, android.util.Base64.URL_SAFE)
        )
        return org.json.JSONObject(decoded).getString("sub")
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