package com.gmail.jaaska.jaakko.calendarcountdown;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by jaakko on 8.5.2016.
 */
public class ExcludedDaysRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView fromDate;
        public TextView toDate;
        public TextView daysCount;


        public ViewHolder(View itemView) {
            super(itemView);

            fromDate = (TextView) itemView.findViewById(R.id.textViewExclDateFrom);
            toDate = (TextView) itemView.findViewById(R.id.textViewExclDateTo);
            daysCount = (TextView) itemView.findViewById(R.id.textViewExclDaysCount);

            // TODO
        }
    }
}
