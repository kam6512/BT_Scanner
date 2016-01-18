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
import android.widget.RadioGroup;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.helper.PrimeHelper;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class UserDataDialogFragment extends DialogFragment {

    private final String TAG = getClass().getSimpleName();

    private View view;

    private TextInputLayout name, age, height, weight, step;
    private RadioGroup genderGroup;

    private SharedPreferences sharedPreferences;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        sharedPreferences = context.getSharedPreferences(PrimeHelper.KEY, Context.MODE_PRIVATE);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.df_prime_user, container, false);
        setUserInput();
        setBtn();
        return view;
    }


    private void setUserInput() {
        name = (TextInputLayout) view.findViewById(R.id.prime_add_user_name);
        age = (TextInputLayout) view.findViewById(R.id.prime_add_user_age);
        height = (TextInputLayout) view.findViewById(R.id.prime_add_user_height);
        weight = (TextInputLayout) view.findViewById(R.id.prime_add_user_weight);
        step = (TextInputLayout) view.findViewById(R.id.prime_add_user_step);
        genderGroup = (RadioGroup) view.findViewById(R.id.gender_group);
    }


    private void setBtn() {
        FloatingActionButton accept = (FloatingActionButton) view.findViewById(R.id.prime_accept_fab);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAccept();
            }
        });
    }


    @DebugLog
    private void onAccept() {
        String userName = name.getEditText().getText().toString();
        String userAge = age.getEditText().getText().toString();
        String userHeight = height.getEditText().getText().toString();
        String userWeight = weight.getEditText().getText().toString();
        String userStep = step.getEditText().getText().toString();

        String userGender;
        switch (genderGroup.getCheckedRadioButtonId()) {
            case R.id.radio_man:
                userGender = getString(R.string.gender_man);
                break;
            case R.id.radio_woman:
                userGender = getString(R.string.gender_woman);
                break;
            default:
                userGender = getString(R.string.gender_man);
                break;
        }

        if (!checkInputLayoutText(view.findViewById(R.id.prime_init_group))) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PrimeHelper.KEY_NAME, userName);
            editor.putString(PrimeHelper.KEY_AGE, userAge);
            editor.putString(PrimeHelper.KEY_HEIGHT, userHeight);
            editor.putString(PrimeHelper.KEY_WEIGHT, userWeight);
            editor.putString(PrimeHelper.KEY_STEP_STRIDE, userStep);
            editor.putString(PrimeHelper.KEY_GENDER, userGender);
            editor.apply();
            getFragmentManager().beginTransaction().remove(this).commit();
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
                    if (TextUtils.isEmpty(textInputLayout.getEditText().getText().toString())) {
                        textInputLayout.setErrorEnabled(true);
                        textInputLayout.setError("다시 입력하세요");
                        hasError = true;
                    } else {
                        textInputLayout.setErrorEnabled(false);
                    }
                }
            }
        }
        return hasError;
    }
}