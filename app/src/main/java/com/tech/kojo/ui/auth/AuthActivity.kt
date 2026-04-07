package com.tech.kojo.ui.auth

import android.os.Bundle
import android.util.Log
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
        sharedPrefManager.setLoggedIn(false)
        val from = intent.getStringExtra("open")
        val graph = navController.navInflater.inflate(R.navigation.auth_section_navigation)

        val startDestinationId = when(from) {
            "setup"-> R.id.fragmentAddProfile
            else -> R.id.fragmentLogin
        }

        graph.setStartDestination(startDestinationId)
        navController.graph = graph

        when (startDestinationId) {
            R.id.fragmentAddProfile -> {
                val bundle = Bundle().apply {
                    putString("from", "1")
                }
                navController.navigate(startDestinationId, bundle)
            }
            else -> {
                navController.navigate(startDestinationId)
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