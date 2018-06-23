package com.gmail.jaaska.jaakko.calendarcountdown.ui

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import com.gmail.jaaska.jaakko.calendarcountdown.R
import com.gmail.jaaska.jaakko.calendarcountdown.data.CountdownSettings
import com.gmail.jaaska.jaakko.calendarcountdown.data.GeneralSettings
import com.gmail.jaaska.jaakko.calendarcountdown.storage.DatabaseHelper
import java.util.*

class MainActivity : AppCompatActivity() {

    private var recyclerView: RecyclerView? = null
    private var adapter: CountdownsRecyclerViewAdapter? = null
    private var countdowns: List<CountdownSettings>? = null
    private var db: DatabaseHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        countdowns = ArrayList()

        db = DatabaseHelper(this, DatabaseHelper.DB_NAME, null, DatabaseHelper.DB_VERSION)

        recyclerView = findViewById<View>(R.id.recyclerViewCountdowns) as RecyclerView
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        adapter = CountdownsRecyclerViewAdapter(countdowns)
        recyclerView!!.adapter = adapter

        val fab = findViewById<View>(R.id.floatingActionButtonAddCountdown) as FloatingActionButton
        fab.setOnClickListener {
            val setupIntent = Intent(this@MainActivity, SetupActivity::class.java)
            setupIntent.putExtra(CountdownSettings.extraName, CountdownSettings())
            startActivity(setupIntent)
        }

    }

    /**
     * This will add stuff to the action bar
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        return true
    }

    /**
     * This handles action bar item clicks.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.menuitem_main_sort -> showSortOrderDialog()
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * Build and show dialog for selecting countdown sort order.
     */
    private fun showSortOrderDialog() {
        // Show the sort order dialog
        val sortOrderDialog = Dialog(this)
        sortOrderDialog.setTitle("Sort by")
        sortOrderDialog.setContentView(R.layout.dialog_sort_order)

        val cancelButton = sortOrderDialog.findViewById<View>(R.id.buttonCancel) as Button
        cancelButton.setOnClickListener { sortOrderDialog.dismiss() }

        val radioGroup = sortOrderDialog.findViewById<View>(R.id.radioSortOrder) as RadioGroup

        // Initialize the radio button to be checked on the current sorting order.
        val radioDaysLeft = sortOrderDialog.findViewById<View>(R.id.radioDaysLeft) as RadioButton
        val radioEventLabel = sortOrderDialog.findViewById<View>(R.id.radioEventLabel) as RadioButton

        when (GeneralSettings.getInstance().sortOrder) {
            GeneralSettings.SORT_BY_DAYS_LEFT -> radioDaysLeft.isChecked = true
            GeneralSettings.SORT_BY_EVENT_LABEL -> radioEventLabel.isChecked = true
        }

        radioGroup.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.radioDaysLeft ->
                    // Store into settings
                    GeneralSettings.getInstance().sortOrder = GeneralSettings.SORT_BY_DAYS_LEFT
                R.id.radioEventLabel ->
                    // Store into settings
                    GeneralSettings.getInstance().sortOrder = GeneralSettings.SORT_BY_EVENT_LABEL
            }

            sortOrderDialog.dismiss()

            // Sort and refresh
            refreshViews()

            // Save to db
            db?.apply {
                openDb()
                saveGeneralSettings()
                closeDb()
            }
        }

        sortOrderDialog.show()
    }

    override fun onResume() {
        super.onResume()

        Log.d(TAG, "onResume() - called")

        // Read settings from DB
        db?.apply {
            openDb()
            countdowns = loadSettings()
            loadGeneralSettings()
            close()
        }

        refreshViews()
    }

    private fun refreshViews() {
        Log.d(TAG, "refreshViews() - called")
        val adapter = CountdownsRecyclerViewAdapter(countdowns)
        recyclerView?.swapAdapter(adapter, true)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
