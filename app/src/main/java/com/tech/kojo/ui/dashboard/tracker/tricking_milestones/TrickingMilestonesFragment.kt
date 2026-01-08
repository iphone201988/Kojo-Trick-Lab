package com.tech.kojo.ui.dashboard.tracker.tricking_milestones

import android.content.Intent
import android.os.Handler
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
import com.tech.kojo.data.model.MilestonesApiResponse
import com.tech.kojo.data.model.MilestonesLevel
import com.tech.kojo.databinding.FragmentTrickingMilestonesBinding
import com.tech.kojo.databinding.TrickingMilestonesRvItemBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrickingMilestonesFragment : BaseFragment<FragmentTrickingMilestonesBinding>() {
    private val viewModel: TrickingMilestonesFragmentVM by viewModels()
    private lateinit var milestonesAdapter: SimpleRecyclerViewAdapter<MilestonesLevel, TrickingMilestonesRvItemBinding>
    private var isProgress = false
    override fun getLayoutResource(): Int {
        return R.layout.fragment_tricking_milestones
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
        // api call
        viewModel.geMilestoneApi(Constants.TRICKING_MILESTONE)


        // refresh
        binding.ssPullRefresh.setColorSchemeResources(
            ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        )
        binding.ssPullRefresh.setOnRefreshListener {
            Handler().postDelayed({
                binding.ssPullRefresh.isRefreshing = false
                isProgress = true
                // api call
                viewModel.geMilestoneApi(Constants.TRICKING_MILESTONE)
            }, 2000)
        }
    }

    /** api response observer ***/
    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.LOADING -> {
                    if (!isProgress) {
                        showLoading()
                    }
                }

                Status.SUCCESS -> {
                    when (it.message) {
                        "geMilestoneApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: MilestonesApiResponse? = BindingUtils.parseJson(jsonData)
                                if (model != null) {
                                    var milestones = model.levels
                                    isProgress = false
                                    milestonesAdapter.list = milestones
                                    binding.tvStarCount.text = model.levelData?.level.toString()

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
    private fun initTrickAdapter() {
        milestonesAdapter =
            SimpleRecyclerViewAdapter(R.layout.tricking_milestones_rv_item, BR.bean) { v, m, _ ->
                when (v?.id) {
                    R.id.cardView -> {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "categoryChecking")
                        intent.putExtra("categoryId", m._id)
                        startActivity(intent)
                    }
                }

            }
        binding.rvMilestones.adapter = milestonesAdapter
    }


}