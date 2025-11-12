package com.example.newbase_2025.ui.dashboard.tracker.my_trick

import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.model.MyTrickData
import com.example.newbase_2025.data.model.SubTitle
import com.example.newbase_2025.databinding.FragmentMyTrickBinding
import com.example.newbase_2025.databinding.TrickRvLayoutItemBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyTrickFragment : BaseFragment<FragmentMyTrickBinding>() {
    private val viewModel: MyTrickFragmentVM by viewModels()
    private lateinit var trickAdapter: SimpleRecyclerViewAdapter<MyTrickData, TrickRvLayoutItemBinding>
    override fun getLayoutResource(): Int {

        return R.layout.fragment_my_trick
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

                    }
                }

            }
        binding.rvMyTrick.adapter = trickAdapter
        trickAdapter.list = getDummyTrickList()
    }

    /**
     * Get dummy trick list
     */
    private fun getDummyTrickList(): ArrayList<MyTrickData> {
        val dummyList = arrayListOf(
            MyTrickData(
                R.drawable.home_list_dummy, "My Tricks", true, arrayListOf(
                    SubTitle("Pop"), SubTitle("Cheat"), SubTitle("Swing Kicks")
                )
            ), MyTrickData(
                R.drawable.home_list_dummy, "Combo Goals", false, arrayListOf(
                    SubTitle("Pop"), SubTitle("Cheat"), SubTitle("Swing")
                )
            ), MyTrickData(
                R.drawable.home_list_dummy, "Session Planner", true, arrayListOf(
                    SubTitle("Pop"), SubTitle("Cheat"), SubTitle("Swing")
                )
            ), MyTrickData(
                R.drawable.home_list_dummy, "Tricking Milestones", false, arrayListOf(
                    SubTitle("Pop"), SubTitle("Cheat"), SubTitle("Swing")
                )
            ), MyTrickData(
                R.drawable.home_list_dummy, "My Stats", false, arrayListOf(
                    SubTitle("Pop"), SubTitle("Cheat"), SubTitle("Swing")
                )
            )
        )

        return dummyList
    }


}