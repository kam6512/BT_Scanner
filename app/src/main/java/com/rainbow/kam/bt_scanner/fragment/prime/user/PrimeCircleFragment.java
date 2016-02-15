package com.rainbow.kam.bt_scanner.fragment.prime.user;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.data.dao.PrimeDao;
import com.rainbow.kam.bt_scanner.tools.data.item.RealmPrimeItem;
import com.rainbow.kam.bt_scanner.tools.data.vo.GoalVo;
import com.rainbow.kam.bt_scanner.tools.helper.PrimeHelper;
import com.rainbow.kam.bt_scanner.tools.view.CircleCounter;

import java.util.Arrays;
import java.util.List;

/**
 * Created by kam6512 on 2016-01-27.
 */
public class PrimeCircleFragment extends Fragment {
    private Context context;

    private int index;
    public static final String KEY_INDEX = "INDEX";

    private static final int INDEX_STEP = PrimeHelper.INDEX_STEP;
    private static final int INDEX_CALORIE = PrimeHelper.INDEX_CALORIE;
    private static final int INDEX_DISTANCE = PrimeHelper.INDEX_DISTANCE;

    private View view;
    private CircleCounter circleCounter;

    private static List<String> unitList;

    private PrimeDao primeDao;


    public static PrimeCircleFragment newInstance(int index) {
        PrimeCircleFragment primeCircleFragment = new PrimeCircleFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_INDEX, index);
        primeCircleFragment.setArguments(bundle);
        return primeCircleFragment;
    }


    private int getIndex() {
        return getArguments().getInt(KEY_INDEX);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
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
        GoalVo goalVo = primeDao.loadGoalData();
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
        circleCounter.setValues(value);
    }
}
