package com.rainbow.kam.bt_scanner.fragment.prime.user;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

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


    public void setTime(Calendar calendar) {
        try {
            SimpleDateFormat date = new SimpleDateFormat("yy년 MM 월 dd일");
            SimpleDateFormat time = new SimpleDateFormat("HH시 mm분");

            this.dateTextView.setText(date.format(calendar.getTime()));
            this.timeTextView.setText(time.format(calendar.getTime()));
        } catch (Exception e) {
            Log.e(TAG,e.getMessage());
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
