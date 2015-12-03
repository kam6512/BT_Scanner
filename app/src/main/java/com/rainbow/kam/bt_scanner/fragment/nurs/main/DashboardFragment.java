package com.rainbow.kam.bt_scanner.fragment.nurs.main;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

    private Activity activity = getActivity();
//    public static Handler handler;

    private View view;
    private TextView time;
    private TextView stepTextview;
    private TextView calorieTextview;
    private TextView distanceTextview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view = inflater.inflate(R.layout.fragmnet_nursing_main_dashboard_21, container, false);
        } else {
            view = inflater.inflate(R.layout.fragmnet_nursing_main_dashboard, container, false);
        }

        time = (TextView) view.findViewById(R.id.deviceTime);

        stepTextview = (TextView) view.findViewById(R.id.dashboard_step);
        calorieTextview = (TextView) view.findViewById(R.id.dashboard_calorie);
        distanceTextview = (TextView) view.findViewById(R.id.dashboard_distance);


        return view;
    }

    public void setTime(String characteristicTime) {
        time.setText("시간 : " + characteristicTime);
    }

    public void setStepData(Bundle stepData) {
        try {
            String step = stepData.getString("STEP");
            String calo = stepData.getString("CALO");
            String dist = stepData.getString("DIST") + "m";
            step = Integer.valueOf(step, 16) + "걸음";
            calo = Integer.valueOf(calo, 16) + "Kcal";

            stepTextview.setText(step);
            calorieTextview.setText(calo);
            distanceTextview.setText(dist);

        } catch (Exception e) {
            e.printStackTrace();
            setFail();
        }
    }

    public void setFail() {
        time.setText("시간 : 연결 실패");
        stepTextview.setText("연결 실패");
        calorieTextview.setText("연결 실패");
        distanceTextview.setText("연결 실패");
    }
}
