package com.example.newbase_2025.ui.dashboard.session_detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.databinding.FragmentSessionDetailBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SessionDetailFragment : BaseFragment<FragmentSessionDetailBinding>() {


    private val viewModel : SessionDetailVm by viewModels()

    override fun getLayoutResource(): Int {
        return R.layout.fragment_session_detail
    }

    override fun getViewModel(): BaseViewModel {
         return viewModel
    }

    override fun onCreateView(view: View) {
        initOnClick()

    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer{
            when(it?.id){

            }
        })
    }


}