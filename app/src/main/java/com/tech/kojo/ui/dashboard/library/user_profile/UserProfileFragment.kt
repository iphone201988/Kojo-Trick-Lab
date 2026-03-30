package com.tech.kojo.ui.dashboard.library.user_profile

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import com.tech.kojo.R
import com.tech.kojo.base.BaseFragment
import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.data.api.Constants
import com.tech.kojo.data.model.ChipData
import com.tech.kojo.data.model.GetOtherUserProfile
import com.tech.kojo.databinding.FragmentUserProfileBinding
import com.tech.kojo.utils.BindingUtils
import com.tech.kojo.utils.Status
import com.tech.kojo.utils.showErrorToast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserProfileFragment : BaseFragment<FragmentUserProfileBinding>() {
    private val viewModel: UserProfileFragmentVM by viewModels()
    private val args: UserProfileFragmentArgs by navArgs()
    val list = mutableListOf<ChipData>()
    override fun getLayoutResource(): Int {

        return R.layout.fragment_user_profile
    }


    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        // click
        initOnClick()
        // api call
        if (args.userId.isNotEmpty()) {
            viewModel.getUserProfile(Constants.AUTH_GET_USER+"/${args.userId}")
        }
        // observer
        initObserver()
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
     * Method to initialize observer
     */
    private fun initObserver() {
        viewModel.observeCommon.observe(viewLifecycleOwner) {
            when (it?.status) {
                Status.SUCCESS -> {
                    when (it.message) {
                        "getUserProfile" -> {
                            runCatching {
                                val model =
                                    BindingUtils.parseJson<GetOtherUserProfile>(it.data.toString())
                                if (model?.success == true&& model.data!=null) {
                                    binding.bean = model.data
                                    binding.ivCircle1.visibility = View.GONE
                                    binding.ivCircle.visibility = View.VISIBLE
                                    binding.ivProfile.visibility = View.VISIBLE
                                    if (model.data.isPrivate==true){
                                        binding.clMain.visibility=View.INVISIBLE
                                        binding.clNoView.visibility=View.VISIBLE
                                    }
                                    else{
                                        binding.clMain.visibility = View.VISIBLE
                                        binding.clNoView.visibility = View.GONE
                                    }
                                    if (!model.data.instagramLink.isNullOrEmpty()) {
                                        list.add(
                                            ChipData(
                                                title = "Instagram",
                                                icon = R.drawable.ic_chip_insta,
                                                link = model.data.instagramLink
                                            )
                                        )
                                    }

                                    if (!model.data.youtubeLink.isNullOrEmpty()) {
                                        list.add(
                                            ChipData(
                                                title = "YouTube",
                                                icon = R.drawable.ic_chip_youtube,
                                                link = model.data.youtubeLink
                                            )
                                        )
                                    }
                                    if (!model.data.tiktockLink.isNullOrEmpty()) {
                                        list.add(
                                            ChipData(
                                                title = "TikTok",
                                                icon = R.drawable.ic_chip_tiktok,
                                                link = model.data.tiktockLink
                                            )
                                        )
                                    }
                                } else {
                                    showErrorToast("Something went wrong")
                                }
                            }.onFailure { e ->
                                showErrorToast(e.message.toString())
                            }.also {
                                hideLoading()
                                handleListUi()
                            }
                        }
                    }
                }
                Status.ERROR -> {
                    hideLoading()
                    showErrorToast(it.message.toString())
                    handleListUi()
                }
                Status.LOADING -> showLoading()
                else -> {

                }
            }
        }
    }

    private fun handleListUi(){
        if(list.isNotEmpty()){
            binding.tvSocialMediaLinks.visibility = View.VISIBLE
            binding.chipGroupStatus.visibility = View.VISIBLE
            chipStatus(list)
        }
        else{
            binding.tvSocialMediaLinks.visibility = View.GONE
            binding.chipGroupStatus.visibility = View.GONE
        }
    }

    private fun chipStatus(list1: List<ChipData?>?) {

        if (list1.isNullOrEmpty()) return

        binding.chipGroupStatus.removeAllViews()

        for (element1 in list1) {

            val chip1 = layoutInflater.inflate(
                R.layout.single_chip_item, binding.chipGroupStatus, false
            ) as Chip

            element1?.let { item ->

                chip1.text = item.title
                chip1.isCheckable = false
                chip1.setChipIconResource(item.icon ?: 0)
                chip1.isChipIconVisible = true

                chip1.setOnClickListener {

                    item.link?.let { link ->

                        var finalUrl = link

                        // If link does not start with http, fix it
                        if (!finalUrl.startsWith("http")) {
                            finalUrl = "https://$finalUrl"
                        }

                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
                            startActivity(intent)
                        } catch (e: Exception) {
                            showErrorToast("Invalid link")
                        }
                    }
                }
            }

            binding.chipGroupStatus.addView(chip1)
        }
    }



}