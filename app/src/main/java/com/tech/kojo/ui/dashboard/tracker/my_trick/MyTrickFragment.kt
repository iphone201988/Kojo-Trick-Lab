package com.tech.kojo.ui.dashboard.tracker.my_trick

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
import com.tech.kojo.data.model.HomeTrickApiResponse
import com.tech.kojo.data.model.HomeTrickVault
import com.tech.kojo.databinding.FragmentMyTrickBinding
import com.tech.kojo.databinding.MyTrickRvItemBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
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