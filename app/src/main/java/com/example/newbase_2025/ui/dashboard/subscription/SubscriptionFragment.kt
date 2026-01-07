package com.example.newbase_2025.ui.dashboard.subscription

import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.model.SubTitle
import com.example.newbase_2025.databinding.FragmentSubscriptionBinding
import com.example.newbase_2025.databinding.PricingCardBinding
import com.example.newbase_2025.ui.common.CommonActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubscriptionFragment : BaseFragment<FragmentSubscriptionBinding>() {
    private val viewModel: SubscriptionVM by viewModels()
    private lateinit var planAdapter: SimpleRecyclerViewAdapter<SubTitle, PricingCardBinding>
    override fun onCreateView(view: View) {
        initView()
        initOnClick()
        initAdapter()
    }

    override fun getLayoutResource(): Int {
        return R.layout.fragment_subscription
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer {
            when (it?.id) {
                R.id.ivBack -> {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }

            }
        })
    }

    private fun initView() {
        binding.clCommon.tvHeader.text = "Subscription"
    }


    private fun initAdapter() {
        planAdapter = SimpleRecyclerViewAdapter(R.layout.pricing_card, BR.bean) { v, m, _ ->
            when (v?.id) {
                R.id.cardView -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "forwardTrick")
                    startActivity(intent)
                }
            }

        }
        binding.rvPlan.adapter = planAdapter
        planAdapter.list = getDummyTrickList()
    }

    /**
     * Get dummy trick list
     */
    private fun getDummyTrickList(): ArrayList<SubTitle> {
        val dummyList = arrayListOf(
            SubTitle("Basic"), SubTitle("Popular"), SubTitle("Premium")
        )

        return dummyList
    }

}