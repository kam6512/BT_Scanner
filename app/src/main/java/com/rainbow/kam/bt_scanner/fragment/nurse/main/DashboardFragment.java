package com.rainbow.kam.bt_scanner.fragment.nurse.main;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

    private TextView time;
    private TextView stepTextView;
    private TextView calorieTextView;
    private TextView distanceTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view = inflater.inflate(R.layout.fragmnet_nursing_main_dashboard_21, container, false);
        } else {
            view = inflater.inflate(R.layout.fragmnet_nursing_main_dashboard, container, false);
        }

        time = (TextView) view.findViewById(R.id.deviceTime);
        stepTextView = (TextView) view.findViewById(R.id.dashboard_step);
        calorieTextView = (TextView) view.findViewById(R.id.dashboard_calorie);
        distanceTextView = (TextView) view.findViewById(R.id.dashboard_distance);

        return view;
    }

    public void setTime(String characteristicTime) {
        try {
            time.setText("시간 : " + characteristicTime);
        } catch (Exception e) {
            setFail();
        }
    }

    public void setStepData(Bundle stepData) {

        String step = stepData.getString("STEP");
        String calorie = stepData.getString("CALORIE");
        String distance = stepData.getString("DISTANCE") + "m";

        if (step != null && calorie != null) {

            step = Integer.valueOf(step, 16) + "걸음";
            calorie = Integer.valueOf(calorie, 16) + "kcal";

            stepTextView.setText(step);
            calorieTextView.setText(calorie);
            distanceTextView.setText(distance);

        } else {
            setFail();
        }
    }

    private void setFail() {
        time.setText("시간 : 연결 실패");
        stepTextView.setText("연결 실패");
        calorieTextView.setText("연결 실패");
        distanceTextView.setText("연결 실패");
    }
}
