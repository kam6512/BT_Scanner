package com.rainbow.kam.bt_scanner.adapter.prime;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.RealmPrimeItem;
import com.rainbow.kam.bt_scanner.tools.helper.PrimeHelper;

import java.util.ArrayList;
import java.util.Collections;

import hugo.weaving.DebugLog;
import io.realm.RealmResults;

/**
 * Created by Kam6512 on 2015-10-14.
 */
public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String TAG = getClass().getSimpleName();

    private int index;

    private final ArrayList<RealmPrimeItem> historyArrayList = new ArrayList<>();


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


    public void setCurrentIndex(int index) {
        this.index = index;
        notifyDataSetChanged();
    }


    @DebugLog
    public void add(RealmResults<RealmPrimeItem> results) {
        historyArrayList.clear();
        historyArrayList.addAll(results);
        Collections.reverse(historyArrayList);
        notifyDataSetChanged();
    }


    class HistoryViewHolder extends RecyclerView.ViewHolder {

        private final TextView history_text, history_date;
        String step;
        String calorie;
        String distance;
        String calendar;


        public HistoryViewHolder(View itemView) {
            super(itemView);
            history_text = (TextView) itemView.findViewById(R.id.history_text);
            history_date = (TextView) itemView.findViewById(R.id.history_date);
        }


        private void bindViews(RealmPrimeItem realmPrimeItem) {
            step = String.valueOf(realmPrimeItem.getStep());
            calorie = String.valueOf(realmPrimeItem.getCalorie());
            distance = String.valueOf(realmPrimeItem.getDistance());
            calendar = realmPrimeItem.getCalendar();

            history_date.setText(calendar);

            switch (index) {
                case PrimeHelper.INDEX_STEP:
                    history_text.setText(step);
                    break;
                case PrimeHelper.INDEX_CALORIE:
                    history_text.setText(calorie);
                    break;
                case PrimeHelper.INDEX_DISTANCE:
                    history_text.setText(distance);
                    break;
                default:
                    break;
            }
        }
    }
}