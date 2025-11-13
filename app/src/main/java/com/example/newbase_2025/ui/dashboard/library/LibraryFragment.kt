package com.example.newbase_2025.ui.dashboard.library

import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.model.LibraryData
import com.example.newbase_2025.data.model.MyTrickData
import com.example.newbase_2025.data.model.TrackerData
import com.example.newbase_2025.databinding.FragmentLibraryBinding
import com.example.newbase_2025.databinding.HolderHomeBinding
import com.example.newbase_2025.databinding.LibraryRvItemBinding
import com.example.newbase_2025.ui.common.CommonActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LibraryFragment : BaseFragment<FragmentLibraryBinding>() {
    private val viewModel: LibraryVM by viewModels()
    private lateinit var recentlyAdapter: SimpleRecyclerViewAdapter<LibraryData, LibraryRvItemBinding>
    private lateinit var mostAdapter: SimpleRecyclerViewAdapter<LibraryData, LibraryRvItemBinding>
    private lateinit var seriesAdapter: SimpleRecyclerViewAdapter<LibraryData, LibraryRvItemBinding>
    private lateinit var veryKicksAdapter: SimpleRecyclerViewAdapter<LibraryData, LibraryRvItemBinding>
    private lateinit var setupsAdapter: SimpleRecyclerViewAdapter<LibraryData, LibraryRvItemBinding>


    override fun getLayoutResource(): Int {
        return R.layout.fragment_library
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
      // adapter
        initAdapter()
    }

    /**
     * Initialize adapter
     */
    private fun initAdapter() {
        recentlyAdapter = SimpleRecyclerViewAdapter(R.layout.library_rv_item, BR.bean) { v, m, _ ->
            when (v?.id) {
                R.id.cardView -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "videoPlayer")
                    startActivity(intent)
                }
            }

        }
        binding.rvRecently.adapter = recentlyAdapter
        recentlyAdapter.list = getDummyLibraryList()

        mostAdapter = SimpleRecyclerViewAdapter(R.layout.library_rv_item, BR.bean) { v, m, _ ->
            when (v?.id) {
                R.id.cardView -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "videoPlayer")
                    startActivity(intent)
                }
            }

        }
        binding.rvPopular.adapter = mostAdapter
        mostAdapter.list = getDummyLibraryList()

        seriesAdapter = SimpleRecyclerViewAdapter(R.layout.library_rv_item, BR.bean) { v, m, _ ->
            when (v?.id) {
                R.id.cardView -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "videoPlayer")
                    startActivity(intent)
                }
            }

        }
        binding.rvSeries.adapter = seriesAdapter
        seriesAdapter.list = getDummyLibraryList()

        veryKicksAdapter = SimpleRecyclerViewAdapter(R.layout.library_rv_item, BR.bean) { v, m, _ ->
            when (v?.id) {
                R.id.cardView -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "videoPlayer")
                    startActivity(intent)
                }
            }

        }
        binding.rvVert.adapter = veryKicksAdapter
        veryKicksAdapter.list = getDummyLibraryList()

        setupsAdapter = SimpleRecyclerViewAdapter(R.layout.library_rv_item, BR.bean) { v, m, _ ->
            when (v?.id) {
                R.id.cardView -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "videoPlayer")
                    startActivity(intent)
                }
            }

        }
        binding.rvSetups.adapter = setupsAdapter
        setupsAdapter.list = getDummyLibraryList()
    }

    /**
     * Get dummy library list
     */
    private fun getDummyLibraryList(): ArrayList<LibraryData> {
        val dummyList = arrayListOf(
            LibraryData(R.drawable.home_list_dummy, "Vertical Kicks"),
            LibraryData(R.drawable.home_list_dummy, "Vertical Kicks"),
            LibraryData(R.drawable.home_list_dummy, "Vertical Kicks"),
            LibraryData(R.drawable.home_list_dummy, "Vertical Kicks"),
            LibraryData(R.drawable.home_list_dummy, "Vertical Kicks"),
            LibraryData(R.drawable.home_list_dummy, "Vertical Kicks"),

        )
        return dummyList
    }


}