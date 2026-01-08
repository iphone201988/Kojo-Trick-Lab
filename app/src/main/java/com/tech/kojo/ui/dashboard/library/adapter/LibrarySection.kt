package com.tech.kojo.ui.dashboard.library.adapter

import com.tech.kojo.data.model.LibrarySery
import com.tech.kojo.data.model.LibraryVideoX

sealed class LibrarySection {
    data class Topic(
        val id: String?,
        val title: String?,
        val videos: List<LibraryVideoX>
    ) : LibrarySection()

    data class SeriesRow(
        val id: String?,
        val title: String?,
        val seriesList: List<LibrarySery>
    ) : LibrarySection()
}