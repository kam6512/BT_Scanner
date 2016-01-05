package com.rainbow.kam.bt_scanner.activity.band;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.RealmItem.RealmPatientItem;
import com.rainbow.kam.bt_scanner.adapter.DeviceAdapter;
import com.rainbow.kam.bt_scanner.fragment.band.init.LogoFragment;
import com.rainbow.kam.bt_scanner.fragment.band.init.SelectDeviceDialogFragment;

import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class BandInitialActivity extends AppCompatActivity implements DeviceAdapter.OnDeviceSelectListener, View.OnClickListener {

    private static final String TAG = BandInitialActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;

    private TextInputLayout name, age, height, weight, step;
    private RadioGroup genderGroup;
    private SelectDeviceDialogFragment nursingSelectDialog;

    private MaterialDialog materialDialog;

    private Realm realm;
    private RealmAsyncTask transaction;

    private String userName;
    private String userAge;
    private String userHeight;
    private String userWeight;
    private String userStep;
    private String userGender;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nursing_init);
        try {
            realm = Realm.getInstance(new RealmConfiguration.Builder(this).build());
            RealmResults<RealmPatientItem> results = realm.where(RealmPatientItem.class).findAll();
            RealmPatientItem realmPatientItem = results.get(0);

            if (realmPatientItem == null) {
                throw new Exception("User Info is NULL");
            } else {
                complete();
            }

        } catch (Exception e) {
            setFragment();
            setDialog();
            setUserInput();
            setBtn();
        }
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


    private void setFragment() {
        final LogoFragment logoFragment = new LogoFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.nursing_start_frame, logoFragment).commit();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isDestroyed()) {
                    getSupportFragmentManager().beginTransaction()
                            .remove(logoFragment)
                            .commit();
                }
            }
        }, 1000);
    }


    private void setDialog() {
        materialDialog = new MaterialDialog.Builder(this)
                .positiveText(R.string.dialog_accept).negativeText(R.string.dialog_fix)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                        if (nursingSelectDialog == null) {
                            nursingSelectDialog = new SelectDeviceDialogFragment();
                        }
                        nursingSelectDialog.show(getSupportFragmentManager(), "DeviceDialog");
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                    }
                }).build();
    }


    private void setUserInput() {
        name = (TextInputLayout) findViewById(R.id.nursing_add_user_name);
        age = (TextInputLayout) findViewById(R.id.nursing_add_user_age);
        height = (TextInputLayout) findViewById(R.id.nursing_add_user_height);
        weight = (TextInputLayout) findViewById(R.id.nursing_add_user_weight);
        step = (TextInputLayout) findViewById(R.id.nursing_add_user_step);
        genderGroup = (RadioGroup) findViewById(R.id.gender_group);
    }


    private void setBtn() {
        FloatingActionButton accept = (FloatingActionButton) findViewById(R.id.nursing_accept_fab);
        accept.setOnClickListener(this);

        FloatingActionButton skip = (FloatingActionButton) findViewById(R.id.nursing_skip_fab);
        skip.setOnClickListener(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    //블루투스 켜짐
                    Toast.makeText(this, R.string.bt_on, Toast.LENGTH_SHORT).show();
                } else {
                    //블루투스 에러
                    Toast.makeText(this, R.string.bt_not_init, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }


    public void onAccept() {
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

            materialDialog.setTitle(R.string.dialog_accept_ok);
            materialDialog.setContent(dialogContent);
            materialDialog.show();
        }

    }


    public void onSkip() {
        userName = getString(R.string.user_name_default);
        userAge = getString(R.string.user_age_default);
        userHeight = getString(R.string.user_height_default);
        userWeight = getString(R.string.user_weight_default);
        userStep = getString(R.string.user_step_default);
        userGender = getString(R.string.user_gender_default);

        materialDialog.setTitle(R.string.dialog_skip);
        materialDialog.setContent(R.string.dialog_skip_warning);
        materialDialog.show();
    }


    private void complete() {
        finish();
        startActivity(new Intent(BandInitialActivity.this, BandContentActivity.class));
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


    @DebugLog
    @Override
    public void onDeviceSelect(final String name, final String address) {
        realm.beginTransaction();
        realm.allObjects(RealmPatientItem.class).clear();

        transaction = realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmPatientItem realmPatientItem = realm.createObject(RealmPatientItem.class);
                realmPatientItem.setName(userName);
                realmPatientItem.setAge(userAge);
                realmPatientItem.setHeight(userHeight);
                realmPatientItem.setWeight(userWeight);
                realmPatientItem.setStep(userStep);
                realmPatientItem.setGender(userGender);
                realmPatientItem.setDeviceName(name);
                realmPatientItem.setDeviceAddress(address);
            }
        }, null);

        realm.commitTransaction();
        realm.close();
        complete();
    }
}
