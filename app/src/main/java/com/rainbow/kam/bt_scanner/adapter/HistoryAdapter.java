package com.rainbow.kam.bt_scanner.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.RealmPrimeItem;

import java.util.ArrayList;
import java.util.Collections;

import hugo.weaving.DebugLog;
import io.realm.RealmResults;

/**
 * Created by Kam6512 on 2015-10-14.
 */
public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String TAG = getClass().getSimpleName();

    private ArrayList<RealmPrimeItem> historyArrayList = new ArrayList<>();


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.i_prime_history, parent, false);
        return new HistoryViewHolder(view);
    }


    @DebugLog
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        HistoryViewHolder serviceViewHolder = (HistoryViewHolder) holder;
        serviceViewHolder.bindViews(historyArrayList.get(position));
    }


    @Override
    public int getItemCount() {
        return historyArrayList.size();
    }


    @DebugLog
    public void add(RealmResults<RealmPrimeItem> results) {
        historyArrayList.clear();
        historyArrayList.addAll(results);
        Collections.reverse(historyArrayList);
        notifyDataSetChanged();
    }


    public class HistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView stepTextView;
        private final TextView calorieTextView;
        private final TextView distanceTextView;
        private final TextView calendarTextView;


        public HistoryViewHolder(View itemView) {
            super(itemView);
            stepTextView = (TextView) itemView.findViewById(R.id.prime_history_step);
            calorieTextView = (TextView) itemView.findViewById(R.id.prime_history_calorie);
            distanceTextView = (TextView) itemView.findViewById(R.id.prime_history_distance);
            calendarTextView = (TextView) itemView.findViewById(R.id.prime_history_calendar);
            itemView.setOnClickListener(this);
        }


        private void bindViews(RealmPrimeItem realmPrimeItem) {
            String step = String.valueOf(realmPrimeItem.getStep());
            String calorie = String.valueOf(realmPrimeItem.getCalorie());
            String distance = String.valueOf(realmPrimeItem.getDistance());
            String calendar = realmPrimeItem.getCalendar();
            stepTextView.setText(step);
            calorieTextView.setText(calorie);
            distanceTextView.setText(distance);
            calendarTextView.setText(calendar);
        }


        @Override
        public void onClick(View v) {

        }
    }
}