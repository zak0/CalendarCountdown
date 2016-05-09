package com.gmail.jaaska.jaakko.calendarcountdown;

import android.content.SharedPreferences;
import android.util.Log;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by jaakko on 2.5.2016.
 */
public class CountdownSettings implements Serializable{

    private final String TAG = "CountdownSettings";

    // used when passing CountdownSettings in Intents.
    public static final String extraName = "CountdownSettings";

    private long endDate;
    private boolean excludeWeekends;

    private String keyEndDate = "endDate";
    private String keyExcludeWeekends = "excludeWeekends";

    public CountdownSettings() {
        endDate = 0;
        excludeWeekends = false;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        Log.d(TAG, "setEndDate() - end date is now "+Long.toString(endDate));
        this.endDate = endDate;
    }

    public boolean isExcludeWeekends() {
        return excludeWeekends;
    }

    public void setExcludeWeekends(boolean excludeWeekends) {
        Log.d(TAG, "setExcludeWeekends() - excludeWeekends: "+Boolean.toString(excludeWeekends));
        this.excludeWeekends = excludeWeekends;
    }

    public CountdownSettings loadFromSharedPrefs(SharedPreferences sharedPreferences) {
        Log.d(TAG, "loadFromSharedPrefs() called");
        CountdownSettings ret = new CountdownSettings();

        ret.setEndDate(sharedPreferences.getLong(keyEndDate, 0));
        ret.setExcludeWeekends(sharedPreferences.getBoolean(keyExcludeWeekends, false));

        return ret;
    }

    public void saveToSharedPrefs(SharedPreferences sharedPreferences) {
        Log.d(TAG, "saveToSharedPrefs() called");
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putLong(keyEndDate, endDate);
        editor.putBoolean(keyExcludeWeekends, excludeWeekends);

        editor.commit();
    }

    public boolean endDateIsValid() {
        return endDate > 0;
    }

    private long getCurrentTimeWithOnlyDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(System.currentTimeMillis()));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis();
    }

    /**
     * Returns time in milliseconds to end date from now.
     * @return
     */
    public long getTimeToEndDate() {
        return endDate - getCurrentTimeWithOnlyDate();
    }

    /**
     * Returns amount of full days to the end date.
     * @return
     */
    public int getDaysToEndDate() {
        long toEnd = getTimeToEndDate();
        long days = toEnd / 1000 / 60 / 60 / 24;

        int ret = 0;

        if(excludeWeekends)
            ret = (int) days - weekEndDaysInTimeFrame(getCurrentTimeWithOnlyDate(), endDate);
        else
            ret = (int) days;


        Log.d(TAG, "getDaysToEndDate() - days: " +Integer.toString(ret));
        return ret;
    }

    /**
     * Method that can be called from outside the class.
     * JUST FOR DEVELOPMENT/TESTING PURPOSES
     */
    public void testHook() {
        //endDate = System.currentTimeMillis();
        //getDaysToEndDate();
        //weekEndDaysInTimeFrame(getCurrentTimeWithOnlyDate(), endDate);
    }

    /**
     * Returns number of weekend days (saturday and sunday) between startTime and endTime.
     * @param startTime
     * @param endTime
     * @return
     */
    private int weekEndDaysInTimeFrame(long startTime, long endTime) {
        int weekEndDays = 0; // number of saturdays and sundays

        Date startDate = new Date(startTime);
        Date endDate = new Date(endTime);
        Calendar cal = Calendar.getInstance();

        // convert start time to date only just in case
        cal.setTime(startDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // get weekdays of start and end date
        int startWeekDay = cal.get(Calendar.DAY_OF_WEEK);
        cal.setTime(endDate);
        int endWeekDay = cal.get(Calendar.DAY_OF_WEEK);

        // calculate full weeks within timeframe
        long timeFrame = endTime - getCurrentTimeWithOnlyDate();
        int weeks = (int) (timeFrame / 1000 / 60 / 60 / 24 / 7);
        int remainderDays = (int) (timeFrame / 1000 / 60 / 60 / 24 % 7);

        // add weekends of each full week
        weekEndDays += weeks * 2;

        // add possible remainder days that hit saturday or sunday
        if(startWeekDay == Calendar.THURSDAY) {
            if(remainderDays == 2)
                weekEndDays += 1;
            else if(remainderDays >= 2)
                weekEndDays += 2;
        }
        else if(startWeekDay == Calendar.FRIDAY) {
            if(remainderDays == 1)
                weekEndDays += 1;
            else if(remainderDays >= 1)
                weekEndDays += 2;
        }
        else if(startWeekDay == Calendar.SATURDAY) {
            if(remainderDays >= 1)
                weekEndDays += 1;
        }



        Log.d(TAG, "weekEndDaysInTimeFrame() - weeks: "+Integer.toString(weeks));
        Log.d(TAG, "weekEndDaysInTimeFrame() - remainderDays: "+Integer.toString(remainderDays));

        return weekEndDays;
    }

    public static class ExcludedDays {
        private long fromDate;
        private long toDate;
        private int daysCount;


    }
}
