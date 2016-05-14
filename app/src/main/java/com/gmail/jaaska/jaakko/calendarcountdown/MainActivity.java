package com.gmail.jaaska.jaakko.calendarcountdown;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.SimpleDateFormat;
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

        /*
        Button buttonSetup = (Button) findViewById(R.id.buttonSetup);
        buttonSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                setupIntent.putExtra(CountdownSettings.extraName, settings);
                startActivity(setupIntent);
            }
        });
*/

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

    /**
     * This handles action bar item clicks.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.menuitem_main_setup:
                Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                setupIntent.putExtra(CountdownSettings.extraName, settings);
                startActivity(setupIntent);
                break;
        }


        return super.onOptionsItemSelected(item);
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
