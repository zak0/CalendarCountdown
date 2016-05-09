package com.gmail.jaaska.jaakko.calendarcountdown;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class SetupActivity extends AppCompatActivity {

    private CountdownSettings settings;
    private CalendarView calendarViewEndDate;
    private CheckBox checkBoxExcludeWeekends;
    private Calendar calendar;
    private TextView textViewSetDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        // Enable up (or back) action to action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getActionBar().setDisplayHomeAsUpEnabled(true);

        // Read settings from Intent
        Intent intent = getIntent();
        settings = (CountdownSettings) intent.getSerializableExtra(CountdownSettings.extraName);

        setTitle("Setup Countdown");

        //calendarViewEndDate = (CalendarView) findViewById(R.id.calendarViewEndDate);
        checkBoxExcludeWeekends = (CheckBox) findViewById(R.id.checkBoxExcludeWeekends);
        textViewSetDate = (TextView) findViewById(R.id.textViewSetEndDate);

        textViewSetDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                if(settings.endDateIsValid()) {
                    cal.setTime(new Date(settings.getEndDate()));
                }
                DatePickerDialog dialog = new DatePickerDialog(SetupActivity.this, new EndDateSetListener(), cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
                dialog.setTitle("Set countdown end date");
                dialog.show();
            }
        });


        checkBoxExcludeWeekends.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                settings.setExcludeWeekends(isChecked);
            }
        });

        setExistingSettingsToViews();

    }

    /**
     * Sets the Views to correspond to the existing settings
     */
    private void setExistingSettingsToViews() {
        // IF first time setting up, set calendar to current date
        String dateString = new SimpleDateFormat("d.M.yyyy").format(new Date(settings.getEndDate()));
        textViewSetDate.setText(dateString);
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

    private class EndDateSetListener implements DatePickerDialog.OnDateSetListener {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);
            settings.setEndDate(calendar.getTimeInMillis());
            //settings.testHook();

            setExistingSettingsToViews();
        }
    }
}
