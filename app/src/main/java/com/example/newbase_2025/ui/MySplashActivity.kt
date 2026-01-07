package com.example.newbase_2025.ui

import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseActivity
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.databinding.ActivityMySplashBinding
import com.example.newbase_2025.ui.auth.AuthActivity
import com.example.newbase_2025.ui.auth.AuthCommonVM
import com.example.newbase_2025.ui.dashboard.DashBoardActivity
import com.example.newbase_2025.ui.onboarding.OnBoardingActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MySplashActivity : BaseActivity<ActivityMySplashBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    override fun getLayoutResource(): Int {
        return R.layout.activity_my_splash
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        // view
        initView()
        // click
        initOnClick()
        val data = sharedPrefManager.getLoginData()
        if (data != null) {
            if (data.isEmailVerified == true) {
                val intent = Intent(this@MySplashActivity, DashBoardActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this@MySplashActivity, AuthActivity::class.java)
                startActivity(intent)
                finish()
            }

        }

    }

    /**
     * Method to initialize click
     */
    private fun initOnClick() {
        viewModel.onClick.observe(this@MySplashActivity) {
            when (it?.id) {
                R.id.letsStart -> {
                    val intent = Intent(this@MySplashActivity, OnBoardingActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    /**
     * Method to initialize view
     */
    private fun initView() {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

}