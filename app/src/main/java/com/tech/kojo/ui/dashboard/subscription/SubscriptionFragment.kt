package com.tech.kojo.ui.dashboard.subscription

import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.tech.kojo.BR
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.base.SimpleRecyclerViewAdapter
import com.tech.kojo.data.model.SubTitle
import com.tech.kojo.databinding.FragmentSubscriptionBinding
import com.tech.kojo.databinding.PricingCardBinding
import com.tech.kojo.ui.common.CommonActivity
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