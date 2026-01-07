package com.example.newbase_2025.ui.dashboard.add_review_post_session

import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.data.api.Constants
import com.example.newbase_2025.data.model.CreateSessionApiResponse
import com.example.newbase_2025.data.model.NextSessionData
import com.example.newbase_2025.databinding.AddSessionBottomItemBinding
import com.example.newbase_2025.databinding.ChooseColorDailogItemBinding
import com.example.newbase_2025.databinding.DeleteOrLogoutDialogItemBinding
import com.example.newbase_2025.databinding.FragmentAddReviewPostSessionBinding
import com.example.newbase_2025.utils.BaseCustomBottomSheet
import com.example.newbase_2025.utils.BaseCustomDialog
import com.example.newbase_2025.utils.BindingUtils
import com.example.newbase_2025.utils.Status
import com.example.newbase_2025.utils.showErrorToast
import com.example.newbase_2025.utils.showSuccessToast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddReviewPostSessionFragment : BaseFragment<FragmentAddReviewPostSessionBinding>() {
    private val viewModel: AddReviewPostSessionVm by viewModels()
    private var sessionId: String? = null
    private var nextData: NextSessionData? = null
    override fun getLayoutResource(): Int {
        return R.layout.fragment_add_review_post_session
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
        // get data
        val pastData = arguments?.getParcelable<NextSessionData>("sessionData")
        if (pastData != null) {
            binding.bean = pastData
        }

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

                R.id.btnSaveSession -> {
                    val data = HashMap<String, Any>()
                    if (nextData != null) {
                        data["review"] = binding.etDescription.text.toString().trim()
                        viewModel.updateSessionPlannerApi(
                            Constants.SESSION_PLANNER_UPDATE + "/${nextData!!._id}", data
                        )
                    }
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
                        "updateSessionPlannerApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: CreateSessionApiResponse? =
                                    BindingUtils.parseJson(jsonData)
                                var update = model?.data
                                if (update != null) {
                                    sessionId = update._id
                                    binding.bean = update
                                    showSuccessToast("Session updated successfully")
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


}