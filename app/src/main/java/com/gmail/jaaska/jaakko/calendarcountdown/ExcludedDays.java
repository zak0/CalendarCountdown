package com.gmail.jaaska.jaakko.calendarcountdown;

import java.io.Serializable;

/**
 * Created by jaakko on 11.5.2016.
 */
public class ExcludedDays implements Serializable {

    private CountdownSettings settings;

    private long fromDate; // exclusion zone start
    private long toDate; // exclusion range end

    public ExcludedDays(CountdownSettings settings) {
        this.settings = settings;
    }

    public ExcludedDays(CountdownSettings settings, long fromDate, long toDate) {
        this.settings = settings;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public long getFromDate() {
        return fromDate;
    }

    public void setFromDate(long fromDate) {
        this.fromDate = fromDate;
    }

    public long getToDate() {
        return toDate;
    }

    public void setToDate(long toDate) {
        this.toDate = toDate;
    }

    /**
     * Returns number of days within this range that are later than today.
     * I.e. the number of days that need to be reduced from countdown.
     * @return
     */
    public int getDaysCount() {


        long from = fromDate > System.currentTimeMillis() ? fromDate : System.currentTimeMillis();
        int totalDays = CountdownSettings.daysInTimeFrame(from, toDate);

        // Reduce weekenddays from the excluded days
        // so that weekend days are not excluded twice)
        // if exclude weekends setting is on.
        if(settings.isExcludeWeekends()) {
            totalDays -= CountdownSettings.weekEndDaysInTimeFrame(from, toDate);
        }

        totalDays += 1; // +1 because days are calculated from 0:00 to 0:00
        totalDays = totalDays < 0 ? 0 : totalDays;

        return totalDays;
    }

    public void setSettings(CountdownSettings settings) {
        this.settings = settings;
    }
}
