package com.rainbow.kam.bt_scanner.fragment.prime.user;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.adapter.prime.HistoryAdapter;
import com.rainbow.kam.bt_scanner.tools.RealmPrimeItem;
import com.rainbow.kam.bt_scanner.tools.design.CircleCounter;
import com.rainbow.kam.bt_scanner.tools.helper.NestedRecyclerViewHelper;
import com.rainbow.kam.bt_scanner.tools.helper.PrimeHelper;

import io.realm.RealmResults;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class HistoryFragment extends Fragment implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();

    private Context context;

    private int step, calorie, distance;

    private View view;
    private CircleCounter circleCounter;

    private final int dotsCount = 3;
    private int currentCount = 0;
    private TextView[] dots;

    private final HistoryAdapter historyAdapter = new HistoryAdapter();


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.f_prime_main_history, container, false);

        setCircleCounter();
        setDotsLayout();
        setRecyclerView();
        setCurrentCounter(0);

        return view;
    }


    private void setCircleCounter() {
        circleCounter = (CircleCounter) view.findViewById(R.id.counter);
        circleCounter.setFirstColor(ContextCompat.getColor(context, R.color.stepAccent));
        circleCounter.setSecondColor(ContextCompat.getColor(context, R.color.stepAccent));
        circleCounter.setThirdColor(ContextCompat.getColor(context, R.color.stepAccent));
        circleCounter.setBackgroundColor(ContextCompat.getColor(context, R.color.md_btn_selected_dark));
        circleCounter.setTextColor(ContextCompat.getColor(context, R.color.stepPrimaryDark));
        circleCounter.setOnClickListener(this);
    }


    private void setDotsLayout() {

        LinearLayout dotsLayout = (LinearLayout) view.findViewById(R.id.pagerCountDots);
        dots = new TextView[dotsCount];
        for (int i = 0; i < dotsCount; i++) {
            dots[i] = new TextView(context);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(30);
            dots[i].setTextColor(ContextCompat.getColor(context, R.color.md_btn_selected_dark));
            dotsLayout.addView(dots[i]);
        }
    }


    private void setRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.history_recycler);
        RecyclerView.LayoutManager layoutManager = new NestedRecyclerViewHelper(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(false);
        recyclerView.setAdapter(historyAdapter);
    }


    private void setValue(int value) {
        circleCounter.setValues(value, value, value);
    }


    public void setCurrentCounter(final int position) {
        currentCount = position;
        for (int i = 0; i < dotsCount; i++) {
            dots[i].setTextColor(ContextCompat.getColor(context, R.color.md_btn_selected_dark));
        }
        dots[position].setTextColor(ContextCompat.getColor(context, R.color.stepPrimaryDark));

        switch (position) {
            case PrimeHelper.INDEX_STEP:
                circleCounter.setRange(20000);
                circleCounter.setMetricText(getString(R.string.prime_step));
                setValue(step);
                break;

            case PrimeHelper.INDEX_CALORIE:
                circleCounter.setRange(100);
                circleCounter.setMetricText(getString(R.string.prime_calorie));
                setValue(calorie);
                break;

            case PrimeHelper.INDEX_DISTANCE:
                circleCounter.setRange(10000);
                circleCounter.setMetricText(getString(R.string.prime_distance));
                setValue(distance);
                break;
        }
        historyAdapter.changeText(position);
    }


    @Override
    public void onClick(View v) {
        if (currentCount == dotsCount - 1) {
            currentCount = -1;
        }
        setCurrentCounter(++currentCount);
    }


    public void addHistory(RealmResults<RealmPrimeItem> results) {
        historyAdapter.add(results);
        step = results.last().getStep();
        calorie = results.last().getCalorie();
        distance = results.last().getDistance();
        setCurrentCounter(0);
    }
}
