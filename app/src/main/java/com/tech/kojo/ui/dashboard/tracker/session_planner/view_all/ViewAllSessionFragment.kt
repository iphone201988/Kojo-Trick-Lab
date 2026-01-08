package com.tech.kojo.ui.dashboard.tracker.session_planner.view_all

import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.GetPastSessionAPiResponse
import com.tech.kojo.data.model.PastSessionData
import com.tech.kojo.databinding.DeleteOrLogoutDialogItemBinding
import com.tech.kojo.databinding.FragmentViewAllSessionBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.utils.BaseCustomDialog
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ViewAllSessionFragment : BaseFragment<FragmentViewAllSessionBinding>() {
    private val viewModel: ViewAllSessionFragmentVM by viewModels()
    private lateinit var clearAllDialogItem: BaseCustomDialog<DeleteOrLogoutDialogItemBinding>
    private var currentPage = 1
    private var sessionId: String?=null
    private var isLoading = false
    private var isLastPage = false
    private var isProgress = false
    private lateinit var viewAllAdapter: ViewAllAdapter
    override fun getLayoutResource(): Int {
        return R.layout.fragment_view_all_session
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        initOnClick()

        // view
        initView()

        // observer
        initObserver()
    }


    /**
     * Method to initialize view
     */

    private fun initView() {
        // adapter
        initAdapter()
        // api call
        val data = HashMap<String, Any>()
        data["page"] = currentPage
        viewModel.getAllSessionApi(data, Constants.SESSION_PLANNER)

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
                // api call
                val data = HashMap<String, Any>()
                data["page"] = 1
                viewModel.getAllSessionApi(data, Constants.SESSION_PLANNER)
            }, 2000)
        }
    }


    /**
     * home adapter handel pagination
     */
    private fun pagination() {
        binding.rvViewAll.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        viewAllAdapter.showLoader()
        isProgress = true
        isLoading = true
        currentPage++
        val data = HashMap<String, Any>()
        data["page"] = currentPage
        viewModel.getAllSessionApi(data, Constants.SESSION_PLANNER)
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

                R.id.tvViewAll -> {
                    clearAllDialogItem()
                    // binding.tvEmpty.visibility = View.VISIBLE
                }

            }
        }
    }

    /** api response observer ***/
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
                        "getAllSessionApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: GetPastSessionAPiResponse? =
                                    BindingUtils.parseJson(jsonData)
                                if (model != null) {
                                    val pastSessionData = model.data
                                    // Convert PastSessionData → FeedItem.Post
                                    val feedItems: List<ViewItem> =
                                        pastSessionData?.filterNotNull()?.map { ViewItem.Post(it) }
                                            ?: emptyList()
                                    isLoading = false
                                    isLastPage = false
                                    isProgress = true
                                    if (currentPage == 1) {
                                        viewAllAdapter.setList(feedItems)
                                    } else {
                                        viewAllAdapter.addToList(feedItems)
                                    }
                                    isLastPage = currentPage == model.totalPages

                                    if (viewAllAdapter.getList().isNotEmpty()) {
                                        binding.clEmpty.visibility = View.GONE
                                    } else {
                                        binding.clEmpty.visibility = View.VISIBLE
                                    }

                                    if (pastSessionData != null && pastSessionData.isNotEmpty()) {
                                        sessionId = model.data[0]?._id
                                    }



                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())

                            }.also {
                                viewAllAdapter.hideLoader()
                                hideLoading()
                            }
                        }

                        "deleteAllSessionPlannerApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: GetPastSessionAPiResponse? =
                                    BindingUtils.parseJson(jsonData)
                                if (model != null) {
                                    val data = HashMap<String, Any>()
                                    data["page"] = currentPage
                                    viewModel.getAllSessionApi(data, Constants.SESSION_PLANNER)
                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())

                            }.also {

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
     * Method to initialize adapter
     */

    private fun initAdapter() {
        viewAllAdapter = ViewAllAdapter(object : ViewAllAdapter.OnItemClickListener {
            override fun onItemClick(item: PastSessionData?, clickedViewId: Int, position: Int) {
                when (clickedViewId) {
                    R.id.clSession -> {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "pastSession")
                        intent.putExtra("pastSessionData", item)
                        startActivity(intent)
                    }

                }
            }
        })

        binding.rvViewAll.adapter = viewAllAdapter
    }

    /**
     * dialog bix initialize and handel
     */
    fun clearAllDialogItem() {
        clearAllDialogItem = BaseCustomDialog(
            requireContext(), R.layout.delete_or_logout_dialog_item
        ) {
            when (it?.id) {
                R.id.btnDeleteCancel -> {
                    clearAllDialogItem.dismiss()
                }

                R.id.btnDeleteComment -> {
                    if (sessionId!=null){
                        val data = HashMap<String, Any>()
                        data["deleteAll"] = true
                        data["sessionId"] = sessionId!!
                        viewModel.deleteAllSessionPlannerApi(Constants.SESSION_PLANNER_DELETE,data)
                    }
                    clearAllDialogItem.dismiss()
                }
            }
        }
        clearAllDialogItem.create()
        clearAllDialogItem.show()

        clearAllDialogItem.binding.apply {
            text.text = getString(R.string.delete_all_session)
            tvSure.text = getString(R.string.are_you_sure_you_want_to_delete_all_session)
            btnDeleteComment.text = getString(R.string.delete)

        }

    }

}