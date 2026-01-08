package com.tech.kojo.ui.dashboard.home.forword_trick

import android.content.Intent
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.viewModels
import com.tech.kojo.BR
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.base.SimpleRecyclerViewAdapter
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.GetTrickByIdApiResponse
import com.tech.kojo.data.model.HomeTrickVault
import com.tech.kojo.data.model.HomeType
import com.tech.kojo.data.model.TrickByIdData
import com.tech.kojo.databinding.ForwardRvItemBinding
import com.tech.kojo.databinding.FragmentForwordTrickBinding
import com.tech.kojo.databinding.VerticalForwardRvItemBinding
import com.tech.kojo.ui.common.CommonActivity
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ForwardTrickFragment : BaseFragment<FragmentForwordTrickBinding>() {
    private val viewModel: ForwardTrickFragmentVM by viewModels()
    private lateinit var forwardAdapter: SimpleRecyclerViewAdapter<HomeType, ForwardRvItemBinding>
    private var trackId: String? = null

    private lateinit var verticalForwardAdapter: SimpleRecyclerViewAdapter<TrickByIdData, VerticalForwardRvItemBinding>
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
        // view
        initView()

        // observer
        initObserver()

    }


    /**
     * Method to initialize view
     */
    private fun initView() {
        val trackData = arguments?.getParcelable<HomeTrickVault>("trackData")
        if (trackData != null) {
            trackId = trackData._id
            val updatedList = mutableListOf<HomeType>()
            // Always add first item
            updatedList.add(
                HomeType(
                    _id = "", name = "All Forward", check = true
                )
            )
            val typeList = trackData.types?.map { it as HomeType } ?: emptyList()
            updatedList.addAll(typeList)
            // Set to adapter
            forwardAdapter.list = updatedList

            // API call
            val data = HashMap<String, Any>()
            data["trickVaultId"] = trackData._id.toString()
            viewModel.getTrickDataByIdApi(data, Constants.TRICKS_DATA)
        }

        binding.tvDescription.makeTextExpandable(
            "This category encompasses all tricks that flip forwards..."
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


    /** api response observer ***/
    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.LOADING -> {
                    showLoading()
                }

                Status.SUCCESS -> {
                    when (it.message) {
                        "getTrickDataByIdApi" -> {
                            runCatching {
                                val jsonData = it.data?.toString().orEmpty()
                                val model: GetTrickByIdApiResponse? =
                                    BindingUtils.parseJson(jsonData)
                                if (model != null) {
                                    var home = model.data
                                    verticalForwardAdapter.list = home
                                    if (verticalForwardAdapter.list.isNotEmpty()) {
                                        binding.clEmpty.visibility = View.GONE
                                    } else {
                                        binding.clEmpty.visibility = View.VISIBLE
                                    }
                                }
                            }.onFailure { e ->
                                Log.e("apiErrorOccurred", "Error: ${e.message}", e)
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                            }
                        }
                    }
                }

                Status.ERROR -> {
                    hideLoading()
                    showErrorToast(it.message.toString())
                }

                else -> {
                }
            }
        }
    }


    /**
     * Initialize adapter
     */
    private fun initForwardAdapter() {
        forwardAdapter = SimpleRecyclerViewAdapter(
            R.layout.forward_rv_item, BR.bean
        ) { v, m, _ ->

            if (v?.id == R.id.clForward) {
                val safeTrackId = trackId ?: return@SimpleRecyclerViewAdapter
                // API call
                val data = HashMap<String, Any>()
                data["trickVaultId"] = safeTrackId
                data["typeId"] = m._id ?: ""
                viewModel.getTrickDataByIdApi(data, Constants.TRICKS_DATA)

                // update selection
                forwardAdapter.list.forEach {
                    it.check = it._id == m._id
                }
                forwardAdapter.notifyDataSetChanged()
            }
        }

        binding.rvForward.adapter = forwardAdapter
    }


    private fun initVerticalForwardAdapter() {
        verticalForwardAdapter =
            SimpleRecyclerViewAdapter(R.layout.vertical_forward_rv_item, BR.bean) { v, m, _ ->
                when (v?.id) {
                    R.id.clForward -> {
                        val intent = Intent(requireContext(), CommonActivity::class.java)
                        intent.putExtra("fromWhere", "homeProgress")
                        intent.putExtra("trackDetailId", m._id)
                        startActivity(intent)
                    }
                }

            }
        binding.rvVerticalForward.adapter = verticalForwardAdapter

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


