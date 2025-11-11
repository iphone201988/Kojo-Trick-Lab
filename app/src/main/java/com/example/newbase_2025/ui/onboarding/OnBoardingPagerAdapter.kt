package com.example.newbase_2025.ui.onboarding

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.newbase_2025.ui.onboarding.sub_fragment.FirstFragment
import com.example.newbase_2025.ui.onboarding.sub_fragment.SecondFragment
import com.example.newbase_2025.ui.onboarding.sub_fragment.ThirdFragment

class OnBoardingPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                FirstFragment()
            }

            1 -> {
                SecondFragment()
            }

            2 -> {
                ThirdFragment()
            }

            else -> {
                FirstFragment()
            }
        }
    }
}