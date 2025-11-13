package com.example.newbase_2025.ui.dashboard.past_session_detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.databinding.FragmentPastSessionDetailBinding
import com.example.newbase_2025.ui.dashboard.change_password.ChangePasswordVM
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PastSessionDetailFragment : BaseFragment<FragmentPastSessionDetailBinding>() {

    private val viewModel: PastSessionDetailVm by viewModels()


    override fun getLayoutResource(): Int {
        return R.layout.fragment_past_session_detail
    }

    override fun getViewModel(): BaseViewModel {
    }

    override fun onCreateView(view: View) {
    }

}