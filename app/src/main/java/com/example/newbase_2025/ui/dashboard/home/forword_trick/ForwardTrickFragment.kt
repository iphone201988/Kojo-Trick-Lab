package com.example.newbase_2025.ui.dashboard.home.forword_trick

import android.content.Intent
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.viewModels
import com.example.newbase_2025.BR
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.base.SimpleRecyclerViewAdapter
import com.example.newbase_2025.data.model.ForwardsData
import com.example.newbase_2025.data.model.RecentData
import com.example.newbase_2025.databinding.ForwardRvItemBinding
import com.example.newbase_2025.databinding.FragmentForwordTrickBinding
import com.example.newbase_2025.databinding.VerticalForwardRvItemBinding
import com.example.newbase_2025.ui.common.CommonActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ForwardTrickFragment : BaseFragment<FragmentForwordTrickBinding>() {
    private val viewModel: ForwardTrickFragmentVM by viewModels()
    private lateinit var forwardAdapter: SimpleRecyclerViewAdapter<ForwardsData, ForwardRvItemBinding>
    private lateinit var verticalForwardAdapter: SimpleRecyclerViewAdapter<RecentData, VerticalForwardRvItemBinding>
    override fun getLayoutResource(): Int {

        return R.layout.fragment_forword_trick
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        initOnClick()
        // adapter
        initForwardAdapter()
        initVerticalForwardAdapter()
        // read more functionality
        binding.tvDescription.makeTextExpandable(
            "This category encompasses all tricks that flip forwards. Note that unlike Gainers, they flip in the opposite direction and are used for smooth transitions."
        )
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
    private fun initForwardAdapter() {
        forwardAdapter = SimpleRecyclerViewAdapter(R.layout.forward_rv_item, BR.bean) { v, m, _ ->
            when (v?.id) {
                R.id.clForward -> {
                    for (i in forwardAdapter.list) {
                        i.check = i.title == m.title
                    }
                    forwardAdapter.notifyDataSetChanged()
                }
            }

        }
        binding.rvForward.adapter = forwardAdapter
        forwardAdapter.list = getDummyForwardList()
    }


    private fun initVerticalForwardAdapter() {
        verticalForwardAdapter =
            SimpleRecyclerViewAdapter(R.layout.vertical_forward_rv_item, BR.bean) { v, m, _ ->
                when (v?.id) {
                  R.id.clForward->{
                      val intent = Intent(requireContext(), CommonActivity::class.java)
                      intent.putExtra("fromWhere", "homeProgress")
                      startActivity(intent)
                  }
                }

            }
        binding.rvVerticalForward.adapter = verticalForwardAdapter
        verticalForwardAdapter.list = getDummyRecentList()
    }

    /**
     * Get dummy forward list
     */
    private fun getDummyForwardList(): ArrayList<ForwardsData> {
        val dummyList = arrayListOf(
            ForwardsData("All Forward Tricks", true),
            ForwardsData("Frontflip Tricks", false),
            ForwardsData("Webster Tricks ", false),
        )

        return dummyList
    }

    /**
     * Get dummy recent list
     */
    private fun getDummyRecentList(): ArrayList<RecentData> {
        val dummyList = arrayListOf(
            RecentData(R.drawable.home_list_dummy, "Dive Roll", 2),
            RecentData(R.drawable.home_list_dummy, "Webster Half", 1),
            RecentData(R.drawable.home_list_dummy, "Fount X-out", 1),
            RecentData(R.drawable.home_list_dummy, "Super Man Frount", 2),
            RecentData(R.drawable.home_list_dummy, "Dive Roll", 2),
            RecentData(R.drawable.home_list_dummy, "Webster Half", 1),
            RecentData(R.drawable.home_list_dummy, "Fount X-out", 1),
            RecentData(R.drawable.home_list_dummy, "Super Man Frount", 2),

            )

        return dummyList
    }

    /***
     * Method to make text expandable
     */
    fun AppCompatTextView.makeTextExpandable(fullText: String, maxLines: Int = 2) {
        post {
            val readMoreText = " Read more"
            val readLessText = " Read less"

            text = fullText
            movementMethod = LinkMovementMethod.getInstance()

            // If text fits within maxLines, do nothing
            if (layout.lineCount <= maxLines) return@post

            val endOfVisibleText = layout.getLineEnd(maxLines - 1)
            val visibleText =
                fullText.substring(0, endOfVisibleText - readMoreText.length).trimEnd()

            val collapsedText = "$visibleText...$readMoreText"
            val collapsedSpannable = SpannableStringBuilder(collapsedText)

            // "Read more" click
            collapsedSpannable.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        val expandedText = "$fullText$readLessText"
                        val expandedSpannable = SpannableStringBuilder(expandedText)
                        expandedSpannable.setSpan(object : ClickableSpan() {
                            override fun onClick(widget: View) {
                                makeTextExpandable(fullText, maxLines)
                            }
                        }, fullText.length, expandedText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                        expandedSpannable.setSpan(
                            ForegroundColorSpan(Color.parseColor("#F9A825")),
                            fullText.length,
                            expandedText.length,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                        )

                        text = expandedSpannable
                        movementMethod = LinkMovementMethod.getInstance()
                    }
                },
                collapsedText.length - readMoreText.length,
                collapsedText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            collapsedSpannable.setSpan(
                ForegroundColorSpan(Color.parseColor("#F9A825")),
                collapsedText.length - readMoreText.length,
                collapsedText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            text = collapsedSpannable
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

}


