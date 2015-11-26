package com.rainbow.kam.bt_scanner.Nursing.Fragment.Main;

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
    public static Handler handler;

    private View view;
    private TextView time;
    private TextView stepTextview, calorieTextview, distanceTextview;
    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;


    public static DashboardFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        DashboardFragment fragment = new DashboardFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    switch (msg.what) {
                        case 1:
                            time.setText("시간 : " + msg.obj);
                            break;
                        case 2:
                            Bundle bundle = msg.getData();
                            String step = bundle.getString("STEP");
                            String calo = bundle.getString("CALO");
                            String dist = bundle.getString("DIST") + "m";
                            step = Integer.valueOf(step, 16) + "걸음";
                            calo = Integer.valueOf(calo, 16) + "Kcal";

                            stepTextview.setText(step);
                            calorieTextview.setText(calo);
                            distanceTextview.setText(dist);
                            break;
                    }
                } catch (Exception e) {
                    time.setText("시간 : 연결 실패");
                    stepTextview.setText("연결 실패");
                    calorieTextview.setText("연결 실패");
                    distanceTextview.setText("연결 실패");
                }

            }
        };
    }

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
}
