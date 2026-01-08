package com.tech.kojo.ui.dashboard.community.adapter

import com.tech.kojo.data.model.PostData

sealed class FeedItem {
    data class Post(val post: PostData) : FeedItem()
    object Loader : FeedItem()
}