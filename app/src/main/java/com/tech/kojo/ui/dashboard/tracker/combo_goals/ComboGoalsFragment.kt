package com.tech.kojo.ui.dashboard.tracker.combo_goals

import android.content.Intent
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.tech.kojo.BR
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.base.SimpleRecyclerViewAdapter
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.CommonApiResponse
import com.tech.kojo.data.model.GetComboApiResponse
import com.tech.kojo.data.model.GetComboData
import com.tech.kojo.databinding.ComboGoalRvItemBinding
import com.tech.kojo.databinding.DeleteOrLogoutDialogItemBinding
import com.tech.kojo.databinding.FragmentComboGoalsBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.utils.BaseCustomDialog
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import com.tech.kojo.utils.showSuccessToast
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

                R.id.btnAddNew ,R.id.btnAddNew2 -> {
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
                                        binding.btnAddNew.visibility = View.VISIBLE
                                    } else {
                                        binding.clEmpty.visibility = View.VISIBLE
                                        binding.btnAddNew.visibility = View.GONE
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
                                        binding.btnAddNew.visibility = View.VISIBLE
                                    } else {
                                        binding.clEmpty.visibility = View.VISIBLE
                                        binding.btnAddNew.visibility = View.GONE
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
                    R.id.tvShowMore -> {
                        m.isExpanded = !m.isExpanded
                        comboGoalAdapter.notifyItemChanged(pos,null)
                    }
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
            text.text = getString(R.string.remove_this_combo_goal)
            tvSure.text = getString(R.string.are_you_sure_you_want_to_remove_nthis_combo_goal)
            btnDeleteComment.text = getString(R.string.remove)

        }
    }

    override fun onResume() {
        super.onResume()
        // api call
        val data = HashMap<String, Any>()
        data["page"] = currentPage
        viewModel.getComboGoalApi(data, Constants.GET_COMBO_GOALS)
    }
}