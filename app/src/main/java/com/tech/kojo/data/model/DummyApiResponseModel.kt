package com.tech.kojo.data.model


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

data class ChipData(
    var title: String,
    var icon : Int,
    var link : String?
)

data class AuthModel(var userEmail: String?, var userPassword: String?)



