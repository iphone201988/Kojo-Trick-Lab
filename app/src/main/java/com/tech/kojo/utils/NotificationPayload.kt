package com.tech.kojo.utils


data class NotificationPayload(
    val body: String?,
    val `data`: NotificationDataPayload?,
    val title: String?,
    val type: String?
)

data class NotificationDataPayload(
    val trickDataId :String?,
    val deepLink: String?,
    val postId: String?,
    val userId: String?
)