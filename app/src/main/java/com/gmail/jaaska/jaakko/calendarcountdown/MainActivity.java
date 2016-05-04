package com.gmail.jaaska.jaakko.calendarcountdown;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "CountdownPrefs";
    private TextView textViewEndDate;
    private TextView textViewDaysLeft;

    private CountdownSettings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = new CountdownSettings().loadFromSharedPrefs(getSharedPreferences(PREFS_NAME, 0));

        textViewEndDate = (TextView) findViewById(R.id.textViewEndDate);
        textViewDaysLeft = (TextView) findViewById(R.id.textViewDaysLeft);

        Button buttonSetup = (Button) findViewById(R.id.buttonSetup);
        buttonSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                setupIntent.putExtra(CountdownSettings.extraName, settings);
                startActivity(setupIntent);
            }
        });


    }

    /**
     * This will add stuff to the action bar
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        settings = new CountdownSettings().loadFromSharedPrefs(getSharedPreferences(PREFS_NAME, 0));

        if(settings.endDateIsValid()) {
            refreshViews();
        }
    }

    private void refreshViews() {
        String dateString = new SimpleDateFormat("d.M.yyyy").format(new Date(settings.getEndDate()));
        textViewEndDate.setText(dateString);
        textViewDaysLeft.setText(Integer.toString(settings.getDaysToEndDate())+ " days left");
    }
}
