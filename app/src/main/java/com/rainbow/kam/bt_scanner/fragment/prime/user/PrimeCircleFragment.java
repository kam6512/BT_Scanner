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
import com.rainbow.kam.bt_scanner.tools.PrimeDao;
import com.rainbow.kam.bt_scanner.tools.RealmPrimeItem;
import com.rainbow.kam.bt_scanner.tools.helper.PrimeHelper;
import com.rainbow.kam.bt_scanner.tools.view.CircleCounter;

import java.util.Arrays;
import java.util.List;

/**
 * Created by kam6512 on 2016-01-27.
 */
public class PrimeCircleFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();

    private int index;

    private Context context;
    private View view;

    private CircleCounter circleCounter;

    private static List<String> unitList;

    private PrimeDao primeDao;


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
            primeDao = PrimeDao.getInstance(context);
            unitList = Arrays.asList(getString(R.string.prime_step), getString(R.string.prime_calorie), getString(R.string.prime_distance));
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.f_prime_circle, container, false);

        index = getIndex();
        setCircleCounterView();
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
        circleCounter.setMetricText(unitList.get(index));
    }


    public void setCircleCounterGoalRange() {
        String goalRange;
        PrimeDao.GoalVO goalVO = primeDao.loadGoalData();
        switch (index) {
            case PrimeHelper.INDEX_STEP:
                goalRange = goalVO.getStepGoal();
                break;
            case PrimeHelper.INDEX_CALORIE:
                goalRange = goalVO.getCalorieGoal();
                break;
            case PrimeHelper.INDEX_DISTANCE:
                goalRange = goalVO.getDistanceGoal();
                break;
            default:
                goalRange = goalVO.getStepGoal();
                break;
        }

        circleCounter.setRange(goalRange);
    }


    public void setCircleValue(RealmPrimeItem realmPrimeItem) {
        int value;
        switch (index) {
            case PrimeHelper.INDEX_STEP:
                value = realmPrimeItem.getStep();
                break;
            case PrimeHelper.INDEX_CALORIE:
                value = realmPrimeItem.getCalorie();
                break;
            case PrimeHelper.INDEX_DISTANCE:
                value = realmPrimeItem.getDistance();
                break;
            default:
                value = realmPrimeItem.getStep();
                break;
        }
        circleCounter.setValues(value);
    }
}
