package com.example.newbase_2025.ui.dashboard.tracker.personal_bests

import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.model.PersonalData
import com.example.newbase_2025.data.model.RecentData
import com.example.newbase_2025.databinding.FragmentPersonalBestsBinding
import com.example.newbase_2025.databinding.PersonalBestsRvItemBinding
import com.example.newbase_2025.databinding.TrainedRecentlyRvItemBinding
import com.example.newbase_2025.ui.common.CommonActivity
import com.example.newbase_2025.ui.dashboard.tracker.my_star.MyStarFragmentVM
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PersonalBestsFragment : BaseFragment<FragmentPersonalBestsBinding>() {
    private val viewModel: PersonalBestsFragmentVM by viewModels()
    private lateinit var personalAdapter: SimpleRecyclerViewAdapter<PersonalData, PersonalBestsRvItemBinding>
    override fun getLayoutResource(): Int {

        return R.layout.fragment_personal_bests
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        initOnClick()
        // adapter
        initPersonalAdapter()
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
    private fun initPersonalAdapter() {
        personalAdapter =
            SimpleRecyclerViewAdapter(R.layout.personal_bests_rv_item, BR.bean) { v, m, _ ->
                when (v?.id) {
                    R.id.clRecent -> {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "progressionDetails")
                        startActivity(intent)
                    }
                }

            }
        binding.rvPersonalBests.adapter = personalAdapter
        personalAdapter.list = getDummyRecentList()
    }

    /**
     * Get dummy recent list
     */
    private fun getDummyRecentList(): ArrayList<PersonalData> {
        val dummyList = arrayListOf(
            PersonalData( "Gainer Switches:", "9"),
            PersonalData( "Corks:", "8"),
            PersonalData( "Gainer Switches:", "9"),
            PersonalData( "Corks:", "8"),
            PersonalData( "Gainer Switches:", "9"),
            PersonalData( "Corks:", "8"),
            PersonalData( "Gainer Switches:", "9"),
            PersonalData( "Corks:", "8"),
            PersonalData( "Gainer Switches:", "9"),
            PersonalData( "Corks:", "8"),

            )

        return dummyList
    }



}