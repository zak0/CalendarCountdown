package com.gmail.jaaska.jaakko.calendarcountdown.ui

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import com.gmail.jaaska.jaakko.calendarcountdown.R
import com.gmail.jaaska.jaakko.calendarcountdown.data.CountdownSettings
import com.gmail.jaaska.jaakko.calendarcountdown.data.ExcludedDays
import com.gmail.jaaska.jaakko.calendarcountdown.util.DateUtil
import kotlinx.android.synthetic.main.dialog_excluded_days.view.*
import java.util.*

/**
 * Created by jaakko on 24.6.2018.
 */
class AddExcludedDaysDialog(context: Context,
                            private val settings: CountdownSettings,
                            private val onExcludedDaysAdded: () -> Unit) : Dialog(context) {

    private val contentView: View
    private var dateFrom: Long = 0
    private var dateTo: Long = 0

    init {

        // Set dates negative to indicate that they're not yet set.
        dateFrom = -1
        dateTo = -1

        contentView = View.inflate(context, R.layout.dialog_excluded_days, null)
        setContentView(contentView)

        contentView.textViewExclDateDlgFrom.setOnClickListener {
            val cal = Calendar.getInstance()
            val dlg = DatePickerDialog(getContext(), DateSetListener(0), cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            dlg.show()
        }

        contentView.textViewExclDateDlgTo.setOnClickListener {
            val cal = Calendar.getInstance()
            val dlg = DatePickerDialog(getContext(), DateSetListener(1), cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            dlg.show()
        }

        val buttonAdd = findViewById<View>(R.id.buttonExclDateDlgAdd) as Button
        buttonAdd.setOnClickListener { addRange() }

        val buttonCancel = findViewById<View>(R.id.buttonExclDateDlgCancel) as Button
        buttonCancel.setOnClickListener { dismiss() }


    }// recyclerView is used to refresh the RecyclerView displaying the excluded ranges on SetupActivity.

    /**
     * Adds set exclusion range into settings.
     */
    private fun addRange() {
        // If set dates are valid...
        if (dateFrom > 0 && dateTo > 0 && dateTo > dateFrom) {
            Log.d(TAG, "addRange() - adding a new range")
            val range = ExcludedDays(settings, dateFrom, dateTo)
            settings.addExcludedDays(range)
            onExcludedDaysAdded()
            dismiss()
        }
    }


    /**
     * Refreshes date views after date is set in a DatePickerDialog.
     *
     */
    private fun refreshViews() {
        if (dateFrom > 0) {
            val dateString = DateUtil.formatDate(dateFrom)
            contentView.textViewExclDateDlgFrom.text = context.getString(R.string.setup_excluded_days_from, dateString)
        }

        if (dateTo > 0) {
            val dateString = DateUtil.formatDate(dateTo)
            contentView.textViewExclDateDlgTo.text = context.getString(R.string.setup_excluded_days_to, dateString)
        }
    }

    private inner class DateSetListener(private val mode: Int // is the dialog for FROM (0) or TO (1) date
    ) : DatePickerDialog.OnDateSetListener {
        override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
            val cal = GregorianCalendar(year, monthOfYear, dayOfMonth)
            if (mode == 0) {
                Log.d(TAG, "fromDate set to " + java.lang.Long.toString(cal.timeInMillis))
                dateFrom = cal.timeInMillis
            } else {
                Log.d(TAG, "toDate set to " + java.lang.Long.toString(cal.timeInMillis))
                dateTo = cal.timeInMillis
            }
            refreshViews()
        }
    }

    companion object {
        private val TAG = AddExcludedDaysDialog::class.java.simpleName
    }
}
