package com.example.newbase_2025.ui.onboarding.sub_fragment

import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.databinding.FragmentThirdBinding
import com.example.newbase_2025.ui.onboarding.OnboardingFragmentVM
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ThirdFragment : BaseFragment<FragmentThirdBinding>() {
    private val viewModel: OnboardingFragmentVM by viewModels()
    override fun getLayoutResource(): Int {
        return R.layout.fragment_third
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // status bar color change

    }


}