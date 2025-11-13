package com.example.newbase_2025.ui.dashboard.tracker.my_trick

import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.model.MyTrickData
import com.example.newbase_2025.data.model.SubTitle
import com.example.newbase_2025.databinding.FragmentMyTrickBinding
import com.example.newbase_2025.databinding.MyTrickRvItemBinding
import com.example.newbase_2025.ui.common.CommonActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyTrickFragment : BaseFragment<FragmentMyTrickBinding>() {
    private val viewModel: MyTrickFragmentVM by viewModels()
    private lateinit var myTrickAdapter: SimpleRecyclerViewAdapter<MyTrickData, MyTrickRvItemBinding>
    override fun getLayoutResource(): Int {

        return R.layout.fragment_my_trick
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // adapter
        initTrickAdapter()

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
                    startActivity(intent)
                }
            }

        }
        binding.rvMyTrick.adapter = myTrickAdapter
        myTrickAdapter.list = getDummyTrickList()
    }

    /**
     * Get dummy trick list
     */
    private fun getDummyTrickList(): ArrayList<MyTrickData> {
        val dummyList = arrayListOf(
            MyTrickData(
                R.drawable.home_list_dummy, "Vertical Kicks", true, arrayListOf(
                    SubTitle("Pop"), SubTitle("Cheat"), SubTitle("Swing Kicks")
                )
            ), MyTrickData(
                R.drawable.home_list_dummy, "Forward Tricks", false, arrayListOf(
                    SubTitle("Frontflip"), SubTitle("Webster"), SubTitle("Janitor")
                )
            ), MyTrickData(
                R.drawable.home_list_dummy, "Backward Tricks", true, arrayListOf(
                    SubTitle("Backflip"),
                    SubTitle("Gainer"),
                    SubTitle("Full"),
                    SubTitle("Corkscrew ")
                )
            ), MyTrickData(
                R.drawable.home_list_dummy, "Outside Tricks", false, arrayListOf(
                    SubTitle("Raiz"),
                    SubTitle("Doubleleg"),
                    SubTitle("Lotus"),
                    SubTitle("Sideflip"),
                    SubTitle("Spyder")
                )
            ), MyTrickData(
                R.drawable.home_list_dummy, "Inside Tricks", false, arrayListOf(
                    SubTitle("Aerial"),
                    SubTitle("Butterfly"),
                    SubTitle("Masterswing"),
                    SubTitle("Tak"),
                    SubTitle("Warp")
                )
            )
        )

        return dummyList
    }


}