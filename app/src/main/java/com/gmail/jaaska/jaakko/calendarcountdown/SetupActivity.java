package com.gmail.jaaska.jaakko.calendarcountdown;

import android.app.DatePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class SetupActivity extends AppCompatActivity {

    private static final String TAG = "SetupActivity";

    private CountdownSettings settings;
    private CheckBox checkBoxExcludeWeekends;
    private Calendar calendar;
    private TextView textViewSetDate;
    private EditText editTextLabel;
    private RecyclerView recyclerViewExcludedRanges;

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        db = new DatabaseHelper(this, DatabaseHelper.DB_NAME, null, DatabaseHelper.DB_VERSION);

        // Hide up (or back) action to action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        // Read settings from Intent
        Intent intent = getIntent();
        settings = (CountdownSettings) intent.getSerializableExtra(CountdownSettings.extraName);

        setTitle("Setup Countdown");

        checkBoxExcludeWeekends = (CheckBox) findViewById(R.id.checkBoxExcludeWeekends);
        textViewSetDate = (TextView) findViewById(R.id.textViewSetEndDate);
        recyclerViewExcludedRanges = (RecyclerView) findViewById(R.id.recyclerViewExcludedDays);
        editTextLabel = (EditText) findViewById(R.id.editTextLabel);

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

        editTextLabel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                settings.setLabel(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

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
        editTextLabel.setText(settings.getLabel());

    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause() - called");
        super.onPause();

        // Save settings to DB
        if(validateInputs()) {
            // But only if entered data is OK.
            db.openDb();
            db.saveCountdownToDB(settings);
            db.closeDb();
        }

        updateWidgets();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop() - called");
        super.onStop();


    }

    /**
     * Checks data entered by the user.
     * @return true if entered data is valid
     */
    private boolean validateInputs() {

        if(settings.getEndDate() > 100)
            return true;

        return false;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.menuitem_setup_delete:
                db.openDb();
                db.deleteCountdown(settings);
                db.closeDb();

                // Point settings to new CountdownSettings so that input data validation fails and it is not saved to database.
                settings = new CountdownSettings();
                finish();
                break;
            case R.id.menuitem_setup_done:
                // Saving is done at onPause(), so we can just finish() this Activity.
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
