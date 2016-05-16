package com.gmail.jaaska.jaakko.calendarcountdown;

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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private RecyclerView recyclerView;

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
        CountdownsRecyclerViewAdapter adapter = new CountdownsRecyclerViewAdapter(countdowns);
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
                // TODO change sorting or show dialog or something...
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
        db.closeDb();

        refreshViews();

    }

    private void refreshViews() {
        Log.d(TAG, "refreshViews() - called");
        CountdownsRecyclerViewAdapter adapter = new CountdownsRecyclerViewAdapter(countdowns);
        recyclerView.swapAdapter(adapter, true);
        //recyclerView.getAdapter().notifyDataSetChanged();
    }
}
