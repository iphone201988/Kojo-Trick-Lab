package com.tech.kojo.ui.dashboard.notification

import android.os.Handler
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tech.kojo.BR
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.base.SimpleRecyclerViewAdapter
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.GetNotificationData
import com.tech.kojo.data.model.GetProfileResponse
import com.tech.kojo.data.model.Notification
import com.tech.kojo.data.model.NotificationData
import com.tech.kojo.databinding.FragmentNotificationNewBinding
import com.tech.kojo.databinding.ItemLayoutNotificationBinding
import com.tech.kojo.ui.dashboard.community.adapter.FeedItem
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone


@AndroidEntryPoint
class NotificationNewFragment : BaseFragment<FragmentNotificationNewBinding>(){

    private val viewModel: NotificationVm by viewModels()

    private lateinit var notificationAdapter: SimpleRecyclerViewAdapter<Notification, ItemLayoutNotificationBinding>
    private var notificationList = ArrayList<Notification>()


    private var isLoading = false
    private var isLastPage = false

    private var currentPage = 1
    private var isProgress = false


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
        sharedPrefManager.setNotificationCount(0)
        // pagination
        pagination()

        // refresh
        binding.ssPullRefresh.setColorSchemeResources(
            ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        )
        binding.ssPullRefresh.setOnRefreshListener {
            Handler().postDelayed({
                binding.ssPullRefresh.isRefreshing = false
                isProgress = true
                getNotificationList()
            }, 2000)
        }

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

            }
        }
    }

    /**
     * handle observer
     */

    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.LOADING -> {
                    if (!isProgress) {
                        showLoading()
                    }
                }

                Status.SUCCESS -> {
                    when (it.message) {
                        "getNotificationApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: GetNotificationData? = BindingUtils.parseJson(jsonData)

                                if (model != null) {

                                    val notifications = model.data.orEmpty()
                                    val feedItems = groupNotificationsByDate(notifications)

                                    isLoading = false
                                    isLastPage = false
                                    isProgress = true

                                    if (currentPage == 1) {
                                        notificationAdapter.setList(feedItems)
                                    } else {
                                        notificationAdapter.addToList(feedItems)
                                    }

                                    isLastPage = currentPage == model.pagination?.totalPages

                                    binding.clEmpty.visibility =
                                        if (notificationAdapter.list.isNotEmpty()) View.GONE else View.VISIBLE
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

    private fun groupNotificationsByDate(
        notifications: List<NotificationData>
    ): List<Notification> {

        val inputFormat = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.getDefault()
        ).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }

        val grouped = notifications
            .filterNotNull()
            .groupBy { notification ->

                val date = try {
                    notification.createdAt?.let { inputFormat.parse(it) }
                } catch (e: Exception) {
                    null
                }

                if (date != null) {
                    val cal = Calendar.getInstance().apply { time = date }

                    when {
                        isSameDay(cal, today) -> "Today"
                        isSameDay(cal, yesterday) -> "Yesterday"
                        else -> outputFormat.format(date)
                    }
                } else {
                    ""
                }
            }

        return grouped.map { entry ->
            Notification(
                date = entry.key,
                list = entry.value
            )
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * home adapter handel pagination
     */
    private fun pagination() {
        binding.rvNotification.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        loadMoreItems()
                    }
                }
            }
        })
    }

    /**
     *  load more function call
     **/
    private fun loadMoreItems() {
        isProgress = true
        isLoading = true
        currentPage++
        val data = HashMap<String, Any>()
        data["page"] = currentPage
        viewModel.getNotificationApi(data, Constants.GET_POST)
    }


    private fun initAdapter() {
        notificationAdapter =
            SimpleRecyclerViewAdapter(R.layout.item_layout_notification, BR.bean) { v, m, pos ->

            }
        binding.rvNotification.adapter = notificationAdapter
        notificationAdapter.list = notificationList
        notificationAdapter.notifyDataSetChanged()
    }

    private fun getNotificationList(){
        val request= HashMap<String, Any>()
        request["page"]=1
        viewModel.getNotificationApi(request, Constants.GET_NOTIFICATION)
    }
}