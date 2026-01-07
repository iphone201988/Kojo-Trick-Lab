package com.example.newbase_2025.ui.dashboard.library.series

import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.api.Constants
import com.example.newbase_2025.data.model.CategoryData
import com.example.newbase_2025.data.model.GetVideoCategoryData
import com.example.newbase_2025.databinding.FragmentSeriesBinding
import com.example.newbase_2025.databinding.SeriesRvItemBinding
import com.example.newbase_2025.ui.common.CommonActivity
import com.example.newbase_2025.utils.BindingUtils
import com.example.newbase_2025.utils.Status
import com.example.newbase_2025.utils.showErrorToast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SeriesFragment : BaseFragment<FragmentSeriesBinding>() {
    private val viewModel: SeriesFragmentVM by viewModels()
    private val args: SeriesFragmentArgs by navArgs()
    private lateinit var seriesAdapter: SimpleRecyclerViewAdapter<CategoryData, SeriesRvItemBinding>
    override fun getLayoutResource(): Int {
        return R.layout.fragment_series
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
     //   binding.clCommon.ivBack.setImageResource(R.drawable.three_icon)
        // adapter
        initAdapter()
        // api call
        if (args.categoryId.isNotEmpty()) {
            viewModel.getVideoCategory(Constants.VIDEO_DATA_GET_TOPIC + "/${args.categoryId}")
        }
        // click
        initOnClick()
        // observer
        initObserver()
    }

    /**
     * Method to initialize observer
     */
    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.SUCCESS -> {
                    when (it.message) {
                        "getVideoCategory" -> {
                            runCatching {
                                val model =
                                    BindingUtils.parseJson<GetVideoCategoryData>(it.data.toString())
                                if (model?.success == true && model.data?.isNotEmpty() == true) {
                                    seriesAdapter.list = model.data
                                    binding.clEmpty.visibility = View.GONE
                                } else {
                                    binding.clEmpty.visibility = View.VISIBLE
                                }

                            }.onFailure { e ->
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

                Status.LOADING -> showLoading()
                else -> {

                }
            }
        }
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

            }
        }
    }

    /**
     * Initialize adapter
     */
    private fun initAdapter() {
        seriesAdapter = SimpleRecyclerViewAdapter(R.layout.series_rv_item, BR.bean) { v, m, _ ->
            when (v?.id) {
                R.id.cardView -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "allVideo")
                    intent.putExtra("videoTopicId", m._id)
                    intent.putExtra("videoTitle", m.title)
                    startActivity(intent)
                }
            }
        }
        binding.rvSeries.adapter = seriesAdapter
    }


}
