package com.gmail.jaaska.jaakko.calendarcountdown.data

import android.util.Log
import com.gmail.jaaska.jaakko.calendarcountdown.util.DateUtil

import java.io.Serializable
import java.util.ArrayList
import java.util.Calendar
import java.util.Date

/**
 * Created by jaakko on 24.6.2018.
 */
class CountdownSettings : Serializable, Comparable<CountdownSettings> {

    var endDate: String = "" // "zero-date" of the countdown as a "dd-MM-yyyy" string
    private var excludeWeekends: Boolean = false // are weekened excluded or not
    var isUseOnWidget: Boolean = false // tells if this is the Countdown to show on a widget

    var excludedDays: ArrayList<ExcludedDays> = ArrayList() // all the day ranges that are excluded from countdown

    var dbId: Int = 0 // ID of the corresponding item in the DB
    var label: String = "" // label of the countdown

    var isExcludeWeekends: Boolean
        get() = excludeWeekends
        set(excludeWeekends) {
            Log.d(TAG, "setExcludeWeekends() - excludeWeekends: " + java.lang.Boolean.toString(excludeWeekends))
            this.excludeWeekends = excludeWeekends
        }

    private val currentTimeWithOnlyDate: Long
        get() {
            val cal = Calendar.getInstance()
            cal.time = Date(System.currentTimeMillis())
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            return cal.timeInMillis
        }

    /**
     * Time in milliseconds to end date from now.
     */
    private val timeToEndDate: Long
        get() = DateUtil.parseDatabaseDate(endDate).time - currentTimeWithOnlyDate

    /**
     * Returns amount of full days to the end date.
     */
    val daysToEndDate: Int
        get() {
            val toEnd = timeToEndDate
            val days = toEnd / 1000 / 60 / 60 / 24

            var ret = if (excludeWeekends)
                days.toInt() - weekEndDaysInTimeFrame(currentTimeWithOnlyDate, DateUtil.parseDatabaseDate(endDate).time)
            else
                days.toInt()

            ret -= getExcludedDaysCount()

            Log.d(TAG, "getDaysToEndDate() - days: " + Integer.toString(ret))
            return ret
        }

    init {
        endDate = DateUtil.formatDatabaseDate(currentTimeWithOnlyDate) // init new countdown to this day
        excludeWeekends = false
        excludedDays = ArrayList()
        label = ""
        dbId = Integer.MIN_VALUE
        isUseOnWidget = false
    }

    fun getExcludedDaysCount(): Int {
        var sum = excludedDays.sumBy { it.daysCount }
        sum = if (sum < 0) 0 else sum
        return sum
    }

    override fun compareTo(other: CountdownSettings): Int {
        var ret = 0

        when (GeneralSettings.sortOrder) {
            GeneralSettings.SORT_BY_DAYS_LEFT -> {
                // Compare by days count
                if (daysToEndDate > other.daysToEndDate) {
                    ret = 1
                } else if (daysToEndDate < other.daysToEndDate) {
                    ret = -1
                }
                return ret
            }

            GeneralSettings.SORT_BY_EVENT_LABEL -> return label.toUpperCase().compareTo(other.label.toUpperCase())
        }

        return ret
    }

    fun addExcludedDays(days: ExcludedDays) {
        excludedDays.add(days)
    }

    companion object {

        private val TAG = CountdownSettings::class.java.simpleName

        // used when passing CountdownSettings in Intents.
        const val extraName = "CountdownSettings"

        /**
         * Returns number of days between startTime and endTime.
         */
        fun daysInTimeFrame(startTime: Long, endTime: Long): Int {
            val startDate = Date(startTime)
            val cal = Calendar.getInstance()

            // convert start time to date only just in case
            cal.time = startDate
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)


            // calculate full weeks within timeframe
            val timeFrame = endTime - cal.timeInMillis

            return (timeFrame / 1000 / 60 / 60 / 24).toInt()
        }

        /**
         * Returns number of weekend days (saturday and sunday) between startTime and endTime.
         */
        fun weekEndDaysInTimeFrame(startTime: Long, endTime: Long): Int {
            var weekEndDays = 0 // number of saturdays and sundays

            val startDate = Date(startTime)
            val endDate = Date(endTime)
            val cal = Calendar.getInstance()

            // convert start time to date only just in case
            cal.time = startDate
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            // get weekdays of start and end date
            val startWeekDay = cal.get(Calendar.DAY_OF_WEEK)
            cal.time = endDate

            // calculate full weeks within timeframe
            val timeFrame = endTime - startTime // getCurrentTimeWithOnlyDate();
            val weeks = (timeFrame / 1000 / 60 / 60 / 24 / 7).toInt()
            val remainderDays = (timeFrame / 1000 / 60 / 60 / 24 % 7).toInt()

            // add weekends of each full week
            weekEndDays += weeks * 2

            // add possible remainder days that hit saturday or sunday
            if (startWeekDay == Calendar.THURSDAY) {
                if (remainderDays == 2)
                    weekEndDays += 1
                else if (remainderDays >= 2)
                    weekEndDays += 2
            } else if (startWeekDay == Calendar.FRIDAY) {
                if (remainderDays == 1)
                    weekEndDays += 1
                else if (remainderDays >= 1)
                    weekEndDays += 2
            } else if (startWeekDay == Calendar.SATURDAY) {
                if (remainderDays >= 1)
                    weekEndDays += 1
            }



            Log.d(TAG, "weekEndDaysInTimeFrame() - weeks: " + Integer.toString(weeks))
            Log.d(TAG, "weekEndDaysInTimeFrame() - remainderDays: " + Integer.toString(remainderDays))

            return weekEndDays
        }
    }
}
