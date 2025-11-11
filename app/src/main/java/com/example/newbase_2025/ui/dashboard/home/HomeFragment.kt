package com.example.newbase_2025.ui.dashboard.home

import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.model.DummyHome
import com.example.newbase_2025.databinding.FragmentHomeBinding
import com.example.newbase_2025.databinding.HolderDummyBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private val viewModel: HomeVM by viewModels()

    override fun getLayoutResource(): Int {
        return R.layout.fragment_home
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        initAdapter()
    }

    private lateinit var adapter: SimpleRecyclerViewAdapter<DummyHome, HolderDummyBinding>
    private fun initAdapter() {
        adapter = SimpleRecyclerViewAdapter(R.layout.holder_home, BR.bean) { _, m, _ ->

        }
        binding.rvHome.adapter = adapter
        adapter.list = getDummyHomeList()
    }

    private fun getDummyHomeList(): ArrayList<DummyHome> {
        val dummyList = arrayListOf(
            DummyHome(R.drawable.home_list_dummy),
            DummyHome(R.drawable.home_list_dummy),
            DummyHome(R.drawable.home_list_dummy),
            DummyHome(R.drawable.home_list_dummy)
        )
        return dummyList
    }


}