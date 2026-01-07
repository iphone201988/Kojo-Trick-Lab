package com.example.newbase_2025.ui.dashboard.tracker.my_trick

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.api.Constants
import com.example.newbase_2025.data.model.HomeTrickApiResponse
import com.example.newbase_2025.data.model.HomeTrickVault
import com.example.newbase_2025.databinding.FragmentMyTrickBinding
import com.example.newbase_2025.databinding.MyTrickRvItemBinding
import com.example.newbase_2025.ui.common.CommonActivity
import com.example.newbase_2025.utils.BindingUtils
import com.example.newbase_2025.utils.Status
import com.example.newbase_2025.utils.showErrorToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyTrickFragment : BaseFragment<FragmentMyTrickBinding>() {
    private val viewModel: MyTrickFragmentVM by viewModels()
    private lateinit var myTrickAdapter: SimpleRecyclerViewAdapter<HomeTrickVault, MyTrickRvItemBinding>
    override fun getLayoutResource(): Int {

        return R.layout.fragment_my_trick
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // adapter
        initTrickAdapter()
        // view
        initView()
        // click
        initOnClick()
        // observer
        initObserver()
    }

    /**
     * Method to initialize view
     */
    private fun initView() {
        // api call
        viewModel.getHomeTrickApi(Constants.GET_TRICKS_VAULT_ALL)
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

    /** api response observer ***/
    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()
                }

                Status.SUCCESS -> {
                    when (it.message) {
                        "getHomeTrickApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: HomeTrickApiResponse? = BindingUtils.parseJson(jsonData)
                                var home = model?.trickVaults
                                if (home != null) {
                                    myTrickAdapter.list = home
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
        myTrickAdapter = SimpleRecyclerViewAdapter(R.layout.my_trick_rv_item, BR.bean) { v, m, _ ->
            when (v?.id) {
                R.id.cardView -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "trainedRecently")
                    intent.putExtra("userProgressId", m._id)
                    startActivity(intent)
                }
            }

        }
        binding.rvMyTrick.adapter = myTrickAdapter
    }


}