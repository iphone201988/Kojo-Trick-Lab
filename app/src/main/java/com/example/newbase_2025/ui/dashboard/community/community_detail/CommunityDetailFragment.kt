package com.example.newbase_2025.ui.dashboard.community.community_detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleAdapter
import androidx.fragment.app.viewModels
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.databinding.FragmentCommunityDetailBinding
import com.example.newbase_2025.databinding.ItemLayoutCommentsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommunityDetailFragment : BaseFragment<FragmentCommunityDetailBinding>() {


    private lateinit  var commentAdapter : SimpleRecyclerViewAdapter<String, ItemLayoutCommentsBinding>
    private val viewModel :  CommunityDetailVm by viewModels()


    override fun getLayoutResource(): Int {
         return R.layout.fragment_community_detail
    }

    override fun getViewModel(): BaseViewModel {
         return viewModel
     }

    override fun onCreateView(view: View) {
        initAdapter()

    }


    private fun initAdapter() {
        commentAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_comments, BR.bean){v, m,pos ->

        }
        binding.rvComments.adapter = commentAdapter
        commentAdapter.list = listOf<String>("","","","")
    }


}