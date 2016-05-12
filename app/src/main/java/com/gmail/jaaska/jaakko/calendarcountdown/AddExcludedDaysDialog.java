package com.gmail.jaaska.jaakko.calendarcountdown;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by jaakko on 11.5.2016.
 */
public class AddExcludedDaysDialog extends Dialog {

    private final String TAG = "AddExcludedDaysDialog";

    private View contentView;
    private TextView textViewFrom;
    private TextView textViewTo;
    private long dateFrom;
    private long dateTo;
    private RecyclerView recyclerView;

    private CountdownSettings settings;

    public AddExcludedDaysDialog(Context context, CountdownSettings settings, RecyclerView recyclerView) {
        super(context);

        // recyclerView is used to refresh the RecyclerView displaying the excluded ranges on SetupActivity.
        this.recyclerView = recyclerView;
        this.settings = settings;

        // Set dates negative to indicate that they're not yet set.
        dateFrom = -1;
        dateTo = -1;

        contentView = getLayoutInflater().inflate(R.layout.dialog_excluded_days, null);
        setContentView(contentView);

        textViewFrom = (TextView) findViewById(R.id.textViewExclDateDlgFrom);
        textViewTo = (TextView) findViewById(R.id.textViewExclDateDlgTo);

        textViewFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                DatePickerDialog dlg = new DatePickerDialog(getContext(), new DateSetListener(0), cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
                dlg.show();
            }
        });

        textViewTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                DatePickerDialog dlg = new DatePickerDialog(getContext(), new DateSetListener(1), cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
                dlg.show();
            }
        });

        Button buttonAdd = (Button) findViewById(R.id.buttonExclDateDlgAdd);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addRange();
            }
        });

        Button buttonCancel = (Button) findViewById(R.id.buttonExclDateDlgCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


    }

    /**
     * Adds set exclusion range into settings.
     */
    private void addRange() {
        // If set dates are valid...
        if(dateFrom > 0 && dateTo > 0 && dateTo > dateFrom) {
            Log.d(TAG, "addRange() - adding a new range");
            ExcludedDays range = new ExcludedDays(settings, dateFrom, dateTo);
            settings.addExcludedDays(range);
            recyclerView.getAdapter().notifyDataSetChanged();
            dismiss();
        }
    }



    /**
     * Refreshes date views after date is set in a DatePickerDialog.
     *
     */
    private void refreshViews() {
        if(dateFrom > 0) {
            String dateString = new SimpleDateFormat("d.M.yyyy").format(new Date(dateFrom));
            textViewFrom.setText("From " + dateString);
        }

        if(dateTo > 0) {
            String dateString = new SimpleDateFormat("d.M.yyyy").format(new Date(dateTo));
            textViewTo.setText("To " + dateString);
        }
    }

    private class DateSetListener implements DatePickerDialog.OnDateSetListener {
        private String TAG = "DateSetListener";

        private int mode; // is the dialog for FROM (0) or TO (1) date

        public DateSetListener(int mode) {
            this.mode = mode;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            Calendar cal = new GregorianCalendar(year, monthOfYear, dayOfMonth);
            if(mode == 0) {
                Log.d(TAG, "fromDate set to "+Long.toString(cal.getTimeInMillis()));
                dateFrom = cal.getTimeInMillis();
            }

            else {
                Log.d(TAG, "toDate set to "+Long.toString(cal.getTimeInMillis()));
                dateTo = cal.getTimeInMillis();
            }

            refreshViews();
        }
    }
}
