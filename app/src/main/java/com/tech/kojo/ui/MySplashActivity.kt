package com.tech.kojo.ui

import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.tech.kojo.R
import com.tech.kojo.base.BaseActivity
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.databinding.ActivityMySplashBinding
import com.tech.kojo.ui.auth.AuthActivity
import com.tech.kojo.ui.auth.AuthCommonVM
import com.tech.kojo.ui.dashboard.DashBoardActivity
import com.tech.kojo.ui.onboarding.OnBoardingActivity
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