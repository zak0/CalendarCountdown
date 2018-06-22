package com.gmail.jaaska.jaakko.calendarcountdown.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.gmail.jaaska.jaakko.calendarcountdown.data.CountdownSettings;
import com.gmail.jaaska.jaakko.calendarcountdown.storage.DatabaseHelper;
import com.gmail.jaaska.jaakko.calendarcountdown.data.GeneralSettings;
import com.gmail.jaaska.jaakko.calendarcountdown.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private RecyclerView recyclerView;
    private CountdownsRecyclerViewAdapter adapter;

    private CountdownSettings settings;
    private List<CountdownSettings> countdowns;

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        countdowns = new ArrayList<>();


        db = new DatabaseHelper(this, DatabaseHelper.DB_NAME, null, DatabaseHelper.DB_VERSION);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewCountdowns);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CountdownsRecyclerViewAdapter(countdowns);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.floatingActionButtonAddCountdown);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                setupIntent.putExtra(CountdownSettings.extraName, new CountdownSettings());
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

    /**
     * This handles action bar item clicks.
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.menuitem_main_sort:
                // Show the sort order dialog
                final Dialog sortOrderDialog = new Dialog(this);
                sortOrderDialog.setTitle("Sort by");
                sortOrderDialog.setContentView(R.layout.dialog_sort_order);

                Button cancelButton = (Button) sortOrderDialog.findViewById(R.id.buttonCancel);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        sortOrderDialog.dismiss();
                    }
                });

                RadioGroup radioGroup = (RadioGroup) sortOrderDialog.findViewById(R.id.radioSortOrder);

                // Initialize the radio button to be checked on the current sorting order.
                RadioButton radioDaysLeft = (RadioButton) sortOrderDialog.findViewById(R.id.radioDaysLeft);
                RadioButton radioEventLabel = (RadioButton) sortOrderDialog.findViewById(R.id.radioEventLabel);

                switch (GeneralSettings.getInstance().getSortOrder()) {
                    case GeneralSettings.SORT_BY_DAYS_LEFT:
                        radioDaysLeft.setChecked(true);
                        break;
                    case GeneralSettings.SORT_BY_EVENT_LABEL:
                        radioEventLabel.setChecked(true);
                        break;
                }

                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int i) {
                        switch(i) {
                            case R.id.radioDaysLeft:
                                // Store into settings
                                GeneralSettings.getInstance().setSortOrder(GeneralSettings.SORT_BY_DAYS_LEFT);
                                break;
                            case R.id.radioEventLabel:
                                // Store into settings
                                GeneralSettings.getInstance().setSortOrder(GeneralSettings.SORT_BY_EVENT_LABEL);
                                break;
                        }

                        sortOrderDialog.dismiss();

                        // Sort and refresh
                        refreshViews();

                        // Save to db
                        db.openDb();
                        db.saveGeneralSettings();
                        db.closeDb();
                    }
                });

                sortOrderDialog.show();
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume() - called");

        // Read settings from DB
        db.openDb();
        countdowns = db.loadSettings();
        db.loadGeneralSettings();
        db.closeDb();

        refreshViews();

    }

    private void refreshViews() {
        Log.d(TAG, "refreshViews() - called");
        Collections.sort(countdowns);
        CountdownsRecyclerViewAdapter adapter = new CountdownsRecyclerViewAdapter(countdowns);
        recyclerView.swapAdapter(adapter, true);
        //recyclerView.getAdapter().notifyDataSetChanged();
    }
}
