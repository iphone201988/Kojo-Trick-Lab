package com.tech.kojo.ui.common

import com.tech.kojo.base.BaseViewModel
import com.tech.kojo.data.api.ApiHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CommonActivityVM @Inject constructor(
    private val apiHelper: ApiHelper,
) : BaseViewModel()

