package com.example.newbase_2025.ui.dashboard.community.create_Post

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
import androidx.lifecycle.Observer
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.databinding.FragmentCreatePostBinding
import com.example.newbase_2025.ui.dashboard.community.CommunityVm
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue


@AndroidEntryPoint
class CreatePostFragment : BaseFragment<FragmentCreatePostBinding>() {

    private val viewModel: CreatePostVm by viewModels()


    override fun getLayoutResource(): Int {
         return R.layout.fragment_create_post
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {

        val text = "Upload Video Clip (20mb max)"
        val spannable = SpannableString(text)


        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.blue)),
            0, 16, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )


        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.colorPrimary)),
            17, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvUploadVideo.text = spannable


        initOnClick()
    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer{

        })
    }

}