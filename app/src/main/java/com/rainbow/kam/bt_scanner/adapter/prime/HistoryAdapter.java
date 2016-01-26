package com.rainbow.kam.bt_scanner.adapter.prime;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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


    private class HistoryViewHolder extends RecyclerView.ViewHolder {

        private final TextView historyText, historyDate;
        private ImageView historyImageView;
        private int[] values = new int[3];
        private String calendar;
        private int[] iconResource = {R.drawable.ic_directions_walk_white_36dp, R.drawable.ic_whatshot_white_36dp, R.drawable.ic_beenhere_white_36dp};


        public HistoryViewHolder(View itemView) {
            super(itemView);
            historyText = (TextView) itemView.findViewById(R.id.history_text);
            historyDate = (TextView) itemView.findViewById(R.id.history_date);
            historyImageView = (ImageView) itemView.findViewById(R.id.history_icon);
        }


        private void bindViews(RealmPrimeItem realmPrimeItem) {
            values[0] = realmPrimeItem.getStep();
            values[1] = realmPrimeItem.getCalorie();
            values[2] = realmPrimeItem.getDistance();
            calendar = realmPrimeItem.getCalendar();

            historyText.setText(String.valueOf(values[index]));
            historyDate.setText(calendar);
            historyImageView.setImageResource(iconResource[index]);
            historyImageView.setColorFilter(Color.parseColor("#0078ff"));
        }
    }
}