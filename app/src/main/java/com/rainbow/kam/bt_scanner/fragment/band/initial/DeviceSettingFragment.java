package com.rainbow.kam.bt_scanner.fragment.band.initial;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.band.BandInitialActivity;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class DeviceSettingFragment extends Fragment implements View.OnClickListener {

    private Activity activity;

    private View view;
    private TextInputLayout name, age, height, weight, step;
    private RadioGroup genderGroup;

    private OnDeviceSettingListener onDeviceSettingListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            activity = (Activity) context;
            try {
                onDeviceSettingListener = (OnDeviceSettingListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString() + " must implement OnServiceItemClickListener");
            }
        } else {
            throw new ClassCastException(context.toString() + " OnAttach Context not cast by Activity");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_nursing_splash_add_user, container, false);

        setUserInput();
        setBtn();

        return view;
    }


    private void setUserInput() {
        name = (TextInputLayout) view.findViewById(R.id.nursing_add_user_name);
        age = (TextInputLayout) view.findViewById(R.id.nursing_add_user_age);
        height = (TextInputLayout) view.findViewById(R.id.nursing_add_user_height);
        weight = (TextInputLayout) view.findViewById(R.id.nursing_add_user_weight);
        step = (TextInputLayout) view.findViewById(R.id.nursing_add_user_step);
        genderGroup = (RadioGroup) view.findViewById(R.id.gender_group);
    }


    private void setBtn() {
        FloatingActionButton accept = (FloatingActionButton) view.findViewById(R.id.nursing_accept_fab);
        accept.setOnClickListener(this);

        FloatingActionButton skip = (FloatingActionButton) view.findViewById(R.id.nursing_skip_fab);
        skip.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nursing_accept_fab:
                onAccept();
                break;
            case R.id.nursing_skip_fab:
                onDeviceSettingListener.onSkip();
                break;
        }

    }


    private void onAccept() {
        String userName = name.getEditText().getText().toString();
        String userAge = age.getEditText().getText().toString();
        String userHeight = height.getEditText().getText().toString();
        String userWeight = weight.getEditText().getText().toString();
        String userStep = step.getEditText().getText().toString();
        String userGender;
        switch (genderGroup.getCheckedRadioButtonId()) {
            case R.id.radio_man:
                userGender = activity.getString(R.string.gender_man);
                break;
            case R.id.radio_woman:
                userGender = activity.getString(R.string.gender_woman);
                break;
            default:
                userGender = activity.getString(R.string.gender_man);
                break;
        }

        if (TextUtils.isEmpty(userName)) {
            name.setError("Name is missing");
        } else if (TextUtils.isEmpty(userAge)) {
            age.setError("Age is missing");
        } else if (TextUtils.isEmpty(userHeight)) {
            height.setError("Height is missing");
        } else if (TextUtils.isEmpty(userWeight)) {
            weight.setError("weight is missing");
        } else if (TextUtils.isEmpty(userStep)) {
            step.setError("step is missing");
        } else {
            name.setErrorEnabled(false);
            age.setErrorEnabled(false);
            height.setErrorEnabled(false);
            weight.setErrorEnabled(false);
            step.setErrorEnabled(false);

            Bundle bundle = new Bundle();
            bundle.putString(BandInitialActivity.BUNDLE_NAME, userName);
            bundle.putString(BandInitialActivity.BUNDLE_AGE, userAge);
            bundle.putString(BandInitialActivity.BUNDLE_HEIGHT, userHeight);
            bundle.putString(BandInitialActivity.BUNDLE_WEIGHT, userWeight);
            bundle.putString(BandInitialActivity.BUNDLE_STEP, userStep);
            bundle.putString(BandInitialActivity.BUNDLE_GENDER, userGender);

            onDeviceSettingListener.onAccept(bundle);
        }
    }


    public interface OnDeviceSettingListener {
        void onAccept(Bundle bundle);

        void onSkip();
    }
}
