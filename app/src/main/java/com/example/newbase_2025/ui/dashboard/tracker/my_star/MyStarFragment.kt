package com.example.newbase_2025.ui.dashboard.tracker.my_star

import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.databinding.FragmentMyStarBinding
import com.example.newbase_2025.ui.common.CommonActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MyStarFragment : BaseFragment<FragmentMyStarBinding>() {
    private val viewModel: MyStarFragmentVM by viewModels()
    override fun getLayoutResource(): Int {

        return R.layout.fragment_my_star
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        initOnClick()
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

                R.id.btnSeeAll -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "personalBests")
                    startActivity(intent)
                }

            }
        }
    }


}