package com.example.newbase_2025.ui.dashboard.stat_visibility

import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.data.api.Constants
import com.example.newbase_2025.data.model.GetProfileResponse
import com.example.newbase_2025.databinding.FragmentStatVisibilityBinding
import com.example.newbase_2025.utils.BindingUtils
import com.example.newbase_2025.utils.Status
import com.example.newbase_2025.utils.showErrorToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatVisibilityFragment : BaseFragment<FragmentStatVisibilityBinding>() {
    private val viewModel: StatVisibilityVM by viewModels()

    override fun getLayoutResource(): Int {
        return R.layout.fragment_stat_visibility
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // view
        initView()
        // click
        initOnClick()
        // observer
        initObserver()
    }

    /**
     * Method to initialize click
     */
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })
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
                        "stayVisibilityApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: GetProfileResponse? = BindingUtils.parseJson(jsonData)
                                if (model != null) {
                                    val profile = model.user
                                    sharedPrefManager.setProfileData(profile)
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
     * Method to initialize view
     */
    var isInitializing = true
    private fun initView() {
        binding.clCommon.tvHeader.text = "Stat Visibility"
        val profile = sharedPrefManager.getProfileData()
        profile?.let {
            binding.switchNotification.isChecked = it.statVisibility?.showTrickingLevel == true
            binding.switchReminder.isChecked = it.statVisibility?.showFavouriteTrick == true
            binding.switchNewVideoAlerts.isChecked = it.statVisibility?.showBestTrick == true

            binding.switchShowPBs.isChecked = it.statVisibility?.showPBs == true
            binding.switchShowTimeSubscribed.isChecked =
                it.statVisibility?.showTimeSubscribed == true
            binding.switchShowTimeTricking.isChecked = it.statVisibility?.showTimeTricking == true
            binding.switchShowMostTrick.isChecked = it.statVisibility?.showTimeSubscribed == true
        }

        isInitializing = false

        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            update("showTrickingLevel", isChecked)
        }

        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            update("showFavouriteTrick", isChecked)
        }

        binding.switchNewVideoAlerts.setOnCheckedChangeListener { _, isChecked ->
            update("showBestTrick", isChecked)
        }
        binding.switchShowPBs.setOnCheckedChangeListener { _, isChecked ->
            update("showPBs", isChecked)
        }
        binding.switchShowTimeSubscribed.setOnCheckedChangeListener { _, isChecked ->
            update("showTimeTricking", isChecked)
        }
        binding.switchShowTimeTricking.setOnCheckedChangeListener { _, isChecked ->
            update("showMostPracticedTrick", isChecked)
        }
        binding.switchShowMostTrick.setOnCheckedChangeListener { _, isChecked ->
            update("showTimeSubscribed", isChecked)
        }
    }

    fun update(key: String, value: Boolean) {
        if (isInitializing) return
        val map = hashMapOf<String, Any>(key to value)
        viewModel.stayVisibilityApi(Constants.UPDATE_PROFILE, map)
    }

}