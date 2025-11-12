package com.example.newbase_2025.data.model

class DummyApiResponseModel : ArrayList<DummyApiItem>()

data class DummyApiItem(
    val id: Int, val imdbId: String, val posterURL: String, val title: String
)

data class DummyHome(
    val image: Int
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