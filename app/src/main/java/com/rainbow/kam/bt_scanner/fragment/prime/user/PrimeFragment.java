package com.rainbow.kam.bt_scanner.fragment.prime.user;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.prime.PrimeActivity;
import com.rainbow.kam.bt_scanner.adapter.prime.HistoryAdapter;
import com.rainbow.kam.bt_scanner.tools.RealmPrimeItem;
import com.rainbow.kam.bt_scanner.tools.design.CircleCounter;
import com.rainbow.kam.bt_scanner.tools.helper.NestedRecyclerViewHelper;
import com.rainbow.kam.bt_scanner.tools.helper.PrimeHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.realm.RealmResults;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class PrimeFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private int index;

    private Context context;
    private View view;
    private TextView labelTextView, valueTextView, dateTextView, timeTextView;

    private CardView primeCard, datetimeCard;
    private ImageView cardImageView;

    private CircleCounter circleCounter;
    private RecyclerView historyRecyclerView;

    private HistoryAdapter historyAdapter;


    public static PrimeFragment newInstance(int index) {
        PrimeFragment primeFragment = new PrimeFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(PrimeHelper.KEY_INDEX, index);
        primeFragment.setArguments(bundle);
        return primeFragment;
    }


    public int getIndex() {
        return getArguments().getInt(PrimeHelper.KEY_INDEX);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PrimeActivity) {
            this.context = context;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.f_prime, container, false);

        index = getIndex();
        setTextView();
        setCardView();
        setCircleCounterView();
        setRecyclerView();

        switch (index) {
            case PrimeHelper.INDEX_STEP:
                labelTextView.setText(getString(R.string.dashboard_step));
                cardImageView.setImageResource(R.drawable.step_wallpaper);
                circleCounter.setRange(20000);
                circleCounter.setMetricText(getString(R.string.prime_step));
                break;

            case PrimeHelper.INDEX_CALORIE:
                labelTextView.setText(getString(R.string.dashboard_calorie));
                cardImageView.setImageResource(R.drawable.calorie_wallpaper);
                circleCounter.setRange(100);
                circleCounter.setMetricText(getString(R.string.prime_calorie));
                break;

            case PrimeHelper.INDEX_DISTANCE:
                labelTextView.setText(getString(R.string.dashboard_distance));
                cardImageView.setImageResource(R.drawable.distance_wallpaper);
                circleCounter.setRange(10000);
                circleCounter.setMetricText(getString(R.string.prime_distance));
                break;
        }

        return view;
    }


    private void setTextView() {
        labelTextView = (TextView) view.findViewById(R.id.prime_label);
        valueTextView = (TextView) view.findViewById(R.id.prime_value);
        dateTextView = (TextView) view.findViewById(R.id.prime_date);
        timeTextView = (TextView) view.findViewById(R.id.prime_time);
    }


    private void setCardView() {
        primeCard = (CardView) view.findViewById(R.id.prime_card);
        datetimeCard = (CardView) view.findViewById(R.id.prime_card_datetime);
        cardImageView = (ImageView) view.findViewById(R.id.prime_card_image);
    }


    private void setCircleCounterView() {
        circleCounter = (CircleCounter) view.findViewById(R.id.counter);
        circleCounter.setFirstColor(ContextCompat.getColor(context, R.color.stepAccent));
        circleCounter.setSecondColor(ContextCompat.getColor(context, R.color.stepAccent));
        circleCounter.setThirdColor(ContextCompat.getColor(context, R.color.stepAccent));
        circleCounter.setBackgroundColor(ContextCompat.getColor(context, R.color.md_btn_selected_dark));
        circleCounter.setTextColor(ContextCompat.getColor(context, R.color.stepPrimaryDark));

    }


    private void setRecyclerView() {
        historyAdapter = new HistoryAdapter(index);
        historyRecyclerView = (RecyclerView) view.findViewById(R.id.history_recycler);
        RecyclerView.LayoutManager layoutManager = new NestedRecyclerViewHelper(context);
        historyRecyclerView.setLayoutManager(layoutManager);
        historyRecyclerView.setNestedScrollingEnabled(false);
        historyRecyclerView.setHasFixedSize(false);
        historyRecyclerView.setAdapter(historyAdapter);
    }


    public void setTime(Calendar calendar) {
        try {
            SimpleDateFormat date = new SimpleDateFormat("yy년 MM 월 dd일");
            SimpleDateFormat time = new SimpleDateFormat("HH시 mm분");

            this.dateTextView.setText(date.format(calendar.getTime()));
            this.timeTextView.setText(time.format(calendar.getTime()));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            setTextFail();
        }
    }


    public void setValue(int value) {
        try {
            valueTextView.setText(String.valueOf(value));
            circleCounter.setValues(value, value, value);
        } catch (Exception e) {
            setTextFail();
        }
    }


    public void setTextFail() {
        String accessDenial = getString(R.string.prime_access_denial);
        dateTextView.setText(accessDenial);
        timeTextView.setText(accessDenial);
        valueTextView.setText(accessDenial);
    }


    public void addHistory(RealmResults<RealmPrimeItem> results) {
        historyAdapter.add(results);
    }
}
