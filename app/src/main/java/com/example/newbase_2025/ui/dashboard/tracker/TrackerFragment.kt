package com.example.newbase_2025.ui.dashboard.tracker

import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.model.DummyHome
import com.example.newbase_2025.data.model.TrickData
import com.example.newbase_2025.databinding.FragmentTrackerBinding
import com.example.newbase_2025.databinding.HolderDummyBinding
import com.example.newbase_2025.databinding.TrickRvLayoutItemBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TrackerFragment : BaseFragment<FragmentTrackerBinding>() {
    private val viewModel: TrackerFragmentVM by viewModels()
    private lateinit var trickAdapter: SimpleRecyclerViewAdapter<TrickData, TrickRvLayoutItemBinding>
    override fun getLayoutResource(): Int {
        return R.layout.fragment_tracker
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // adapter
        initAdapter()
    }


    private fun initAdapter() {
        trickAdapter = SimpleRecyclerViewAdapter(R.layout.trick_rv_layout_item, BR.bean) { v, m, _ ->
            when(v?.id){
                R.id.cardView->{

                }
            }

        }
        binding.rvTrick.adapter = trickAdapter
        trickAdapter.list = getDummyTrickList()
    }

    private fun getDummyTrickList(): ArrayList<TrickData> {
        val dummyList = arrayListOf(
            TrickData(R.drawable.home_list_dummy,"My Tricks"),
            TrickData(R.drawable.home_list_dummy,"Combo Goals"),
            TrickData(R.drawable.home_list_dummy,"Session Planner"),
            TrickData(R.drawable.home_list_dummy,"Tricking Milestones"),
            TrickData(R.drawable.home_list_dummy,"My Stats"),
        )
        return dummyList
    }


}