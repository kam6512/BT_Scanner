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
import android.widget.RadioGroup;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.data.dao.PrimeDao;
import com.rainbow.kam.bt_scanner.tools.data.vo.UserVo;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class UserDataDialogFragment extends DialogFragment {

    private View view;

    private TextInputLayout nameTextInput, ageTextInput, heightTextInput, weightTextInput;
    private RadioGroup genderGroup;

    private PrimeDao primeDao;

    private UserVo userVo;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        primeDao = PrimeDao.getInstance(context);
    }


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
        userVo = primeDao.loadUserData();
    }


    private void setUserValueView() {
        nameTextInput.getEditText().setText(userVo.name);
        ageTextInput.getEditText().setText(userVo.age);
        heightTextInput.getEditText().setText(userVo.height);
        weightTextInput.getEditText().setText(userVo.weight);
        if (userVo.gender) {
            genderGroup.check(R.id.radio_man);
        } else {
            genderGroup.check(R.id.radio_woman);
        }
    }


    private void onAccept() {
        if (!isValueHasError(view.findViewById(R.id.prime_init_group))) {
            userVo.name = nameTextInput.getEditText().getText().toString();
            userVo.age = ageTextInput.getEditText().getText().toString();
            userVo.height = heightTextInput.getEditText().getText().toString();
            userVo.weight = weightTextInput.getEditText().getText().toString();
            switch (genderGroup.getCheckedRadioButtonId()) {
                case R.id.radio_man:
                    userVo.gender = true;
                    break;
                case R.id.radio_woman:
                    userVo.gender = false;
                    break;
                default:
                    userVo.gender = true;
                    break;
            }
            primeDao.saveUserData(userVo);
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
}