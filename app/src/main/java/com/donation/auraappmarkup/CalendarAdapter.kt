package com.donation.auraappmarkup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.auraappmarkup.CalendarDate
import com.example.auraappmarkup.DayType

class CalendarAdapter(
    private val onDateClick: (Int) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.DateViewHolder>() {

    private var dates = listOf<CalendarDate>()

    fun updateDates(newDates: List<CalendarDate>) {
        dates = newDates
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_date, parent, false)
        return DateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        holder.bind(dates[position])
    }

    override fun getItemCount() = dates.size

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        fun bind(date: CalendarDate) {
            tvDate.text = date.day.toString()

            // Reset background and text color
            tvDate.background = null
            tvDate.setTextColor(
                if (date.isCurrentMonth) {
                    ContextCompat.getColor(itemView.context, android.R.color.black)
                } else {
                    ContextCompat.getColor(itemView.context, android.R.color.darker_gray)
                }
            )

            // Apply styling based on day type
            when (date.dayType) {
                DayType.PERIOD -> {
                    tvDate.setBackgroundResource(R.drawable.day_period_bg)
                    tvDate.setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            android.R.color.white
                        )
                    )
                }

                DayType.TODAY -> {
                    tvDate.setBackgroundResource(R.drawable.day_fertile_bg)
                    tvDate.setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            android.R.color.white
                        )
                    )
                }

                DayType.PREDICTED_PERIOD -> {
                    tvDate.setBackgroundResource(R.drawable.day_predicted_bg_xml)
                    tvDate.setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            android.R.color.white
                        )
                    )
                }

                DayType.NORMAL -> {
                    // Keep default styling
                }
            }

            itemView.setOnClickListener {
                if (date.isCurrentMonth) {
                    onDateClick(date.day)
                }
            }
        }
    }
}