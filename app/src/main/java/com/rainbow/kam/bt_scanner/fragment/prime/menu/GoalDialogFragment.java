package com.rainbow.kam.bt_scanner.fragment.prime.menu;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.prime.PrimeActivity;
import com.rainbow.kam.bt_scanner.tools.PrimeDao;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class GoalDialogFragment extends DialogFragment {

    private View view;

    private TextInputLayout stepTextInput, calorieTextInput, distanceTextInput;

    private String step, calorie, distance;

    private PrimeDao primeDao;

    private OnSaveGoalListener onSaveGoalListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PrimeActivity) {
            primeDao = PrimeDao.getInstance(context);
            onSaveGoalListener = (OnSaveGoalListener) context;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.df_prime_goal, container, false);
        setGoalInput();
        setBtn();
        setSavedGoalValue();
        setGoalValueView();
        return view;
    }


    private void setGoalInput() {
        stepTextInput = (TextInputLayout) view.findViewById(R.id.prime_goal_step);
        calorieTextInput = (TextInputLayout) view.findViewById(R.id.prime_goal_calorie);
        distanceTextInput = (TextInputLayout) view.findViewById(R.id.prime_goal_distance);
    }


    private void setBtn() {
        FloatingActionButton accept = (FloatingActionButton) view.findViewById(R.id.prime_accept_goal_fab);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAccept();
            }
        });
    }


    private void setSavedGoalValue() {
        PrimeDao.GoalVO goalVO = primeDao.loadGoalData();
        step = goalVO.getStepGoal();
        calorie = goalVO.getCalorieGoal();
        distance = goalVO.getDistanceGoal();
    }


    private void setGoalValueView() {
        stepTextInput.getEditText().setText(step);
        calorieTextInput.getEditText().setText(calorie);
        distanceTextInput.getEditText().setText(distance);
    }


    private void onAccept() {
        if (!isValueHasError(view.findViewById(R.id.prime_goal_group))) {
            step = stepTextInput.getEditText().getText().toString();
            calorie = calorieTextInput.getEditText().getText().toString();
            distance = distanceTextInput.getEditText().getText().toString();
            saveGoal();
        }
    }


    private boolean isValueHasError(View view) {
        boolean hasError = false;
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int length = viewGroup.getChildCount();
            for (int i = 0; i < length; i++) {
                View v = viewGroup.getChildAt(i);
                if (v instanceof TextInputLayout) {
                    TextInputLayout textInputLayout = (TextInputLayout) v;
                    String goal = textInputLayout.getEditText().getText().toString();
                    if (TextUtils.isEmpty(goal) || Integer.valueOf(goal) < 5) {
                        setGoalValueView();
                        hasError = true;
                    } else {
                        textInputLayout.setErrorEnabled(false);
                    }
                }
            }
        }
        return hasError;
    }


    private void saveGoal() {
        primeDao.saveGoalData(step,calorie,distance);
        onSaveGoalListener.onSaveGoal();
    }


    public interface OnSaveGoalListener {
        void onSaveGoal();
    }
}