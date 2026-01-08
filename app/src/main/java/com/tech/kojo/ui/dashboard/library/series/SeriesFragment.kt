package com.tech.kojo.ui.dashboard.library.series

import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.tech.kojo.BR
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.base.SimpleRecyclerViewAdapter
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.CategoryData
import com.tech.kojo.data.model.GetVideoCategoryData
import com.tech.kojo.databinding.FragmentSeriesBinding
import com.tech.kojo.databinding.SeriesRvItemBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
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
