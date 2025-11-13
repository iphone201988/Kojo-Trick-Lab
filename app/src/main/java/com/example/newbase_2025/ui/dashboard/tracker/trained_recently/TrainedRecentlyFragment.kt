package com.example.newbase_2025.ui.dashboard.tracker.trained_recently

import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.base.local.SharedPrefManager
import com.example.newbase_2025.data.model.RecentData
import com.example.newbase_2025.databinding.FragmentTrainedRecentlyBinding
import com.example.newbase_2025.databinding.TrainedRecentlyRvItemBinding
import com.example.newbase_2025.ui.common.CommonActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TrainedRecentlyFragment : BaseFragment<FragmentTrainedRecentlyBinding>() {
    private val viewModel: TrainedRecentlyFragmentVM by viewModels()
    private lateinit var trainedRecentAdapter: SimpleRecyclerViewAdapter<RecentData, TrainedRecentlyRvItemBinding>
    override fun getLayoutResource(): Int {

        return R.layout.fragment_trained_recently
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        binding.check = 1
        // adapter
        initTrickAdapter()
        // click
        initOnClick()
        // clear
        sharedPrefManager.clearList(SharedPrefManager.KEY.COMBO_LIST)
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

                R.id.tvProgress -> {
                    binding.check = 1
                    trainedRecentAdapter.list = getDummyRecentList()
                }

                R.id.tvTrack -> {
                    binding.check = 2
                    val filteredList = getDummyRecentList().filter { item -> item.check == 1 }
                    trainedRecentAdapter.list = ArrayList(filteredList)
                }
            }
        }
    }

    /**
     * Initialize adapter
     */
    private fun initTrickAdapter() {
        trainedRecentAdapter =
            SimpleRecyclerViewAdapter(R.layout.trained_recently_rv_item, BR.bean) { v, m, _ ->
                when (v?.id) {
                    R.id.clRecent -> {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "progressionDetails")
                        startActivity(intent)
                    }
                }

            }
        binding.rvTrainedRecently.adapter = trainedRecentAdapter
        trainedRecentAdapter.list = getDummyRecentList()
    }

    /**
     * Get dummy recent list
     */
    private fun getDummyRecentList(): ArrayList<RecentData> {
        val dummyList = arrayListOf(
            RecentData(R.drawable.home_list_dummy, "Dive Roll", 2),
            RecentData(R.drawable.home_list_dummy, "Webster Half", 1),
            RecentData(R.drawable.home_list_dummy, "Fount X-out", 1),
            RecentData(R.drawable.home_list_dummy, "Super Man Frount", 2),

            )

        return dummyList
    }


}