package com.gmail.jaaska.jaakko.calendarcountdown.data

import java.io.Serializable

/**
 * Created by jaakko on 24.6.2018.
 */
class ExcludedDays : Serializable {

    private var settings: CountdownSettings? = null

    var fromDate: Long = 0 // exclusion zone start
    var toDate: Long = 0 // exclusion range end

    var dbId: Int = 0 // ID of the corresponding item in the DB

    /**
     * Number of days within this range that are later than today.
     * I.e. the number of days that need to be reduced from countdown.
     */
    val daysCount: Int
        get() {
            val from = if (fromDate > System.currentTimeMillis()) fromDate else System.currentTimeMillis()
            var totalDays = CountdownSettings.daysInTimeFrame(from, toDate)

            // Reduce weekenddays from the excluded days
            // so that weekend days are not excluded twice)
            // if exclude weekends setting is on.
            // +1 because days are calculated from 0:00 to 0:00
            if (settings?.isExcludeWeekends == true) {
                totalDays -= CountdownSettings.weekEndDaysInTimeFrame(from, toDate)
            }

            totalDays += 1
            totalDays = if (totalDays < 0) 0 else totalDays

            return totalDays
        }

    constructor() {
        dbId = Integer.MIN_VALUE
    }

    constructor(settings: CountdownSettings) {
        this.settings = settings
        dbId = Integer.MIN_VALUE
    }

    constructor(settings: CountdownSettings, fromDate: Long, toDate: Long) {
        this.settings = settings
        this.fromDate = fromDate
        this.toDate = toDate

        dbId = Integer.MIN_VALUE
    }

    fun setSettings(settings: CountdownSettings) {
        this.settings = settings
    }
}
