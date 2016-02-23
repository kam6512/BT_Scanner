package com.rainbow.kam.bt_scanner.adapter.prime;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.data.item.RealmPrimeItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Kam6512 on 2015-10-14.
 */
public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int index;

    private final ArrayList<RealmPrimeItem> historyArrayList = new ArrayList<>();

    private final List<Drawable> iconDrawable;
    private final List<String> unit;


    public HistoryAdapter(Context context) {
        iconDrawable = Arrays.asList(ContextCompat.getDrawable(context, R.drawable.ic_directions_walk_white_36dp),
                ContextCompat.getDrawable(context, R.drawable.ic_whatshot_white_36dp),
                ContextCompat.getDrawable(context, R.drawable.ic_beenhere_white_36dp));
        unit = Arrays.asList(context.getString(R.string.prime_step), context.getString(R.string.prime_calorie), context.getString(R.string.prime_distance));
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.i_prime_history, parent, false);
        return new HistoryViewHolder(view);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        HistoryViewHolder historyViewHolder = (HistoryViewHolder) holder;
        historyViewHolder.bindViews(historyArrayList.get(position));
    }


    @Override
    public int getItemCount() {
        return historyArrayList.size();
    }


    public void setCurrentIndex(int index) {
        this.index = index;
        notifyDataSetChanged();
    }


    public void setEmptyList() {
        historyArrayList.clear();
        for (int i = 0; i < 10; i++) {
            historyArrayList.add(new RealmPrimeItem());
        }
        notifyDataSetChanged();
    }


    public void setHistoryList(List<RealmPrimeItem> results) {
        historyArrayList.clear();
        historyArrayList.addAll(results);
        Collections.reverse(historyArrayList);
        notifyDataSetChanged();
    }


    private class HistoryViewHolder extends RecyclerView.ViewHolder {

        private final TextView historyText, historyDate;
        private final ImageView historyImageView;
        private List<Integer> values;
        private String calendar;


        public HistoryViewHolder(View itemView) {
            super(itemView);
            historyText = (TextView) itemView.findViewById(R.id.history_text);
            historyDate = (TextView) itemView.findViewById(R.id.history_date);
            historyImageView = (ImageView) itemView.findViewById(R.id.history_icon);
            historyImageView.setColorFilter(Color.parseColor("#0078ff"));
        }


        private void bindViews(RealmPrimeItem realmPrimeItem) {
            values = Arrays.asList(
                    realmPrimeItem.getStep(),
                    realmPrimeItem.getCalorie(),
                    realmPrimeItem.getDistance());

            calendar = realmPrimeItem.getCalendar();
            if (TextUtils.isEmpty(calendar)) {
                calendar = "--";
            }
            String history = values.get(index) + unit.get(index);
            historyText.setText(history);
            historyDate.setText(calendar);
            historyImageView.post(new Runnable() {
                @Override
                public void run() {
                    historyImageView.setImageDrawable(iconDrawable.get(index));
                }
            });
        }
    }
}