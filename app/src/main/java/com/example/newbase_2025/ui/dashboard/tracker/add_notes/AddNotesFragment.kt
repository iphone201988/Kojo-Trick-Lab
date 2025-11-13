package com.example.newbase_2025.ui.dashboard.tracker.add_notes

import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.databinding.FragmentAddNotesBinding
import com.example.newbase_2025.ui.common.CommonActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddNotesFragment : BaseFragment<FragmentAddNotesBinding>() {
    private val viewModel: AddNotesFragmentVM by viewModels()

    override fun getLayoutResource(): Int {

        return R.layout.fragment_add_notes
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



            }
        }
    }


}