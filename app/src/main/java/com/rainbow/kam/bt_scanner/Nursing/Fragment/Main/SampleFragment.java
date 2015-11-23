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
public class SampleFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private Activity activity;
    public static Handler handler;
    private Bundle bundle;
    private View view;
    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;

    private CircleCounter etcCircleCounter;

    private int step;
    private int calo;
    private int dist;

    public static SampleFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        SampleFragment fragment = new SampleFragment();
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
                bundle = msg.getData();
                step = Integer.valueOf(bundle.getString("STEP"), 16);
                calo = Integer.valueOf(bundle.getString("CALO"), 16);
                dist = Integer.valueOf(bundle.getString("DIST"));
                Log.e(TAG, step + " " + calo + " " + dist + " ");

            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


                view = inflater.inflate(R.layout.fragmnet_nursing_main_sample, container, false);
                etcCircleCounter = (CircleCounter) view.findViewById(R.id.counter);
                etcCircleCounter.setFirstWidth(getResources().getDimension(R.dimen.first));
                etcCircleCounter.setSecondWidth(getResources().getDimension(R.dimen.second));
                etcCircleCounter.setThirdWidth(getResources().getDimension(R.dimen.third));
                etcCircleCounter.setFirstColor(getResources().getColor(R.color.etcAccent));
                etcCircleCounter.setSecondColor(getResources().getColor(R.color.etcPrimary));
                etcCircleCounter.setThirdColor(getResources().getColor(R.color.etcPrimaryDark));
                etcCircleCounter.setBackgroundColor(getResources().getColor(R.color.etcColor));


//        stepCircleCounter.setValues(20, 50, 70);
//        calorieCircleCounter.setValues(20, 50, 70);
//        distanceCircleCounter.setValues(20, 50, 70);
//        etcCircleCounter.setValues(20, 50, 70);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        activity = getActivity();
    }


}
