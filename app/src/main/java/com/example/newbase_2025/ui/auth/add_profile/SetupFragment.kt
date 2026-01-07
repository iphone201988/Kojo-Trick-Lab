package com.example.newbase_2025.ui.auth.add_profile

import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.api.Constants
import com.example.newbase_2025.data.model.GetProfileResponse
import com.example.newbase_2025.utils.BindingUtils
import com.example.newbase_2025.databinding.FragmentSetupBinding
import com.example.newbase_2025.databinding.PersonalDialogItemBinding
import com.example.newbase_2025.databinding.UnPinLayoutBinding
import com.example.newbase_2025.ui.auth.AuthCommonVM
import com.example.newbase_2025.ui.auth.forgot.ForgotEmailFragmentDirections
import com.example.newbase_2025.utils.BaseCustomDialog
import com.example.newbase_2025.utils.Status
import com.example.newbase_2025.utils.showErrorToast
import com.example.newbase_2025.utils.showSuccessToast
import com.yesterselga.countrypicker.CountryPicker
import com.yesterselga.countrypicker.CountryPickerListener
import com.yesterselga.countrypicker.Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SetupFragment : BaseFragment<FragmentSetupBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    private var commonDialog: BaseCustomDialog<PersonalDialogItemBinding>? = null
    private lateinit var commonAdapter: SimpleRecyclerViewAdapter<String, UnPinLayoutBinding>
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

                R.id.btnConfirm -> {
                    val name = binding.etTricking.text.toString().trim()
                    val country = binding.etCountry.text.toString().trim()
                    val time = binding.etTime.text.toString().trim()
                    val signature = binding.etSignature.text.toString().trim()
                    val dream = binding.etDream.text.toString().trim()
                    val instagram = binding.etInstagram.text.toString().trim()
                    val tiktok = binding.etTikTok.text.toString().trim()
                    val youtube = binding.etYouTube.text.toString().trim()
                    val data = HashMap<String, Any>()
                    if (name.isNotEmpty()) {
                        data["trickingNickname"] = name
                    }
                    if (country.isNotEmpty()) {
                        data["country"] = country
                    }
                    if (time.isNotEmpty()) {
                        data["timeTricking"] = time
                    }
                    if (signature.isNotEmpty()) {
                        data["signatureTrick"] = signature
                    }
                    if (dream.isNotEmpty()) {
                        data["dreamTrick"] = dream
                    }
                    if (instagram.isNotEmpty()) {
                        data["instagramLink"] = instagram
                    }
                    if (tiktok.isNotEmpty()) {
                        data["tiktockLink"] = tiktok
                    }
                    if (youtube.isNotEmpty()) {
                        data["youtubeLink"] = youtube
                    }

                    viewModel.setupProfileApi(data, Constants.UPDATE_PROFILE)

                }

                R.id.etCountry -> {
                    val picker = CountryPicker.newInstance(
                        "Select Country", Theme.DARK
                    ) // dialog title and theme
                    picker.setListener(object : CountryPickerListener {
                        override fun onSelectCountry(
                            name: String?, code: String?, dialCode: String?, flagDrawableResID: Int
                        ) {
                            // Implement your code here
                            binding.etCountry.setText(name)
                            picker.dismiss()
                        }
                    })
                    picker.show(requireActivity().supportFragmentManager, "COUNTRY_PICKER")
                }

                R.id.etTime -> {
                    commonDialog()
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
                        "setupProfileApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: GetProfileResponse? = BindingUtils.parseJson(jsonData)
                                if (model != null) {
                                    val profile = model.user
                                    sharedPrefManager.setProfileData(profile)
                                    val action = ForgotEmailFragmentDirections.navigateToSuccessfullyFragment(successfulType = "signup")
                                    BindingUtils.navigateWithSlide(findNavController(), action)
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

    /** common dialog  handel ***/
    private fun commonDialog() {
        commonDialog = BaseCustomDialog(requireActivity(), R.layout.personal_dialog_item) {

        }
        commonDialog!!.create()
        commonDialog!!.show()
        // adapter
        initCommonAdapter()
    }

    /** handle adapter **/
    private fun initCommonAdapter() {
        commonAdapter = SimpleRecyclerViewAdapter(R.layout.un_pin_layout, BR.bean) { v, m, pos ->
            when (v?.id) {
                R.id.consMainUnPin -> {
                    binding.etTime.setText(m.toString())
                    commonDialog?.dismiss()
                }
            }
        }
        commonAdapter.list = commonList()

        commonDialog?.binding?.rvCommon?.adapter = commonAdapter
    }

    private fun commonList(): ArrayList<String> {
        return arrayListOf(
            "< 1 year",
            "1-3 years",
            "3-5 years",
            "5+ years",
        )
    }

}