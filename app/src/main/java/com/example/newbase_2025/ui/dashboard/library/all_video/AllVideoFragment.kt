package com.example.newbase_2025.ui.dashboard.library.all_video

import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.model.AllVideoData
import com.example.newbase_2025.databinding.AllVideosRvItemBinding
import com.example.newbase_2025.databinding.FragmentAllVideoBinding
import com.example.newbase_2025.ui.common.CommonActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllVideoFragment : BaseFragment<FragmentAllVideoBinding>() {
    private val viewModel: AllVideoFragmentVM by viewModels()
    private lateinit var allVideoAdapter: SimpleRecyclerViewAdapter<AllVideoData, AllVideosRvItemBinding>
    override fun getLayoutResource(): Int {
        return R.layout.fragment_all_video
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // adapter
        initAdapter()


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
    private fun initAdapter() {
        allVideoAdapter =
            SimpleRecyclerViewAdapter(R.layout.all_videos_rv_item, BR.bean) { v, m, _ ->
                when (v?.id) {
                    R.id.cardView -> {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "forwardTrick")
                        startActivity(intent)
                    }
                }

            }
        binding.rvAllVideo.adapter = allVideoAdapter
        allVideoAdapter.list = getDummyTrickList()
    }

    /**
     * Get dummy trick list
     */
    private fun getDummyTrickList(): ArrayList<AllVideoData> {
        val dummyList = arrayListOf(
            AllVideoData("Dive Roll - Session 1", true),
            AllVideoData("Dive Roll - Session 2", true),
            AllVideoData("Dive Roll - Session 3", false),
            AllVideoData("Dive Roll - Session 4", false),
            AllVideoData("Dive Roll - Session 5", false),
            AllVideoData("Dive Roll - Session 6", false),
            AllVideoData("Dive Roll - Session 7", false),
            AllVideoData("Dive Roll - Session 8", false),
            AllVideoData("Dive Roll - Session 9", false),


            )


        return dummyList
    }

}
