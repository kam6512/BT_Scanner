package com.rainbow.kam.bt_scanner.fragment.nurs.splash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.nurs.MainNursingActivity;
import com.rainbow.kam.bt_scanner.patient.Patient;
import com.rainbow.kam.bt_scanner.tools.design.RippleView;

import java.lang.ref.WeakReference;

import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmResults;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class SplashNursingFragmentAddUser extends Fragment implements View.OnClickListener, RippleView.OnRippleCompleteListener {

    private Activity activity;
    public static SplashNursingFragmentHandler handler;

    private FragmentManager fm;
    private SplashNursingDialog dialogFragment;

    private TextInputLayout name, age, height, weight, step;
    private RadioGroup genderGroup;
    private FloatingActionButton nextFab;

    private String userName;
    private String userAge;
    private String userHeight;
    private String userWeight;
    private String userStep;
    private String userGender;

    private Realm realm;
    private RealmAsyncTask transaction;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view;
        view = inflater.inflate(R.layout.fragment_nursing_splash_adduser, container, false);

        name = (TextInputLayout) view.findViewById(R.id.nursing_adduser_name);
        age = (TextInputLayout) view.findViewById(R.id.nursing_adduser_age);
        height = (TextInputLayout) view.findViewById(R.id.nursing_adduser_height);
        weight = (TextInputLayout) view.findViewById(R.id.nursing_adduser_weight);
        step = (TextInputLayout) view.findViewById(R.id.nursing_adduser_step);
        genderGroup = (RadioGroup) view.findViewById(R.id.gender_group);

        nextFab = (FloatingActionButton) view.findViewById(R.id.nursing_next_fab);
        nextFab.setOnClickListener(this);

        RippleView skip = (RippleView) view.findViewById(R.id.nursing_adduser_skip);
        skip.setOnRippleCompleteListener(this);

        fm = getFragmentManager();
        handler = new SplashNursingFragmentHandler(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        activity = getActivity();
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

    private void handleMessage(Message msg) {
        final Bundle callbackBundle = msg.getData();

        Realm.removeDefaultConfiguration();
        realm = Realm.getInstance(getActivity());
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
                patient.setDeviceName(callbackBundle.getString("name"));
                patient.setDeviceAddress(callbackBundle.getString("address"));
            }
        }, new Realm.Transaction.Callback() {
            @Override
            public void onSuccess() {
                RealmResults<Patient> results = realm.where(Patient.class).equalTo("name", name.getEditText().getText().toString()).findAll();
                dismissDeviceListDialog();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(), "fail " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
        realm.commitTransaction();
    }

    private void showAcceptDialog(String res) {
        new MaterialDialog.Builder(getActivity()).title("기입 정보가 확실한지 확인해주시기 바랍니다.")
                .content(res)
                .positiveText("확인").negativeText("수정")
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
                }).show();
    }

    private void showSkipDialog() {

        userName = activity.getString(R.string.username_default);
        userAge = activity.getString(R.string.userage_default);
        userHeight = activity.getString(R.string.userheight_default);
        userWeight = activity.getString(R.string.userweight_default);
        userStep = activity.getString(R.string.userstep_default);
        userGender = activity.getString(R.string.usergender_default);

        new MaterialDialog.Builder(activity).title(R.string.skip)
                .content(R.string.skip_warning)
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
                }).show();
    }

    private void showDeviceListDialog() {
        if (fm != null) {
            dialogFragment = new SplashNursingDialog();
            dialogFragment.show(fm, "DeviceDialog");
        }
    }

    private void dismissDeviceListDialog() {
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }
        getActivity().finish();
        startActivity(new Intent(getActivity(), MainNursingActivity.class));
    }

    @Override
    public void onClick(View v) {
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

    @Override
    public void onComplete(RippleView rippleView) {
        showSkipDialog();
    }

    private static class SplashNursingFragmentHandler extends Handler {
        private final WeakReference<SplashNursingFragmentAddUser> fragmentWeakReference;

        private SplashNursingFragmentHandler(SplashNursingFragmentAddUser splashNursingFragmentAddUser) {
            fragmentWeakReference = new WeakReference<>(splashNursingFragmentAddUser);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SplashNursingFragmentAddUser splashNursingFragmentAddUser = fragmentWeakReference.get();
            if (splashNursingFragmentAddUser != null) {
                splashNursingFragmentAddUser.handleMessage(msg);
            }
        }
    }
}
