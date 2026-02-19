package com.tech.kojo.ui.dashboard.tracker.add_combo

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.data.api.Constants
import com.tech.kojo.databinding.FragmentAddComboBinding
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import com.tech.kojo.utils.showInfoToast
import com.tech.kojo.utils.showSuccessToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddComboFragment : BaseFragment<FragmentAddComboBinding>() {
    private val viewModel: AddComboFragmentVM by viewModels()
    override fun getLayoutResource(): Int {

        return R.layout.fragment_add_combo
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        initOnClick()
        // observer
        initObserver()
        binding.etCombo.setOnTouchListener { v, event ->
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

                R.id.btnSave -> {
                    val comboText = binding.etCombo.text.toString().trim()
                    if (comboText.isNotEmpty()) {
                        val data = HashMap<String, Any>()
                        data["goal"] = comboText
                        viewModel.createComboGoalApi(Constants.COMBO_GOALS_CREATE, data)
                    } else {
                        showInfoToast("Please add combo")
                    }
                }


            }
        }
        binding.etCombo.addTextChangedListener(object : TextWatcher {
            private var isEditing = false
            private var lastLength = 0
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                lastLength = s?.length ?: 0
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return
                if (s.isNullOrEmpty()) return

                val text = s.toString()
                isEditing = true
                if (text.length < lastLength) {
                    if (text.endsWith(" →")) {
                        s.delete(text.length - 2, text.length)
                    }
                    else if (text.endsWith(" → ")) {
                        s.delete(text.length - 3, text.length)
                    }
                    else if (text.endsWith("→")) {
                        s.delete(text.length - 1, text.length)
                    }

                    isEditing = false
                    return
                }
                if (text.endsWith(" ")) {
                    if (!text.trim().endsWith("→")) {
                        s.replace(text.length - 1, text.length, " → ")
                    }
                }
                isEditing = false
            }
        })
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
                        "createComboGoalApi" -> {
                            runCatching {
                                showSuccessToast("Add combo successfully")
                                requireActivity().finish()
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