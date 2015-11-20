package com.rainbow.kam.bt_scanner.Nursing.Fragment.Main;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.Tools.Design.CircleCounter;

/**
 * Created by sion on 2015-11-04.
 */
public class StepFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private Activity activity;
    public static Handler handler;
    private Bundle bundle;
    private View view;
    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;

    private CircleCounter stepCircleCounter, calorieCircleCounter, distanceCircleCounter, etcCircleCounter;

    private int step;
    private int calo;
    private int dist;

    public static StepFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        StepFragment fragment = new StepFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        switch (mPage) {
            case 2:
                view = inflater.inflate(R.layout.fragmnet_nursing_main_step, container, false);
                stepCircleCounter = (CircleCounter) view.findViewById(R.id.step_counter);
                stepCircleCounter.setFirstWidth(getResources().getDimension(R.dimen.first));
                stepCircleCounter.setSecondWidth(getResources().getDimension(R.dimen.second));
                stepCircleCounter.setThirdWidth(getResources().getDimension(R.dimen.third));
                stepCircleCounter.setFirstColor(getResources().getColor(R.color.stepAccent));
                stepCircleCounter.setSecondColor(getResources().getColor(R.color.stepPrimary));
                stepCircleCounter.setThirdColor(getResources().getColor(R.color.stepPrimaryDark));
                stepCircleCounter.setBackgroundColor(getResources().getColor(R.color.stepColor));
                break;
            case 3:
                view = inflater.inflate(R.layout.fragmnet_nursing_main_calorie, container, false);
                calorieCircleCounter = (CircleCounter) view.findViewById(R.id.calorie_counter);
                calorieCircleCounter.setFirstWidth(getResources().getDimension(R.dimen.first));
                calorieCircleCounter.setSecondWidth(getResources().getDimension(R.dimen.second));
                calorieCircleCounter.setThirdWidth(getResources().getDimension(R.dimen.third));
                calorieCircleCounter.setFirstColor(getResources().getColor(R.color.caloAccent));
                calorieCircleCounter.setSecondColor(getResources().getColor(R.color.caloPrimary));
                calorieCircleCounter.setThirdColor(getResources().getColor(R.color.caloPrimaryDark));
                calorieCircleCounter.setBackgroundColor(getResources().getColor(R.color.caloColor));
                break;
            case 4:
                view = inflater.inflate(R.layout.fragmnet_nursing_main_distance, container, false);
                distanceCircleCounter = (CircleCounter) view.findViewById(R.id.distance_counter);
                distanceCircleCounter.setFirstWidth(getResources().getDimension(R.dimen.first));
                distanceCircleCounter.setSecondWidth(getResources().getDimension(R.dimen.second));
                distanceCircleCounter.setThirdWidth(getResources().getDimension(R.dimen.third));
                distanceCircleCounter.setFirstColor(getResources().getColor(R.color.dirAccent));
                distanceCircleCounter.setSecondColor(getResources().getColor(R.color.dirPrimary));
                distanceCircleCounter.setThirdColor(getResources().getColor(R.color.dirPrimaryDark));
                distanceCircleCounter.setBackgroundColor(getResources().getColor(R.color.dirColor));
                break;
            case 5:
                view = inflater.inflate(R.layout.fragmnet_nursing_main_sample, container, false);
                etcCircleCounter = (CircleCounter) view.findViewById(R.id.counter);
                etcCircleCounter.setFirstWidth(getResources().getDimension(R.dimen.first));
                etcCircleCounter.setSecondWidth(getResources().getDimension(R.dimen.second));
                etcCircleCounter.setThirdWidth(getResources().getDimension(R.dimen.third));
                etcCircleCounter.setFirstColor(getResources().getColor(R.color.etcAccent));
                etcCircleCounter.setSecondColor(getResources().getColor(R.color.etcPrimary));
                etcCircleCounter.setThirdColor(getResources().getColor(R.color.etcPrimaryDark));
                etcCircleCounter.setBackgroundColor(getResources().getColor(R.color.etcColor));
                break;
        }

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                bundle = msg.getData();
                step = Integer.valueOf(bundle.getString("STEP"), 16);
                calo = Integer.valueOf(bundle.getString("CALO"), 16);
                dist = Integer.valueOf(bundle.getString("DIST"));
                Log.e(TAG, step + " " + calo + " " + dist + "");

                stepCircleCounter.setValues(step, calo, dist);
                calorieCircleCounter.setValues(calo, dist, step);
                distanceCircleCounter.setValues(dist, step, calo);
                etcCircleCounter.setValues(dist, step, calo);
            }
        };

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        activity = getActivity();
    }


}
