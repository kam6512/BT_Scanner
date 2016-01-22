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

    private TextInputLayout nameTextInput, ageTextInput, heightTextInput, weightTextInput, stepTextInput;
    private RadioGroup genderGroup;

    private SharedPreferences sharedPreferences;

    private String name;
    private String age;
    private String height;
    private String weight;
    private String step;
    private String gender;


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
        nameTextInput = (TextInputLayout) view.findViewById(R.id.prime_add_user_name);
        ageTextInput = (TextInputLayout) view.findViewById(R.id.prime_add_user_age);
        heightTextInput = (TextInputLayout) view.findViewById(R.id.prime_add_user_height);
        weightTextInput = (TextInputLayout) view.findViewById(R.id.prime_add_user_weight);
        stepTextInput = (TextInputLayout) view.findViewById(R.id.prime_add_user_step);
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
        name = nameTextInput.getEditText().getText().toString();
        age = ageTextInput.getEditText().getText().toString();
        height = heightTextInput.getEditText().getText().toString();
        weight = weightTextInput.getEditText().getText().toString();
        step = stepTextInput.getEditText().getText().toString();
        switch (genderGroup.getCheckedRadioButtonId()) {
            case R.id.radio_man:
                gender = getString(R.string.gender_man);
                break;
            case R.id.radio_woman:
                gender = getString(R.string.gender_woman);
                break;
            default:
                gender = getString(R.string.gender_man);
                break;
        }

        if (!checkInputLayoutText(view.findViewById(R.id.prime_init_group))) {
            saveUserInfo();
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


    private void saveUserInfo() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PrimeHelper.KEY_NAME, name);
        editor.putString(PrimeHelper.KEY_AGE, age);
        editor.putString(PrimeHelper.KEY_HEIGHT, height);
        editor.putString(PrimeHelper.KEY_WEIGHT, weight);
        editor.putString(PrimeHelper.KEY_STEP_STRIDE, step);
        editor.putString(PrimeHelper.KEY_GENDER, gender);
        editor.apply();
    }
}