package com.example.newbase_2025

import android.view.View
import androidx.activity.viewModels
import com.example.newbase_2025.base.BaseActivity
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.utils.Status
import com.example.newbase_2025.utils.showErrorToast
import com.example.newbase_2025.utils.showSuccessToast
import com.example.newbase_2025.data.model.DummyApiItem
import com.example.newbase_2025.databinding.ActivityMainBinding
import com.example.newbase_2025.databinding.HolderDummyBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val viewModel: MainActivityVM by viewModels()
    override fun getLayoutResource(): Int {
        return R.layout.activity_main
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView() {
        initView()
        initOnClick()
        initObservers()
    }

    private fun initView() {
        binding.ivNoDataFound.visibility = View.VISIBLE
        binding.rvMAinActivity.visibility = View.GONE

        initAdapter()
    }

    private fun initOnClick() {
        viewModel.onClick.observe(this) {
            when (it?.id) {
                R.id.tvClickMe -> {
                    //viewModel.getDummyList()
                }
            }
        }
    }

    private fun initObservers() {
        viewModel.observeCommon.observe(this) {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading("Loading")
                }

                Status.SUCCESS -> {
                    hideLoading()
                    it.data.let {
                        // adapter.list = it1
                        binding.ivNoDataFound.visibility = View.GONE
                        binding.tvClickMe.visibility = View.GONE
                        binding.rvMAinActivity.visibility = View.VISIBLE
                    }
                }

                Status.ERROR -> {
                    hideLoading()
                    showErrorToast(it.message.toString())
                }

                else -> {}
            }
        }
    }

    private lateinit var adapter: SimpleRecyclerViewAdapter<DummyApiItem, HolderDummyBinding>
    private fun initAdapter() {
        adapter = SimpleRecyclerViewAdapter(R.layout.holder_dummy, BR.bean) { _, m, _ ->
            showSuccessToast(m.title)
        }
        binding.rvMAinActivity.adapter = adapter
    }
}