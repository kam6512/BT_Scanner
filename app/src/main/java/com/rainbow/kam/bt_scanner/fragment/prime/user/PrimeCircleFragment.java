package com.rainbow.kam.bt_scanner.fragment.prime.user;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.prime.PrimeActivity;
import com.rainbow.kam.bt_scanner.data.item.RealmPrimeItem;
import com.rainbow.kam.bt_scanner.data.vo.GoalVo;
import com.rainbow.kam.bt_scanner.tools.view.CircleCounter;

import java.util.Arrays;
import java.util.List;

/**
 * Created by kam6512 on 2016-01-27.
 */
public class PrimeCircleFragment extends Fragment {

    private Context context;

    private static final String KEY_INDEX = "INDEX";

    private enum Index {
        INDEX_STEP(PrimeActivity.INDEX_STEP),
        INDEX_CALORIE(PrimeActivity.INDEX_CALORIE),
        INDEX_DISTANCE(PrimeActivity.INDEX_DISTANCE);

        private final int value;


        Index(int value) {
            this.value = value;
        }
    }

    private Index index;

    private View view;
    private CircleCounter circleCounter;

    private static List<String> unitList;


    public static PrimeCircleFragment newInstance(int index) {
        PrimeCircleFragment primeCircleFragment = new PrimeCircleFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_INDEX, index);
        primeCircleFragment.setArguments(bundle);
        return primeCircleFragment;
    }


    private Index getIndex() {
        Index[] indexValues = Index.values();
        return indexValues[getArguments().getInt(KEY_INDEX)];
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        unitList = Arrays.asList(getString(R.string.prime_step), getString(R.string.prime_calorie), getString(R.string.prime_distance));
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.f_prime_circle, container, false);

        index = getIndex();
        setCircleCounterView();

        return view;
    }


    private void setCircleCounterView() {
        circleCounter = (CircleCounter) view.findViewById(R.id.counter);
        circleCounter.setFirstColor(ContextCompat.getColor(context, R.color.stepAccent));
        circleCounter.setSecondColor(ContextCompat.getColor(context, R.color.stepAccent));
        circleCounter.setThirdColor(ContextCompat.getColor(context, R.color.stepAccent));
        circleCounter.setBackgroundColor(ContextCompat.getColor(context, R.color.text_dark));
        circleCounter.setTextColor(ContextCompat.getColor(context, R.color.stepPrimaryDark));
        circleCounter.setMetricText(unitList.get(index.value));
    }


    public void setCircleCounterGoalRange(GoalVo goalVo) {
        String goalRange;
        switch (index) {
            case INDEX_STEP:
                goalRange = goalVo.stepGoal;
                break;
            case INDEX_CALORIE:
                goalRange = goalVo.calorieGoal;
                break;
            case INDEX_DISTANCE:
                goalRange = goalVo.distanceGoal;
                break;
            default:
                goalRange = goalVo.stepGoal;
                break;
        }
        circleCounter.setRange(goalRange);
    }


    public void setCircleValue(RealmPrimeItem realmPrimeItem) {
        int value;
        switch (index) {
            case INDEX_STEP:
                value = realmPrimeItem.getStep();
                break;
            case INDEX_CALORIE:
                value = realmPrimeItem.getCalorie();
                break;
            case INDEX_DISTANCE:
                value = realmPrimeItem.getDistance();
                break;
            default:
                value = realmPrimeItem.getStep();
                break;
        }
        if (circleCounter != null) {
            circleCounter.setValues(value);
        }
    }


    public void setNoneValue() {
        if (circleCounter != null) {
            circleCounter.setValues(0);
        }
    }
}
