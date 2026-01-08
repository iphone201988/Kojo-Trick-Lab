package com.tech.kojo.ui.onboarding.sub_fragment

import android.view.View
import androidx.fragment.app.viewModels
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.databinding.FragmentThirdBinding
import com.tech.kojo.ui.onboarding.OnboardingFragmentVM
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