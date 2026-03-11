package com.tech.kojo.ui.dashboard.tracker.session_planner

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tech.kojo.R
import com.tech.kojo.databinding.ItemDayBinding
import java.time.LocalDate

class CalendarAdapter(
    private val days: MutableList<CalendarDay>,
    private val onDayClick: (LocalDate) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DayVH>() {

    inner class DayVH(val binding: ItemDayBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayVH {
        val binding =
            ItemDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayVH(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: DayVH, position: Int) {

        val item = days[position]
        val context = holder.itemView.context
        val today = LocalDate.now()
        val isPastDate = item.date?.isBefore(today) == true
        val isCurrentDate = item.date == today

        holder.binding.tvDay.text = item.date?.dayOfMonth?.toString() ?: ""
        holder.binding.bgSelected.setBackgroundResource(R.drawable.normal_date_bg)
        holder.binding.tvDay.setTextColor(
            ContextCompat.getColor(context, R.color.blue)
        )
        holder.binding.eventDots.visibility = View.GONE
        holder.binding.eventDots.alpha = 1f
        holder.itemView.isEnabled = true
        holder.itemView.isClickable = true
        holder.itemView.alpha = 1f

        when {
            // 1️⃣ SELECTED DATE (Highest Priority - Only one date shows this)
            item.isSelected -> {
                holder.binding.bgSelected.setBackgroundResource(R.drawable.selected_date_bg)
                holder.binding.tvDay.setTextColor(
                    ContextCompat.getColor(context, R.color.white)
                )

                if (item.eventCount > 0) {
                    holder.binding.eventDots.visibility = View.VISIBLE
                    addDots(holder.binding.eventDots, item.eventColors)
                }
            }

            // 3️⃣ PAST DATE WITH EVENTS
            isPastDate && item.eventCount > 0 -> {
                holder.binding.bgSelected.setBackgroundResource(R.drawable.event_past_bg)
                holder.binding.tvDay.setTextColor(
                    ContextCompat.getColor(context, R.color.blue_40)
                )

                holder.binding.eventDots.visibility = View.VISIBLE
                addDots(holder.binding.eventDots, item.eventColors)
            }

            // 4️⃣ PAST DATE WITHOUT EVENTS
            isPastDate -> {
                holder.binding.tvDay.setTextColor(
                    ContextCompat.getColor(context, R.color.blue_40)
                )
            }

            // 5️⃣ FUTURE DATE WITH EVENTS
            item.eventCount > 0 -> {
                holder.binding.bgSelected.setBackgroundResource(R.drawable.event_future_bg)
                holder.binding.tvDay.setTextColor(
                    ContextCompat.getColor(context, R.color.blue)
                )

                holder.binding.eventDots.visibility = View.VISIBLE
                addDots(holder.binding.eventDots, item.eventColors)
            }

            // 6️⃣ NORMAL FUTURE DATE
            else -> {
                holder.binding.bgSelected.setBackgroundResource(R.drawable.normal_date_bg)
                holder.binding.tvDay.setTextColor(
                    ContextCompat.getColor(context, R.color.blue)
                )
            }
        }

        // -----------------------------
        // Click Handling
        // -----------------------------
        holder.itemView.setOnClickListener {
            item.date?.let { date ->
                updateSelection(date)
                onDayClick(date)
            }
        }
    }

    override fun getItemCount(): Int = days.size

    // -----------------------------
    // Selection Logic
    // -----------------------------
    fun updateSelection(date: LocalDate) {
        days.forEach {
            it.isSelected = it.date == date
        }
        notifyDataSetChanged()
    }
    // -----------------------------
    // Event Bars
    // -----------------------------
    private fun addDots(container: GridLayout, colors: List<String>) {

        container.removeAllViews()

        val bars = minOf(colors.size, 4)

        for (i in 0 until bars) {

            val view = View(container.context).apply {

                layoutParams = GridLayout.LayoutParams().apply {
                    width = dpToPx(10, container.context)
                    height = dpToPx(3, container.context)
                    setMargins(
                        dpToPx(2, container.context),
                        dpToPx(2, container.context),
                        dpToPx(2, container.context),
                        dpToPx(2, container.context)
                    )
                    rowSpec = GridLayout.spec(i / 2)
                    columnSpec = GridLayout.spec(i % 2)
                }

                background = ContextCompat.getDrawable(context, R.drawable.event_bar)

                val colorRes = colorMap[colors[i]] ?: R.color.red_color
                backgroundTintList =
                    ContextCompat.getColorStateList(context, colorRes)
            }

            container.addView(view)
        }
    }

    private fun dpToPx(dp: Int, context: android.content.Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    private val colorMap = mapOf(
        "blue" to R.color.purple,
        "green" to R.color.green_color,
        "orange" to R.color.orange,
        "red" to R.color.red_color
    )
}