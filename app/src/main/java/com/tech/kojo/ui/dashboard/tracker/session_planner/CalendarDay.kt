package com.tech.kojo.ui.dashboard.tracker.session_planner

import java.time.LocalDate

data class CalendarDay(
    val date: LocalDate?,
    val isToday: Boolean = false,
    var isSelected: Boolean = false,
    var eventCount: Int = 0,
    var eventColors: List<String> = emptyList()
)