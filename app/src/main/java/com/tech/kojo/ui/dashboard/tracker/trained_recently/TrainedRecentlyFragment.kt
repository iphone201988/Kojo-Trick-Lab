package com.tech.kojo.ui.dashboard.tracker.trained_recently

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
import com.tech.kojo.data.model.GetTrainedRecentlyAPiResponse
import com.tech.kojo.data.model.RecentlyData
import com.tech.kojo.databinding.FragmentTrainedRecentlyBinding
import com.tech.kojo.databinding.TrainedRecentlyRvItemBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TrainedRecentlyFragment : BaseFragment<FragmentTrainedRecentlyBinding>() {
    private val viewModel: TrainedRecentlyFragmentVM by viewModels()
    private lateinit var trainedRecentAdapter: SimpleRecyclerViewAdapter<RecentlyData, TrainedRecentlyRvItemBinding>
    private var trainedRecentlyList = ArrayList<RecentlyData>()
    override fun getLayoutResource(): Int {

        return R.layout.fragment_trained_recently
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // view
        initView()
        // adapter
        initTrickAdapter()
        // click
        initOnClick()
        // observer
        initObserver()
    }


    /**
     * Method to initialize view
     */
    private fun initView() {
        binding.check = 1
        val userProgressId = arguments?.getString("userProgressId")
        if (userProgressId != null) {
            // API call
            val data = HashMap<String, Any>()
            data["trickVaultId"] = userProgressId
            viewModel.getUserProgressApi(data, Constants.PROGRESS_TRACKER)
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

                R.id.tvProgress -> {
                    binding.check = 1
                    val list = trainedRecentlyList
                    trainedRecentAdapter.list = list
                    updateEmptyState(list)
                }

                R.id.tvTrack -> {
                    binding.check = 2
                    val list = trainedRecentlyList.filter { it.completedSteps == it.totalSteps }
                    trainedRecentAdapter.list = list
                    updateEmptyState(list)
                }

            }
        }
    }

    private fun updateEmptyState(list: List<Any>) {
        binding.clEmpty.visibility =
            if (list.isEmpty()) View.VISIBLE else View.GONE
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
                        "getUserProgressApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: GetTrainedRecentlyAPiResponse? =
                                    BindingUtils.parseJson(jsonData)
                                val recently = model?.data
                                if (recently != null) {
                                    trainedRecentlyList = recently as ArrayList<RecentlyData>
                                    trainedRecentAdapter.list = trainedRecentlyList
                                    updateEmptyState(trainedRecentlyList)
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
        trainedRecentAdapter =
            SimpleRecyclerViewAdapter(R.layout.trained_recently_rv_item, BR.bean) { v, m, _ ->
                when (v?.id) {
                    R.id.clRecent -> {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "progressionDetails")
                        intent.putExtra("progressId", m._id)
                        startActivity(intent)
                    }
                }

            }
        binding.rvTrainedRecently.adapter = trainedRecentAdapter
    }


}