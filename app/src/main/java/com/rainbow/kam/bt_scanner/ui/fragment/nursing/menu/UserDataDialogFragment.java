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
import android.widget.RadioGroup;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.data.dao.NursingDao;
import com.rainbow.kam.bt_scanner.data.vo.UserVo;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class UserDataDialogFragment extends DialogFragment {

    private View view;

    @Bind(R.id.nursing_add_user_name) TextInputLayout nameTextInput;
    @Bind(R.id.nursing_add_user_age) TextInputLayout ageTextInput;
    @Bind(R.id.nursing_add_user_height) TextInputLayout heightTextInput;
    @Bind(R.id.nursing_add_user_weight) TextInputLayout weightTextInput;
    @Bind(R.id.gender_group) RadioGroup genderGroup;

    @Bind(R.id.nursing_accept_fab) FloatingActionButton accept;

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
        ButterKnife.bind(this, view);
        textInputLayoutList = Arrays.asList(nameTextInput, ageTextInput, heightTextInput, weightTextInput);
        setBtn();
        setSavedUserValue();
        setUserValueView();
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