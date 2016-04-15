package com.rainbow.kam.bt_scanner.ui.adapter.nursing;

import android.content.Context;
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
import com.rainbow.kam.bt_scanner.data.item.UserMovementItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;

/**
 * Created by Kam6512 on 2015-10-14.
 */
public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private int index;

    private final ArrayList<UserMovementItem> historyArrayList = new ArrayList<>();

    private final List<Drawable> iconDrawable;
    private final List<String> unit;

    public HistoryAdapter(Context context) {
        iconDrawable = Arrays.asList(ContextCompat.getDrawable(context, R.drawable.ic_directions_walk_white_36dp),
                ContextCompat.getDrawable(context, R.drawable.ic_whatshot_white_36dp),
                ContextCompat.getDrawable(context, R.drawable.ic_beenhere_white_36dp));

        unit = Arrays.asList(context.getString(R.string.nursing_step), context.getString(R.string.nursing_calorie), context.getString(R.string.nursing_distance));
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.i_nursing_history, parent, false);
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
            historyArrayList.add(new UserMovementItem());
        }
        notifyDataSetChanged();
    }


    public void setHistoryList(List<UserMovementItem> results) {
        historyArrayList.clear();
        historyArrayList.addAll(results);
        Collections.reverse(historyArrayList);
        notifyDataSetChanged();
    }


    class HistoryViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.history_text) TextView historyText;
        @Bind(R.id.history_date) TextView historyDate;
        @Bind(R.id.history_icon) ImageView historyImageView;
        @BindColor(R.color.stepPrimaryDark) int iconFilterColor;

        private List<Integer> values;
        private String calendarString;


        public HistoryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            historyImageView.setColorFilter(iconFilterColor);
        }


        private void bindViews(UserMovementItem userMovementItem) {
            values = Arrays.asList(
                    userMovementItem.getStep(),
                    userMovementItem.getCalorie(),
                    userMovementItem.getDistance());

            calendarString = userMovementItem.getCalendar();

            if (TextUtils.isEmpty(calendarString)) {
                calendarString = "--";
            }
            historyText.setText(values.get(index) + unit.get(index));
            historyDate.setText(calendarString);
            historyImageView.post(() -> historyImageView.setImageDrawable(iconDrawable.get(index)));
        }
    }
}