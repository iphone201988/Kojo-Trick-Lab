package com.tech.kojo.ui.dashboard.notification

import android.view.View
import androidx.fragment.app.viewModels
import com.tech.kojo.BR
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.base.SimpleRecyclerViewAdapter
import com.tech.kojo.data.model.Notification
import com.tech.kojo.data.model.NotificationData
import com.tech.kojo.databinding.FragmentNotificationNewBinding
import com.tech.kojo.databinding.ItemLayoutNotificationBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class NotificationNewFragment : BaseFragment<FragmentNotificationNewBinding>() {

    private val viewModel: NotificationVm by viewModels()

    private lateinit var notificationAdapter: SimpleRecyclerViewAdapter<Notification, ItemLayoutNotificationBinding>
    private var notificationList = ArrayList<Notification>()


    override fun getLayoutResource(): Int {
        return R.layout.fragment_notification_new
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        binding.clCommon.tvHeader.text = "Notification"
        getNotificationList()
        initAdapter()
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
                    requireActivity().finish()
                }

            }
        }
    }

    private fun getNotificationList() {
        notificationList.add(
            Notification(
                "Today", listOf(
                    NotificationData(
                        "New videos added",
                        "See the new videos to learn new tricks",
                        "20min ago"
                    ),
                    NotificationData(
                        "Upcoming Session",
                        "15, Sep 2025 session upcoming",
                        "20min ago"
                    ),
                )
            )
        )
        notificationList.add(
            Notification(
                "Yesterday", listOf(
                    NotificationData(
                        "New videos added",
                        "See the new videos to learn new tricks",
                        "1day ago"
                    ),
                    NotificationData(
                        "Upcoming Session",
                        "15, Sep 2025 session upcoming",
                        "1day ago"
                    ),
                )
            )
        )
    }


    private fun initAdapter() {
        notificationAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_layout_notification, BR.bean) { v, m, pos ->

            }
        binding.rvNotification.adapter = notificationAdapter
        notificationAdapter.list = notificationList
        notificationAdapter.notifyDataSetChanged()

    }
}