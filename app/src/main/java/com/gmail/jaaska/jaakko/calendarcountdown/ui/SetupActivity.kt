package com.gmail.jaaska.jaakko.calendarcountdown.ui

import android.app.DatePickerDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import com.gmail.jaaska.jaakko.calendarcountdown.R
import com.gmail.jaaska.jaakko.calendarcountdown.data.CountdownSettings
import com.gmail.jaaska.jaakko.calendarcountdown.storage.DatabaseHelper
import com.gmail.jaaska.jaakko.calendarcountdown.widget.CountdownAppWidget
import kotlinx.android.synthetic.main.activity_setup.*
import java.text.SimpleDateFormat
import java.util.*

class SetupActivity : AppCompatActivity() {

    // This can be lateinit as it should always be in Intent extras
    private lateinit var settings: CountdownSettings
    private var calendar: Calendar? = null

    private var db: DatabaseHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        db = DatabaseHelper(this, DatabaseHelper.DB_NAME, null, DatabaseHelper.DB_VERSION)

        // Hide up (or back) action to action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        // Read settings from Intent
        val intent = intent
        settings = intent.getSerializableExtra(CountdownSettings.extraName) as CountdownSettings

        title = "Setup Countdown"

        textViewSetEndDate.setOnClickListener {
            val cal = Calendar.getInstance()
            if (settings.endDateIsValid()) cal.time = Date(settings.endDate)

            val dialog = DatePickerDialog(this@SetupActivity, EndDateSetListener(),
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            dialog.setTitle("Set countdown end date")
            dialog.show()
        }

        editTextLabel.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                settings.label = s.toString()
            }

            override fun afterTextChanged(s: Editable) {

            }
        })

        checkBoxExcludeWeekends.setOnCheckedChangeListener { _, isChecked ->
            settings.isExcludeWeekends = isChecked
            recyclerViewExcludedDays.adapter.notifyDataSetChanged()
        }

        checkBoxWidget.setOnCheckedChangeListener { _, isChecked -> settings.isUseOnWidget = isChecked }

        val buttonAddExcludedDays = findViewById<View>(R.id.buttonAddExcludeRange) as Button
        buttonAddExcludedDays.setOnClickListener {
            val dlg = AddExcludedDaysDialog(this@SetupActivity, settings, recyclerViewExcludedDays)
            dlg.show()
        }

        val adapter = ExcludedDaysRecyclerViewAdapter(settings.excludedDays)
        recyclerViewExcludedDays.adapter = adapter
        recyclerViewExcludedDays.layoutManager = LinearLayoutManager(this)

        setExistingSettingsToViews()
    }

    /**
     * Sets the Views to correspond to the existing settings
     */
    private fun setExistingSettingsToViews() {
        // If first time setting up, set calendar to current date
        // TODO Consider using system default locale instead of Locale.US
        val dateString = SimpleDateFormat("d.M.yyyy", Locale.US).format(Date(settings.endDate))
        textViewSetEndDate.text = dateString
        checkBoxExcludeWeekends.isChecked = settings.isExcludeWeekends
        checkBoxWidget.isChecked = settings.isUseOnWidget == true
        editTextLabel.setText(settings.label)
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

    private inner class EndDateSetListener : DatePickerDialog.OnDateSetListener {
        override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
            calendar = GregorianCalendar(year, monthOfYear, dayOfMonth).also {
                settings.endDate = it.timeInMillis
            }
            //settings.testHook();

            setExistingSettingsToViews()
        }
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

    companion object {
        private const val TAG = "SetupActivity"
    }
}
