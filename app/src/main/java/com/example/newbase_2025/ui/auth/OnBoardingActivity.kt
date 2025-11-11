package com.example.newbase_2025.ui.auth

import android.content.Intent
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

        val pages = listOf(
            OnBoardPage(R.drawable.lawyer_pana),
            OnBoardPage(R.drawable.onbaord2),
            OnBoardPage(R.drawable.onboard3)
        )


        // Initialize the adapter
        val adapter = OnBoardAdapter(pages, this)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        binding.tvHeading.setText(getString(R.string.on_boarding1))
                        binding.tvSubHeading.setText(getString(R.string.on_boarding_des1))
                        binding.ivThreeDot.setImageResource(R.drawable.dot1)

                    }

                    1 -> {
                        binding.tvHeading.setText(getString(R.string.on_boarding2))
                        binding.tvSubHeading.setText(getString(R.string.on_boarding_des2))
                        binding.ivThreeDot.setImageResource(R.drawable.dot2)

                    }

                    2 -> {
                        binding.tvHeading.setText(getString(R.string.on_boarding3))
                        binding.tvSubHeading.setText(getString(R.string.on_boarding_des3))
                        binding.ivThreeDot.setImageResource(R.drawable.dot3)

                    }

                }
            }
        })
    }

    private fun initOnClick() {
        viewModel.onClick.observe(this) {
            when (it?.id) {
                R.id.ivBack->{
                    finish()
                }
                R.id.tvNext -> {
                    if (viewPager.currentItem == 2) {
                        val intent = Intent(this, CommonActivity::class.java)
                        intent.putExtra("From", "LoginFragment")
                        startActivity(intent)
                        finish()
                    } else {
                        viewPager.currentItem += 1
                    }
                }
            }
        }
    }

    data class OnBoardPage(val imageResId: Int)
}