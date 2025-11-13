package com.example.newbase_2025.ui.dashboard.tracker.combo_goals

import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.base.local.SharedPrefManager
import com.example.newbase_2025.data.model.ComboGoalsData
import com.example.newbase_2025.databinding.ComboGoalRvItemBinding
import com.example.newbase_2025.databinding.FragmentComboGoalsBinding
import com.example.newbase_2025.ui.common.CommonActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ComboGoalsFragment : BaseFragment<FragmentComboGoalsBinding>() {
    private val viewModel: ComboGoalsFragmentVM by viewModels()
    private lateinit var comboGoalAdapter: SimpleRecyclerViewAdapter<ComboGoalsData, ComboGoalRvItemBinding>

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

    }

    override fun onResume() {
        super.onResume()
        val savedGoals: List<ComboGoalsData> =
            sharedPrefManager.getList(SharedPrefManager.KEY.COMBO_LIST)
        comboGoalAdapter.list = savedGoals
        if (comboGoalAdapter.list.isNotEmpty()) {
            binding.ivProgress.visibility = View.GONE
            binding.tvEmptyCombo.visibility = View.GONE
        } else {
            binding.ivProgress.visibility = View.VISIBLE
            binding.tvEmptyCombo.visibility = View.VISIBLE
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

                R.id.btnAddNew -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "addComboGals")
                    startActivity(intent)
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
                        comboGoalAdapter.removeItem(pos)
                        val savedGoals: List<ComboGoalsData> =
                            sharedPrefManager.getList(SharedPrefManager.KEY.COMBO_LIST)
                        sharedPrefManager.saveList(SharedPrefManager.KEY.COMBO_LIST, savedGoals)
                        if (comboGoalAdapter.list.isNotEmpty()) {
                            binding.ivProgress.visibility = View.GONE
                            binding.tvEmptyCombo.visibility = View.GONE
                        } else {
                            binding.ivProgress.visibility = View.VISIBLE
                            binding.tvEmptyCombo.visibility = View.VISIBLE
                        }
                        comboGoalAdapter.notifyItemChanged(pos)

                    }

                    R.id.clProgress->{
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "addNotes")
                        startActivity(intent)
                    }
                }

            }
        binding.rvComboGoal.adapter = comboGoalAdapter
        if (comboGoalAdapter.list.isNotEmpty()) {
            binding.ivProgress.visibility = View.GONE
            binding.tvEmptyCombo.visibility = View.GONE
        } else {
            binding.ivProgress.visibility = View.VISIBLE
            binding.tvEmptyCombo.visibility = View.VISIBLE
        }
    }

}