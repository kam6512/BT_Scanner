package com.rainbow.kam.bt_scanner.ui.fragment.nursing.user;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.data.item.UserMovementItem;
import com.rainbow.kam.bt_scanner.data.vo.GoalVo;
import com.rainbow.kam.bt_scanner.tools.view.CircleCounter;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindString;
import butterknife.ButterKnife;

/**
 * Created by kam6512 on 2016-01-27.
 */
public class NursingCircleFragment extends Fragment {


    private static final String KEY_INDEX = "INDEX";

    private static int index;

    @Bind(R.id.counter) CircleCounter circleCounter;

    @BindColor(R.color.stepAccent) int circleColor;
    @BindColor(R.color.text_dark) int circleBackGroundColor;
    @BindColor(R.color.stepPrimaryDark) int circleTextColor;

    private static List<String> unitList;

    @BindString(R.string.nursing_step) String stepUnit;
    @BindString(R.string.nursing_calorie) String calorieUnit;
    @BindString(R.string.nursing_distance) String distanceUnit;


    public static NursingCircleFragment newInstance(int index) {
        NursingCircleFragment nursingCircleFragment = new NursingCircleFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_INDEX, index);
        nursingCircleFragment.setArguments(bundle);
        return nursingCircleFragment;
    }


    private int getIndex() {
        return getArguments().getInt(KEY_INDEX);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_nursing_circle, container, false);
        ButterKnife.bind(this, view);
        unitList = Arrays.asList(stepUnit, calorieUnit, distanceUnit);
        index = getIndex();
        setCircleCounterView();

        return view;
    }


    private void setCircleCounterView() {
        circleCounter.setFirstColor(circleColor);
        circleCounter.setSecondColor(circleColor);
        circleCounter.setThirdColor(circleColor);
        circleCounter.setBackgroundColor(circleBackGroundColor);
        circleCounter.setTextColor(circleTextColor);
        circleCounter.setMetricText(unitList.get(index));
    }


    public void setCircleCounterGoalRange(GoalVo goalVo) {
        String goalRange;
        switch (index) {
            case NursingFragment.INDEX_STEP:
                goalRange = goalVo.stepGoal;
                break;
            case NursingFragment.INDEX_CALORIE:
                goalRange = goalVo.calorieGoal;
                break;
            case NursingFragment.INDEX_DISTANCE:
                goalRange = goalVo.distanceGoal;
                break;
            default:
                goalRange = goalVo.stepGoal;
                break;
        }
        circleCounter.setRange(goalRange);
    }


    public void setCircleValue(UserMovementItem userMovementItem) {
        int value;
        switch (index) {
            case NursingFragment.INDEX_STEP:
                value = userMovementItem.getStep();
                break;
            case NursingFragment.INDEX_CALORIE:
                value = userMovementItem.getCalorie();
                break;
            case NursingFragment.INDEX_DISTANCE:
                value = userMovementItem.getDistance();
                break;
            default:
                value = userMovementItem.getStep();
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
