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
import android.widget.RadioGroup;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.data.dao.NursingDao;
import com.rainbow.kam.bt_scanner.data.vo.UserVo;

import java.util.Arrays;
import java.util.List;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class UserDataDialogFragment extends DialogFragment {

    private View view;

    private TextInputLayout nameTextInput, ageTextInput, heightTextInput, weightTextInput;
    private RadioGroup genderGroup;

    private List<TextInputLayout> textInputLayoutList;

    private NursingDao nursingDao;

    private UserVo userVo;

    private OnSaveUserDataListener onSaveUserDataListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        nursingDao = NursingDao.getInstance(context);
        onSaveUserDataListener = (OnSaveUserDataListener) context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.df_nursing_user, container, false);
        setUserInput();
        setBtn();
        setSavedUserValue();
        setUserValueView();
        return view;
    }


    private void setUserInput() {
        nameTextInput = (TextInputLayout) view.findViewById(R.id.nursing_add_user_name);
        ageTextInput = (TextInputLayout) view.findViewById(R.id.nursing_add_user_age);
        heightTextInput = (TextInputLayout) view.findViewById(R.id.nursing_add_user_height);
        weightTextInput = (TextInputLayout) view.findViewById(R.id.nursing_add_user_weight);
        genderGroup = (RadioGroup) view.findViewById(R.id.gender_group);

        textInputLayoutList = Arrays.asList(nameTextInput, ageTextInput, heightTextInput, weightTextInput);
    }


    private void setBtn() {
        FloatingActionButton accept = (FloatingActionButton) view.findViewById(R.id.nursing_accept_fab);
        accept.setOnClickListener(v -> {
            if (!isValueHasError()) {
                onAccept();
            }
        });
    }


    private void setSavedUserValue() {
        userVo = nursingDao.loadUserData();
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
        saveUserData();
    }


    private boolean isValueHasError() {
        boolean hasError = false;
        EditText editText;
        String value;
        for (TextInputLayout textInputLayout : textInputLayoutList) {
            editText = textInputLayout.getEditText();
            value = editText.getText().toString();
            if (TextUtils.isEmpty(value)) {
                textInputLayout.setError(getString(R.string.nursing_setting_user_error));
                hasError = true;
            } else {
                textInputLayout.setErrorEnabled(false);
            }
        }
        return hasError;
    }


    private void saveUserData() {
        nursingDao.saveUserData(userVo);
        onSaveUserDataListener.onSaveUserData();
    }


    public interface OnSaveUserDataListener {
        void onSaveUserData();
    }
}