package com.rainbow.kam.bt_scanner.fragment.nurse.main;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

    private CircleCounter etcCircleCounter;

    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_nursing_main_sample, container, false);
        etcCircleCounter = (CircleCounter) view.findViewById(R.id.counter);
        etcCircleCounter.setFirstWidth(getResources().getDimension(R.dimen.first));
        etcCircleCounter.setSecondWidth(getResources().getDimension(R.dimen.second));
        etcCircleCounter.setThirdWidth(getResources().getDimension(R.dimen.third));
        etcCircleCounter.setFirstColor(ContextCompat.getColor(context, R.color.etcAccent));
        etcCircleCounter.setSecondColor(ContextCompat.getColor(context, R.color.etcPrimary));
        etcCircleCounter.setThirdColor(ContextCompat.getColor(context, R.color.etcPrimaryDark));
        etcCircleCounter.setBackgroundColor(ContextCompat.getColor(context, R.color.etcColor));

        return view;
    }

    public void setSample(int sample) {
        etcCircleCounter.setValues(sample, sample, sample);
    }
}
