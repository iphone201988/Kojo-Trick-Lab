package com.example.newbase_2025.ui.dashboard.tracker.combo_goals

import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.api.Constants
import com.example.newbase_2025.data.model.CommonApiResponse
import com.example.newbase_2025.data.model.GetComboApiResponse
import com.example.newbase_2025.data.model.GetComboData
import com.example.newbase_2025.databinding.ComboGoalRvItemBinding
import com.example.newbase_2025.databinding.DeleteOrLogoutDialogItemBinding
import com.example.newbase_2025.databinding.FragmentComboGoalsBinding
import com.example.newbase_2025.ui.common.CommonActivity
import com.example.newbase_2025.utils.BaseCustomDialog
import com.example.newbase_2025.utils.BindingUtils
import com.example.newbase_2025.utils.Status
import com.example.newbase_2025.utils.showErrorToast
import com.example.newbase_2025.utils.showSuccessToast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ComboGoalsFragment : BaseFragment<FragmentComboGoalsBinding>() {
    private val viewModel: ComboGoalsFragmentVM by viewModels()
    private lateinit var comboGoalAdapter: SimpleRecyclerViewAdapter<GetComboData, ComboGoalRvItemBinding>
    private lateinit var deleteComboDialogItem: BaseCustomDialog<DeleteOrLogoutDialogItemBinding>
    private var currentPage = 1
    private var position = -1
    override fun getLayoutResource(): Int {

        return R.layout.fragment_combo_goals
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // adapter
        initComboGoalAdapter()
        // click
        initOnClick()
        // view
        initView()
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

                R.id.btnAddNew -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "addComboGals")
                    startActivity(intent)
                }

            }
        }
    }


    /**
     * Method to initialize view
     */
    private fun initView() {
        // api call
        val data = HashMap<String, Any>()
        data["page"] = currentPage
        viewModel.getComboGoalApi(data, Constants.GET_COMBO_GOALS)


        // refresh
        binding.ssPullRefresh.setColorSchemeResources(
            ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        )
        binding.ssPullRefresh.setOnRefreshListener {
            Handler().postDelayed({
                binding.ssPullRefresh.isRefreshing = false
                // api call
                val data = HashMap<String, Any>()
                data["page"] = 1
                viewModel.getComboGoalApi(data, Constants.GET_COMBO_GOALS)
            }, 2000)
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
                        "getComboGoalApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: GetComboApiResponse? = BindingUtils.parseJson(jsonData)
                                var combo = model?.data
                                if (combo != null) {
                                    comboGoalAdapter.list = combo
                                    if (comboGoalAdapter.list.isNotEmpty()) {
                                        binding.clEmpty.visibility = View.GONE
                                    } else {
                                        binding.clEmpty.visibility = View.VISIBLE
                                    }
                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }

                        "deleteComboGoalApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: CommonApiResponse? = BindingUtils.parseJson(jsonData)
                                if (model?.success == true) {
                                    comboGoalAdapter.removeItem(position)
                                    if (comboGoalAdapter.list.isNotEmpty()) {
                                        binding.clEmpty.visibility = View.GONE
                                    } else {
                                        binding.clEmpty.visibility = View.VISIBLE
                                    }
                                    comboGoalAdapter.notifyItemChanged(position)

                                    showSuccessToast(it.message.toString())
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
     * Initialize adapter
     */
    private fun initComboGoalAdapter() {
        comboGoalAdapter =
            SimpleRecyclerViewAdapter(R.layout.combo_goal_rv_item, BR.bean) { v, m, pos ->
                when (v?.id) {
                    R.id.clRemoves -> {
                        position = pos
                        if (m._id != null) {
                            deleteComboDialogItem(m._id)
                        }

                    }

                    R.id.clProgress -> {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "addNotes")
                        intent.putExtra("comboData", m)
                        startActivity(intent)
                    }
                }

            }
        binding.rvComboGoal.adapter = comboGoalAdapter

    }


    /**
     * dialog bix initialize and handel
     */
    fun deleteComboDialogItem(type: String) {
        deleteComboDialogItem = BaseCustomDialog(
            requireContext(), R.layout.delete_or_logout_dialog_item
        ) {
            when (it?.id) {
                R.id.btnDeleteCancel -> {
                    deleteComboDialogItem.dismiss()
                }

                R.id.btnDeleteComment -> {
                    viewModel.deleteComboGoalApi(Constants.COMBO_GOALS_DELETE + "/$type")
                    deleteComboDialogItem.dismiss()
                }
            }
        }
        deleteComboDialogItem.create()
        deleteComboDialogItem.show()

        deleteComboDialogItem.binding.apply {
            text.text = getString(R.string.delete_this_combo_goal)
            tvSure.text = getString(R.string.are_you_sure_you_want_to_delete_nthis_combo_goal)
            btnDeleteComment.text = getString(R.string.delete)

        }

    }
}