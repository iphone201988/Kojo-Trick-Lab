package com.example.newbase_2025.ui.dashboard.library.video_player

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.model.CommentData
import com.example.newbase_2025.data.model.LibraryData
import com.example.newbase_2025.databinding.CommentBottomSheetItemBinding
import com.example.newbase_2025.databinding.FragmentVideoPlayerBinding
import com.example.newbase_2025.databinding.LibraryRvItemBinding
import com.example.newbase_2025.databinding.MessageRvItemBinding
import com.example.newbase_2025.ui.common.CommonActivity
import com.example.newbase_2025.utils.BaseCustomBottomSheet
import com.example.newbase_2025.utils.showInfoToast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VideoPlayerFragment : BaseFragment<FragmentVideoPlayerBinding>() {
    private val viewModel: VideoPlayerFragmentVM by viewModels()
    private lateinit var relatedAdapter: SimpleRecyclerViewAdapter<LibraryData, LibraryRvItemBinding>
    private lateinit var commentsBottomSheet: BaseCustomBottomSheet<CommentBottomSheetItemBinding>
    private lateinit var commentsAdapter: SimpleRecyclerViewAdapter<CommentData, MessageRvItemBinding>
    override fun getLayoutResource(): Int {

        return R.layout.fragment_video_player
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

                R.id.clComments -> {
                    initBottomSheet()
                    Log.d("gdgfdfg", "initOnClick: ")
                }

                R.id.ivUser -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "series")
                    startActivity(intent)
                }

                R.id.ivPerson -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "userProfile")
                    startActivity(intent)
                }

            }
        }
    }


    /**
     * Initialize adapter
     */
    private fun initAdapter() {
        relatedAdapter = SimpleRecyclerViewAdapter(R.layout.library_rv_item, BR.bean) { v, m, _ ->
            when (v?.id) {
                R.id.cardView -> {

                }
            }

        }

        binding.rvRelated.adapter = relatedAdapter
        relatedAdapter.list = getDummyLibraryList()
    }

    /**
     * Initialize bottom sheet
     */
    private fun initBottomSheet() {
        commentsBottomSheet = BaseCustomBottomSheet(
            requireContext(), R.layout.comment_bottom_sheet_item
        ) { view ->
            when (view?.id) {
                R.id.ivSend -> {
                    val message = commentsBottomSheet.binding.etComments.text.toString().trim()
                    if (message.isNotEmpty()) {
                        val newComment = CommentData("Just now", message)
                        val updatedList = ArrayList(commentsAdapter.list)
                        updatedList.add(0, newComment)
                        commentsAdapter.list = updatedList
                        commentsAdapter.notifyDataSetChanged()

                        // Clear the EditText
                        commentsBottomSheet.binding.etComments.text?.clear()

                        // Scroll to the latest comment
                        commentsBottomSheet.binding.rvMessage.scrollToPosition(0)

                    } else {
                        showInfoToast("Please enter message")
                    }
                }

                R.id.tvTop -> {
                    commentsBottomSheet.binding.check = 1
                }

                R.id.tvMost -> {
                    commentsBottomSheet.binding.check = 2
                }

                R.id.ivPerson -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "userProfile")
                    startActivity(intent)
                }
            }
        }
        commentsBottomSheet.behavior.isDraggable = true
        commentsBottomSheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        commentsBottomSheet.show()
        commentsBottomSheet.binding.check = 1
        initCommentAdapter()
    }


    private fun initCommentAdapter() {
        commentsAdapter = SimpleRecyclerViewAdapter(R.layout.message_rv_item, BR.bean) { v, m, _ ->
            when (v?.id) {
                R.id.ivPerson -> {
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "userProfile")
                    startActivity(intent)
                }
            }

        }

        commentsBottomSheet.binding.rvMessage.adapter = commentsAdapter
        commentsAdapter.list = getDummyMessageList()
        commentsAdapter.notifyDataSetChanged()
        Log.d("gdgfdfg", "commentsAdapter  ${commentsAdapter.list.size}")
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

    /**
     * Get dummy message list
     */
    private fun getDummyMessageList(): ArrayList<CommentData> {
        val dummyList = arrayListOf(
            CommentData("1 day ago", "Love this video it helped me a lot"),
            CommentData(
                "2 day ago",
                "Lorem Ipsum is simply dummy text of the printing and typesetting industry. "
            ),
            CommentData(
                "3 day ago",
                "Lorem Ipsum is simply dummy text of the printing and typesetting industry. "
            ),
            CommentData(
                "3 day ago",
                "Lorem Ipsum is simply dummy text of the printing and typesetting industry. "
            ),


            )
        return dummyList
    }


}