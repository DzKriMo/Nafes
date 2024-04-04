package com.thekrimo.nafes

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.thekrimo.nafes.databinding.ActivityCalenderBinding
import java.text.DateFormatSymbols
import java.util.Calendar
import android.content.Intent
import android.widget.GridView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.thekrimo.nafes.databinding.FragmentHomeBinding
import java.util.*

class CalenderActivity : BaseActivity() {
    private lateinit var binding: ActivityCalenderBinding
    private lateinit var adapter: DayAdapter
    private var currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH)
    private var currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalenderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = DayAdapter(this, getDaysOfMonth(currentMonth, currentYear))
        binding.calendarGrid.numColumns = 7
        binding.calendarGrid.adapter = adapter

        binding.prevMonthButton.setOnClickListener {
            updateMonth(-1)
        }

        binding.nextMonthButton.setOnClickListener {
            updateMonth(1)
        }
        binding.back.setOnClickListener {
            onBackPressed()
        }

        updateMonthName(currentMonth, currentYear)

        adapter.setOnItemClickListener { position ->
            val clickedDay = adapter.getItem(position)

            Toast.makeText(
                this, clickedDay,
                Toast.LENGTH_SHORT
            ).show()

            // here  nzid logic t3 passing the date to the scheduling activity/fragment
        }
    }

    private fun updateMonth(offset: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(currentYear, currentMonth, 1)
        calendar.add(Calendar.MONTH, offset)
        currentMonth = calendar.get(Calendar.MONTH)
        currentYear = calendar.get(Calendar.YEAR)
        updateMonthName(currentMonth, currentYear)
        adapter.clear()
        adapter.addAll(getDaysOfMonth(currentMonth, currentYear))
        binding.calendarGrid.adapter = adapter
    }

    private fun updateMonthName(month: Int, year: Int) {
        val monthName = DateFormatSymbols().months[month]
        binding.monthNameTextView.text = "$monthName $year"
    }

    private fun getDaysOfMonth(month: Int, year: Int): List<String> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        val numDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val daysOfMonth = mutableListOf<String>()

        repeat(calendar.get(Calendar.DAY_OF_WEEK) - 1) {
            daysOfMonth.add("")
        }

        for (i in 1..numDays) {
            daysOfMonth.add(i.toString())
        }
        return daysOfMonth
    }

    private inner class DayAdapter(context: Context, daysOfMonth: List<String>) :
        ArrayAdapter<String>(context, R.layout.grid_cell, daysOfMonth) {

        private var onItemClickListener: ((Int) -> Unit)? = null
        private val currentDayOfMonth: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        private val currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH)
        private val currentYear: Int = Calendar.getInstance().get(Calendar.YEAR)

        fun setOnItemClickListener(listener: (Int) -> Unit) {
            onItemClickListener = listener
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            val holder: ViewHolder

            if (view == null) {
                view = LayoutInflater.from(context).inflate(R.layout.grid_cell, parent, false)
                holder = ViewHolder()
                holder.dayNumberTextView = view.findViewById(R.id.day_number)
                view.tag = holder
            } else {
                holder = view.tag as ViewHolder
            }

            val day = getItem(position)
            holder.dayNumberTextView.text = day

            // Check if the day is in the current month and year
            val isInCurrentMonthYear = day != "" && currentMonth == Calendar.getInstance().get(Calendar.MONTH)
                    && currentYear == Calendar.getInstance().get(Calendar.YEAR)
            view?.isEnabled = isInCurrentMonthYear


            if (day == currentDayOfMonth.toString() && isInCurrentMonthYear) {
                holder.dayNumberTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
                view?.setBackgroundResource(R.drawable.current_day_background)
            } else {
                holder.dayNumberTextView.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                view?.setBackgroundResource(0)
            }


            if (isInCurrentMonthYear) {
                view?.setOnClickListener {
                    onItemClickListener?.invoke(position)
                }
            }

            return view!!
        }

        private inner class ViewHolder {
            lateinit var dayNumberTextView: TextView
        }
    }

}
