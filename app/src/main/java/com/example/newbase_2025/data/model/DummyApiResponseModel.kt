package com.example.newbase_2025.data.model


data class PersonalData(
    val title: String, var value: String,
)



data class SubTitle(
    val title: String
)

data class AllVideoData(
     val title: String, var check: Boolean = false,
)


data class Notification(
    var date : String,
    var list : List<NotificationData>

)

data class NotificationData(
    var title: String,
    var notification : String,
    var time : String
)



