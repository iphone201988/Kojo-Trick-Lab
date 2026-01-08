package com.tech.kojo.ui.dashboard.add_review_post_session

import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.CreateSessionApiResponse
import com.tech.kojo.data.model.NextSessionData
import com.tech.kojo.databinding.FragmentAddReviewPostSessionBinding
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import com.tech.kojo.utils.showSuccessToast
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