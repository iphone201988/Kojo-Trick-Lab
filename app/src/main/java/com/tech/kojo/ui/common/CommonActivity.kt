package com.tech.kojo.ui.common

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.tech.kojo.R
import com.tech.kojo.base.BaseActivity
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.data.model.GetComboData
import com.tech.kojo.data.model.HomeProgressStep
import com.tech.kojo.data.model.HomeTrickVault
import com.tech.kojo.data.model.NextSessionData
import com.tech.kojo.data.model.PastSessionData
import com.tech.kojo.data.model.PostData
import com.tech.kojo.databinding.ActivityCommonBinding
import com.tech.kojo.utils.BindingUtils
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

                    "trainedRecently" -> {
                        val userProgressId = intent.getStringExtra("userProgressId")
                        val bundle = Bundle()
                        bundle.putString("userProgressId", userProgressId)
                        graph.setStartDestination(R.id.fragmentTrainedRecently)
                        navController.setGraph(graph, bundle)
                    }

                    "progressionDetails" -> {
                        val progressId = intent.getStringExtra("progressId")
                        val bundle = Bundle()
                        bundle.putString("trackDetailId", progressId)
                        graph.setStartDestination(R.id.fragmentProgressionDetails)
                        navController.setGraph(graph, bundle)
                    }

                    "comboGoals" -> {
                        graph.setStartDestination(R.id.fragmentComboGoals)
                        navController.setGraph(graph, null)
                    }

                    "addComboGals" -> {
                        graph.setStartDestination(R.id.fragmentAddCombo)
                        navController.setGraph(graph, null)
                    }

                    "addNotes" -> {
                        val comboData = intent.getParcelableExtra<GetComboData>("comboData")
                        val bundle = Bundle()
                        bundle.putParcelable("comboData", comboData)
                        graph.setStartDestination(R.id.fragmentAddNotes)
                        navController.setGraph(graph, bundle)
                    }

                    "forwardTrick" -> {
                        val trackData = intent.getParcelableExtra<HomeTrickVault>("trackData")
                        val bundle = Bundle()
                        bundle.putParcelable("trackData", trackData)
                        graph.setStartDestination(R.id.fragmentForwardTrick)
                        navController.setGraph(graph, bundle)
                    }

                    "homeProgress" -> {
                        val trackDetailId = intent.getStringExtra("trackDetailId")
                        val bundle = Bundle()
                        bundle.putString("trackDetailId", trackDetailId)
                        graph.setStartDestination(R.id.fragmentHomeProgressDetails)
                        navController.setGraph(graph, bundle)
                    }

                    "finalProgress" -> {
                        val progressData =
                            intent.getParcelableExtra<HomeProgressStep>("progressData")
                        val trackDetailId = intent.getStringExtra("trackDetailId")
                        val diveName = intent.getStringExtra("diveName")
                        val bundle = Bundle()
                        bundle.putParcelable("progressData", progressData)
                        bundle.putString("trackDetailId", trackDetailId)
                        bundle.putString("diveName", diveName)
                        graph.setStartDestination(R.id.fragmentFinalProgress)
                        navController.setGraph(graph, bundle)
                    }

                    "trickingMilestones" -> {
                        graph.setStartDestination(R.id.fragmentTrickingMilestones)
                        navController.setGraph(graph, null)
                    }

                    "categoryChecking" -> {
                        val categoryId = intent.getStringExtra("categoryId")
                        val bundle = Bundle()
                        bundle.putString("categoryId", categoryId)
                        graph.setStartDestination(R.id.fragmentCategoryChecking)
                        navController.setGraph(graph, bundle)
                    }

                    "myStar" -> {
                        graph.setStartDestination(R.id.fragmentMyStar)
                        navController.setGraph(graph, null)
                    }

                    "personalBests" -> {
                        graph.setStartDestination(R.id.fragmentPersonalBests)
                        navController.setGraph(graph, null)
                    }

                    "videoPlayer" -> {
                        val topicId = intent.getStringExtra("topicId")
                        val categoryId = intent.getStringExtra("categoryId")
                        val videoId = intent.getStringExtra("videoId")
                        val bundle = Bundle()
                        bundle.putString("topicId", topicId)
                        bundle.putString("videoId", videoId)
                        bundle.putString("categoryId", categoryId)
                        graph.setStartDestination(R.id.fragmentVideoPlayer)
                        navController.setGraph(graph, bundle)
                    }

                    "userProfile" -> {
                        val userId = intent.getStringExtra("userId")
                        val bundle = Bundle()
                        bundle.putString("userId", userId)
                        graph.setStartDestination(R.id.fragmentUserProfile)
                        navController.setGraph(graph, bundle)
                    }

                    "series" -> {
                        val categoryId = intent.getStringExtra("categoryId")
                        val title = intent.getStringExtra("title")
                        val bundle = Bundle()
                        bundle.putString("categoryId", categoryId)
                        bundle.putString("title", title)
                        graph.setStartDestination(R.id.fragmentSeries)
                        navController.setGraph(graph, bundle)
                    }

                    "notification" -> {
                        graph.setStartDestination(R.id.fragmentNotification)
                        navController.setGraph(graph, null)
                    }

                    "changePassword" -> {
                        graph.setStartDestination(R.id.fragmentChangePassword)
                        navController.setGraph(graph, null)
                    }

                    "statVisibility" -> {
                        graph.setStartDestination(R.id.fragmentStatVisibility)
                        navController.setGraph(graph, null)
                    }

                    "subscription" -> {
                        graph.setStartDestination(R.id.fragmentSubscription)
                        navController.setGraph(graph, null)
                    }

                    "allVideo" -> {
                        val videoTopicId = intent.getStringExtra("videoTopicId")
                        val videoTitle = intent.getStringExtra("videoTitle")
                        val bundle = Bundle()
                        bundle.putString("videoTopicId", videoTopicId)
                        bundle.putString("videoTitle", videoTitle)
                        graph.setStartDestination(R.id.fragmentAllVideo)
                        navController.setGraph(graph, bundle)
                    }

                    "createPost" -> {
                        graph.setStartDestination(R.id.fragmentCreatePost)
                        navController.setGraph(graph, null)
                    }

                    "communityDetail" -> {
                        val communityData = intent.getParcelableExtra<PostData>("communityData")
                        val bundle = Bundle()
                        bundle.putParcelable("communityData", communityData)
                        graph.setStartDestination(R.id.fragmentCommunityDetail)
                        navController.setGraph(graph, bundle)
                    }

                    "notificationNew" -> {
                        graph.setStartDestination(R.id.fragmentNotificationNew)
                        navController.setGraph(graph, null)
                    }

                    "sessionPlanner" -> {
                        graph.setStartDestination(R.id.fragmentSessionPlanner)
                        navController.setGraph(graph, null)
                    }

                    "allSession" -> {
                        graph.setStartDestination(R.id.fragmentViewAllSession)
                        navController.setGraph(graph, null)
                    }

                    "sessionDetails" -> {
                        val nextSessionData =
                            intent.getParcelableExtra<NextSessionData>("sessionData")
                        val bundle = Bundle()
                        bundle.putParcelable("sessionData", nextSessionData)
                        graph.setStartDestination(R.id.fragmentSessionDetails)
                        navController.setGraph(graph, bundle)
                    }

                    "pastSession" -> {
                        val pastSessionData =
                            intent.getParcelableExtra<PastSessionData>("pastSessionData")
                        val bundle = Bundle()
                        bundle.putParcelable("pastSessionData", pastSessionData)


                        graph.setStartDestination(R.id.fragmentPastSession)
                        navController.setGraph(graph, bundle)
                    }

                    "addReviewSession" -> {
                        val sessionData = intent.getParcelableExtra<NextSessionData>("sessionData")
                        val bundle = Bundle()
                        bundle.putParcelable("sessionData", sessionData)
                        graph.setStartDestination(R.id.fragmentAddReviewSession)
                        navController.setGraph(graph, bundle)
                    }

                    "editProfileInfo" -> {
                        graph.setStartDestination(R.id.fragmentEditProfileInfo)
                        navController.setGraph(graph, null)
                    }

                    "editDataProfile" -> {
                        graph.setStartDestination(R.id.fragmentEditProfile)
                        navController.setGraph(graph, null)
                    }

                    "video" -> {
                        val videoUrl = intent.getStringExtra("videoUrl")
                        val bundle = Bundle()
                        bundle.putString("videoUrl", videoUrl)
                        graph.setStartDestination(R.id.fragmentVideo)
                        navController.setGraph(graph, bundle)
                    }

                    "fragmentSeeAll" -> {
                        val topicId = intent.getStringExtra("topicId")
                        val title = intent.getStringExtra("title")
                        val bundle = Bundle()
                        bundle.putString("topicId", topicId)
                        bundle.putString("title", title)
                        graph.setStartDestination(R.id.fragmentSeeAll)
                        navController.setGraph(graph, bundle)
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
