package com.tech.kojo.ui.dashboard.tracker.progression_details

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.tech.kojo.BR
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.base.SimpleRecyclerViewAdapter
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.HomeProgressApiResponse
import com.tech.kojo.data.model.HomeProgressStep
import com.tech.kojo.data.model.VideoLink
import com.tech.kojo.databinding.FragmentProgressionDetailsBinding
import com.tech.kojo.databinding.ProgressionDetailsRvItemBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProgressionDetailsFragment : BaseFragment<FragmentProgressionDetailsBinding>() {
    private val viewModel: ProgressionDetailsFragmentVM by viewModels()
    private lateinit var progressionDetailsAdapter: SimpleRecyclerViewAdapter<HomeProgressStep, ProgressionDetailsRvItemBinding>
    private var trackDetailId: String? = null
    private var diveName: String? = null
    override fun getLayoutResource(): Int {

        return R.layout.fragment_progression_details
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // adapter
        initProgressionDetailsAdapter()
        // click
        initOnClick()

        // observer
        initObserver()
    }

    override fun onResume() {
        super.onResume()
        //view
        initView()
    }

    /**
     * Method to initialize view
     */
    private fun initView() {
        // get argument
        trackDetailId = arguments?.getString("trackDetailId")

        //  API call
        trackDetailId?.let { id ->
            val data = HashMap<String, Any>()
            viewModel.getTrickDataByIdApi(data, Constants.TRICKS_DATA + "/$id")
        } ?: run {
            showErrorToast("Something went wrong!")
        }
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
                    intent.putExtra("fromWhere", "comboGoals")
                    startActivity(intent)
                }

            }
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
                        "getTrickDataByIdApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: HomeProgressApiResponse? =
                                    BindingUtils.parseJson(jsonData)
                                if (model != null) {
                                    diveName = model.data?.name
                                    val home = model.data
                                    binding.tvTrack.text = home?.description.orEmpty()
                                    binding.tvPhill.text = home?.name.orEmpty()
                                    binding.tvSkip.text = home?.name.orEmpty()
                                    // Safe list
                                    val safeList = home?.steps ?: emptyList()
                                    progressionDetailsAdapter.list = safeList

                                    // view pager data set
                                    val allVideos: List<VideoLink> =
                                        home?.steps?.flatMap { it?.videoLinks ?: emptyList() }
                                            ?.filterNotNull() ?: emptyList()
                                    // Setup ViewPager
                                    val adapter = UserImagePagerAdapter(allVideos) { videoItem ->
                                        val intent =
                                            Intent(requireContext(), CommonActivity::class.java)
                                        intent.putExtra("fromWhere", "video")
                                        intent.putExtra("videoUrl", videoItem.link)
                                        startActivity(intent)
                                    }

                                    binding.viewpager.adapter = adapter

                                    // dot indicators
                                    setupViewPager()


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
        progressionDetailsAdapter =
            SimpleRecyclerViewAdapter(R.layout.progression_details_rv_item, BR.bean) { v, m, _ ->
                when (v?.id) {
                    R.id.clProgress -> {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "finalProgress")
                        intent.putExtra("progressData", m)
                        intent.putExtra("trackDetailId", trackDetailId)
                        intent.putExtra("diveName", diveName)
                        startActivity(intent)
                    }
                }

            }
        binding.rvProgressionDetails.adapter = progressionDetailsAdapter

    }


}