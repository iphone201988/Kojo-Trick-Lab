package com.tech.kojo.ui.dashboard.community.adapter

import com.tech.kojo.data.model.GetCommentData
import com.tech.kojo.data.model.PostData

sealed class FeedItem {
    data class Post(val post: PostData) : FeedItem()
    object Loader : FeedItem()
}


sealed class FeedDetailItem {
    data class PostComment(val comment: GetCommentData) : FeedDetailItem()
    object Loader : FeedDetailItem()
}