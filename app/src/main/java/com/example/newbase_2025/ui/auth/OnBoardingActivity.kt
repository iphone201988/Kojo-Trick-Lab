package com.example.newbase_2025.ui.auth

import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseActivity
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.utils.BindingUtils
import com.example.newbase_2025.databinding.ActivityOnBoardingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingActivity : BaseActivity<ActivityOnBoardingBinding>() {
    private val viewModel: AuthCommonVM by viewModels()
    private lateinit var viewPager: ViewPager2

    override fun getLayoutResource(): Int {
        return R.layout.activity_on_boarding
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        initOnClick()
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        viewPager = binding.viewPager
        viewPager.isUserInputEnabled = true
        BindingUtils.statusBarStyleWhite(this)


    }

    private fun initOnClick() {
        viewModel.onClick.observe(this) {
            when (it?.id) {
            }


        }
    }

}