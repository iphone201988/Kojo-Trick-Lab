package com.tech.kojo.ui.dashboard.tracker

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.tech.kojo.BR
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.base.SimpleRecyclerViewAdapter
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.GetTrackerApiResponse
import com.tech.kojo.data.model.GetTrackerData
import com.tech.kojo.databinding.FragmentTrackerBinding
import com.tech.kojo.databinding.TrickRvLayoutItemBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TrackerFragment : BaseFragment<FragmentTrackerBinding>() {
    private val viewModel: TrackerFragmentVM by viewModels()
    private lateinit var trickAdapter: SimpleRecyclerViewAdapter<GetTrackerData, TrickRvLayoutItemBinding>
    override fun getLayoutResource(): Int {

        return R.layout.fragment_tracker
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // view
        initView()

        // observer
        initObserver()

        // adapter
        initTrickAdapter()

    }

    /**
     * Method to initialize view
     */
    private fun initView() {
        // api call
        viewModel.geTrackApi(Constants.MILESTONE_CATEGORY_UI)
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
                        "geTrackApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: GetTrackerApiResponse? = BindingUtils.parseJson(jsonData)
                                var tracker = model?.data
                                if (tracker != null) {
                                    trickAdapter.list = tracker
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
     * Initialize adapter
     */
    private fun initTrickAdapter() {
        trickAdapter =
            SimpleRecyclerViewAdapter(R.layout.trick_rv_layout_item, BR.bean) { v, m, pos ->
                when (pos) {
                    0 -> {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "myTrick")
                        startActivity(intent)
                    }

                    1 -> {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "comboGoals")
                        startActivity(intent)
                    }

                    2 -> {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "sessionPlanner")
                        startActivity(intent)
                    }

                    3 -> {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "trickingMilestones")
                        startActivity(intent)
                    }

                    4 -> {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "myStar")
                        startActivity(intent)
                    }
                }

            }
        binding.rvTrick.adapter = trickAdapter
    }


}