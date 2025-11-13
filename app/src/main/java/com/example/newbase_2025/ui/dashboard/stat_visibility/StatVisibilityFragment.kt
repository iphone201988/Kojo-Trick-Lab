package com.example.newbase_2025.ui.dashboard.stat_visibility

import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.databinding.FragmentStatVisibilityBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatVisibilityFragment : BaseFragment<FragmentStatVisibilityBinding>() {
    private val viewModel: StatVisibilityVM by viewModels()
    override fun onCreateView(view: View) {
        initOnClick()
        initView()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_stat_visibility
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private fun initView() {
        binding.clCommon.tvHeader.text = "Stat Visibility"


    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

}