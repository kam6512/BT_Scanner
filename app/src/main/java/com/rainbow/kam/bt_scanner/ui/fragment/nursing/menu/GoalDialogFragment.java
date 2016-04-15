package com.rainbow.kam.bt_scanner.ui.fragment.nursing.menu;

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

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class GoalDialogFragment extends DialogFragment {

    private View view;

    @Bind(R.id.nursing_goal_step) TextInputLayout stepTextInput;
    @Bind(R.id.nursing_goal_calorie) TextInputLayout calorieTextInput;
    @Bind(R.id.nursing_goal_distance) TextInputLayout distanceTextInput;

    @Bind(R.id.nursing_accept_goal_fab) FloatingActionButton accept;

    private List<TextInputLayout> textInputLayoutList;

    private NursingDao nursingDao;

    private GoalVo goalVo;

    private OnSaveGoalListener onSaveGoalListener;


    @Override public void onAttach(Context context) {
        super.onAttach(context);
        nursingDao = NursingDao.getInstance(context);
        onSaveGoalListener = (OnSaveGoalListener) context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.df_nursing_goal, container, false);
        ButterKnife.bind(this, view);
        textInputLayoutList = Arrays.asList(stepTextInput, calorieTextInput, distanceTextInput);
        setBtn();
        setSavedGoalValue();
        setGoalValueView();
        return view;
    }


    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


    private void setBtn() {
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