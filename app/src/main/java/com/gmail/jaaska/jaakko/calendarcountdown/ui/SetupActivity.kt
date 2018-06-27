package com.gmail.jaaska.jaakko.calendarcountdown.ui

import android.app.DatePickerDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.gmail.jaaska.jaakko.calendarcountdown.R
import com.gmail.jaaska.jaakko.calendarcountdown.data.CountdownSettings
import com.gmail.jaaska.jaakko.calendarcountdown.storage.DatabaseHelper
import com.gmail.jaaska.jaakko.calendarcountdown.util.DateUtil
import com.gmail.jaaska.jaakko.calendarcountdown.widget.CountdownAppWidget
import kotlinx.android.synthetic.main.activity_setup.*
import kotlinx.android.synthetic.main.dialog_countdown_title.view.*
import kotlinx.android.synthetic.main.listitem_setup.view.*
import java.util.*

class SetupActivity : AppCompatActivity() {

    // This can be lateinit as it should always be in Intent extras
    private lateinit var settings: CountdownSettings
    private var db: DatabaseHelper? = null
    private var setupItems: ArrayList<Int> = ArrayList()
    private var adapter: SetupRecyclerViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        db = DatabaseHelper(this, DatabaseHelper.DB_NAME, DatabaseHelper.DB_VERSION)

        // Hide up (or back) action to action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        // Read settings from Intent
        val intent = intent
        settings = intent.getSerializableExtra(CountdownSettings.extraName) as CountdownSettings

        title = "Setup Countdown"

        setupItems.add(SetupItemType.TITLE)
        setupItems.add(SetupItemType.THE_DATE)
        setupItems.add(SetupItemType.EXCLUDED_DAYS)
        setupItems.add(SetupItemType.EXCLUDE_WEEKENDS)
        setupItems.add(SetupItemType.USE_ON_WIDGET)

        adapter = SetupRecyclerViewAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerView.adapter = adapter
    }

    override fun onPause() {
        Log.d(TAG, "onPause() - called")
        super.onPause()

        updateWidgets()
    }

    override fun onStop() {
        Log.d(TAG, "onStop() - called")
        super.onStop()
    }

    /**
     * Checks data entered by the user.
     */
    private fun validateInputs(): Boolean {
        return settings.endDate > 100
    }

    /**
     * Updates all visible widgets with possibly changed settings.
     */
    private fun updateWidgets() {
        val intent = Intent(this, CountdownAppWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val name = ComponentName(this, CountdownAppWidget::class.java)
        val ids = AppWidgetManager.getInstance(this).getAppWidgetIds(name)
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_setup, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.menuitem_setup_delete -> {
                db?.apply {
                    openDb()
                    deleteCountdown(settings)
                    closeDb()
                }

                // Point settings to new CountdownSettings so that input data validation fails and it is not saved to database.
                settings = CountdownSettings()
                finish()
            }
            R.id.menuitem_setup_done -> {
                // Save settings to DB
                db?.apply {
                    if (validateInputs()) {
                        // But only if entered data is OK.
                        openDb()
                        saveCountdownToDB(settings)
                        closeDb()
                    }
                }

                // Then finish() this activity.
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showSetTitleDialog() {
        val view = View.inflate(this, R.layout.dialog_countdown_title, null)

        if (settings.label.isNotEmpty()) {
            view.editTextTitle.setText(settings.label)
        }

        AlertDialog.Builder(this)
                .setView(view)
                .setTitle(R.string.setup_dialog_title_title)
                .setPositiveButton(R.string.common_done) { _, _ ->
                    settings.label = view.editTextTitle.text.toString()
                    adapter?.notifyDataSetChanged()
                }
                .setNegativeButton(R.string.common_cancel) { _, _ -> }
                .show()
    }

    private fun showSetDateDialog() {
        val calendar = Calendar.getInstance().apply { time = Date(settings.endDate) }
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                settings.endDate = calendar.timeInMillis
                adapter?.notifyDataSetChanged()
        }, calendar[Calendar.YEAR], calendar[Calendar.MONTH], calendar[Calendar.DAY_OF_MONTH])
                .show()

    }

    private object SetupItemType {
        const val TITLE = 100
        const val THE_DATE = 200
        const val EXCLUDE_WEEKENDS = 300
        const val EXCLUDED_DAYS = 400
        const val USE_ON_WIDGET = 500
    }

    private inner class SetupRecyclerViewAdapter : RecyclerView.Adapter<SetupItemViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetupItemViewHolder {
            val view = layoutInflater.inflate(R.layout.listitem_setup, parent, false)
            return SetupItemViewHolder(view)
        }

        override fun getItemCount(): Int {
            return setupItems.size
        }

        override fun onBindViewHolder(holder: SetupItemViewHolder, position: Int) {
            val item = setupItems[position]

            holder.itemView.apply {
                when(item) {
                    SetupItemType.TITLE -> {
                        title.text = getString(R.string.setup_setting_countdown_title)
                        subtitle.text = settings.label
                        setOnClickListener { showSetTitleDialog() }
                    }
                    SetupItemType.THE_DATE -> {
                        title.text = getString(R.string.setup_setting_end_date)
                        subtitle.text = DateUtil.formatDate(settings.endDate)
                        setOnClickListener { showSetDateDialog() }
                    }
                    SetupItemType.EXCLUDE_WEEKENDS -> {
                        title.text = getString(R.string.setup_setting_exclude_weekends)
                        setupCheckbox.visibility = View.VISIBLE
                        setupCheckbox.isChecked = settings.isExcludeWeekends
                        setupCheckbox.setOnCheckedChangeListener { _, checked ->
                            settings.isExcludeWeekends = checked
                            adapter?.notifyDataSetChanged()
                        }
                        setOnClickListener {
                            Handler().postDelayed({
                                setupCheckbox.isChecked = !setupCheckbox.isChecked
                            }, 300)
                        }
                        subtitle.text = if (settings.isExcludeWeekends) {
                            val now = System.currentTimeMillis()
                            getString(R.string.setup_setting_exclude_weekends_subtitle_enabled,
                                    CountdownSettings.weekEndDaysInTimeFrame(now, settings.endDate))
                        } else {
                            getString(R.string.setup_setting_exclude_weekends_subtitle_disabled)
                        }
                    }
                    SetupItemType.EXCLUDED_DAYS -> {
                        title.text = getString(R.string.setup_setting_excluded_days)
                        subtitle.text = if (settings.excludedDays.isNotEmpty()) {
                            getString(R.string.setup_setting_excluded_days_subtitle_set,
                                    settings.getExcludedDaysCount())
                        } else {
                            getString(R.string.setup_setting_excluded_days_subtitle_none)
                        }
                    }
                    SetupItemType.USE_ON_WIDGET -> {
                        title.text = getString(R.string.setup_setting_use_on_widget)
                        setupCheckbox.visibility = View.VISIBLE
                        setupCheckbox.isChecked = settings.isUseOnWidget
                        setupCheckbox.setOnCheckedChangeListener { _, checked ->
                            settings.isUseOnWidget = checked
                            adapter?.notifyDataSetChanged()
                        }
                        setOnClickListener {
                            Handler().postDelayed({
                                setupCheckbox.isChecked = !setupCheckbox.isChecked
                            }, 300)
                        }
                        subtitle.text = if (settings.isUseOnWidget) {
                            getString(R.string.setup_setting_use_on_widget_subtitle_enabled)
                        } else {
                            getString(R.string.setup_setting_use_on_widget_subtitle_disabled)
                        }
                    }
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            return setupItems[position]
        }
    }


    private inner class SetupItemViewHolder(view: View) : RecyclerView.ViewHolder(view)

    companion object {
        private const val TAG = "SetupActivity"
    }
}
