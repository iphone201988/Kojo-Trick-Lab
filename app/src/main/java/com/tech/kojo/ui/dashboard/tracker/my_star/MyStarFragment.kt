package com.tech.kojo.ui.dashboard.tracker.my_star

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.tech.kojo.BR
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.base.SimpleRecyclerViewAdapter
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.GetProfileResponse
import com.tech.kojo.databinding.AddFavouriteTrickBottomSheetBinding
import com.tech.kojo.databinding.FragmentMyStarBinding
import com.tech.kojo.databinding.PersonalDialogItemBinding
import com.tech.kojo.databinding.UnPinLayoutBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.utils.BaseCustomBottomSheet
import com.tech.kojo.utils.BaseCustomDialog
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import com.tech.kojo.utils.showInfoToast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MyStarFragment : BaseFragment<FragmentMyStarBinding>() {
    private val viewModel: MyStarFragmentVM by viewModels()
    private var skin: String? = null
    private lateinit var updateBottomSheet: BaseCustomBottomSheet<AddFavouriteTrickBottomSheetBinding>
    private var commonDialog: BaseCustomDialog<PersonalDialogItemBinding>? = null
    private lateinit var commonAdapter: SimpleRecyclerViewAdapter<String, UnPinLayoutBinding>

    override fun getLayoutResource(): Int {

        return R.layout.fragment_my_star
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        initOnClick()
        // api call
        viewModel.getProfileApi(Constants.GET_PROFILE)
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

                R.id.btnSeeAll -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "personalBests")
                    startActivity(intent)
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
                                    binding.ivCircle1.visibility = View.GONE
                                    binding.ivCircle.visibility = View.VISIBLE
                                    binding.ivProfile.visibility = View.VISIBLE

                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }

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

}