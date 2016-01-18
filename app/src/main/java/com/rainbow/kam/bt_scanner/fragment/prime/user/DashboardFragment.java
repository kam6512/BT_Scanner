package com.rainbow.kam.bt_scanner.fragment.prime.user;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class DashboardFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private TextView dateTextView, timeTextView;
    private TextView stepTextView;
    private TextView calorieTextView;
    private TextView distanceTextView;

    private NestedScrollView nestedScrollView;


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
//        nestedScrollView.setNestedScrollingEnabled(false);
        nestedScrollView.setFillViewport(true);
        return view;
    }


    public void setTime(StringBuilder date, StringBuilder time) {
        try {
            this.dateTextView.setText(date);
            this.timeTextView.setText(time);
        } catch (Exception e) {
            setTextFail();
        }
    }


    public void setStepData(int step, int calorie, int distance) {
        try {
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
}
