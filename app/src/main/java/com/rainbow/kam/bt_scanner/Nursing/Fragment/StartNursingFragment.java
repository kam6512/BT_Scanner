package com.rainbow.kam.bt_scanner.Nursing.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rainbow.kam.bt_scanner.Nursing.Activity.MainNursingActivity;
import com.rainbow.kam.bt_scanner.Nursing.Patient.Patient;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.Nursing.Activity.StartNursingActivity;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class StartNursingFragment extends Fragment implements View.OnClickListener {

    public static Handler handler;
    FragmentManager fm;
    SelectNursingDeviceFragment dialogFragment;

    private View view;
    private LinearLayout pilotLayout;

    private Animation animation;

    private TextInputLayout name, age, height, weight, step;
    private RadioGroup genderGroup;

    String userName;
    String userAge;
    String userHeight;
    String userWeight;
    String userStep;
    String gender;

    private Realm realm;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (StartNursingActivity.indexByStart == 0) {

            view = inflater.inflate(R.layout.fragment_nursing_splash, container, false);

            pilotLayout = (LinearLayout) view.findViewById(R.id.nursing_start_pilot);

//            animation = AnimationUtils.loadAnimation(getActivity(), R.anim.show_fab);
//            pilotLayout.startAnimation(animation);
//            StartNursingActivity.nursingHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                startNursingFab.setVisibility(View.VISIBLE);
//                startNursingFab.startAnimation(animation);
//
//                }
//            }, 200);

            StartNursingActivity.indexByStart++;

        } else {

            fm = getFragmentManager();

            view = inflater.inflate(R.layout.fragment_nursing_adduser, container, false);

            name = (TextInputLayout) view.findViewById(R.id.nursing_adduser_name);
            age = (TextInputLayout) view.findViewById(R.id.nursing_adduser_age);
            height = (TextInputLayout) view.findViewById(R.id.nursing_adduser_height);
            weight = (TextInputLayout) view.findViewById(R.id.nursing_adduser_weight);
            step = (TextInputLayout) view.findViewById(R.id.nursing_adduser_step);
            genderGroup = (RadioGroup) view.findViewById(R.id.gender_group);

            StartNursingActivity.startNursingFab.setOnClickListener(this);


            handler = new Handler() {
                @Override
                public void handleMessage(final Message msg) {
                    super.handleMessage(msg);


                    Bundle callbackBundle = msg.getData();

                    realm = Realm.getInstance(getActivity());
                    realm.beginTransaction();
                    realm.allObjects(Patient.class).clear();

                    Patient patient = realm.createObject(Patient.class);
                    patient.setName(userName);
                    patient.setAge(userAge);
                    patient.setHeight(userHeight);
                    patient.setWeight(userWeight);
                    patient.setStep(userStep);
                    patient.setGender(gender);
                    patient.setDeviceName(callbackBundle.getString("name"));
                    patient.setDeviceAddress(callbackBundle.getString("address"));
                    realm.commitTransaction();

                    RealmResults<Patient> results = realm.where(Patient.class).equalTo("name", name.getEditText().getText().toString()).findAll();
                    Log.e(StartNursingActivity.TAG, results.size() + " / " + results.get(0).getName() + " / " + results.get(0).getAge() + " / " + results.get(0).getHeight() + " / " + results.get(0).getWeight() + " / " + results.get(0).getStep() + " / " + results.get(0).getDeviceName() + " / " + results.get(0).getDeviceAddress());

                    dismissDeviceListDialog();
                }
            };

        }

        return view;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
    }

    private void showAcceptDialog(String res) {
        new MaterialDialog.Builder(getActivity()).title("기입 정보가 확실한지 확인해주시기 바랍니다.")
                .content(res)
                .positiveText("확인").negativeText("수정")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        materialDialog.dismiss();
                        showDeviceListDialog();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        materialDialog.dismiss();
                    }
                }).show();
    }

    private void showDeviceListDialog() {
        if (fm != null) {
            dialogFragment = new SelectNursingDeviceFragment();
            dialogFragment.show(fm, "DeviceDialog");
        }
    }

    private void dismissDeviceListDialog() {
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }
        getActivity().finish();
    }

    @Override
    public void onClick(View v) {
        userName = name.getEditText().getText().toString();
        userAge = age.getEditText().getText().toString();
        userHeight = height.getEditText().getText().toString();
        userWeight = weight.getEditText().getText().toString();
        userStep = step.getEditText().getText().toString();
        gender = "";

        switch (genderGroup.getCheckedRadioButtonId()) {
            case R.id.radio_man:
                gender = "남성";
                break;
            case R.id.radio_woman:
                gender = "여성";
                break;
            default:
                gender = "남성";
                break;
        }


        if (userName == null || userName.equals("") || userName.length() <= 1) {
            name.setError("Name is missing");
        } else if (userAge == null || userAge.equals("") || userAge.length() <= 0) {
            age.setError("Age is missing");
        } else if (userHeight == null || userHeight.equals("") || userHeight.length() <= 0) {
            height.setError("Height is missing");
        } else if (userWeight == null || userWeight.equals("") || userWeight.length() <= 0) {
            weight.setError("weight is missing");
        } else if (userStep == null || userStep.equals("") || userStep.length() <= 0) {
            step.setError("step is missing");
        } else {
            name.setErrorEnabled(false);
            age.setErrorEnabled(false);
            height.setErrorEnabled(false);
            weight.setErrorEnabled(false);
            step.setErrorEnabled(false);

            String res = "이름 : " + userName +
                    "\n성별 : " + gender +
                    "\n나이 : " + userAge +
                    "\n키 : " + userHeight +
                    "\n몸무게 : " + userWeight +
                    "\n걸음너비 : " + userStep;
            showAcceptDialog(res);
        }
    }
}
