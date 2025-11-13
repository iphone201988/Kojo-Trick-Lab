package com.example.newbase_2025.ui.dashboard.home.progress

import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.model.ProgressionDetailsData
import com.example.newbase_2025.databinding.FragmentHomeProgressDetailsBinding
import com.example.newbase_2025.databinding.HomeProgressItemBinding
import com.example.newbase_2025.databinding.ProgressionDetailsRvItemBinding
import com.example.newbase_2025.ui.common.CommonActivity
import com.example.newbase_2025.ui.dashboard.tracker.progression_details.ProgressionDetailsFragmentVM
import com.example.newbase_2025.ui.dashboard.tracker.progression_details.UserImagePagerAdapter
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeProgressDetailsFragment : BaseFragment<FragmentHomeProgressDetailsBinding>() {
    private val viewModel: HomeProgressDetailsVM by viewModels()
    private lateinit var homeProgressionDetailsAdapter: SimpleRecyclerViewAdapter<ProgressionDetailsData, HomeProgressItemBinding>
    override fun getLayoutResource(): Int {

        return R.layout.fragment_home_progress_details
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // adapter
        initProgressionDetailsAdapter()
        // click
        initOnClick()
        // view pager data set
        val adapter = UserImagePagerAdapter(getDummyImageList()) { imageUrl ->


        }
        binding.viewpager.adapter = adapter
        // dot indicators
        setupViewPager()
    }


    /**
     * Method to initialize click
     */
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.ivProgress -> {
                    requireActivity().finish()
                }

                R.id.btnMarkCompleted -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "finalProgress")
                    startActivity(intent)
                }

            }
        }
    }


    /*** setup view pager ***/
    private fun setupViewPager() {
        val pageCount = binding.viewpager.adapter?.itemCount ?: 0

        binding.dotsIndicator.apply {
            setSliderColor(
                ContextCompat.getColor(requireActivity(), R.color.colorPrimary),
                ContextCompat.getColor(requireActivity(), R.color.white)
            )
            setSliderWidth(
                resources.getDimension(com.intuit.sdp.R.dimen._8sdp),
                resources.getDimension(com.intuit.sdp.R.dimen._36sdp)
            )
            setSliderHeight(resources.getDimension(com.intuit.sdp.R.dimen._8sdp))
            setSlideMode(IndicatorSlideMode.SCALE)
            setIndicatorStyle(IndicatorStyle.ROUND_RECT)
            setPageSize(pageCount)
            notifyDataChanged()
            setupWithViewPager(binding.viewpager)
        }

    }

    /**
     * Initialize adapter
     */
    private fun initProgressionDetailsAdapter() {
        homeProgressionDetailsAdapter =
            SimpleRecyclerViewAdapter(R.layout.home_progress_item, BR.bean) { v, m, _ ->
                when (v?.id) {
                    R.id.cardView -> {

                    }
                }

            }
        binding.rvHomeProgressionDetails.adapter = homeProgressionDetailsAdapter
        homeProgressionDetailsAdapter.list = getDummyTrickList()
    }

    /**
     * Get dummy trick list
     */
    private fun getDummyTrickList(): ArrayList<ProgressionDetailsData> {
        val dummyList = arrayListOf(
            ProgressionDetailsData("1", "Forward Roll Basics", 2),
            ProgressionDetailsData("2", "Long Jump Prep", 1),
            ProgressionDetailsData("3", "Dive Prep", 3),
            ProgressionDetailsData("4", "Dive Prep", 3),


            )

        return dummyList
    }

    /**
     * Get dummy image list
     */
    private fun getDummyImageList(): ArrayList<Int> {
        val dummyList = arrayListOf(
            R.drawable.home_list_dummy,
            R.drawable.home_list_dummy,
            R.drawable.home_list_dummy,
            R.drawable.home_list_dummy
        )

        return dummyList
    }


}