package com.tech.kojo.ui.dashboard.notification

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
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
import com.tech.kojo.data.model.Notification
import com.tech.kojo.data.model.NotificationData
import com.tech.kojo.databinding.FragmentNotificationNewBinding
import com.tech.kojo.databinding.ItemLayoutInnerNotificationBinding
import com.tech.kojo.databinding.ItemLayoutNotificationBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@AndroidEntryPoint
class NotificationNewFragment : BaseFragment<FragmentNotificationNewBinding>() {

    private val viewModel: NotificationVm by viewModels()

    private lateinit var notificationAdapter: SimpleRecyclerViewAdapter<Notification, ItemLayoutNotificationBinding>

    // Pagination variables
    private var currentPage = 1
    private var totalPages = 1
    private var isLoading = false
    private var isLastPage = false

    // Progress bar for pagination
    private var progressBar: ProgressBar? = null

    // Master list to store all notifications across pages
    private var allNotifications = mutableListOf<NotificationData>()


    companion object {
        var isSubscribed: Boolean?=false
        @BindingAdapter("childNotificationAdapter")
        @JvmStatic
        fun childNotificationAdapter(view: RecyclerView, notification: List<NotificationData>?) {
            val layoutManager = LinearLayoutManager(view.context)
            view.layoutManager = layoutManager

            val childNotificationAdapter =
                SimpleRecyclerViewAdapter<NotificationData, ItemLayoutInnerNotificationBinding>(
                    R.layout.item_layout_inner_notification, BR.bean
                ) { v, m, _ ->
                    when (v.id) {
                        R.id.clMain -> {
                            Log.d("NotificationClick", "Item clicked: ${m.type}")
                            when (m.type) {
                                "NEW_POST", "POST_LIKE", "POST_COMMENT" -> {
                                    val intent = Intent(v.context, CommonActivity::class.java)
                                    intent.putExtra("fromWhere", "communityDetail")
                                    intent.putExtra("postId", m.data?.postId)
                                    v.context.startActivity(intent)
                                }
                                "SESSION_REMINDER" -> {
                                    // Handle session reminder
                                }
                                "CLIP_REVIEW" -> {
                                    // Handle clip review
                                }
                                "NEW_VIDEO"->{

                                }
                                "NEW_TRICK" -> {
                                    if (isSubscribed==false){
                                        Toast.makeText(v.context,"You don't have any subscription",
                                            Toast.LENGTH_SHORT).show()
                                        return@SimpleRecyclerViewAdapter
                                    }
                                    val intent = Intent(v.context, CommonActivity::class.java)
                                    intent.putExtra("fromWhere", "homeProgress")
                                    intent.putExtra("trackDetailId", m.data?.trickDataId)
                                    v.context.startActivity(intent)
                                }
                                else -> {
                                    Log.e("childAdapter", "childNotificationAdapter: ${m.type} clicked")
                                }
                            }
                        }
                    }
                }
            view.adapter = childNotificationAdapter
            childNotificationAdapter.list = notification
        }
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_notification_new
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        setupUI()
        setupPagination()
        initAdapter()
        initOnClick()
        initObserver()
        isSubscribed = sharedPrefManager.getLoginData()?.isSubscription
        // Initial load
        loadFirstPage()

        // Clear notification count
        sharedPrefManager.setNotificationCount(0)
    }

    private fun setupUI() {
        binding.clCommon.tvHeader.text = "Notification"

        // Add progress bar at the bottom of RecyclerView for pagination
        addPaginationProgressBar()
    }

    private fun addPaginationProgressBar() {
        // Create progress bar programmatically
        progressBar = ProgressBar(requireContext()).apply {
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )
            visibility = View.GONE
        }

        // Add to your layout if needed, or handle in adapter
    }


    /**
     * Load first page of notifications
     */
    private fun loadFirstPage() {
        currentPage = 1
        allNotifications.clear() // Clear the master list when loading first page
        val request = HashMap<String, Any>()
        request["page"] = currentPage
        viewModel.getNotificationApi(request, Constants.GET_NOTIFICATION)
    }

    /**
     * Setup pagination scroll listener
     */
    private fun setupPagination() {
        binding.rvNotification.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                // Debug logging
                Log.d("Pagination", "=== Pagination Debug ===")
                Log.d("Pagination", "isLoading: $isLoading")
                Log.d("Pagination", "isLastPage: $isLastPage")
                Log.d("Pagination", "currentPage: $currentPage")
                Log.d("Pagination", "totalPages: $totalPages")
                Log.d("Pagination", "totalItemCount: $totalItemCount")
                Log.d("Pagination", "lastVisibleItem: $lastVisibleItemPosition")

                // Check if we need to load more
                val isLastItemVisible = lastVisibleItemPosition == totalItemCount - 1
                val hasMorePages = currentPage < totalPages
                val shouldLoadMore = !isLoading && !isLastPage && isLastItemVisible && hasMorePages

                Log.d("Pagination", "isLastItemVisible: $isLastItemVisible")
                Log.d("Pagination", "hasMorePages: $hasMorePages")
                Log.d("Pagination", "shouldLoadMore: $shouldLoadMore")

                if (shouldLoadMore) {
                    loadMoreItems()
                }
            }
        })
    }

    /**
     * Load more items for pagination
     */
    private fun loadMoreItems() {
        isLoading = true
        currentPage++

        Log.d("Pagination", "Loading more items. Page: $currentPage")

        val data = HashMap<String, Any>()
        data["page"] = currentPage
        viewModel.getNotificationApi(data, Constants.GET_NOTIFICATION)

        // Show progress at the bottom
        showLoadingProgress()
    }

    /**
     * Show loading indicator at the bottom
     */
    private fun showLoadingProgress() {
        progressBar?.visibility = View.VISIBLE
    }

    /**
     * Hide loading indicator at the bottom
     */
    private fun hideLoadingProgress() {
        progressBar?.visibility = View.GONE
    }

    /**
     * Initialize click listeners
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
     * Initialize adapter
     */
    private fun initAdapter() {
        notificationAdapter = SimpleRecyclerViewAdapter<Notification, ItemLayoutNotificationBinding>(
            R.layout.item_layout_notification,
            BR.bean
        ) { v, m, pos ->

        }

        binding.rvNotification.adapter = notificationAdapter
    }

    /**
     * Initialize observers
     */
    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner) { response ->
            when (response?.status) {
                Status.LOADING -> {
                    // Only show loading dialog for first page and not during refresh
                    if (currentPage == 1) {
                        showLoading()
                    }
                }

                Status.SUCCESS -> {
                    when (response.message) {
                        "getNotificationApi" -> {
                            // Parse the response data
                            val jsonData = response.data?.toString().orEmpty()
                            val model: GetNotificationData? = BindingUtils.parseJson(jsonData)

                            if (model != null) {
                                handleNotificationResponse(model)
                            } else {
                                handleParseError()
                            }
                        }
                    }
                }

                Status.ERROR -> {
                    handleError(response.message.toString())
                }

                else -> {
                    // Handle other states if needed
                }
            }
        }
    }

    /**
     * Handle notification API response
     */
    private fun handleNotificationResponse(response: GetNotificationData) {
        hideLoading()
        hideLoadingProgress()
        runCatching {
            val newNotifications = response.data.orEmpty()

            // Add new notifications to the master list
            allNotifications.addAll(newNotifications)

            // Regroup all notifications from scratch to combine same dates across pages
            val groupedNotifications = groupNotificationsByDate(allNotifications)

            // Update pagination info from API response with null safety
            val currentPageFromResponse = response.page ?: 1
            totalPages = response.totalPages ?: 1

            Log.d("Pagination", "API Response - Current Page: $currentPageFromResponse, Total Pages: $totalPages")
            Log.d("Pagination", "Received ${newNotifications.size} new notifications")
            Log.d("Pagination", "Total notifications in memory: ${allNotifications.size}")
            Log.d("Pagination", "Total groups after regrouping: ${groupedNotifications.size}")

            // Always set the entire list with regrouped notifications
            notificationAdapter.setList(groupedNotifications)

            // Update pagination state with proper null safety
            isLoading = false
            isLastPage = currentPageFromResponse >= totalPages

            Log.d("Pagination", "After update - isLoading: $isLoading, isLastPage: $isLastPage")

            // Show/hide empty view
            binding.clEmpty.visibility =
                if (notificationAdapter.list.isEmpty()) View.VISIBLE else View.GONE

        }.onFailure { e ->
            Log.e("apiErrorOccurred", "Error: ${e.message}", e)
            handleApiError(e)
        }.also {
        }
    }

    /**
     * Handle parse error
     */
    private fun handleParseError() {
        Log.e("NotificationError", "Failed to parse notification data")
        showErrorToast("Failed to parse notification data")
        isLoading = false
        hideLoadingProgress()
    }

    /**
     * Handle API error
     */
    private fun handleApiError(e: Throwable) {
        showErrorToast(e.message.toString())

        // Reset pagination state on error
        if (currentPage > 1) {
            currentPage--
            isLoading = false

            // Remove the failed page's notifications from master list
            // Since we don't know exactly which ones were added, we need to reload all notifications
            // or we can keep track of page-wise notifications. For simplicity, we'll just reset
            // the master list and reload from first page if needed
            if (allNotifications.isNotEmpty()) {
                // Optional: Implement retry logic or just keep the existing data
                Log.e("Pagination", "Error occurred while loading page $currentPage. Data may be inconsistent.")
            }
        }

        hideLoadingProgress()
    }

    /**
     * Handle error
     */
    private fun handleError(message: String) {
        hideLoading()
        hideLoadingProgress()
        showErrorToast(message)
        isLoading = false

        // If error occurred while loading more, revert page count
        if (currentPage > 1) {
            currentPage--
        }
    }

    private fun groupNotificationsByDate(
        notifications: List<NotificationData>
    ): List<Notification> {

        if (notifications.isEmpty()) {
            return emptyList()
        }

        val inputFormat = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()
        ).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }

        val grouped = notifications.filterNotNull().groupBy { notification ->
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
                "Unknown"
            }
        }

        return grouped.map { entry ->
            Notification(
                date = entry.key,
                list = entry.value
            )
        }.sortedByDescending { notification ->
            // Sort by date (most recent first)
            when (notification.date) {
                "Today" -> 3
                "Yesterday" -> 2
                else -> 1
            }
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressBar = null
        allNotifications.clear()
    }
}