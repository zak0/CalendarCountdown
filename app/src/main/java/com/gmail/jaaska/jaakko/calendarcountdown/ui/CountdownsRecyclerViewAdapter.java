package com.gmail.jaaska.jaakko.calendarcountdown.ui;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gmail.jaaska.jaakko.calendarcountdown.data.CountdownSettings;
import com.gmail.jaaska.jaakko.calendarcountdown.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by jaakko on 16.5.2016.
 */
public class CountdownsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CountdownSettings> items;
    private static final String TAG = "CntdwnsRcclrVwAdap";

    public CountdownsRecyclerViewAdapter(List<CountdownSettings> items) {
        this.items = items;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_countdown, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CountdownSettings item = items.get(position);
        ViewHolder vh = (ViewHolder) holder;

        String dateString = new SimpleDateFormat("d.M.yyyy").format(new Date(item.getEndDate()));
        vh.textViewEndDate.setText(dateString);
        vh.textViewDaysCount.setText(Integer.toString(item.getDaysToEndDate()));
        vh.textViewLabel.setText(item.getLabel());
        vh.item = item;
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount() - items count: "+Integer.toString(items.size()));
        return items.size();
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewDaysCount;
        public TextView textViewLabel;
        public TextView textViewEndDate;

        public CountdownSettings item;

        public ViewHolder(View itemView) {
            super(itemView);

            textViewDaysCount = (TextView) itemView.findViewById(R.id.textViewCountdownDaysToGo);
            textViewLabel = (TextView) itemView.findViewById(R.id.textViewCountdownLabel);
            textViewEndDate = (TextView) itemView.findViewById(R.id.textViewCountdownEndDate);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent setupIntent = new Intent(v.getContext(), SetupActivity.class);
                    setupIntent.putExtra(CountdownSettings.extraName, item);
                    v.getContext().startActivity(setupIntent);
                }
            });

        }
    }
}
