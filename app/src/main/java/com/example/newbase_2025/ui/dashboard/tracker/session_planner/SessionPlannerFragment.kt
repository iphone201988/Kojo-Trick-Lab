package com.example.newbase_2025.ui.dashboard.tracker.session_planner

import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.model.SessionPlannerData
import com.example.newbase_2025.databinding.FragmentSessionPlannerBinding
import com.example.newbase_2025.databinding.PastRvItemBinding
import com.example.newbase_2025.databinding.UpcomingRvItemBinding
import com.example.newbase_2025.ui.common.CommonActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SessionPlannerFragment : BaseFragment<FragmentSessionPlannerBinding>() {
    private val viewModel: SessionPlannerFragmentVM by viewModels()
    private lateinit var upcomingAdapter: SimpleRecyclerViewAdapter<SessionPlannerData, UpcomingRvItemBinding>
    private lateinit var pastAdapter: SimpleRecyclerViewAdapter<SessionPlannerData, PastRvItemBinding>
    override fun getLayoutResource(): Int {

        return R.layout.fragment_session_planner
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // adapter
        initAdapter()

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

                R.id.tvViewAll -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "allSession")
                    startActivity(intent)
                }

            }
        }
    }

    /**
     * Initialize adapter
     */
    private fun initAdapter() {
        upcomingAdapter = SimpleRecyclerViewAdapter(R.layout.upcoming_rv_item, BR.bean) { v, m, _ ->
            when (v?.id) {
                R.id.cardView -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "trainedRecently")
                    startActivity(intent)
                }
            }

        }
        binding.rvUpcoming.adapter = upcomingAdapter
        upcomingAdapter.list = getDummySessionList()

        pastAdapter = SimpleRecyclerViewAdapter(R.layout.past_rv_item, BR.bean) { v, m, _ ->
            when (v?.id) {
                R.id.cardView -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "trainedRecently")
                    startActivity(intent)
                }
            }

        }
        binding.rvPast.adapter = pastAdapter
        pastAdapter.list = getDummySessionList()
    }

    /**
     * Get dummy trick list
     */
    private fun getDummySessionList(): ArrayList<SessionPlannerData> {
        val dummyList = arrayListOf(
            SessionPlannerData(1),
            SessionPlannerData(2),
            SessionPlannerData(3),
            SessionPlannerData(4),
        )



        return dummyList
    }


}