package com.tech.kojo.ui.dashboard.tracker.session_planner

import android.content.res.ColorStateList
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

    inner class DayVH(val binding: ItemDayBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayVH {
        val binding = ItemDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // Make cell width match parent /7 by using parent's measured width if required in the fragment
        return DayVH(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: DayVH, position: Int) {
        val item = days[position]
        val context = holder.itemView.context

        holder.binding.tvDay.text = item.date?.dayOfMonth?.toString() ?: ""

        val isPastDate = item.date?.isBefore(LocalDate.now()) == true

        // Reset defaults
        holder.binding.eventDots.visibility = View.GONE
        holder.binding.bgSelected.setBackgroundResource(R.drawable.un_selected_date_color)
        holder.binding.tvDay.setTextColor(ContextCompat.getColor(context, R.color.blue))
        holder.itemView.isEnabled = true
        holder.itemView.isClickable = true

        when {
            item.isSelected -> {
                holder.binding.bgSelected.setBackgroundResource(R.drawable.blue_circle)
                holder.binding.tvDay.setTextColor(ContextCompat.getColor(context, R.color.white))
                if (item.eventCount > 0) {
                    holder.binding.eventDots.visibility = View.VISIBLE
                    addDots(holder.binding.eventDots, item.eventColors)
                }
            }

            item.isToday -> {
                holder.binding.bgSelected.setBackgroundResource(R.drawable.bg_circle)
                holder.binding.bgSelected.backgroundTintList =
                    ColorStateList.valueOf(
                        ContextCompat.getColor(holder.itemView.context, R.color.blue)
                    )
                holder.binding.tvDay.setTextColor(ContextCompat.getColor(context, R.color.white))
                if (item.eventCount > 0) {
                    holder.binding.eventDots.visibility = View.VISIBLE
                    addDots(holder.binding.eventDots, item.eventColors)
                }
            }

            isPastDate && item.eventCount > 0 -> {
                holder.binding.bgSelected.setBackgroundResource(R.drawable.event_old_bg)
                holder.binding.tvDay.setTextColor(ContextCompat.getColor(context, R.color.blue))
                holder.binding.eventDots.visibility = View.VISIBLE
                addDots(holder.binding.eventDots, item.eventColors)
                holder.itemView.isEnabled = false
                holder.itemView.isClickable = false
            }

            isPastDate -> {
                holder.binding.bgSelected.setBackgroundResource(R.drawable.un_selected_date_color)
                holder.binding.tvDay.setTextColor(ContextCompat.getColor(context, R.color.blue))
                holder.binding.eventDots.visibility = View.GONE
                holder.itemView.isEnabled = false
                holder.itemView.isClickable = false
            }

            item.eventCount > 0 -> {
                holder.binding.bgSelected.setBackgroundResource(R.drawable.event_date_bg)
                holder.binding.tvDay.setTextColor(ContextCompat.getColor(context, R.color.blue))
                holder.binding.eventDots.visibility = View.VISIBLE
                addDots(holder.binding.eventDots, item.eventColors)
            }

            else -> {
                holder.binding.bgSelected.setBackgroundResource(R.drawable.un_selected_date_color)
                holder.binding.tvDay.setTextColor(ContextCompat.getColor(context, R.color.blue))
                holder.binding.eventDots.visibility = View.GONE
            }
        }

        holder.itemView.setOnClickListener {
            item.date?.let { d ->
                if (!isPastDate) onDayClick(d)
            }
        }
    }

    override fun getItemCount() = days.size

    fun updateSelection(date: LocalDate) {
        days.forEach { it.isSelected = it.date == date }
        notifyDataSetChanged()
    }

    private fun addDots(container: GridLayout, colors: List<String>) {
        container.removeAllViews()

        val bars = minOf(colors.size, 4)
        for (i in 0 until bars) {
            val view = View(container.context).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = dpToPx(10, container.context)
                    height = dpToPx(4, container.context)
                    setMargins(dpToPx(2, container.context), dpToPx(2, container.context), dpToPx(2, container.context), dpToPx(2, container.context))
                    rowSpec = GridLayout.spec(i / 2)
                    columnSpec = GridLayout.spec(i % 2)
                }

                background = ContextCompat.getDrawable(context, R.drawable.event_bar)
                val colorRes = colorMap[colors[i]] ?: R.color.red_color
                backgroundTintList = ContextCompat.getColorStateList(context, colorRes)
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
        "red" to R.color.red_color,
    )
}

