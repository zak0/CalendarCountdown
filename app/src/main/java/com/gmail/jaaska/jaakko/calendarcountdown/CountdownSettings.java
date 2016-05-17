package com.gmail.jaaska.jaakko.calendarcountdown;

import android.content.SharedPreferences;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by jaakko on 2.5.2016.
 */
public class CountdownSettings implements Serializable{

    private static final String TAG = "CountdownSettings";

    // used when passing CountdownSettings in Intents.
    public static final String extraName = "CountdownSettings";

    private long endDate; // "zero-date" of the countdown
    private boolean excludeWeekends; // are weekened excluded or not
    private boolean useOnWidget; // tells if this is the Countdown to show on a widget

    // SharedPreferences keys
    private String keyEndDate = "endDate";
    private String keyExcludeWeekends = "excludeWeekends";
    private String keyExcludedRangesFromDates = "excludedRangesFromDates";
    private String keyExcludedRangesToDates = "excludedRangesToDates";

    private List<ExcludedDays> excludedDays; // all the day ranges that are excluded from countdown

    private int dbId; // ID of the corresponding item in the DB
    private String label; // label of the countdown

    public CountdownSettings() {
        endDate = 0;
        excludeWeekends = false;
        excludedDays = new ArrayList<>();
        label = "";
        dbId = Integer.MIN_VALUE;
        useOnWidget = false;
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

    /**
     * THIS WILL BE OMITTED. NOW USES AN SQLITE DB FOR STORAGE
     *
     * @param sharedPreferences
     * @return
     */
    public CountdownSettings loadFromSharedPrefs(SharedPreferences sharedPreferences) {
        Log.d(TAG, "loadFromSharedPrefs() called");
        CountdownSettings ret = new CountdownSettings();

        ret.setEndDate(sharedPreferences.getLong(keyEndDate, 0));
        ret.setExcludeWeekends(sharedPreferences.getBoolean(keyExcludeWeekends, false));

        ArrayList<ExcludedDays> loadedExcludedDays = excludedDaysFromStringSet(sharedPreferences.getStringSet(keyExcludedRangesFromDates, new LinkedHashSet<String>()),
                                                                                sharedPreferences.getStringSet(keyExcludedRangesToDates, new LinkedHashSet<String>()));

        Log.d(TAG, "loadFromSharedPrefs() - loadedExcludedDays.size() = "+Integer.toString(loadedExcludedDays.size()));
        // Set correct ref to CountdownSettings for loadedExcludedDays
        for(int i = 0; i < loadedExcludedDays.size(); i++) {
            loadedExcludedDays.get(i).setSettings(ret);
        }

        ret.setExcludedDays(loadedExcludedDays);


        return ret;
    }

    /**
     * THIS WILL BE OMITTED. NOW USES AN SQLITE DB FOR STORAGE
     * @param sharedPreferences
     */
    public void saveToSharedPrefs(SharedPreferences sharedPreferences) {
        Log.d(TAG, "saveToSharedPrefs() called");
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putLong(keyEndDate, endDate);
        editor.putBoolean(keyExcludeWeekends, excludeWeekends);
        editor.putStringSet(keyExcludedRangesFromDates, excludedDaysToStringSet(0));
        editor.putStringSet(keyExcludedRangesToDates, excludedDaysToStringSet(1));

        editor.commit();
    }

    /**
     * THIS WILL BE OMITTED. NOW USES AN SQLITE DB FOR STORAGE
     *
     * Ugly way to store excluded days into SharedPrefs as it
     * supports Set<String>s and not ArrayList<>s directly.
     *
     *
     * @return
     */
    private Set<String> excludedDaysToStringSet(int toOrFrom) {

        Set<String> ret = new LinkedHashSet<>();

        ExcludedDays item;
        for(int i = 0; i < excludedDays.size(); i++) {
            item = excludedDays.get(i);
            if(toOrFrom == 0)
                ret.add(Long.toString(item.getFromDate()));
            else
                ret.add(Long.toString(item.getToDate()));

        }

        Log.d(TAG, "excludedFromDaysToStringSet() - converted " +Integer.toString(ret.size())+ " items");


        return ret;
    }



    /**
     * THIS WILL BE OMITTED. NOW USES AN SQLITE DB FOR STORAGE
     *
     *  Ugly way to store excluded days into SharedPrefs as it
     * supports Set<String>s and not ArrayList<>s directly.
     *
     * Reverse operation to excludedDaysToStringSet()
     *
     * @param fromSet
     * @param toSet
     * @return
     */
    private ArrayList<ExcludedDays> excludedDaysFromStringSet(Set<String> fromSet, Set<String> toSet) {


        ArrayList<ExcludedDays> ret = new ArrayList<>();

        Iterator<String> fromIter = fromSet.iterator();
        Iterator<String> toIter = toSet.iterator();

        ExcludedDays nextToAdd;
        while(fromIter.hasNext()) { // both iterators should have the same amount of items
            // Reference to CountDownSettings needs to be added later to ExcludedDays created here.

            nextToAdd = new ExcludedDays(null);
            nextToAdd.setToDate(Long.parseLong(toIter.next()));
            nextToAdd.setFromDate(Long.parseLong(fromIter.next()));
            ret.add(nextToAdd);
        }

        Log.d(TAG, "excludedDaysFromStringSet() - recovered " +Integer.toString(ret.size())+ " items");

        return ret;
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

        // TODO: Reduce excluded days.
        ret -= getExcludedDaysCountInTimeFrame(getCurrentTimeWithOnlyDate(), endDate);

        Log.d(TAG, "getDaysToEndDate() - days: " +Integer.toString(ret));
        return ret;
    }

    private int getExcludedDaysCountInTimeFrame(long from, long to) {
        int sum = 0;

        for(int i = 0; i < excludedDays.size(); i++) {
            sum += excludedDays.get(i).getDaysCount();
        }

        sum = sum < 0 ? 0 : sum;
        return sum;
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
     * Returns number of days between startTime and endTime.
     * @param startTime
     * @param endTime
     * @return
     */
    public static int daysInTimeFrame(long startTime, long endTime) {
        Date startDate = new Date(startTime);
        Calendar cal = Calendar.getInstance();

        // convert start time to date only just in case
        cal.setTime(startDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);


        // calculate full weeks within timeframe
        long timeFrame = endTime - cal.getTimeInMillis();
        int days = (int) (timeFrame / 1000 / 60 / 60 / 24);

        return days;
    }

    /**
     * Returns number of weekend days (saturday and sunday) between startTime and endTime.
     * @param startTime
     * @param endTime
     * @return
     */
    public static int weekEndDaysInTimeFrame(long startTime, long endTime) {
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
        long timeFrame = endTime - startTime; // getCurrentTimeWithOnlyDate();
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


    public void addExcludedDays(ExcludedDays days) {
        excludedDays.add(days);
    }

    public void setExcludedDays(List<ExcludedDays> excludedDays) {
        this.excludedDays = excludedDays;
    }

    public List<ExcludedDays> getExcludedDays() {
        Log.d(TAG, "getExcludedDays() - count: "+Integer.toString(excludedDays.size()));

        return excludedDays;
    }

    public int getDbId() {
        return dbId;
    }

    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isUseOnWidget() {
        return useOnWidget;
    }

    public void setUseOnWidget(boolean useOnWidget) {
        this.useOnWidget = useOnWidget;
    }
}
