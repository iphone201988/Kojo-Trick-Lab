package com.example.newbase_2025.ui.dashboard.library.series

import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.model.SeriesData
import com.example.newbase_2025.databinding.FragmentSeriesBinding
import com.example.newbase_2025.databinding.SeriesRvItemBinding
import com.example.newbase_2025.ui.common.CommonActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SeriesFragment : BaseFragment<FragmentSeriesBinding>() {
    private val viewModel: SeriesFragmentVM by viewModels()
    private lateinit var seriesAdapter: SimpleRecyclerViewAdapter<SeriesData, SeriesRvItemBinding>
    override fun getLayoutResource(): Int {
        return R.layout.fragment_series
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        binding.clCommon.ivBack.setBackgroundResource(R.drawable.three_icon)
        // adapter
        initAdapter()


        // click
        initOnClick()
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
                    intent.putExtra("fromWhere", "forwardTrick")
                    startActivity(intent)
                }
            }

        }
        binding.rvSeries.adapter = seriesAdapter
        seriesAdapter.list = getDummyTrickList()
    }

    /**
     * Get dummy trick list
     */
    private fun getDummyTrickList(): ArrayList<SeriesData> {
        val dummyList = arrayListOf(
            SeriesData("Loren ispam", "9"),
            SeriesData("Vertical Kicks", "5"),
            SeriesData("Vertical Kicks", "9"),
            SeriesData("Loren ispam", "9"),

            )


        return dummyList
    }

}
