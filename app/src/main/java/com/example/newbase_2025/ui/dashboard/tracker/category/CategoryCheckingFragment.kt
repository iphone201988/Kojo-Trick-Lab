package com.example.newbase_2025.ui.dashboard.tracker.category

import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.api.Constants
import com.example.newbase_2025.data.model.CategoryTrick
import com.example.newbase_2025.data.model.CommonApiResponse
import com.example.newbase_2025.data.model.GetCategoryByIdApiResponse
import com.example.newbase_2025.databinding.CatgoryRvItemBinding
import com.example.newbase_2025.databinding.FragmentCategoryCheckingBinding
import com.example.newbase_2025.utils.BindingUtils
import com.example.newbase_2025.utils.Status
import com.example.newbase_2025.utils.showErrorToast
import com.example.newbase_2025.utils.showSuccessToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryCheckingFragment : BaseFragment<FragmentCategoryCheckingBinding>() {
    private val viewModel: CategoryCheckingFragmentVM by viewModels()
    private lateinit var categoryAdapter: SimpleRecyclerViewAdapter<CategoryTrick, CatgoryRvItemBinding>
    private var position = -1
    private var categoryLevelId: String? = null

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
        // view
        initView()
        // observer
        initObserver()

    }

    /**
     * Method to initialize view
     */
    private fun initView() {
        categoryLevelId = arguments?.getString("categoryId")
        if (!categoryLevelId.isNullOrEmpty()) {
            // api call
            viewModel.geMilestoneByIdApi(Constants.TRICKING_MILESTONE_TRICKS + "/$categoryLevelId")
        }

    }

    /** api response observer ***/
    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()
                }

                Status.SUCCESS -> {
                    when (it.message) {
                        "geMilestoneByIdApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: GetCategoryByIdApiResponse? =
                                    BindingUtils.parseJson(jsonData)
                                var category = model?.tricks
                                if (category != null) {
                                    categoryAdapter.list = category
                                    check = category.all { it?.progressStatus == "completed" }

                                    if(categoryAdapter.list.isNotEmpty()){
                                        binding.ivUnSelected.isClickable = true
                                        binding.ivUnSelected.isEnabled = true
                                        binding.ivUnSelected.isFocusable = true
                                        if (check) {
                                            binding.ivUnSelected.setImageResource(R.drawable.bg_complete)
                                        } else {
                                            binding.ivUnSelected.setImageResource(R.drawable.unselected_box)
                                        }
                                        binding.clEmpty.visibility= View.GONE
                                    }else{
                                        binding.ivUnSelected.isClickable = false
                                        binding.ivUnSelected.isEnabled = false
                                        binding.ivUnSelected.isFocusable = false
                                        binding.clEmpty.visibility= View.VISIBLE
                                    }
                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }

                        "createTrickApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: CommonApiResponse? = BindingUtils.parseJson(jsonData)
                                if (model?.success == true) {
                                    if (position != -1) {
                                        showSuccessToast(it.message.toString())
                                        if (categoryAdapter.list[position].progressStatus.equals("completed")) {
                                            categoryAdapter.list[position].progressStatus =
                                                "completed"
                                        } else {
                                            categoryAdapter.list[position].progressStatus =
                                                "pending"
                                        }
                                        position = -1
                                    }
                                    categoryAdapter.notifyDataSetChanged()
                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }
                    }
                }

                Status.ERROR -> {
                    hideLoading()
                    showErrorToast(it.message.toString())
                }

                else -> {
                }
            }
        }
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
                        categoryAdapter.list.forEach { it.progressStatus = "completed" }

                        if (categoryLevelId != null) {
                            val data = HashMap<String, Any>()
                            data["trickingMilestoneLevelId"] = categoryLevelId!!
                            data["status"] = "completed"
                            data["createForAll"] = true
                            viewModel.createTrickApi(
                                Constants.TRICKING_MILESTONE_PROGRESS, data
                            )
                        }

                    } else {
                        // Unselect all
                        binding.ivUnSelected.setImageResource(R.drawable.unselected_box)
                        categoryAdapter.list.forEach { it.progressStatus = "pending" }

                        if (categoryLevelId != null) {
                            val data = HashMap<String, Any>()
                            data["trickingMilestoneLevelId"] = categoryLevelId!!
                            data["createForAll"] = true
                            data["status"] = "pending"
                            viewModel.createTrickApi(
                                Constants.TRICKING_MILESTONE_PROGRESS, data
                            )
                        }
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
        categoryAdapter = SimpleRecyclerViewAdapter(
            R.layout.catgory_rv_item, BR.bean
        ) { v, m, pos ->
            when (v?.id) {
                R.id.clCategory -> {
                    position = pos
                    if (categoryLevelId != null && m._id != null) {
                        val newStatus = if (m.progressStatus == "completed") "pending"
                        else "completed"
                        categoryAdapter.list[pos].progressStatus = newStatus
                        categoryAdapter.notifyItemChanged(pos)
                        val data = HashMap<String, Any>()
                        data["trickingMilestoneLevelId"] = categoryLevelId!!
                        data["trickingMilestoneTrickId"] = m._id
                        data["status"] = newStatus
                        viewModel.createTrickApi(
                            Constants.TRICKING_MILESTONE_PROGRESS, data
                        )
                    }
                }
            }
        }
        binding.rvCategory.adapter = categoryAdapter

    }


}