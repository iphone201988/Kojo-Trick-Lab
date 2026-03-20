package com.tech.kojo.ui.auth

import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.tech.kojo.R
import com.tech.kojo.base.BaseActivity
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.databinding.ActivityAuthBinding
import com.tech.kojo.utils.BindingUtils
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
        BindingUtils.statusBarStyleWhite(this)
        initView()
        val from = intent.getStringExtra("open")
        navController.graph =
            navController.navInflater.inflate(R.navigation.auth_section_navigation).apply {
                if (from=="setup"){
                    setStartDestination(R.id.fragmentSetup)
                }
                else{
                    setStartDestination(R.id.fragmentLogin)
                }
            }
    }

    private fun initView() {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }
    }

}