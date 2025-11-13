package com.example.newbase_2025.ui.dashboard.tracker.add_combo

import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.base.local.SharedPrefManager
import com.example.newbase_2025.data.model.ComboGoalsData
import com.example.newbase_2025.databinding.FragmentAddComboBinding
import com.example.newbase_2025.utils.showInfoToast
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
                    val comboText = binding.etEmail.text.toString().trim()
                    if (comboText.isNotEmpty()) {
                        val existingList = sharedPrefManager.getList<ComboGoalsData>(
                            SharedPrefManager.KEY.COMBO_LIST
                        ).toMutableList()
                        existingList.add(ComboGoalsData(comboText))
                        // Save updated list
                        sharedPrefManager.saveList(SharedPrefManager.KEY.COMBO_LIST, existingList)
                        requireActivity().finish()
                    } else {
                        showInfoToast("Please add combo")
                    }
                }


            }
        }
    }



}