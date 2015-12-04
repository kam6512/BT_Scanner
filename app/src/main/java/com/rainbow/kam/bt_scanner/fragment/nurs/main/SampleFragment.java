package com.rainbow.kam.bt_scanner.fragment.nurs.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.design.CircleCounter;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class SampleFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private View view;
    private CircleCounter etcCircleCounter;


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


        return view;
    }


    public void setSample(int sample) {
        etcCircleCounter.setValues(sample, sample, sample);
    }
}
