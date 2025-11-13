package com.example.newbase_2025.data.model

class DummyApiResponseModel : ArrayList<DummyApiItem>()

data class DummyApiItem(
    val id: Int, val imdbId: String, val posterURL: String, val title: String
)



data class TrackerData(
    val image: Int, val title: String
)

data class MyTrickData(
    val image: Int, val title: String, var check: Boolean = false, var subTitle: ArrayList<SubTitle>
)

data class SubTitle(
    val title: String
)

data class RecentData(
    val image: Int, val title: String, var check: Int,
)

data class ProgressionDetailsData(
    val count: String, val title: String, var type: Int,
)
data class ComboGoalsData(
     val title: String
)

data class ForwardsData(
    val title: String, var check: Boolean = false,
)
