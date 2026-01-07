package com.example.newbase_2025.ui.dashboard.session_detail

import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.data.api.Constants
import com.example.newbase_2025.data.model.CommonApiResponse
import com.example.newbase_2025.data.model.CreateSessionApiResponse
import com.example.newbase_2025.data.model.NextSessionData
import com.example.newbase_2025.data.model.PastSessionData
import com.example.newbase_2025.databinding.AddSessionBottomItemBinding
import com.example.newbase_2025.databinding.ChooseColorDailogItemBinding
import com.example.newbase_2025.databinding.DeleteOrLogoutDialogItemBinding
import com.example.newbase_2025.databinding.FragmentSessionDetailBinding
import com.example.newbase_2025.ui.common.CommonActivity
import com.example.newbase_2025.utils.BaseCustomBottomSheet
import com.example.newbase_2025.utils.BaseCustomDialog
import com.example.newbase_2025.utils.BindingUtils
import com.example.newbase_2025.utils.Status
import com.example.newbase_2025.utils.showErrorToast
import com.example.newbase_2025.utils.showInfoToast
import com.example.newbase_2025.utils.showSuccessToast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class SessionDetailFragment : BaseFragment<FragmentSessionDetailBinding>() {
    private val viewModel: SessionDetailVm by viewModels()
    private lateinit var pastSessionDialogItem: BaseCustomDialog<DeleteOrLogoutDialogItemBinding>
    private lateinit var chooseColorDialog: BaseCustomDialog<ChooseColorDailogItemBinding>
    private lateinit var addSessionBottomSheet: BaseCustomBottomSheet<AddSessionBottomItemBinding>
    private var sessionId: String? = null
    private var colorCode = "red"
    private var dateType = ""
    private  var  nextData: NextSessionData ?=null

    override fun getLayoutResource(): Int {
        return R.layout.fragment_session_detail
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        initOnClick()
        // view
        initView()
        // observer
        initObserver()

    }

    /**
     * Method to initialize view
     */
    private fun initView() {
        // get data
        val pastData = arguments?.getParcelable<NextSessionData>("sessionData")
        if (pastData != null) {
            sessionId = pastData._id
            binding.bean = pastData
            nextData = pastData
        }

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

                R.id.tvDeleteSession -> {
                    deleteSessionDialogItem()
                }

                R.id.btnAddSession -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "addReviewSession")
                    intent.putExtra("sessionData", nextData)
                    startActivity(intent)
                }

                R.id.tvEditSession -> {
                    initBottomSheet()
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
                        "deleteSessionApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: CommonApiResponse? = BindingUtils.parseJson(jsonData)
                                if (model?.success == true) {
                                    showSuccessToast(model.message.toString())
                                    requireActivity().finish()
                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }

                        "updateSessionPlannerApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: CreateSessionApiResponse? =
                                    BindingUtils.parseJson(jsonData)
                                var update = model?.data
                                if (update != null) {
                                    sessionId = update._id
                                    binding.bean = update
                                    nextData = update
                                    showSuccessToast("Session updated successfully")
                                    addSessionBottomSheet.dismiss()
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
    fun deleteSessionDialogItem() {
        pastSessionDialogItem = BaseCustomDialog(
            requireContext(), R.layout.delete_or_logout_dialog_item
        ) {
            when (it?.id) {
                R.id.btnDeleteCancel -> {
                    pastSessionDialogItem.dismiss()
                }

                R.id.btnDeleteComment -> {
                    viewModel.deleteSessionApi(Constants.SESSION_PLANNER_DELETE+"/$sessionId")
                    pastSessionDialogItem.dismiss()
                }
            }
        }
        pastSessionDialogItem.create()
        pastSessionDialogItem.show()

        pastSessionDialogItem.binding.apply {
            text.text = getString(R.string.delete_this_session)
            tvSure.text = getString(R.string.are_you_sure_you_want_to_delete_this_session)
            btnDeleteComment.text = getString(R.string.delete)

        }

    }

    /**
     * Initialize bottom sheet
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun initBottomSheet() {
        addSessionBottomSheet = BaseCustomBottomSheet(
            requireContext(), R.layout.add_session_bottom_item
        ) { view ->
            when (view?.id) {
                R.id.ivSelectColor -> {
                    initDialog()
                }

                R.id.btnSave -> {
                    val sessionTitle =
                        addSessionBottomSheet.binding.etSessionTitle.text.toString().trim()
                    val enterNote = addSessionBottomSheet.binding.etEnterNote.text.toString().trim()
                    if (sessionTitle.isEmpty()) {
                        showInfoToast("Please add session title")
                    } else if (enterNote.isEmpty()) {
                        showInfoToast("Please add note")
                    } else {
                        val data = HashMap<String, Any>()
                        data["title"] = sessionTitle
                        data["note"] = enterNote
                        data["color"] = colorCode
                        if (nextData!=null){
                            viewModel.updateSessionPlannerApi(Constants.SESSION_PLANNER_UPDATE+"/${nextData!!._id}", data)
                        }


                    }

                }
            }
        }
        addSessionBottomSheet.behavior.isDraggable = true
        addSessionBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        addSessionBottomSheet.show()

        addSessionBottomSheet.binding.btnSave.text = getString(R.string.update_session)
        addSessionBottomSheet.binding.tvTitle.text = getString(R.string.update_session_for)

        if (nextData!=null){
            dateType = nextData!!.date.toString()
            colorCode = nextData!!.color.toString()
            addSessionBottomSheet.binding.bean = nextData
        }
    }
    /**
     * choose color dialog initialize
     */
    private fun initDialog() {
        chooseColorDialog = BaseCustomDialog(requireContext(), R.layout.choose_color_dailog_item) {
            when (it?.id) {
                R.id.tvRed -> {
                    colorCode = "red"
                    addSessionBottomSheet.binding.ivSelectColor.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(), R.color.red_color
                        )
                    )
                    chooseColorDialog.dismiss()
                }

                R.id.tvGreen -> {
                    colorCode = "green"
                    addSessionBottomSheet.binding.ivSelectColor.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(), R.color.green_color
                        )
                    )
                    chooseColorDialog.dismiss()
                }

                R.id.tvOrange -> {
                    colorCode = "orange"
                    addSessionBottomSheet.binding.ivSelectColor.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(), R.color.orange
                        )
                    )
                    chooseColorDialog.dismiss()
                }

                R.id.tvBlue -> {
                    colorCode = "blue"
                    addSessionBottomSheet.binding.ivSelectColor.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(), R.color.purple
                        )
                    )
                    chooseColorDialog.dismiss()
                }

                R.id.tvCancel -> {
                    colorCode = "red"
                    chooseColorDialog.dismiss()
                }


            }
        }
        chooseColorDialog.setCancelable(true)
        chooseColorDialog.create()
        chooseColorDialog.show()

    }

}