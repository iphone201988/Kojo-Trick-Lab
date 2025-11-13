package com.example.newbase_2025.ui.dashboard.home

import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.api.Constants
import com.example.newbase_2025.data.model.MyTrickData
import com.example.newbase_2025.data.model.SubTitle
import com.example.newbase_2025.databinding.FragmentHomeBinding
import com.example.newbase_2025.databinding.HolderDummyBinding
import com.example.newbase_2025.databinding.HolderHomeBinding
import com.example.newbase_2025.ui.common.CommonActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private val viewModel: HomeVM by viewModels()
    private lateinit var homeAdapter: SimpleRecyclerViewAdapter<MyTrickData, HolderHomeBinding>
    override fun getLayoutResource(): Int {
        return R.layout.fragment_home
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // adapter
        initAdapter()

    }

    /**
     * Initialize adapter
     */
    private fun initAdapter() {
        homeAdapter = SimpleRecyclerViewAdapter(R.layout.holder_home, BR.bean) { v, m, _ ->
            when (v?.id) {
                R.id.cardView -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "forwardTrick")
                    startActivity(intent)
                }
            }

        }
        binding.rvHome.adapter = homeAdapter
        homeAdapter.list = getDummyTrickList()
    }

    /**
     * Get dummy trick list
     */
    private fun getDummyTrickList(): ArrayList<MyTrickData> {
        val dummyList = arrayListOf(
            MyTrickData(
                R.drawable.home_list_dummy, "Vertical Kicks", true, arrayListOf(
                    SubTitle("Pop"), SubTitle("Cheat"), SubTitle("Swing Kicks")
                )
            ), MyTrickData(
                R.drawable.home_list_dummy, "Forward Tricks", false, arrayListOf(
                    SubTitle("Frontflip"), SubTitle("Webster"), SubTitle("Janitor")
                )
            ), MyTrickData(
                R.drawable.home_list_dummy, "Backward Tricks", true, arrayListOf(
                    SubTitle("Backflip"),
                    SubTitle("Gainer"),
                    SubTitle("Full"),
                    SubTitle("Corkscrew ")
                )
            ), MyTrickData(
                R.drawable.home_list_dummy, "Outside Tricks", false, arrayListOf(
                    SubTitle("Raiz"),
                    SubTitle("Doubleleg"),
                    SubTitle("Lotus"),
                    SubTitle("Sideflip"),
                    SubTitle("Spyder")
                )
            ), MyTrickData(
                R.drawable.home_list_dummy, "Inside Tricks", false, arrayListOf(
                    SubTitle("Aerial"),
                    SubTitle("Butterfly"),
                    SubTitle("Masterswing"),
                    SubTitle("Tak"),
                    SubTitle("Warp")
                )
            )
        )

        return dummyList
    }

}