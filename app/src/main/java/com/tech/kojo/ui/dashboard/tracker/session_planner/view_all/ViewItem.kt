package com.tech.kojo.ui.dashboard.tracker.session_planner.view_all

import com.tech.kojo.data.model.PastSessionData


sealed class ViewItem {
    data class Post(val post: PastSessionData) : ViewItem()
    data object Loader : ViewItem()
}