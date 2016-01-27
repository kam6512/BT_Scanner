package com.rainbow.kam.bt_scanner.fragment.prime.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.prime.PrimeActivity;
import com.rainbow.kam.bt_scanner.tools.helper.PrimeHelper;
import com.rainbow.kam.bt_scanner.tools.view.CircleCounter;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2016-01-27.
 */
public class PrimeCircleFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();

    private int index;

    private Context context;
    private View view;

    private CircleCounter circleCounter;

    private final int[] unit = {R.string.prime_step, R.string.prime_calorie, R.string.prime_distance};

    private final String[] keyRange = {PrimeHelper.KEY_GOAL_STEP, PrimeHelper.KEY_GOAL_CALORIE, PrimeHelper.KEY_GOAL_DISTANCE};
    private final int[] defRange = {R.string.goal_def_step, R.string.goal_def_calorie, R.string.goal_def_distance};

    private SharedPreferences sharedPreferences;


    public static PrimeCircleFragment newInstance(int index) {
        PrimeCircleFragment primeCircleFragment = new PrimeCircleFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PrimeHelper.KEY_INDEX, index);
        primeCircleFragment.setArguments(bundle);
        return primeCircleFragment;
    }


    private int getIndex() {
        return getArguments().getInt(PrimeHelper.KEY_INDEX);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PrimeActivity) {
            this.context = context;
            sharedPreferences = context.getSharedPreferences(PrimeHelper.KEY, Context.MODE_PRIVATE);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.f_prime_circle, container, false);

        index = getIndex();
        setCircleCounterView();
        setResource();
        setCircleCounterGoalRange();

        return view;
    }


    private void setCircleCounterView() {
        circleCounter = (CircleCounter) view.findViewById(R.id.counter);
        circleCounter.setFirstColor(ContextCompat.getColor(context, R.color.stepAccent));
        circleCounter.setSecondColor(ContextCompat.getColor(context, R.color.stepAccent));
        circleCounter.setThirdColor(ContextCompat.getColor(context, R.color.stepAccent));
        circleCounter.setBackgroundColor(ContextCompat.getColor(context, R.color.text_dark));
        circleCounter.setTextColor(ContextCompat.getColor(context, R.color.stepPrimaryDark));
    }


    private void setResource() {
        circleCounter.setMetricText(getString(unit[index]));
    }


    @DebugLog
    public void setCircleCounterGoalRange() {
        circleCounter.setRange(sharedPreferences.getString(keyRange[index], getString(defRange[index])));
        circleCounter.resetRange();
    }


    public void setCircleValue(int value) {
        circleCounter.setValues(value, value, value);
    }
}
