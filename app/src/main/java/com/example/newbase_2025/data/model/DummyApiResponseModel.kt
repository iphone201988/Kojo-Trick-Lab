package com.example.newbase_2025.data.model

class DummyApiResponseModel : ArrayList<DummyApiItem>()

data class DummyApiItem(
    val id: Int,
    val imdbId: String,
    val posterURL: String,
    val title: String
)