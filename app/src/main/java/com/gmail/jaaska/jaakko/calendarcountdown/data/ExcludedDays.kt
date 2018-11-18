package com.gmail.jaaska.jaakko.calendarcountdown.data

import com.gmail.jaaska.jaakko.calendarcountdown.util.DateUtil
import java.io.Serializable

/**
 * Created by jaakko on 24.6.2018.
 */
class ExcludedDays : Serializable {

    private var settings: CountdownSettings? = null

    var fromDate: String = "" // exclusion zone start as a "dd-MM-yyyy" string
    var toDate: String = "" // exclusion range end as a "dd-MM-yyyy" string

    var dbId: Int = 0 // ID of the corresponding item in the DB

    private val fromDateLong: Long
        get() = DateUtil.parseDatabaseDate(fromDate).time

    private val toDateLong: Long
        get() = DateUtil.parseDatabaseDate(toDate).time

    /**
     * Number of days within this range that are later than today.
     * I.e. the number of days that need to be reduced from countdown.
     */
    val daysCount: Int
        get() {

            val from = if (fromDateLong > System.currentTimeMillis()) fromDateLong else System.currentTimeMillis()
            var totalDays = CountdownSettings.daysInTimeFrame(from, toDateLong)

            // Reduce weekend days from the excluded days
            // so that weekend days are not excluded twice)
            // if exclude weekends setting is on.
            // +1 because days are calculated from 0:00 to 0:00
            if (settings?.isExcludeWeekends == true) {
                totalDays -= CountdownSettings.weekEndDaysInTimeFrame(from, toDateLong)
            }

            totalDays += 1
            totalDays = if (totalDays < 0) 0 else totalDays

            return totalDays
        }

    constructor() {
        dbId = Integer.MIN_VALUE
    }

    constructor(settings: CountdownSettings, fromDate: String, toDate: String) {
        this.settings = settings
        this.fromDate = fromDate
        this.toDate = toDate

        dbId = Integer.MIN_VALUE
    }

    fun setSettings(settings: CountdownSettings) {
        this.settings = settings
    }
}
