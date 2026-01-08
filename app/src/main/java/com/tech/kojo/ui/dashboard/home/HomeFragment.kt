package com.tech.kojo.ui.dashboard.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import com.tech.kojo.BR
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.base.SimpleRecyclerViewAdapter
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.HomeTrickApiResponse
import com.tech.kojo.data.model.HomeTrickVault
import com.tech.kojo.databinding.FragmentHomeBinding
import com.tech.kojo.databinding.HolderHomeBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private val viewModel: HomeVM by viewModels()
    private lateinit var homeAdapter: SimpleRecyclerViewAdapter<HomeTrickVault, HolderHomeBinding>

    private var PERMISSION_REQUEST_CODE = 16

    override fun getLayoutResource(): Int {
        return R.layout.fragment_home
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // view
        initView()
        // adapter
        initAdapter()
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
                                    homeAdapter.list = home
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
    private fun initAdapter() {
        homeAdapter = SimpleRecyclerViewAdapter(R.layout.holder_home, BR.bean) { v, m, _ ->
            when (v?.id) {
                R.id.cardView -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "forwardTrick")
                    intent.putExtra("trackData", m)
                    startActivity(intent)
                }
            }

        }
        binding.rvHome.adapter = homeAdapter

    }




}