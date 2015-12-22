package com.rainbow.kam.bt_scanner.fragment.nurse.splash;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.patient.Patient;

import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmConfiguration;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class SplashNursingFragmentAddUser extends Fragment implements View.OnClickListener {

    private Activity activity;

    private SplashNursingSelectDialog nursingSelectDialog;

    private View view;
    private TextInputLayout name, age, height, weight, step;
    private RadioGroup genderGroup;

    private MaterialDialog materialDialog;

    private String userName;
    private String userAge;
    private String userHeight;
    private String userWeight;
    private String userStep;
    private String userGender;

    private Realm realm;
    private RealmAsyncTask transaction;

    private OnDeviceSavedListener onDeviceSavedListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            try {
                activity = (Activity) context;
                onDeviceSavedListener = (OnDeviceSavedListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString() + " must implement OnDeviceSavedListener");
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
        setDialog();

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (transaction != null && !transaction.isCancelled()) {
            transaction.cancel();
        }
        if (realm != null) {
            realm.close();
        }
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


    private void setDialog() {
        materialDialog = new MaterialDialog.Builder(activity)
                .positiveText(R.string.accept).negativeText(R.string.fix)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                        showDeviceListDialog();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                    }
                }).build();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nursing_accept_fab:
                onAccept();
                break;
            case R.id.nursing_skip_fab:
                onSkip();
                break;
        }

    }


    private void onAccept() {
        userName = name.getEditText().getText().toString();
        userAge = age.getEditText().getText().toString();
        userHeight = height.getEditText().getText().toString();
        userWeight = weight.getEditText().getText().toString();
        userStep = step.getEditText().getText().toString();

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

            String dialogContent = "이름 : " + userName +
                    "\n성별 : " + userGender +
                    "\n나이 : " + userAge +
                    "\n키 : " + userHeight +
                    "\n몸무게 : " + userWeight +
                    "\n걸음너비 : " + userStep;

            showAcceptDialog(dialogContent);
        }
    }


    private void onSkip() {
        userName = activity.getString(R.string.username_default);
        userAge = activity.getString(R.string.user_age_default);
        userHeight = activity.getString(R.string.user_height_default);
        userWeight = activity.getString(R.string.user_weight_default);
        userStep = activity.getString(R.string.user_step_default);
        userGender = activity.getString(R.string.user_gender_default);

        showSkipDialog();
    }


    private void showAcceptDialog(String res) {
        materialDialog.setTitle(R.string.accept_ok);
        materialDialog.setContent(res);
        materialDialog.show();
    }


    private void showSkipDialog() {
        materialDialog.setTitle(R.string.skip);
        materialDialog.setContent(R.string.skip_warning);
        materialDialog.show();
    }


    private void showDeviceListDialog() {
        if (nursingSelectDialog == null) {
            nursingSelectDialog = new SplashNursingSelectDialog();
        }
        nursingSelectDialog.show(getFragmentManager(), "DeviceDialog");
    }


    private void dismissDeviceListDialog() {
        if (nursingSelectDialog != null) {
            nursingSelectDialog.dismiss();
        }
    }


    public void saveDB(final String name, final String address) {

        realm = Realm.getInstance(new RealmConfiguration.Builder(activity).build());
        realm.beginTransaction();
        realm.allObjects(Patient.class).clear();

        transaction = realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Patient patient = realm.createObject(Patient.class);
                patient.setName(userName);
                patient.setAge(userAge);
                patient.setHeight(userHeight);
                patient.setWeight(userWeight);
                patient.setStep(userStep);
                patient.setGender(userGender);
                patient.setDeviceName(name);
                patient.setDeviceAddress(address);
            }
        }, new Realm.Transaction.Callback() {
            @Override
            public void onSuccess() {
//                RealmResults<Patient> results = realm.where(Patient.class).equalTo("name", name.getEditText().getText().toString()).findAll();
                dismissDeviceListDialog();
                onDeviceSavedListener.OnDeviceSaveSuccess();
            }

            @Override
            public void onError(Exception e) {
                onDeviceSavedListener.OnDeviceSaveFail();
            }
        });

        realm.commitTransaction();
        realm.close();

    }


    public interface OnDeviceSavedListener {
        void OnDeviceSaveSuccess();

        void OnDeviceSaveFail();
    }
}
