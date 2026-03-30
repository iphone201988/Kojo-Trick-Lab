package com.tech.kojo.ui.dashboard.tracker.my_star

import android.content.Intent
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tech.kojo.BR
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.base.SimpleRecyclerViewAdapter
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.GetPersonalBestModel
import com.tech.kojo.data.model.GetPersonalBestModelData
import com.tech.kojo.data.model.GetProfileResponse
import com.tech.kojo.data.model.PersonalBest
import com.tech.kojo.databinding.AddFavouriteTrickBottomSheetBinding
import com.tech.kojo.databinding.DeleteOrLogoutDialogItemBinding
import com.tech.kojo.databinding.FragmentMyStarBinding
import com.tech.kojo.databinding.PersonalDialogItemBinding
import com.tech.kojo.databinding.UnPinLayoutBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.utils.BaseCustomBottomSheet
import com.tech.kojo.utils.BaseCustomDialog
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.BindingUtils.titleCaseFormattedWithSpace
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import com.tech.kojo.utils.showInfoToast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MyStarFragment : BaseFragment<FragmentMyStarBinding>() {
    private val viewModel: MyStarFragmentVM by viewModels()
    private lateinit var updateBottomSheet: BaseCustomBottomSheet<AddFavouriteTrickBottomSheetBinding>
    private var commonDialog: BaseCustomDialog<PersonalDialogItemBinding>? = null
    private lateinit var commonAdapter: SimpleRecyclerViewAdapter<String, UnPinLayoutBinding>
    private var isPrivate: Boolean? = false
    private lateinit var updateProfileStatusDialog: BaseCustomDialog<DeleteOrLogoutDialogItemBinding>
    private var personalBestList = ArrayList<PersonalBest>()

    private var favTrick:String?=null
    private var bestTrick:String?=null

    override fun getLayoutResource(): Int {

        return R.layout.fragment_my_star
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        initOnClick()
        // observer
        initObserver()
    }


    /**
     * Method to initialize click
     */
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().finish()
                }
                R.id.ivNotification->{
                    val intent = Intent(requireActivity(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "notificationNew")
                    startActivity(intent)
                }
                R.id.btnSeeAll -> {
                    if (personalBestList!=null) {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "personalBests")
                        intent.putExtra("personalBestList", personalBestList)
                        startActivity(intent)
                    }
                }

                R.id.ivEditBg -> {
                    commonDialog()
                }

                R.id.ivRollEdit -> {
                    initBottomSheet(1)
                }

                R.id.ivTwistEdit -> {
                    initBottomSheet(2)
                }

                R.id.clProgress -> {
                    updateProfileStatus()
                }

            }
        }
    }


    /** api response observer ***/
    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()
                }

                Status.SUCCESS -> {
                    when (it.message) {
                        "getProfileApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: GetProfileResponse? = BindingUtils.parseJson(jsonData)
                                if (model != null) {
                                    var profile = model.user
                                    sharedPrefManager.setLoginData(profile)
                                    binding.bean = model
                                    favTrick = profile?.favouriteTrick
                                    bestTrick = profile?.bestTrick
                                    isPrivate = profile?.isPrivate
                                    binding.ivCircle1.visibility = View.GONE
                                    binding.ivCircle.visibility = View.VISIBLE
                                    binding.ivProfile.visibility = View.VISIBLE
                                    personalBestList = profile?.personalBest as ArrayList<PersonalBest>
                                    // Find and display top personal best
                                    val topPersonalBest = findTopPersonalBest(personalBestList)
                                    updateTopPersonalBestDisplay(topPersonalBest)
                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }

//                        "getPersonalBest" -> {
//                            runCatching {
//                                val jsonData = it.data?.toString().orEmpty()
//                                val model: GetPersonalBestModel? = BindingUtils.parseJson(jsonData)
//                                if (model != null) {
//                                    if (!model.personalBests.isNullOrEmpty()) {
//                                        personalBestList = model.personalBests as ArrayList<GetPersonalBestModelData>
//
//                                        // Find and display top personal best
//                                        val topPersonalBest = findTopPersonalBest(personalBestList)
//                                        updateTopPersonalBestDisplay(topPersonalBest)
//                                    }
//                                }
//                            }.onFailure { e ->
//                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
//                                showErrorToast(e.message.toString())
//                            }.also {
//                                viewModel.getProfileApi(Constants.GET_PROFILE)
//                            }
//                        }

                        "editProfileApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: GetProfileResponse? = BindingUtils.parseJson(jsonData)
                                if (model != null) {
                                    // api call
                                    viewModel.getProfileApi(Constants.GET_PROFILE)
                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
                            }.also {

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
     * Find the top personal best (highest value, latest if tie)
     */
    private fun findTopPersonalBest(personalBests: List<PersonalBest>?): PersonalBest? {
        if (personalBests.isNullOrEmpty()) return null

        return personalBests.maxByOrNull { it.count ?: 0 }
    }

    /**
     * Update UI with top personal best
     */
    private fun updateTopPersonalBestDisplay(topPersonalBest: PersonalBest?) {
        if (topPersonalBest != null) {
            val trickName = topPersonalBest.name ?: "Unknown Trick"
            val value = topPersonalBest.count ?: 0
            titleCaseFormattedWithSpace(binding.tvKick,trickName)
            binding.tvReps.text = "$value reps"

        }
    }


    /**
     * Initialize bottom sheet
     */
    private fun initBottomSheet(type: Int) {
        updateBottomSheet = BaseCustomBottomSheet(
            requireContext(), R.layout.add_favourite_trick_bottom_sheet
        ) { view ->
            when (view?.id) {
                R.id.btnSave -> {
                    if (type == 1) {
                        val enterNote = updateBottomSheet.binding.etEnterNote.text.toString().trim()
                        if (enterNote.isEmpty()) {
                            showInfoToast("Please add favourite Trick")
                        } else {
                            val data = HashMap<String, Any>()
                            data["favouriteTrick"] = enterNote
                            viewModel.editProfileApi(Constants.UPDATE_PROFILE, data)
                        }
                    } else {
                        val enterNote = updateBottomSheet.binding.etEnterNote.text.toString().trim()
                        if (enterNote.isEmpty()) {
                            showInfoToast("Please add best Trick")
                        } else {
                            val data = HashMap<String, Any>()
                            data["bestTrick"] = enterNote
                            viewModel.editProfileApi(Constants.UPDATE_PROFILE, data)

                        }
                    }
                    updateBottomSheet.dismiss()

                }
            }
        }
        updateBottomSheet.behavior.isDraggable = true
        updateBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
            if (type == 2) {
                updateBottomSheet.binding.tvTitle.text = "Update Best Trick:"
            } else {
                updateBottomSheet.binding.tvTitle.text = "Update Favourite Trick:"
        }
        updateBottomSheet.show()

    }

    /** common dialog  handel ***/
    private fun commonDialog() {
        commonDialog = BaseCustomDialog(requireActivity(), R.layout.personal_dialog_item) {

        }
        commonDialog!!.create()
        commonDialog!!.show()
        // adapter
        initCommonAdapter()
    }

    /** handle adapter **/
    private fun initCommonAdapter() {
        commonAdapter = SimpleRecyclerViewAdapter(R.layout.un_pin_layout, BR.bean) { v, m, pos ->
            when (v?.id) {
                R.id.consMainUnPin -> {
                    val data = HashMap<String, Any>()
                    data["skin"] = m.toString()
                    viewModel.editProfileApi(Constants.UPDATE_PROFILE, data)
                    commonDialog?.dismiss()
                }
            }
        }
        commonAdapter.list = commonList()

        commonDialog?.binding?.rvCommon?.adapter = commonAdapter
    }


    private fun commonList(): ArrayList<String> {
        return arrayListOf(
            "Skin 1",
            "Skin 2",
            "Skin 3",
            "Skin 4",
            "Skin 5",
            "Skin 6",
        )
    }

    private fun updateProfileStatus() {
        updateProfileStatusDialog = BaseCustomDialog(
            requireActivity(), R.layout.delete_or_logout_dialog_item
        ) {
            when (it?.id) {
                R.id.btnDeleteCancel -> {
                    updateProfileStatusDialog.dismiss()
                }

                R.id.btnDeleteComment -> {
                    val data = HashMap<String, Any>()
                    isPrivate?.let { it1 -> data["isPrivate"] = !it1 }
                    viewModel.editProfileApi(Constants.UPDATE_PROFILE, data)
                    updateProfileStatusDialog.dismiss()
                }
            }
        }
        updateProfileStatusDialog.create()
        updateProfileStatusDialog.show()
        updateProfileStatusDialog.binding.apply {
            val status = if (isPrivate == true) "Public" else "Private"
            text.text = "Update Profile Status"
            val fullText = "Would you like to update your \nprofile status as $status ?"

            val spannable = SpannableString(fullText)

            val start = fullText.indexOf(status)
            val end = start + status.length

            spannable.setSpan(
                StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            spannable.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.colorPrimary)),
                start,
                end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            tvSure.text = spannable
            btnDeleteComment.text = "Change"
            btnDeleteComment.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorPrimary
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.notificationCount.value = sharedPrefManager.getNotificationCount()
//        viewModel.getPersonalBest(Constants.GET_PERSONAL_BEST)
        viewModel.getProfileApi(Constants.GET_PROFILE)
    }

}