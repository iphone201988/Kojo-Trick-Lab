package com.example.newbase_2025.ui.dashboard.past_session_detail

import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.data.api.Constants
import com.example.newbase_2025.data.model.CommonApiResponse
import com.example.newbase_2025.data.model.LikedApiResponse
import com.example.newbase_2025.data.model.PastSessionData
import com.example.newbase_2025.databinding.DeleteOrLogoutDialogItemBinding
import com.example.newbase_2025.databinding.FragmentPastSessionDetailBinding
import com.example.newbase_2025.utils.BaseCustomDialog
import com.example.newbase_2025.utils.BindingUtils
import com.example.newbase_2025.utils.Status
import com.example.newbase_2025.utils.showErrorToast
import com.example.newbase_2025.utils.showSuccessToast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PastSessionDetailFragment : BaseFragment<FragmentPastSessionDetailBinding>() {
    private val viewModel: PastSessionDetailVm by viewModels()
    private var sessionId: String? = null
    private lateinit var pastSessionDialogItem: BaseCustomDialog<DeleteOrLogoutDialogItemBinding>

    override fun getLayoutResource(): Int {
        return R.layout.fragment_past_session_detail
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
     * Method to initialize click
     */
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().finish()
                }

                R.id.btnRemoveSession -> {
                    deleteSessionDialogItem()
                }
            }
        }
    }


    /**
     * Method to initialize view
     */
    private fun initView() {
        // get data
        val pastData = arguments?.getParcelable<PastSessionData>("pastSessionData")
        if (pastData != null) {
            sessionId = pastData._id
            binding.bean = pastData
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
                        "deleteSessionApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: CommonApiResponse? = BindingUtils.parseJson(jsonData)
                                if (model?.success == true) {
                                    showSuccessToast(model.message.toString())
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
     * dialog bix initialize and handel
     */
    fun deleteSessionDialogItem() {
        pastSessionDialogItem = BaseCustomDialog(
            requireContext(), R.layout.delete_or_logout_dialog_item
        ) {
            when (it?.id) {
                R.id.btnDeleteCancel -> {
                    pastSessionDialogItem.dismiss()
                }

                R.id.btnDeleteComment -> {
                    viewModel.deleteSessionApi(Constants.SESSION_PLANNER_DELETE+"/$sessionId")
                    pastSessionDialogItem.dismiss()
                }
            }
        }
        pastSessionDialogItem.create()
        pastSessionDialogItem.show()

        pastSessionDialogItem.binding.apply {
            text.text = getString(R.string.delete_this_session)
            tvSure.text = getString(R.string.are_you_sure_you_want_to_delete_this_session)
            btnDeleteComment.text = getString(R.string.delete)

        }

    }

}