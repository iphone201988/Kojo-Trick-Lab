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
import com.example.newbase_2025.data.model.MilestonesData
import com.example.newbase_2025.data.model.TrackerData
import com.example.newbase_2025.databinding.FragmentTrackerBinding
import com.example.newbase_2025.databinding.FragmentTrickingMilestonesBinding
import com.example.newbase_2025.databinding.TrickRvLayoutItemBinding
import com.example.newbase_2025.databinding.TrickingMilestonesRvItemBinding
import com.example.newbase_2025.ui.common.CommonActivity
import com.example.newbase_2025.ui.dashboard.tracker.TrackerFragmentVM
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class TrickingMilestonesFragment : BaseFragment<FragmentTrickingMilestonesBinding>() {
    private val viewModel: TrickingMilestonesFragmentVM by viewModels()
    private lateinit var milestonesAdapter: SimpleRecyclerViewAdapter<MilestonesData, TrickingMilestonesRvItemBinding>
    override fun getLayoutResource(): Int {

        return R.layout.fragment_tricking_milestones
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // adapter
        initTrickAdapter()
        // click
        initOnClick()
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
                R.id.clStar->{
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "myStar")
                    startActivity(intent)

                }
            }
        }
    }

    /**
     * Initialize adapter
     */
    private fun initTrickAdapter() {
        milestonesAdapter =
            SimpleRecyclerViewAdapter(R.layout.tricking_milestones_rv_item, BR.bean) { v, m, _ ->
                when (v?.id) {
                    R.id.cardView -> {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "categoryChecking")
                        startActivity(intent)
                    }
                }

            }
        binding.rvMilestones.adapter = milestonesAdapter
        milestonesAdapter.list = getDummyTrickList()
    }

    /**
     * Get dummy trick list
     */
    private fun getDummyTrickList(): ArrayList<MilestonesData> {
        val dummyList = arrayListOf(
            MilestonesData(R.drawable.home_list_dummy, "Beginner",1),
            MilestonesData(R.drawable.home_list_dummy, "Intermediate",2),
            MilestonesData(R.drawable.home_list_dummy, "Advanced",3),
            MilestonesData(R.drawable.home_list_dummy, "Elite",3),
            MilestonesData(R.drawable.home_list_dummy, "Pro",3),

        )
        return dummyList
    }


}