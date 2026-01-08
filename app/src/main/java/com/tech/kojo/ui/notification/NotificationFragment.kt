package com.tech.kojo.ui.notification

import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.GetProfileResponse
import com.tech.kojo.databinding.FragmentNotificationBinding
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationFragment : BaseFragment<FragmentNotificationBinding>() {
    private val viewModel: NotificationVM by viewModels()

    override fun getLayoutResource(): Int {
        return R.layout.fragment_notification
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
            when(it?.id){
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
                        "notificationApi" -> {
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
        binding.clCommon.tvHeader.text = "Notification"

        val profile = sharedPrefManager.getProfileData()
        profile?.let {
            binding.switchNotification.isChecked = it.notificationAlert == true
            binding.switchReminder.isChecked = it.sesionReminderAlert == true
            binding.switchNewVideoAlerts.isChecked = it.newVideoAlert == true
        }

        isInitializing = false

        binding.switchNotification.setOnCheckedChangeListener { _, isChecked ->
            update("notificationAlert", isChecked)
        }

        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            update("sesionReminderAlert", isChecked)
        }

        binding.switchNewVideoAlerts.setOnCheckedChangeListener { _, isChecked ->
            update("newVideoAlert", isChecked)
        }
    }

    fun update(key: String, value: Boolean) {
        if (isInitializing) return
        val map = hashMapOf<String, Any>(key to value)
        viewModel.notificationApi(Constants.UPDATE_PROFILE, map)
    }



}