package com.rainbow.kam.bt_scanner.adapter.prime;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.fragment.prime.user.HistoryFragment;
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

    private ArrayList<HistoryViewHolder> historyViewHolders = new ArrayList<>();


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
        historyViewHolders.add(serviceViewHolder);
    }


    @Override
    public int getItemCount() {
        return historyArrayList.size();
    }


    public void changeText(int dotsIndex) {
        for (HistoryViewHolder historyViewHolder : historyViewHolders) {
            historyViewHolder.onCircleCounterChange(dotsIndex);
        }
    }


    @DebugLog
    public void add(RealmResults<RealmPrimeItem> results) {
        historyArrayList.clear();
        historyArrayList.addAll(results);
        Collections.reverse(historyArrayList);
        notifyDataSetChanged();
    }


    class HistoryViewHolder extends RecyclerView.ViewHolder {

        private final TextView history_text;
        String step;
        String calorie;
        String distance;
        String calendar;


        public HistoryViewHolder(View itemView) {
            super(itemView);
            history_text = (TextView) itemView.findViewById(R.id.history_text);
        }


        private void bindViews(RealmPrimeItem realmPrimeItem) {
            step = String.valueOf(realmPrimeItem.getStep());
            calorie = String.valueOf(realmPrimeItem.getCalorie());
            distance = String.valueOf(realmPrimeItem.getDistance());
            calendar = realmPrimeItem.getCalendar();
            history_text.setText(step);
        }


        public void onCircleCounterChange(int dotsIndex) {
            switch (dotsIndex) {
                case 0:
                    history_text.setText(step);
                    break;
                case 1:
                    history_text.setText(calorie);
                    break;
                case 2:
                    history_text.setText(distance);
                    break;
            }
        }
    }
}