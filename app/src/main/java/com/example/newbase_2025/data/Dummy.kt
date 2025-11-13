package com.example.newbase_2025.data

    data class Notification(
        var date : String,
        var list : List<NotificationData>

    )

    data class NotificationData(
        var title: String,
        var notification : String,
        var time : String
    )

