package com.example.newbase_2025.ui.notification

import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.databinding.FragmentNotificationBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationFragment : BaseFragment<FragmentNotificationBinding>() {
    private val viewModel: NotificationVM by viewModels()
    override fun onCreateView(view: View) {
        initView()
        initOnClick()

    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer {
            when(it?.id){
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun initView() {
        binding.clCommon.tvHeader.text = "Notification"

    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_notification
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }
}