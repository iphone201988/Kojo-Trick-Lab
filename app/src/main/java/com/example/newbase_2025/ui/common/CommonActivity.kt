package com.example.newbase_2025.ui.common

import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseActivity
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.databinding.ActivityCommonBinding
import com.example.newbase_2025.utils.BindingUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CommonActivity : BaseActivity<ActivityCommonBinding>() {
    private val viewModel: CommonActivityVM by viewModels()
    private val navController: NavController by lazy {
        (supportFragmentManager.findFragmentById(R.id.mainNavigationHost) as NavHostFragment).navController
    }

    override fun getLayoutResource(): Int {
        return R.layout.activity_common
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        // set up
        setupSystemUI()
        // navigation
        setupNavigation()
    }

    /**
     * setup system ui
     */
    private fun setupSystemUI() {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        BindingUtils.statusBarStyleBlack(this)
    }

    /**
     * setup navigation
     */
    private fun setupNavigation() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                val graph = navController.navInflater.inflate(R.navigation.common_navigation)
                val fromWhere = intent.getStringExtra("fromWhere")
                when (fromWhere) {
                    "myTrick" -> {
                        graph.setStartDestination(R.id.fragmentMyTrick)
                        navController.setGraph(graph, null)
                    }
                    "editProfilePic" -> {
                        graph.setStartDestination(R.id.fragmentAddProfile)
                        navController.setGraph(graph, null)
                    }
                    "editProfile" -> {
                        graph.setStartDestination(R.id.fragmentSetup)
                        navController.setGraph(graph, null)
                    }

                    else -> {
                        graph.setStartDestination(R.id.fragmentMyTrick)
                        navController.setGraph(graph, null)
                    }
                }
            }
        }
    }

}
