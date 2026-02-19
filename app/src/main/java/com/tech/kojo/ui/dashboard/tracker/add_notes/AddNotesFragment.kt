package com.tech.kojo.ui.dashboard.tracker.add_notes

import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.GetComboData
import com.tech.kojo.data.model.UpdateNotesApiResponse
import com.tech.kojo.databinding.FragmentAddNotesBinding
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import com.tech.kojo.utils.showInfoToast
import com.tech.kojo.utils.showSuccessToast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddNotesFragment : BaseFragment<FragmentAddNotesBinding>() {
    private val viewModel: AddNotesFragmentVM by viewModels()
    private var goalId: String? = null
    override fun getLayoutResource(): Int {

        return R.layout.fragment_add_notes
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        initOnClick()
        // observer
        initObserver()
        // view
        initView()
    }

    /**
     * Method to initialize view
     */
    private fun initView() {
        val comboData = arguments?.getParcelable<GetComboData>("comboData")
        if (comboData != null) {
            binding.bean = comboData
            goalId = comboData?._id
        }

        binding.etEmail.setOnTouchListener { v, event ->
            v.parent.requestDisallowInterceptTouchEvent(true)
            false
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
//                R.id.tvShowMore->{
//                    comboData?.isExpanded?.let { it1 -> comboData?.isExpanded = !it1 }
//                    binding.invalidateAll()
//                    // 🔥 Force scroll re-measure after expansion
//                    binding.root.post {
//                        binding.root.requestLayout()
//                    }
//                }

                R.id.btnSave -> {
                    val notesText = binding.etEmail.text.toString().trim()
                    val goalText = binding.tvGoal.text.toString().trim()
                    if (notesText.isNotEmpty()) {
                        val data = HashMap<String, Any>()
                        data["notes"] = notesText
                        data["goal"] = goalText
                        viewModel.addNotesAPi(Constants.COMBO_GOALS_UPDATE + "/$goalId", data)
                    } else {
                        showInfoToast("Please add notes")
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
                        "addNotesAPi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: UpdateNotesApiResponse? =
                                    BindingUtils.parseJson(jsonData)
                                if (model != null) {
                                    showSuccessToast(it.message.toString())
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


}