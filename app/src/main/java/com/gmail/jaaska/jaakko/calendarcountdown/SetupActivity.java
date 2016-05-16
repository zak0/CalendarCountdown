package com.gmail.jaaska.jaakko.calendarcountdown;

import android.app.DatePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class SetupActivity extends AppCompatActivity {

    private static final String TAG = "SetupActivity";

    private CountdownSettings settings;
    private CheckBox checkBoxExcludeWeekends;
    private Calendar calendar;
    private TextView textViewSetDate;
    private RecyclerView recyclerViewExcludedRanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        // Enable up (or back) action to action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Read settings from Intent
        Intent intent = getIntent();
        settings = (CountdownSettings) intent.getSerializableExtra(CountdownSettings.extraName);

        setTitle("Setup Countdown");

        checkBoxExcludeWeekends = (CheckBox) findViewById(R.id.checkBoxExcludeWeekends);
        textViewSetDate = (TextView) findViewById(R.id.textViewSetEndDate);
        recyclerViewExcludedRanges = (RecyclerView) findViewById(R.id.recyclerViewExcludedDays);

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
                recyclerViewExcludedRanges.getAdapter().notifyDataSetChanged();
            }
        });

        Button buttonAddExcludedDays = (Button) findViewById(R.id.buttonAddExcludeRange);
        buttonAddExcludedDays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddExcludedDaysDialog dlg = new AddExcludedDaysDialog(SetupActivity.this, settings, recyclerViewExcludedRanges);
                dlg.show();
            }
        });

        ExcludedDaysRecyclerViewAdapter adapter = new ExcludedDaysRecyclerViewAdapter(settings.getExcludedDays());
        recyclerViewExcludedRanges.setAdapter(adapter);
        recyclerViewExcludedRanges.setLayoutManager(new LinearLayoutManager(this));

        setExistingSettingsToViews();

    }

    /**
     * Sets the Views to correspond to the existing settings
     */
    private void setExistingSettingsToViews() {
        // If first time setting up, set calendar to current date
        String dateString = new SimpleDateFormat("d.M.yyyy").format(new Date(settings.getEndDate()));
        textViewSetDate.setText(dateString);
        checkBoxExcludeWeekends.setChecked(settings.isExcludeWeekends());

    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause() - called");
        super.onPause();

        // Save settings to DB
        // As a list for future support of multiple countdowns.
        DatabaseHelper db = new DatabaseHelper(this, DatabaseHelper.DB_NAME, null, DatabaseHelper.DB_VERSION);
        db.openDb();
        List<CountdownSettings> list = new ArrayList<>();
        list.add(settings);
        db.saveToDB(list);
        db.closeDb();

        //settings.saveToSharedPrefs(getSharedPreferences(MainActivity.PREFS_NAME, 0));
        updateWidgets();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop() - called");
        super.onStop();


    }

    /**
     * Updates all visible widgets with possibly changed settings.
     */
    private void updateWidgets() {
        Intent intent = new Intent(this, CountdownAppWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        ComponentName name = new ComponentName(this, CountdownAppWidget.class);
        int [] ids = AppWidgetManager.getInstance(this).getAppWidgetIds(name);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        sendBroadcast(intent);
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
