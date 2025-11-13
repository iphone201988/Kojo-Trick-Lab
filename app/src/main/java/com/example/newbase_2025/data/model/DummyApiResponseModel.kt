package com.example.newbase_2025.data.model

class DummyApiResponseModel : ArrayList<DummyApiItem>()

data class DummyApiItem(
    val id: Int, val imdbId: String, val posterURL: String, val title: String
)



data class TrackerData(
    val image: Int, val title: String
)

data class LibraryData(
    val image: Int, val title: String
)

data class MilestonesData(
    val image: Int, val title: String,var type :Int
)
data class CategoryData(
    val name: String,var check: Boolean = false
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


data class PersonalData(
    val title: String, var value: String,
)

data class CommentData(
    val time: String, val message: String
)
data class SeriesData(
    val title: String, val videoCount: String
)


data class AllVideoData(
     val title: String, var check: Boolean = false,
)

data class SessionPlannerData(
   var type: Int,
)

