package com.example.newbase_2025.ui.dashboard.home.final_progress

import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.model.ProgressionDetailsData
import com.example.newbase_2025.databinding.FragmentFinalProgressBinding
import com.example.newbase_2025.databinding.HomeProgressItemBinding
import com.example.newbase_2025.ui.common.CommonActivity
import com.example.newbase_2025.ui.dashboard.tracker.progression_details.UserImagePagerAdapter
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FinalProgressFragment : BaseFragment<FragmentFinalProgressBinding>() {
    private val viewModel: FinalProgressFragmentVM by viewModels()
    override fun getLayoutResource(): Int {

        return R.layout.fragment_final_progress
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
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
    val repsCount = MutableLiveData(0)
    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner) {
            when (it?.id) {
                R.id.ivProgress -> {
                    requireActivity().finish()
                }

                R.id.ivPlus -> {
                    val current = repsCount.value ?: 0
                    val newCount = current + 1
                    repsCount.value = newCount
                    binding.etCount.setText(newCount.toString())
                }

                R.id.ivMinus -> {
                    val current = repsCount.value ?: 0
                    val newCount = if (current > 0) current - 1 else 0
                    repsCount.value = newCount
                    binding.etCount.setText(newCount.toString())
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