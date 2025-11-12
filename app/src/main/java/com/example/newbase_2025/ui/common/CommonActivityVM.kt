package com.example.newbase_2025.ui.common

import com.example.newbase_2025.base.BaseViewModel
import com.example.newbase_2025.data.api.ApiHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CommonActivityVM @Inject constructor(
    private val apiHelper: ApiHelper,
) : BaseViewModel()

