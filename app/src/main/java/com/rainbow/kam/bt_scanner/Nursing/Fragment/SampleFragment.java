package com.rainbow.kam.bt_scanner.Nursing.Fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.Tools.Design.CircleCounter;

/**
 * Created by sion on 2015-11-04.
 */
public class SampleFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private Activity activity = getActivity();
    public static Handler handler;
    private View view;
    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;

    private CircleCounter circleCounter;


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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragmnet_nursing_sample, container, false);
        circleCounter = (CircleCounter) view.findViewById(R.id.counter);
        circleCounter.setFirstWidth(getResources().getDimension(R.dimen.first));
        circleCounter.setSecondWidth(getResources().getDimension(R.dimen.second));
        circleCounter.setThirdWidth(getResources().getDimension(R.dimen.third));

        switch (mPage) {
            case 2:
                circleCounter.setFirstColor(getResources().getColor(R.color.stepAccent));
                circleCounter.setSecondColor(getResources().getColor(R.color.stepPrimary));
                circleCounter.setThirdColor(getResources().getColor(R.color.stepPrimaryDark));
                circleCounter.setBackgroundColor(getResources().getColor(R.color.stepColor));
                break;
            case 3:
                circleCounter.setFirstColor(getResources().getColor(R.color.caloAccent));
                circleCounter.setSecondColor(getResources().getColor(R.color.caloPrimary));
                circleCounter.setThirdColor(getResources().getColor(R.color.caloPrimaryDark));
                circleCounter.setBackgroundColor(getResources().getColor(R.color.caloColor));
                break;
            case 4:
                circleCounter.setFirstColor(getResources().getColor(R.color.dirAccent));
                circleCounter.setSecondColor(getResources().getColor(R.color.dirPrimary));
                circleCounter.setThirdColor(getResources().getColor(R.color.dirPrimaryDark));
                circleCounter.setBackgroundColor(getResources().getColor(R.color.dirColor));
                break;
            case 5:
                circleCounter.setFirstColor(getResources().getColor(R.color.etcAccent));
                circleCounter.setSecondColor(getResources().getColor(R.color.etcPrimary));
                circleCounter.setThirdColor(getResources().getColor(R.color.etcPrimaryDark));
                circleCounter.setBackgroundColor(getResources().getColor(R.color.etcColor));
                break;
        }


        circleCounter.setValues(20, 40, 50);
        return view;
    }
}
