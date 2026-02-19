package com.tech.kojo.ui.onboarding

import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.tech.kojo.R
import com.tech.kojo.base.BaseActivity
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.databinding.ActivityOnBoardingBinding
import com.tech.kojo.ui.auth.AuthActivity
import com.tech.kojo.utils.BindingUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnBoardingActivity : BaseActivity<ActivityOnBoardingBinding>() {
    private val viewModel: OnboardingFragmentVM by viewModels()
    override fun getLayoutResource(): Int {
        return R.layout.activity_on_boarding
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        BindingUtils.statusBarStyleWhite(this)
        binding.type =1
        // view
        initView()
        // click
        initOnClick()

        // adapter
        val adapter = OnBoardingPagerAdapter(this@OnBoardingActivity)
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false

        //  Listen to page changes
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.type = if (position == 0) 1 else 2
                when (position) {
                    0 -> binding.ivProgress.setImageResource(R.drawable.first_page)
                    1 -> binding.ivProgress.setImageResource(R.drawable.second_page)
                    2 -> binding.ivProgress.setImageResource(R.drawable.third_page)
                }
                // Next / Start button text
                binding.tvNext.text = if (position == 2) { "Start"
                } else {
                    getString(R.string.next)
                }
            }
        })
    }

    /**
     * Method to initialize click
     */
    private fun initOnClick() {
        viewModel.onClick.observe(this@OnBoardingActivity) {
            when (it?.id) {
                R.id.letsStart -> {
                    val intent = Intent(this@OnBoardingActivity, OnBoardingActivity::class.java)
                    startActivity(intent)
                }

                R.id.clNext -> {
                    val currentItem = binding.viewPager.currentItem
                    if (currentItem < 2) {
                        binding.viewPager.setCurrentItem(currentItem + 1, true)
                    }else{
                        sharedPrefManager.setOnBoarding("true")
                        val intent = Intent(this@OnBoardingActivity, AuthActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }

                R.id.clBack -> {
                    val currentItem = binding.viewPager.currentItem
                    if (currentItem > 0) {
                        binding.viewPager.setCurrentItem(currentItem - 1, true)
                    }
                }

                R.id.clNext1 -> {
                    val currentItem = binding.viewPager.currentItem
                    if (currentItem < 2) {
                        binding.viewPager.setCurrentItem(currentItem + 1, true)
                    }
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