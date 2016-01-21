package com.rainbow.kam.bt_scanner.fragment.prime.user;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.prime.PrimeActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class DashboardFragment extends Fragment implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();

    private int step, calorie, distance;

    private TextView dateTextView, timeTextView;
    private TextView stepTextView, calorieTextView, distanceTextView;
    private CardView stepCard, calorieCard, distanceCard;

    private NestedScrollView nestedScrollView;

    private OnClickCardListener onClickCardListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PrimeActivity) {
            onClickCardListener = (OnClickCardListener) context;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_prime_main_dashboard, container, false);

        dateTextView = (TextView) view.findViewById(R.id.dashboard_date);
        timeTextView = (TextView) view.findViewById(R.id.dashboard_time);
        stepTextView = (TextView) view.findViewById(R.id.dashboard_step);
        calorieTextView = (TextView) view.findViewById(R.id.dashboard_calorie);
        distanceTextView = (TextView) view.findViewById(R.id.dashboard_distance);
        nestedScrollView = (NestedScrollView) view.findViewById(R.id.dashboard_root);
        nestedScrollView.setFillViewport(true);

        stepCard = (CardView) view.findViewById(R.id.card_step);
        stepCard.setOnClickListener(this);
        calorieCard = (CardView) view.findViewById(R.id.card_calorie);
        calorieCard.setOnClickListener(this);
        distanceCard = (CardView) view.findViewById(R.id.card_distance);
        distanceCard.setOnClickListener(this);

        return view;
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


    public void setStepData(int step, int calorie, int distance) {
        try {
            this.step = step;
            this.calorie = calorie;
            this.distance = distance;
            stepTextView.setText(step + " 걸음");
            calorieTextView.setText(calorie + " kcal");
            distanceTextView.setText(distance + " M");
        } catch (Exception e) {
            setTextFail();
        }
    }


    public void setTextFail() {
        dateTextView.setText("Access Denial");
        timeTextView.setText("Access Denial");
        stepTextView.setText("Access Denial");
        calorieTextView.setText("Access Denial");
        distanceTextView.setText("Access Denial");
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.card_step:
                onClickCardListener.onStepClick(step);
                break;
            case R.id.card_calorie:
                onClickCardListener.onCalorieClick(calorie);
                break;
            case R.id.card_distance:
                onClickCardListener.onDistanceClick(distance);
                break;
        }
    }


    public interface OnClickCardListener {
        void onStepClick(int value);

        void onCalorieClick(int value);

        void onDistanceClick(int value);
    }
}
