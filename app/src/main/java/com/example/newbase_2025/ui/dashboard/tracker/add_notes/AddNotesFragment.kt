package com.example.newbase_2025.ui.dashboard.tracker.add_notes

import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.data.api.Constants
import com.example.newbase_2025.data.model.GetComboData
import com.example.newbase_2025.data.model.UpdateNotesApiResponse
import com.example.newbase_2025.databinding.FragmentAddNotesBinding
import com.example.newbase_2025.utils.BindingUtils
import com.example.newbase_2025.utils.Status
import com.example.newbase_2025.utils.showErrorToast
import com.example.newbase_2025.utils.showInfoToast
import com.example.newbase_2025.utils.showSuccessToast
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
            goalId = comboData._id
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