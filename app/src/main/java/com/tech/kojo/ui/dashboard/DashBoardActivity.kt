package com.tech.kojo.ui.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.tech.kojo.BR
import com.tech.kojo.R
import com.tech.kojo.base.BaseActivity
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.base.SimpleRecyclerViewAdapter
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.CommonApiResponse
import com.tech.kojo.data.model.GetTopicCategoryData
import com.tech.kojo.data.model.TopicCategoryData
import com.tech.kojo.databinding.ActivityDashBoardBinding
import com.tech.kojo.databinding.DailogVideoMenuBinding
import com.tech.kojo.databinding.DeleteOrLogoutDialogItemBinding
import com.tech.kojo.databinding.DialogSettingsBinding
import com.tech.kojo.databinding.RvCategoryItemBinding
import com.tech.kojo.ui.auth.AuthActivity
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.ui.dashboard.community.CommunityFragment
import com.tech.kojo.ui.dashboard.home.HomeFragment
import com.tech.kojo.ui.dashboard.library.LibraryFragment
import com.tech.kojo.ui.dashboard.profile.ProfileFragment
import com.tech.kojo.ui.dashboard.tracker.TrackerFragment
import com.tech.kojo.utils.BaseCustomDialog
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.event.SingleRequestEvent
import com.tech.kojo.utils.showErrorToast
import com.tech.kojo.utils.showSuccessToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashBoardActivity : BaseActivity<ActivityDashBoardBinding>() {
    private val viewModel: DashBoardActivityVM by viewModels()
    private lateinit var commonDialog: BaseCustomDialog<DialogSettingsBinding>
    private lateinit var menuDialog: BaseCustomDialog<DailogVideoMenuBinding>
    private lateinit var categoryAdapter: SimpleRecyclerViewAdapter<TopicCategoryData, RvCategoryItemBinding>
    private lateinit var deleteOrLogoutDialogItem: BaseCustomDialog<DeleteOrLogoutDialogItemBinding>
    private var PERMISSION_REQUEST_CODE = 16
    companion object {
        var changeImage = SingleRequestEvent<Boolean>()
    }

    override fun getLayoutResource(): Int {
        return R.layout.activity_dash_board
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        // check permission
        if (Build.VERSION.SDK_INT > 32) {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                getNotificationPermission()
            }
        }
        // view
        initView()
        // setup bottom sheet
        setupBottomNav()
        binding.navHome.performClick()
        // dialog
        initDialog()
        // click
        initOnClick()
        // observer
        initObserver()
    }

    /**
     * Method to initialize view
     */
    private fun initView() {
        // set up
        setupSystemUI()
        binding.type = 1
        // status bar color change
        BindingUtils.statusBarStyleWhite(this)
        // data set
        val data = sharedPrefManager.getProfileData()
        if (data != null) {
            Glide.with(this@DashBoardActivity).load(Constants.BASE_URL_IMAGE + data.profilePicture)
                .placeholder(R.drawable.holder_dummy).error(R.drawable.holder_dummy)
                .into(binding.profileImage)
        }
    }


    override fun onResume() {
        super.onResume()
        changeImage.observe(this@DashBoardActivity) {
            when (it?.status) {
                Status.LOADING -> {

                }

                Status.SUCCESS -> {
                    // data set
                    val data = sharedPrefManager.getProfileData()
                    if (data != null) {
                        Glide.with(this@DashBoardActivity)
                            .load(Constants.BASE_URL_IMAGE + data.profilePicture)
                            .placeholder(R.drawable.holder_dummy).error(R.drawable.holder_dummy)
                            .into(binding.profileImage)
                    }
                }

                Status.ERROR -> {

                }

                else -> {

                }
            }
        }
    }

    /**
     * click handel
     */
    private fun initOnClick() {
        viewModel.onClick.observe(this, Observer {
            when (it?.id) {
                R.id.ivNotification -> {
                    val intent = Intent(this, CommonActivity::class.java)
                    intent.putExtra("fromWhere", "notificationNew")
                    startActivity(intent)
                }

                R.id.ivDrawer -> {
                    commonDialog.show()
                }

                R.id.ivDrawer1 -> {
                    // api call
                    viewModel.getAllCategory(Constants.GET_ALL_CATEGORY)

                }
            }
        })
    }
    private var currentSelectedTabId: Int = -1
    /**
     * setup bottom sheet
     */
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

                if (currentSelectedTabId == tab.id) return@setOnClickListener

                currentSelectedTabId = tab.id
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
                        showFragment(CommunityFragment())
                    }

                    R.id.nav_profile -> {
                        binding.type = 4
                        showFragment(ProfileFragment())
                    }
                }
            }
        }
    }

    /**
     * show fragment
     */
    private fun showFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()
    }

    /**
     * dialog initialize
     */
    private fun initDialog() {
        commonDialog = BaseCustomDialog(this@DashBoardActivity, R.layout.dialog_settings) {
            when (it?.id) {
                R.id.tvNotification -> {
                    val intent = Intent(this@DashBoardActivity, CommonActivity::class.java)
                    intent.putExtra("fromWhere", "notification")
                    startActivity(intent)
                    commonDialog.dismiss()
                }

                R.id.tvChangePassword -> {
                    val intent = Intent(this@DashBoardActivity, CommonActivity::class.java)
                    intent.putExtra("fromWhere", "changePassword")
                    startActivity(intent)
                    commonDialog.dismiss()
                }

                R.id.tvStatVisibility -> {
                    val intent = Intent(this@DashBoardActivity, CommonActivity::class.java)
                    intent.putExtra("fromWhere", "statVisibility")
                    startActivity(intent)
                    commonDialog.dismiss()
                }

                R.id.tvSubscription -> {
                    val intent = Intent(this@DashBoardActivity, CommonActivity::class.java)
                    intent.putExtra("fromWhere", "subscription")
                    startActivity(intent)
                    commonDialog.dismiss()
                }

                R.id.tvLogout -> {
                    deleteOrLogoutDialogItem(1)
                }

                R.id.tvDeleteAccount -> {
                    deleteOrLogoutDialogItem(2)
                }
            }
        }
        commonDialog.setCancelable(true)
    }

    /**
     * menu dialog initialize
     */
    private fun initMenuDialog(categoryList: List<TopicCategoryData>) {
        menuDialog = BaseCustomDialog(this@DashBoardActivity, R.layout.dailog_video_menu) {

        }
        menuDialog.setCancelable(true)
        menuDialog.create()
        menuDialog.show()
        // adapter
        initAdapter(categoryList)

    }


    /**
     * Initialize adapter
     */
    private fun initAdapter(categoryList: List<TopicCategoryData>) {
        categoryAdapter = SimpleRecyclerViewAdapter(R.layout.rv_category_item, BR.bean) { v, m, _ ->
            when (v?.id) {
                R.id.clCategory -> {
                    val intent = Intent(this@DashBoardActivity, CommonActivity::class.java)
                    intent.putExtra("categoryId", m._id)
                    intent.putExtra("title", m.title)
                    intent.putExtra("fromWhere", "series")
                    startActivity(intent)
                    menuDialog.dismiss()
                }
            }
        }
        menuDialog.binding.rvCategory.adapter = categoryAdapter
        categoryAdapter.list = categoryList
    }

    /** api response observer ***/
    private fun initObserver() {
        viewModel.observeCommon.observe(this@DashBoardActivity) {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()
                }

                Status.SUCCESS -> {
                    when (it.message) {
                        "deleteAccountApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: CommonApiResponse? = BindingUtils.parseJson(jsonData)
                                if (model?.success == true) {
                                    sharedPrefManager.clear()
                                    showSuccessToast(model.message.toString())
                                    val intent =
                                        Intent(this@DashBoardActivity, AuthActivity::class.java)
                                    startActivity(intent)
                                    finishAffinity()
                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }

                        "logoutApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: CommonApiResponse? = BindingUtils.parseJson(jsonData)
                                if (model?.success == true) {
                                    sharedPrefManager.clear()
                                    showSuccessToast(model.message.toString())
                                    val intent =
                                        Intent(this@DashBoardActivity, AuthActivity::class.java)
                                    startActivity(intent)
                                    finishAffinity()
                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }

                        "getAllCategory" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: GetTopicCategoryData? = BindingUtils.parseJson(jsonData)
                                if (model?.success == true && model.data?.isNotEmpty() == true) {
                                    initMenuDialog(model.data as List<TopicCategoryData>)
                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }
                    }
                }

                Status.ERROR -> {
                    hideLoading()
                    showErrorToast(it.message.toString())
                }

                else -> {

                }
            }
        }
    }

    /**
     * dialog bix initialize and handel
     */
    fun deleteOrLogoutDialogItem(type: Int) {
        deleteOrLogoutDialogItem = BaseCustomDialog(
            this@DashBoardActivity, R.layout.delete_or_logout_dialog_item
        ) {
            when (it?.id) {
                R.id.btnDeleteCancel -> {
                    deleteOrLogoutDialogItem.dismiss()
                }

                R.id.btnDeleteComment -> {
                    when (type) {
                        1 -> {
                            val data = HashMap<String, Any>()
                            viewModel.logoutApi(data, Constants.LOGOUT)
                        }

                        else -> {
                            val data = HashMap<String, Any>()
                            viewModel.deleteAccountApi(Constants.DELETE_ACCOUNT, data)
                        }
                    }
                    deleteOrLogoutDialogItem.dismiss()
                }
            }
        }
        deleteOrLogoutDialogItem.create()
        deleteOrLogoutDialogItem.show()

        when (type) {
            1 -> {
                deleteOrLogoutDialogItem.binding.apply {
                    text.text = getString(R.string.log_out)
                    tvSure.text = getString(R.string.are_you_sure_you_want_to_logout)
                    btnDeleteComment.text = getString(R.string.log_out)
                }
            }

            else -> {
                deleteOrLogoutDialogItem.binding.apply {
                    text.text = getString(R.string.delete_my_account)
                    tvSure.text = getString(R.string.are_you_sure_you_want_to_delete_nthis_account)
                    btnDeleteComment.text = getString(R.string.delete)
                }
            }
        }
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


    private fun getNotificationPermission() {
        try {
            if (Build.VERSION.SDK_INT > 32) {
                ActivityCompat.requestPermissions(
                    this@DashBoardActivity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), PERMISSION_REQUEST_CODE
                )
            }
        } catch (e: Exception) {
            Log.e("fsds", "setUpObserver: $e")
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                } else {
                    showErrorToast("Notification Permission Denied")
                }
            }
        }
    }

    override fun onBackPressed() {
        when (currentSelectedTabId) {
            R.id.nav_home -> {
                super.onBackPressed()
            }
            else -> {
                binding.navHome.performClick()
            }
        }
    }

}