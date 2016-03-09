package com.rainbow.kam.bt_scanner.fragment.nursing.menu;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.data.dao.NursingDao;
import com.rainbow.kam.bt_scanner.data.vo.GoalVo;

import java.util.Arrays;
import java.util.List;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class GoalDialogFragment extends DialogFragment {

    private View view;

    private TextInputLayout stepTextInput, calorieTextInput, distanceTextInput;

    private List<TextInputLayout> textInputLayoutList;

    private NursingDao nursingDao;

    private GoalVo goalVo;

    private OnSaveGoalListener onSaveGoalListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        nursingDao = NursingDao.getInstance(context);
        onSaveGoalListener = (OnSaveGoalListener) context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.df_nursing_goal, container, false);
        setGoalInput();
        setBtn();
        setSavedGoalValue();
        setGoalValueView();
        return view;
    }


    private void setGoalInput() {
        stepTextInput = (TextInputLayout) view.findViewById(R.id.nursing_goal_step);
        calorieTextInput = (TextInputLayout) view.findViewById(R.id.nursing_goal_calorie);
        distanceTextInput = (TextInputLayout) view.findViewById(R.id.nursing_goal_distance);

        textInputLayoutList = Arrays.asList(stepTextInput, calorieTextInput, distanceTextInput);
    }


    private void setBtn() {
        FloatingActionButton accept = (FloatingActionButton) view.findViewById(R.id.nursing_accept_goal_fab);
        accept.setOnClickListener(v -> {
            if (!isValueHasError()) {
                onAccept();
            }
        });
    }


    private void setSavedGoalValue() {
        goalVo = nursingDao.loadGoalData();
    }


    private void setGoalValueView() {
        stepTextInput.getEditText().setText(goalVo.stepGoal);
        calorieTextInput.getEditText().setText(goalVo.calorieGoal);
        distanceTextInput.getEditText().setText(goalVo.distanceGoal);
    }


    private void onAccept() {
        goalVo.stepGoal = stepTextInput.getEditText().getText().toString();
        goalVo.calorieGoal = calorieTextInput.getEditText().getText().toString();
        goalVo.distanceGoal = distanceTextInput.getEditText().getText().toString();
        saveGoal();
    }


    private boolean isValueHasError() {
        boolean hasError = false;
        String goal;
        EditText editText;
        for (TextInputLayout textInputLayout : textInputLayoutList) {
            editText = textInputLayout.getEditText();
            goal = editText.getText().toString();
            if (TextUtils.isEmpty(goal) || Integer.valueOf(goal) < 5) {
                setGoalValueView();
                hasError = true;
            } else {
                textInputLayout.setErrorEnabled(false);
            }
        }
        return hasError;
    }


    private void saveGoal() {
        nursingDao.saveGoalData(goalVo);
        onSaveGoalListener.onSaveGoal(goalVo);
    }


    public interface OnSaveGoalListener {
        void onSaveGoal(GoalVo goalVo);
    }
}