package zak0.github.calendarcountdown.ui

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import zak0.github.calendarcountdown.R
import zak0.github.calendarcountdown.data.CountdownSettings
import zak0.github.calendarcountdown.data.GeneralSettings
import zak0.github.calendarcountdown.storage.DatabaseHelper
import zak0.github.calendarcountdown.widget.CountdownAppWidgetProvider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_sort_order.*

class MainActivity : AppCompatActivity() {

    private var adapter: CountdownsRecyclerViewAdapter? = null
    private var countdowns: ArrayList<CountdownSettings>? = null
    private var db: DatabaseHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        countdowns = ArrayList()

        db = DatabaseHelper(this, DatabaseHelper.DB_NAME, DatabaseHelper.DB_VERSION)

        recyclerViewCountdowns.layoutManager = LinearLayoutManager(this)
        adapter = CountdownsRecyclerViewAdapter(countdowns ?: ArrayList())
        recyclerViewCountdowns.adapter = adapter

        floatingActionButtonAddCountdown.setOnClickListener {
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

        sortOrderDialog.buttonCancel.setOnClickListener { sortOrderDialog.dismiss() }

        // Initialize the radio button to be checked on the current sorting order.
        when (GeneralSettings.sortOrder) {
            GeneralSettings.SORT_BY_DAYS_LEFT -> sortOrderDialog.radioDaysLeft.isChecked = true
            GeneralSettings.SORT_BY_EVENT_LABEL -> sortOrderDialog.radioEventLabel.isChecked = true
        }

        sortOrderDialog.radioSortOrder.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.radioDaysLeft ->
                    // Store into settings
                    GeneralSettings.sortOrder = GeneralSettings.SORT_BY_DAYS_LEFT
                R.id.radioEventLabel ->
                    // Store into settings
                    GeneralSettings.sortOrder = GeneralSettings.SORT_BY_EVENT_LABEL
            }

            sortOrderDialog.dismiss()

            // Sort and refresh
            refreshViews()

            // Refresh also possible home screen widgets
            CountdownAppWidgetProvider.sendRefreshBroadcast(this)

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
            countdowns = ArrayList(loadSettings())
            loadGeneralSettings()
            close()
        }

        refreshViews()
    }

    private fun refreshViews() {
        Log.d(TAG, "refreshViews() - called")
        countdowns?.sort()
        val adapter = CountdownsRecyclerViewAdapter(countdowns ?: ArrayList())
        recyclerViewCountdowns.swapAdapter(adapter, true)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
