package com.gmail.jaaska.jaakko.calendarcountdown.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import android.view.View
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
class AddExcludedDaysDialog(private val context: Context,
                            private val settings: CountdownSettings,
                            private val onExcludedDaysAdded: () -> Unit) {

    private val contentView: View
    private var dateFrom: Long = 0
    private var dateTo: Long = 0
    private var dialog: AlertDialog? = null

    init {
        // Set dates negative to indicate that they're not yet set.
        dateFrom = -1
        dateTo = -1

        contentView = View.inflate(context, R.layout.dialog_excluded_days, null)

        dialog = AlertDialog.Builder(context).apply {
            setTitle(R.string.add_excluded_days_dialog_title)
            setView(contentView)
            setPositiveButton(R.string.add_excluded_days_dialog_add_button, null)
            setNegativeButton(R.string.common_cancel) { _, _ -> }
        }.create()

        // Custom OnClickListener is needed to make the dialog not dismiss when user tries to add
        // excluded days and to/from date is missing.
        dialog?.setOnShowListener {
            dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
                setOnClickListener { addRange() }
            }
        }

        contentView.fromCard.setOnClickListener {
            val cal = Calendar.getInstance()
            val dlg = DatePickerDialog(context, DateSetListener(0),
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            dlg.show()
        }

        contentView.toCard.setOnClickListener {
            val cal = Calendar.getInstance()
            val dlg = DatePickerDialog(context, DateSetListener(1),
                    cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            dlg.show()
        }
    }

    fun show() {
        dialog?.show()
    }

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
            dialog?.dismiss()
        } else {
            // Dates weren't valid...
            AlertDialog.Builder(context)
                    .setTitle(R.string.common_oops)
                    .setMessage(R.string.add_excluded_days_dialog_days_not_set_alert_message)
                    .setPositiveButton(R.string.common_okay) { _, _ -> }
                    .show()
        }
    }

    /**
     * Refreshes date views after date is set in a DatePickerDialog.
     *
     */
    private fun refreshViews() {
        if (dateFrom > 0) {
            val dateString = DateUtil.formatDate(dateFrom)
            contentView.fromSubtitle.text = dateString
        }

        if (dateTo > 0) {
            val dateString = DateUtil.formatDate(dateTo)
            contentView.toSubtitle.text = dateString
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
