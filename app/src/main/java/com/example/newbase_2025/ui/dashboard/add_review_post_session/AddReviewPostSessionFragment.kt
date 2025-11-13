package com.example.newbase_2025.ui.dashboard.add_review_post_session

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.databinding.FragmentAddReviewPostSessionBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddReviewPostSessionFragment : BaseFragment<FragmentAddReviewPostSessionBinding>() {

    private val viewModel : AddReviewPostSessionVm by viewModels()

    override fun getLayoutResource(): Int {
     return R.layout.fragment_add_review_post_session
    }

    override fun getViewModel(): BaseViewModel {
          return viewModel
    }

    override fun onCreateView(view: View) {
        val datePart = "15, Sep 2025"
        val sessionPart = "has 4 sessions"
        val fullText = "$datePart $sessionPart"

        val spannable = SpannableString(fullText)

        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.blue)),
            0, datePart.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )


        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.black)),
            datePart.length + 1, fullText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.date.text = spannable


    }

}