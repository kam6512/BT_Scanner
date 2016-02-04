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
import com.rainbow.kam.bt_scanner.tools.RealmPrimeItem;

import java.util.ArrayList;
import java.util.Collections;

import hugo.weaving.DebugLog;
import io.realm.RealmResults;

/**
 * Created by Kam6512 on 2015-10-14.
 */
public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;

    private int index;

    private final ArrayList<RealmPrimeItem> historyArrayList = new ArrayList<>();

    private final Drawable[] iconDrawable;
    private final int[] unit = {R.string.prime_step, R.string.prime_calorie, R.string.prime_distance};


    public HistoryAdapter(Context context) {
        this.context = context;
        iconDrawable = new Drawable[]{ContextCompat.getDrawable(context, R.drawable.ic_directions_walk_white_36dp),
                ContextCompat.getDrawable(context, R.drawable.ic_whatshot_white_36dp),
                ContextCompat.getDrawable(context, R.drawable.ic_beenhere_white_36dp)};

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


    @DebugLog
    public void add(RealmResults<RealmPrimeItem> results) {
        historyArrayList.clear();
        addDummyData(results);
        historyArrayList.addAll(results);

        Collections.reverse(historyArrayList);
        notifyDataSetChanged();
    }


    private void addDummyData(RealmResults<RealmPrimeItem> results) {
        if (results.size() <= 31) {
            for (int i = results.size(); i < 31; i++) {
                historyArrayList.add(new RealmPrimeItem());
            }
        }
    }


    private class HistoryViewHolder extends RecyclerView.ViewHolder {

        private final TextView historyText, historyDate;
        private final ImageView historyImageView;
        private final int[] values = new int[3];
        private String calendar;


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
            if (TextUtils.isEmpty(calendar)) {
                calendar = "--";
            }

            historyText.setText(values[index] + context.getString(unit[index]));
            historyDate.setText(calendar);
            historyImageView.post(new Runnable() {
                @Override
                public void run() {
                    historyImageView.setImageDrawable(iconDrawable[index]);
                    historyImageView.setColorFilter(Color.parseColor("#0078ff"));
                }
            });
        }
    }
}