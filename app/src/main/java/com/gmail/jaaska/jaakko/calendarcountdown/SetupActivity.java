package com.gmail.jaaska.jaakko.calendarcountdown;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class SetupActivity extends AppCompatActivity {

    private CountdownSettings settings;
    private CalendarView calendarViewEndDate;
    private CheckBox checkBoxExcludeWeekends;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        Intent intent = getIntent();
        //settings = new CountdownSettings().loadFromSharedPrefs(getSharedPreferences(MainActivity.PREFS_NAME, 0));
        settings = (CountdownSettings) intent.getSerializableExtra(CountdownSettings.extraName);

        calendarViewEndDate = (CalendarView) findViewById(R.id.calendarViewEndDate);
        checkBoxExcludeWeekends = (CheckBox) findViewById(R.id.checkBoxExcludeWeekends);

        // TODO: Maybe read this from locale??
        calendarViewEndDate.setFirstDayOfWeek(Calendar.MONDAY);

        calendarViewEndDate.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                calendar = new GregorianCalendar(year, month, dayOfMonth);
                settings.setEndDate(calendar.getTimeInMillis());
                settings.testHook();
            }
        });

        checkBoxExcludeWeekends.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setExcludeWeekends(isChecked);
            }
        });

        setExistingSettings();
    }

    /**
     * Sets the Views to correspond to the existing settings
     */
    private void setExistingSettings() {
        // IF first time setting up, set calendar to current date
        long calDate = settings.endDateIsValid() ? settings.getEndDate() : System.currentTimeMillis();
        calendarViewEndDate.setDate(calDate, true, false);
        checkBoxExcludeWeekends.setChecked(settings.isExcludeWeekends());

    }

    @Override
    protected void onPause() {
        super.onPause();

        settings.saveToSharedPrefs(getSharedPreferences(MainActivity.PREFS_NAME, 0));
    }

    @Override
    protected void onStop() {
        super.onStop();

        settings.saveToSharedPrefs(getSharedPreferences(MainActivity.PREFS_NAME, 0));
    }
}
