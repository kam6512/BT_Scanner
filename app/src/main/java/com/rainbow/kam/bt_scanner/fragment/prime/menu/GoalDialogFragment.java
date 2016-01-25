package com.rainbow.kam.bt_scanner.fragment.prime.menu;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.rainbow.kam.bt_scanner.tools.helper.PrimeHelper;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class GoalDialogFragment extends DialogFragment {

    private final String TAG = getClass().getSimpleName();

    private View view;

    private TextInputLayout stepTextInput, calorieTextInput, distanceTextInput;

    private String step;
    private String calorie;
    private String distance;

    private SharedPreferences sharedPreferences;

    private OnSettingGoalListener onSettingGoalListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PrimeActivity) {
            sharedPreferences = context.getSharedPreferences(PrimeHelper.KEY, Context.MODE_PRIVATE);
            onSettingGoalListener = (OnSettingGoalListener) context;
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.df_prime_goal, container, false);
        setGoalInput();
        setBtn();
        return view;
    }


    private void setGoalInput() {
        stepTextInput = (TextInputLayout) view.findViewById(R.id.prime_goal_step);
        calorieTextInput = (TextInputLayout) view.findViewById(R.id.prime_goal_calorie);
        distanceTextInput = (TextInputLayout) view.findViewById(R.id.prime_goal_distance);
        setGoalEditText();
    }


    private void setGoalEditText() {
        stepTextInput.getEditText().setText(
                sharedPreferences.getString(PrimeHelper.KEY_GOAL_STEP, getString(R.string.goal_def_step)));
        calorieTextInput.getEditText().setText(
                sharedPreferences.getString(PrimeHelper.KEY_GOAL_CALORIE, getString(R.string.goal_def_calorie)));
        distanceTextInput.getEditText().setText(
                sharedPreferences.getString(PrimeHelper.KEY_GOAL_DISTANCE, getString(R.string.goal_def_distance)));

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


    @DebugLog
    private void onAccept() {
        step = stepTextInput.getEditText().getText().toString();
        calorie = calorieTextInput.getEditText().getText().toString();
        distance = distanceTextInput.getEditText().getText().toString();

        if (!checkInputLayoutText(view.findViewById(R.id.prime_goal_group))) {
            saveGoal();
            getFragmentManager().beginTransaction().remove(this).commit();
            onSettingGoalListener.onSettingGoal();
        }
    }


    @DebugLog
    private boolean checkInputLayoutText(View view) {
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
                        setGoalEditText();
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
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PrimeHelper.KEY_GOAL_STEP, step);
        editor.putString(PrimeHelper.KEY_GOAL_CALORIE, calorie);
        editor.putString(PrimeHelper.KEY_GOAL_DISTANCE, distance);
        editor.apply();
    }


    public interface OnSettingGoalListener {
        void onSettingGoal();
    }
}