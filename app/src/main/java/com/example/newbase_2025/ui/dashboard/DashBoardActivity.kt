package com.example.newbase_2025.ui.dashboard

import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseActivity
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.databinding.ActivityDashBoardBinding
import com.example.newbase_2025.ui.dashboard.home.HomeFragment
import com.example.newbase_2025.ui.dashboard.library.LibraryFragment
import com.example.newbase_2025.ui.dashboard.profile.ProfileFragment
import com.example.newbase_2025.ui.dashboard.tracker.TrackerFragment
import com.example.newbase_2025.utils.BindingUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashBoardActivity : BaseActivity<ActivityDashBoardBinding>() {
    private val viewModel: DashBoardActivityVM by viewModels()
    override fun getLayoutResource(): Int {
        return R.layout.activity_dash_board
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
        binding.type = 1
        BindingUtils.statusBarStyleWhite(this)
        setupBottomNav()
        binding.navHome.performClick()
    }

    private fun setupBottomNav() {
        val tabs = listOf(
            Triple(
                binding.navHome,
                binding.navHome.getChildAt(0) as ImageView,
                binding.navHome.getChildAt(1) as TextView
            ), Triple(
                binding.navLibrary,
                binding.navLibrary.getChildAt(0) as ImageView,
                binding.navLibrary.getChildAt(1) as TextView
            ), Triple(
                binding.navTracker,
                binding.navTracker.getChildAt(0) as ImageView,
                binding.navTracker.getChildAt(1) as TextView
            ), Triple(
                binding.navCommunity,
                binding.navCommunity.getChildAt(0) as ImageView,
                binding.navCommunity.getChildAt(1) as TextView
            ), Triple(
                binding.navProfile,
                binding.navProfile.getChildAt(0) as ImageView,
                binding.navProfile.getChildAt(1) as TextView
            )
        )

        tabs.forEach { (tab, icon, text) ->
            tab.setOnClickListener {
                // Reset all tabs
                tabs.forEach { (t, i, txt) ->
                    t.setBackgroundResource(0)
                    // Skip tint reset for profile tab
                    if (t.id != R.id.nav_profile) {
                        i.setColorFilter(ContextCompat.getColor(this, android.R.color.white))
                        txt.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                    } else {
                        txt.setTextColor(ContextCompat.getColor(this, android.R.color.white))
                    }
                }

                // Highlight selected tab
                tab.setBackgroundResource(R.drawable.bg_nav_item_selected)

                // Only change tint/text if it's NOT the profile tab
                if (tab.id != R.id.nav_profile) {
                    icon.setColorFilter(ContextCompat.getColor(this, R.color.nav_selected_icon))
                    text.setTextColor(ContextCompat.getColor(this, R.color.nav_selected_text))
                } else {
                    text.setTextColor(ContextCompat.getColor(this, R.color.nav_selected_text))
                }

                // Handle navigation
                when (tab.id) {
                    R.id.nav_home -> {
                        binding.type = 1
                        showFragment(HomeFragment())
                    }

                    R.id.nav_library -> {
                        binding.type = 3
                        showFragment(LibraryFragment())
                    }

                    R.id.nav_tracker -> {
                        binding.type = 2
                        showFragment(TrackerFragment())
                    }

                    R.id.nav_community -> {
                        binding.type = 1
                        showFragment(HomeFragment())
                    }

                    R.id.nav_profile -> {
                        binding.type = 1
                        showFragment(ProfileFragment())
                    }
                }
            }
        }
    }


    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment)
            .commit()
    }

}