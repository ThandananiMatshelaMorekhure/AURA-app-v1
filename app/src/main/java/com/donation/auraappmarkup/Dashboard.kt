package com.example.auraappmarkup

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.donation.auraappmarkup.CalendarAdapter
import com.donation.auraappmarkup.R
import java.text.SimpleDateFormat
import java.util.*

class Dashboard : AppCompatActivity() {

    private lateinit var tvCurrentDate: TextView
    private lateinit var tvMonthYear: TextView
    private lateinit var tvPeriodInfo: TextView
    private lateinit var btnPreviousMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton
    private lateinit var btnLogPeriod: CardView
    private lateinit var rvCalendar: RecyclerView

    private lateinit var cardTodoList: CardView
    private lateinit var cardDailyArticles: CardView
    private lateinit var cardAnalysis: CardView
    private lateinit var cardSettings: CardView

    private lateinit var calendarAdapter: CalendarAdapter
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("MMMM", Locale.getDefault())
    private val currentDateFormat = SimpleDateFormat("MMMM dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        initViews()
        setupCalendar()
        setupClickListeners()
        updateUI()
    }

    private fun initViews() {
        tvCurrentDate = findViewById(R.id.tvCurrentDate)
        tvMonthYear = findViewById(R.id.tvMonthYear)
        tvPeriodInfo = findViewById(R.id.tvPeriodInfo)
        btnPreviousMonth = findViewById(R.id.btnPreviousMonth)
        btnNextMonth = findViewById(R.id.btnNextMonth)
        btnLogPeriod = findViewById(R.id.btnLogPeriod)
        rvCalendar = findViewById(R.id.rvCalendar)

        cardTodoList = findViewById(R.id.cardTodoList)
        cardDailyArticles = findViewById(R.id.cardDailyArticles)
        cardAnalysis = findViewById(R.id.cardAnalysis)
        cardSettings = findViewById(R.id.cardSettings)
    }

    private fun setupCalendar() {
        rvCalendar.layoutManager = GridLayoutManager(this, 7)
        calendarAdapter = CalendarAdapter { date ->
            // Handle date click
            Toast.makeText(this, getString(R.string.date_selected, date), Toast.LENGTH_SHORT).show()
        }
        rvCalendar.adapter = calendarAdapter

        updateCalendarDates()
    }



    private fun updateCalendarDates() {
        val dates = generateCalendarDates()
        calendarAdapter.updateDates(dates)
    }

    private fun generateCalendarDates(): List<CalendarDate> {
        val dates = mutableListOf<CalendarDate>()
        val tempCalendar = calendar.clone() as Calendar

        // Set to first day of month
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK) - 1

        // Get previous month days to fill the first week
        tempCalendar.add(Calendar.DAY_OF_MONTH, -firstDayOfWeek)

        // Generate 42 days (6 weeks)
        for (i in 0 until 42) {
            val isCurrentMonth = tempCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
            val day = tempCalendar.get(Calendar.DAY_OF_MONTH)
            val isToday = isToday(tempCalendar)
            val dayType = getDayType(tempCalendar)

            dates.add(CalendarDate(day, isCurrentMonth, isToday, dayType))
            tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return dates
    }

    private fun isToday(cal: Calendar): Boolean {
        val today = Calendar.getInstance()
        return cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }

    private fun getDayType(cal: Calendar): DayType {
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val month = cal.get(Calendar.MONTH)
        val currentMonth = calendar.get(Calendar.MONTH)

        return when {
            month == currentMonth && day in 2..5 -> DayType.PERIOD
            month == currentMonth && day == 30 -> DayType.TODAY
            month == currentMonth + 1 && day in 1..3 -> DayType.PREDICTED_PERIOD
            else -> DayType.NORMAL
        }
    }

    private fun setupClickListeners() {
        btnPreviousMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateUI()
            updateCalendarDates()
        }

        btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateUI()
            updateCalendarDates()
        }

        btnLogPeriod.setOnClickListener {
            Toast.makeText(this, getString(R.string.log_period_clicked), Toast.LENGTH_SHORT).show()
        }

        cardTodoList.setOnClickListener {
            Toast.makeText(this, getString(R.string.todo_clicked), Toast.LENGTH_SHORT).show()
        }

        cardDailyArticles.setOnClickListener {
            Toast.makeText(this, getString(R.string.articles_clicked), Toast.LENGTH_SHORT).show()
        }

        cardAnalysis.setOnClickListener {
            Toast.makeText(this, getString(R.string.analysis_clicked), Toast.LENGTH_SHORT).show()
        }

        cardSettings.setOnClickListener {
            Toast.makeText(this, getString(R.string.settings_clicked), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI() {
        tvCurrentDate.text = currentDateFormat.format(Date())
        tvMonthYear.text = dateFormat.format(calendar.time)
    }
}

// Data classes for calendar
data class CalendarDate(
    val day: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val dayType: DayType
)

enum class DayType {
    NORMAL,
    PERIOD,
    TODAY,
    PREDICTED_PERIOD
}