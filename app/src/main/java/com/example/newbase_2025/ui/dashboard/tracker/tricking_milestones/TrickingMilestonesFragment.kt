package com.example.newbase_2025.ui.dashboard.tracker.tricking_milestones

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.model.TrackerData
import com.example.newbase_2025.databinding.FragmentTrackerBinding
import com.example.newbase_2025.databinding.FragmentTrickingMilestonesBinding
import com.example.newbase_2025.databinding.TrickRvLayoutItemBinding
import com.example.newbase_2025.ui.common.CommonActivity
import com.example.newbase_2025.ui.dashboard.tracker.TrackerFragmentVM
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class TrickingMilestonesFragment : BaseFragment<FragmentTrickingMilestonesBinding>() {
    private val viewModel: TrickingMilestonesFragmentVM by viewModels()
    private lateinit var trickAdapter: SimpleRecyclerViewAdapter<TrackerData, TrickRvLayoutItemBinding>
    override fun getLayoutResource(): Int {

        return R.layout.fragment_tricking_milestones
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
            SimpleRecyclerViewAdapter(R.layout.trick_rv_layout_item, BR.bean) { v, m, _ ->
                when (v?.id) {
                    R.id.cardView -> {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "myTrick")
                        startActivity(intent)
                    }
                }

            }
        //binding.rvTrick.adapter = trickAdapter
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