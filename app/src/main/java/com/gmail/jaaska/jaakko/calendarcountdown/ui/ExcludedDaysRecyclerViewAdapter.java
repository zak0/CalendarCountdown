package com.gmail.jaaska.jaakko.calendarcountdown.ui;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmail.jaaska.jaakko.calendarcountdown.data.ExcludedDays;
import com.gmail.jaaska.jaakko.calendarcountdown.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by jaakko on 8.5.2016.
 */
public class ExcludedDaysRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ExcludedDays> items;

    public ExcludedDaysRecyclerViewAdapter(List<ExcludedDays> items) {
        this.items = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_excluded_days, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ExcludedDays item = items.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
        ((ViewHolder) holder).items = this.items;
        ((ViewHolder) holder).item = item;

        Date from = new Date(item.getFromDate());
        Date to = new Date(item.getToDate());
        String fromString = new SimpleDateFormat("d.M.yyyy").format(from);
        String toString = new SimpleDateFormat("d.M.yyyy").format(to);

        viewHolder.fromDate.setText("From " + fromString);
        viewHolder.toDate.setText("To " + toString);
        viewHolder.daysCount.setText(Integer.toString(item.getDaysCount()) + " days");

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView fromDate;
        public TextView toDate;
        public TextView daysCount;

        public List<ExcludedDays> items;
        public ExcludedDays item;


        public ViewHolder(View itemView) {
            super(itemView);

            fromDate = (TextView) itemView.findViewById(R.id.textViewExclDateFrom);
            toDate = (TextView) itemView.findViewById(R.id.textViewExclDateTo);
            daysCount = (TextView) itemView.findViewById(R.id.textViewExclDaysCount);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // TODO: Notify user somehow (refresh the RecyclerView or atleast throw a Toast??)
                    items.remove(item);
                    return true;
                }
            });
        }
    }
}
