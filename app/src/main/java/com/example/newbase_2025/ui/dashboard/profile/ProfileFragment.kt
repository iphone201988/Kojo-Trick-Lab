package com.example.newbase_2025.ui.dashboard.profile

import android.content.Intent
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.newbase_2025.R
import com.example.newbase_2025.base.BaseFragment
import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.databinding.FragmentProfileBinding
import com.example.newbase_2025.ui.common.CommonActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>() {
    private val viewModel: ProfileVM by viewModels()
    override fun getLayoutResource(): Int {
        return R.layout.fragment_profile
    }

    override fun getViewModel(): BaseViewModel {
        return viewModel
    }

    override fun onCreateView(view: View) {
        intiView()
        initOnClick()
    }

    private fun intiView() {

    }

    private fun initOnClick() {
        viewModel.onClick.observe(viewLifecycleOwner, Observer {
            when(it?.id){
                R.id.tvEditProfilePic ->{
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "editProfilePic")
                    startActivity(intent)
                }
                R.id.tvEditProfile ->{
                    val intent = Intent(requireContext(), CommonActivity::class.java)
                    intent.putExtra("fromWhere", "editProfile")
                    startActivity(intent)
                }
            }
        })

    }


}