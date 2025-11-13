package com.example.newbase_2025.ui.dashboard.tracker

import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.model.TrackerData
import com.example.newbase_2025.databinding.FragmentTrackerBinding
import com.example.newbase_2025.databinding.TrickRvLayoutItemBinding
import com.example.newbase_2025.ui.common.CommonActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TrackerFragment : BaseFragment<FragmentTrackerBinding>() {
    private val viewModel: TrackerFragmentVM by viewModels()
    private lateinit var trickAdapter: SimpleRecyclerViewAdapter<TrackerData, TrickRvLayoutItemBinding>
    override fun getLayoutResource(): Int {

        return R.layout.fragment_tracker
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // adapter
        initTrickAdapter()
    }


    /**
     * Initialize adapter
     */
    private fun initTrickAdapter() {
        trickAdapter =
            SimpleRecyclerViewAdapter(R.layout.trick_rv_layout_item, BR.bean) { v, m, pos ->
                when (pos) {
                    0 -> {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "myTrick")
                        startActivity(intent)
                    }

                    3 -> {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "trickingMilestones")
                        startActivity(intent)
                    }

                    2 ->{
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "sessionPlanner")
                        startActivity(intent)
                    }
                }

            }
        binding.rvTrick.adapter = trickAdapter
        trickAdapter.list = getDummyTrickList()
    }

    /**
     * Get dummy trick list
     */
    private fun getDummyTrickList(): ArrayList<TrackerData> {
        val dummyList = arrayListOf(
            TrackerData(R.drawable.home_list_dummy, "My Tricks"),
            TrackerData(R.drawable.home_list_dummy, "Combo Goals"),
            TrackerData(R.drawable.home_list_dummy, "Session Planner"),
            TrackerData(R.drawable.home_list_dummy, "Tricking Milestones"),
            TrackerData(R.drawable.home_list_dummy, "My Stats"),
        )
        return dummyList
    }


}