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

    private View view;

    private TextInputLayout nameTextInput, ageTextInput, heightTextInput, weightTextInput;
    private RadioGroup genderGroup;

    private String name, age, height, weight;
    private boolean gender;

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
        setSavedUserValue();
        setUserValueView();
        return view;
    }


    private void setUserInput() {
        nameTextInput = (TextInputLayout) view.findViewById(R.id.prime_add_user_name);
        ageTextInput = (TextInputLayout) view.findViewById(R.id.prime_add_user_age);
        heightTextInput = (TextInputLayout) view.findViewById(R.id.prime_add_user_height);
        weightTextInput = (TextInputLayout) view.findViewById(R.id.prime_add_user_weight);
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


    private void setSavedUserValue() {
        name = sharedPreferences.getString(PrimeHelper.KEY_NAME, getString(R.string.user_name_default));
        age = sharedPreferences.getString(PrimeHelper.KEY_AGE, getString(R.string.user_age_default));
        height = sharedPreferences.getString(PrimeHelper.KEY_HEIGHT, getString(R.string.user_height_default));
        weight = sharedPreferences.getString(PrimeHelper.KEY_WEIGHT, getString(R.string.user_weight_default));
        gender = sharedPreferences.getBoolean(PrimeHelper.KEY_GENDER, true);
    }


    private void setUserValueView() {
        nameTextInput.getEditText().setText(name);
        ageTextInput.getEditText().setText(age);
        heightTextInput.getEditText().setText(height);
        weightTextInput.getEditText().setText(weight);
        if (gender) {
            genderGroup.check(R.id.radio_man);
        } else {
            genderGroup.check(R.id.radio_woman);
        }
    }


    @DebugLog
    private void onAccept() {
        if (!isValueHasError(view.findViewById(R.id.prime_init_group))) {
            name = nameTextInput.getEditText().getText().toString();
            age = ageTextInput.getEditText().getText().toString();
            height = heightTextInput.getEditText().getText().toString();
            weight = weightTextInput.getEditText().getText().toString();
            switch (genderGroup.getCheckedRadioButtonId()) {
                case R.id.radio_man:
                    gender = true;
                    break;
                case R.id.radio_woman:
                    gender = false;
                    break;
                default:
                    gender = true;
                    break;
            }
            saveUserValue();
            getFragmentManager().beginTransaction().remove(this).commit();
        }
    }


    @DebugLog
    private boolean isValueHasError(View view) {
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


    private void saveUserValue() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PrimeHelper.KEY_NAME, name);
        editor.putString(PrimeHelper.KEY_AGE, age);
        editor.putString(PrimeHelper.KEY_HEIGHT, height);
        editor.putString(PrimeHelper.KEY_WEIGHT, weight);
        editor.putBoolean(PrimeHelper.KEY_GENDER, gender);
        editor.apply();
    }
}