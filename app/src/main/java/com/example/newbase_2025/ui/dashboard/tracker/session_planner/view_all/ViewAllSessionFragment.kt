package com.example.newbase_2025.ui.dashboard.tracker.session_planner.view_all

import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.model.SessionPlannerData
import com.example.newbase_2025.databinding.FragmentViewAllSessionBinding
import com.example.newbase_2025.databinding.UpcomingRvItemBinding
import com.example.newbase_2025.ui.common.CommonActivity
import com.example.newbase_2025.utils.showInfoToast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ViewAllSessionFragment : BaseFragment<FragmentViewAllSessionBinding>() {
    private val viewModel: ViewAllSessionFragmentVM by viewModels()
    private lateinit var upcomingAdapter: SimpleRecyclerViewAdapter<SessionPlannerData, UpcomingRvItemBinding>

    override fun getLayoutResource(): Int {
        return R.layout.fragment_view_all_session
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
                    upcomingAdapter.list = emptyList()
                    upcomingAdapter.notifyDataSetChanged()
                    binding.tvEmpty.visibility = View.VISIBLE

                    // Optional: Show a toast or log
                    showInfoToast("All items deleted")
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