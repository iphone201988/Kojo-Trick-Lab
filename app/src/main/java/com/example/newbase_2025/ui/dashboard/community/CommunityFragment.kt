package com.example.newbase_2025.ui.dashboard.community

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
import com.example.newbase_2025.databinding.FragmentCommunityBinding
import com.example.newbase_2025.databinding.ItemLayoutCommunityBinding
import com.example.newbase_2025.ui.common.CommonActivity
import com.example.newbase_2025.ui.dashboard.profile.ProfileVM
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue


@AndroidEntryPoint
class CommunityFragment : BaseFragment<FragmentCommunityBinding>() {

    private val viewModel: CommunityVm by viewModels()

    private lateinit var communityAdapter : SimpleRecyclerViewAdapter<String, ItemLayoutCommunityBinding>

    override fun getLayoutResource(): Int {
        return R.layout.fragment_community
    }

    override fun getViewModel(): BaseViewModel {
       return viewModel
    }

    override fun onCreateView(view: View) {
        binding.check = 1
        initOnClick()
        initAdapter()

    }

    private fun initAdapter() {
        communityAdapter = SimpleRecyclerViewAdapter(R.layout.item_layout_community, BR.bean){v, m,pos ->
            when(v.id){
                R.id.cardView ->{
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "community_detail")
                    startActivity(intent)
                }
            }
        }
        binding.rvCommunity.adapter = communityAdapter
        communityAdapter.list = listOf<String>("","","","")
    }


    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {


                R.id.tvNewest -> {
                    binding.check = 1
                }

                R.id.tvCOmpleteTask -> {
                    binding.check = 2
                }

                R.id.tvPinedPost ->{
                    binding.check  = 3
                }
                R.id.createPost ->{
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "createPost")
                    startActivity(intent)
                }
            }
        }
    }

}