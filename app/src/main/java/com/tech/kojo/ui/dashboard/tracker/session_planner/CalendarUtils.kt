package com.tech.kojo.ui.dashboard.tracker.session_planner

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate

object CalendarUtils {

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMonthDays(year: Int, month: Int): MutableList<CalendarDay> {
        val list = mutableListOf<CalendarDay>()
        val first = LocalDate.of(year, month, 1)
        val totalDays = first.lengthOfMonth()

        val startOffset = first.dayOfWeek.value % 7

        repeat(startOffset) {
            list.add(CalendarDay(null))
        }

        for (d in 1..totalDays) {
            val date = LocalDate.of(year, month, d)
            list.add(
                CalendarDay(
                    date = date,
                    isToday = date == LocalDate.now(),
                    eventCount = (1..4).random()   // 1-4 dots
                )
            )
        }
        return list
    }


}
