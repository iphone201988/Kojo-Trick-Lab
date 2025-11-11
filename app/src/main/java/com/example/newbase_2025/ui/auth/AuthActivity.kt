package com.example.newbase_2025.ui.auth

import androidx.activity.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseActivity
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.databinding.ActivityAuthBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AuthActivity : BaseActivity<ActivityAuthBinding>() {
    private val viewModel: AuthCommonVM by viewModels()


    private val navController: NavController by lazy {
        (supportFragmentManager.findFragmentById(R.id.onBoardingNav) as NavHostFragment).navController
    }

    override fun getLayoutResource(): Int {
        return R.layout.activity_auth
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        navController.graph =
            navController.navInflater.inflate(R.navigation.auth_section_navigation).apply {
                setStartDestination(R.id.fragmentLogin)
            }
    }


}