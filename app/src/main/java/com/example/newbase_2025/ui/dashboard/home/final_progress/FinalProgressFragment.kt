package com.example.newbase_2025.ui.dashboard.home.final_progress

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.api.Constants
import com.example.newbase_2025.data.model.HomeKeypoint
import com.example.newbase_2025.data.model.HomeProgressStep
import com.example.newbase_2025.data.model.UserProgressApiResponse
import com.example.newbase_2025.data.model.VideoLink
import com.example.newbase_2025.databinding.FragmentFinalProgressBinding
import com.example.newbase_2025.databinding.StepRvItemBinding
import com.example.newbase_2025.ui.common.CommonActivity
import com.example.newbase_2025.ui.dashboard.tracker.progression_details.UserImagePagerAdapter
import com.example.newbase_2025.utils.BindingUtils
import com.example.newbase_2025.utils.Status
import com.example.newbase_2025.utils.showErrorToast
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FinalProgressFragment : BaseFragment<FragmentFinalProgressBinding>() {
    private val viewModel: FinalProgressFragmentVM by viewModels()
    private lateinit var stepAdapter: SimpleRecyclerViewAdapter<HomeKeypoint, StepRvItemBinding>
    val repsCount = MutableLiveData(0)
    private var diveName: String? = null
    private var trackDetailId: String? = null
    private var stepId: String? = null

    override fun getLayoutResource(): Int {
        return R.layout.fragment_final_progress
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        initOnClick()
        // dot indicators
        setupViewPager()
        // view
        initView()
        // observer
        initObserver()

    }

    /**
     * Method to initialize view
     */
    private fun initView() {
        // adapter
        initStepAdapter()
        // get data bundle
        val progressData = arguments?.getParcelable<HomeProgressStep>("progressData")
        trackDetailId = arguments?.getString("trackDetailId")
        diveName = arguments?.getString("diveName")
        // set data
        progressData?.let { data ->
            stepId = data._id
            binding.tvDive.text = diveName.orEmpty()
            binding.tvSkip.text = diveName.orEmpty()
            stepAdapter.list = data.keypoints ?: emptyList()
            // SAFE REPS
            val reps = data.progress?.repsCount ?: 0
            binding.etCount.setText(reps.toString())
            repsCount.value = reps

            // SAFE VIDEO LINKS (very important)
            val safeVideos: List<VideoLink> =
                data.videoLinks?.filterNotNull()?.toList() ?: emptyList()
            // viewpager
            val adapter = UserImagePagerAdapter(safeVideos) { videoItem ->
                // click listener callback
                val intent = Intent(requireContext(), CommonActivity::class.java)
                intent.putExtra("fromWhere", "video")
                intent.putExtra("videoUrl", videoItem.link)
                startActivity(intent)
            }
            binding.viewpager.adapter = adapter
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

                R.id.btnCompleted -> {
                    apiCall("completed")
                }

                R.id.btnAttempted -> {
                    apiCall("attempted")
                }

            }
        }
    }

    /**
     * Method to api call
     */

    private fun apiCall(status: String) {
        val data = HashMap<String, Any>()
        if (!trackDetailId.isNullOrEmpty() && !stepId.isNullOrEmpty()) {
            val timeSpentMillis = System.currentTimeMillis() - screenStartTime
            val timeSpentSeconds = timeSpentMillis / 1000
            data["trickDataId"] = trackDetailId!!
            data["stepId"] = stepId!!
            data["isSaved"] = true
            data["repsCount"] = binding.etCount.text.toString().trim()
            data["status"] = status
            data["timeTaken"] = timeSpentSeconds
            viewModel.userProgressApi(Constants.POST_PROGRESS, data)
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
                        "userProgressApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: UserProgressApiResponse? =
                                    BindingUtils.parseJson(jsonData)
                                if (model?.success == true) requireActivity().finish()
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

        if (pageCount <= 1) {
            //  Only one image → show static dot
            binding.staticDot.visibility = View.VISIBLE
            binding.dotsIndicator.visibility = View.GONE
        } else {
            //  Multiple images → use animated indicator
            binding.staticDot.visibility = View.GONE
            binding.dotsIndicator.visibility = View.VISIBLE
        }
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
    private fun initStepAdapter() {
        stepAdapter = SimpleRecyclerViewAdapter(R.layout.step_rv_item, BR.bean) { v, m, _ ->
            when (v?.id) {
                R.id.clProgress -> {

                }
            }

        }
        binding.rvStep.adapter = stepAdapter
    }

    private var screenStartTime: Long = 0L

    override fun onResume() {
        super.onResume()
        screenStartTime = System.currentTimeMillis()
    }

}