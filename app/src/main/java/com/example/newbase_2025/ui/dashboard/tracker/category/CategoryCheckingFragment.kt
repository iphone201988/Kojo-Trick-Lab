package com.example.newbase_2025.ui.dashboard.tracker.category

import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.model.CategoryData
import com.example.newbase_2025.databinding.CatgoryRvItemBinding
import com.example.newbase_2025.databinding.FragmentCategoryCheckingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryCheckingFragment : BaseFragment<FragmentCategoryCheckingBinding>() {
    private val viewModel: CategoryCheckingFragmentVM by viewModels()
    private lateinit var categoryAdapter: SimpleRecyclerViewAdapter<CategoryData, CatgoryRvItemBinding>
    override fun getLayoutResource(): Int {

        return R.layout.fragment_category_checking
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
    private var check = false
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().finish()
                }

                R.id.ivUnSelected -> {
                    check = !check
                    if (check) {
                        // Select all
                        binding.ivUnSelected.setImageResource(R.drawable.bg_complete)
                        categoryAdapter.list.forEach { it.check = true }
                    } else {
                        // Unselect all
                        binding.ivUnSelected.setImageResource(R.drawable.unselected_box)
                        categoryAdapter.list.forEach { it.check = false }
                    }

                    categoryAdapter.notifyDataSetChanged()
                }

            }
        }
    }

    /**
     * Initialize adapter
     */
    private fun initTrickAdapter() {
        categoryAdapter = SimpleRecyclerViewAdapter(R.layout.catgory_rv_item, BR.bean) { v, m, _ ->
            when (v?.id) {
                R.id.clCategory -> {
                    m.check = !m.check
                    categoryAdapter.notifyDataSetChanged()
                }

            }

        }
        binding.rvCategory.adapter = categoryAdapter
        categoryAdapter.list = getDummyTrickList()
    }

    /**
     * Get dummy trick list
     */
    private fun getDummyTrickList(): ArrayList<CategoryData> {
        val dummyList = arrayListOf(
            CategoryData("Dive Roll", true),
            CategoryData("Dive Roll", true),
            CategoryData("Dive Roll", true),
            CategoryData("Dive Roll", false),
            CategoryData("Dive Roll", false),

            )
        return dummyList
    }


}