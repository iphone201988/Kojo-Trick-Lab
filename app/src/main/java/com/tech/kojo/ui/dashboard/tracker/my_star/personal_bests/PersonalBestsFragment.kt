package com.tech.kojo.ui.dashboard.tracker.my_star.personal_bests

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
import com.tech.kojo.data.model.PersonalBest
import com.tech.kojo.data.model.PersonalBestModel
import com.tech.kojo.databinding.DialogAddRepsBinding
import com.tech.kojo.databinding.FragmentPersonalBestsBinding
import com.tech.kojo.databinding.PersonalBestsRvItemBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.utils.BaseCustomDialog
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.BindingUtils.titleCaseFormattedWithSpace
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import com.tech.kojo.utils.showSuccessToast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PersonalBestsFragment : BaseFragment<FragmentPersonalBestsBinding>() {
    private val viewModel: PersonalBestsFragmentVM by viewModels()
    private lateinit var personalAdapter: SimpleRecyclerViewAdapter<PersonalBest, PersonalBestsRvItemBinding>
    private var personalBestList = ArrayList<PersonalBest>()

    //    private var popupList = ArrayList<HomeTrickVault>()
//    private var trickId: String?=null
//    private lateinit var dialogAddRepsBinding: BaseCustomDialog<DialogAddRepsBinding>
    private lateinit var dialogEditDeleteRepsBinding: BaseCustomDialog<DialogAddRepsBinding>
    private var position: Int? = null
    override fun getLayoutResource(): Int {

        return R.layout.fragment_personal_bests
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        arguments?.let {
            personalBestList = it.getParcelableArrayList("personalBestList") ?: arrayListOf()
        }
        if (personalBestList.isNotEmpty()) {
            // adapter
            initPersonalAdapter()
            binding.tvNoView.visibility = View.GONE
        } else {
            binding.tvNoView.visibility = View.VISIBLE
        }
//        // api call
//        viewModel.getHomeTrickApi(Constants.GET_TRICKS_VAULT_ALL)
        // click
        initOnClick()

        // observer
        initObserver()
//        // dialog
//        initDialog()
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
//                R.id.clSelectTrick->{
//                    BindingUtils.showDropdownModel(it, popupList) { selected ->
//                        binding.tvPracticed.text = selected.name
//                        trickId = selected?._id
//                        dialogAddRepsBinding.binding.tvTitle.text="${selected.name}"
//                        dialogAddRepsBinding.show()
//                    }
//                }

                R.id.ivNotification->{
                    val intent = Intent(requireActivity(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "notificationNew")
                    startActivity(intent)
                }

                R.id.btnSaveChanges -> {

                    val requestList = personalBestList.map {
                        HashMap<String, Any>().apply {
                            put("id", it._id ?: "")
                            put("value", it.count ?: 0)
                        }
                    }

                    Log.d("PersonalBests", "Final Request List: $requestList")
                    viewModel.updatePersonalBest(Constants.UPDATE_PERSONAL_BEST, requestList)
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
                        "updatePersonalBest" -> {
                            runCatching {
                                val jsonData = it.data.toString().orEmpty()
                                val model: PersonalBestModel? =
                                    BindingUtils.parseJson(jsonData)
                                if (model?.success == true) {
                                    showSuccessToast(model.message.toString())
                                    requireActivity().finish()
                                }
                            }.onFailure {

                            }.also { hideLoading() }
                        }
//                        "getHomeTrickApi" -> {
//                            runCatching {
//                                val jsonData = it.data?.toString().orEmpty()
//                                val model: HomeTrickApiResponse? = BindingUtils.parseJson(jsonData)
//                                var home = model?.trickVaults
//                                if (home != null) {
//                                    popupList = home as ArrayList<HomeTrickVault>
//                                }
//                            }.onFailure { e ->
//                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
//                                showErrorToast(e.message.toString())
//                            }.also {
//                                hideLoading()
//                            }
//                        }
//                        "createPersonalTrick"->{
//                            runCatching {
//                                val jsonData = it?.data?.toString().orEmpty()
//                                val model : CreatePersonalBestModel? = BindingUtils.parseJson(jsonData)
//                                if (model!=null){
//                                    if (model.personalBest!=null){
//                                        personalBestList.add(0,model.personalBest)
//                                        personalAdapter.list = personalBestList
//                                        personalAdapter.notifyDataSetChanged()
//                                        trickId = null
//                                    }
//                                }
//                            }.onFailure { e ->
//                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
//                                showErrorToast(e.message.toString())
//                            }.also {
//                                hideLoading()
//                            }
//                        }
//                        "editPersonalTrick"->{
//                            runCatching {
//                                val jsonData = it?.data?.toString().orEmpty()
//                                val model : CreatePersonalBestModel? = BindingUtils.parseJson(jsonData)
//                                if (model!=null){
//                                    if (model.personalBest!=null){
//                                        showSuccessToast("Personal Trick Updated Successfully")
//                                        val updatedItem = model.personalBest
//                                        val index = personalBestList.indexOfFirst { item -> item._id == updatedItem?._id }
//                                        if (index != -1) {
//                                            personalBestList[index] = updatedItem!!
//                                            personalAdapter.list = personalBestList
//                                            personalAdapter.notifyItemChanged(index)
//                                        }
//                                    }
//                                }
//                            }.onFailure { e ->
//                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
//                                showErrorToast(e.message.toString())
//                            }.also {
//                                hideLoading()
//                            }
//                        }
//                        "deletePersonalTrick"->{
//                            runCatching {
//                                val jsonData = it?.data?.toString().orEmpty()
//                                val model : CommonApiResponse? = BindingUtils.parseJson(jsonData)
//                                if (model!=null){
//                                    showSuccessToast("Personal Trick Deleted Successfully")
//                                    personalBestList.removeAt(position!!)
//                                    personalAdapter.list = personalBestList
//                                    personalAdapter.notifyDataSetChanged()
//                                    position = null
//                                }
//                            }.onFailure { e ->
//                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
//                                showErrorToast(e.message.toString())
//                            }.also {
//                                hideLoading()
//                            }
//                        }
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
     * Initialize adapter
     */
    private fun initPersonalAdapter() {
        personalAdapter =
            SimpleRecyclerViewAdapter(R.layout.personal_bests_rv_item, BR.bean) { v, m, pos ->
                when (v?.id) {
                    R.id.tvEdit -> {
                        position = pos
                        initEditDeleteDialog(m)
                    }
                }

            }
        binding.rvPersonalBests.adapter = personalAdapter
        personalAdapter.list = personalBestList
    }

//    private fun initDialog(){
//        dialogAddRepsBinding = BaseCustomDialog(requireContext(), R.layout.dialog_add_reps){
//            when(it?.id){
//                R.id.tvCancel->{
//                    dialogAddRepsBinding.dismiss()
//                }
//                R.id.tvSave->{
//                    if (dialogAddRepsBinding.binding.etReps.text.isNullOrEmpty()){
//                        showErrorToast("Please enter reps")
//                        return@BaseCustomDialog
//                    }
//                    val request = HashMap<String, Any>()
//                    request["trickVaultId"]=trickId.toString()
//                    request["value"]=dialogAddRepsBinding.binding.etReps.text.toString().trim()
//                    viewModel.createPersonalTrick(Constants.GET_PERSONAL_BEST,request)
//                    dialogAddRepsBinding.dismiss()
//                }
//            }
//        }
//        dialogAddRepsBinding.setCancelable(false)
//    }


    private fun initEditDeleteDialog(model: PersonalBest) {
        dialogEditDeleteRepsBinding = BaseCustomDialog(requireContext(), R.layout.dialog_add_reps) {
            when (it?.id) {
                R.id.tvCancel -> {
//                    viewModel.deletePersonalTrick("${Constants.GET_PERSONAL_BEST}/${model._id}")
                    val newValue = 0

                    position?.let { pos ->

                        val oldItem = personalBestList[pos]

                        val updatedItem = oldItem.copy(count = newValue)
                        personalBestList[pos] = updatedItem
                        personalAdapter.list = personalBestList
                        personalAdapter.notifyItemChanged(pos)
                    }
                    dialogEditDeleteRepsBinding.dismiss()
                }

//                R.id.tvSave -> {
//                    if (dialogEditDeleteRepsBinding.binding.etReps.text.isNullOrEmpty()) {
//                        showErrorToast("Please enter reps")
//                        return@BaseCustomDialog
//                    }
//                    HashMap<String, Any>()
//                    request["trickVaultId"]=model.trickVaultId?._id.toString()
//                    request["value"]=dialogEditDeleteRepsBinding.binding.etReps.text.toString().trim()
//                    viewModel.editPersonalTrick("${Constants.GET_PERSONAL_BEST}/${model._id}",request)
//                    dialogEditDeleteRepsBinding.dismiss()
//                }

                R.id.tvSave -> {

                    val input = dialogEditDeleteRepsBinding.binding.etReps.text?.toString()?.trim()

                    if (input.isNullOrEmpty()) {
                        showErrorToast("Please enter reps")
                        return@BaseCustomDialog
                    }

                    val newValue = input.toIntOrNull() ?: 0

                    position?.let { pos ->

                        val oldItem = personalBestList[pos]

                        val updatedItem = oldItem.copy(count = newValue)
                        personalBestList[pos] = updatedItem
                        personalAdapter.list = personalBestList
                        personalAdapter.notifyItemChanged(pos)
                    }

                    dialogEditDeleteRepsBinding.dismiss()
                }
            }
        }
        dialogEditDeleteRepsBinding.setCancelable(false)
        dialogEditDeleteRepsBinding.binding.tvCancel.text = "Delete"
        if (model.count!=null && model.count!=0){
            dialogEditDeleteRepsBinding.binding.etReps.setText(model.count.toString())
        }
        titleCaseFormattedWithSpace(dialogEditDeleteRepsBinding.binding.tvTitle,model.name)
        dialogEditDeleteRepsBinding.show()
    }

    override fun onResume() {
        super.onResume()
        viewModel.notificationCount.value = sharedPrefManager.getNotificationCount()
    }

}