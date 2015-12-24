package com.rainbow.kam.bt_scanner.activity.band;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.RealmItem.RealmPatientItem;
import com.rainbow.kam.bt_scanner.adapter.DeviceAdapter;
import com.rainbow.kam.bt_scanner.fragment.band.initial.DeviceSettingFragment;
import com.rainbow.kam.bt_scanner.fragment.band.initial.LogoFragment;
import com.rainbow.kam.bt_scanner.fragment.band.initial.SelectDeviceDialogFragment;

import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class BandInitialActivity extends AppCompatActivity implements DeviceAdapter.OnDeviceSelectListener, DeviceSettingFragment.OnDeviceSettingListener {

    private static final String TAG = BandInitialActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;

    public static final String BUNDLE_NAME = "NAME";
    public static final String BUNDLE_AGE = "AGE";
    public static final String BUNDLE_HEIGHT = "HEIGHT";
    public static final String BUNDLE_WEIGHT = "WEIGHT";
    public static final String BUNDLE_STEP = "STEP";
    public static final String BUNDLE_GENDER = "GENDER";

    private DeviceSettingFragment deviceSettingFragment;
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
        setContentView(R.layout.activity_nursing_splash);
        try {
            realm = Realm.getInstance(new RealmConfiguration.Builder(this).build());
            RealmResults<RealmPatientItem> results = realm.where(RealmPatientItem.class).findAll();
            RealmPatientItem realmPatientItem = results.get(0);

            if (realmPatientItem == null) {
                throw new Exception();
            } else {
                userCheckComplete();
            }

        } catch (Exception e) {
            setFragment();
            setDialog();
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


    @DebugLog
    private void setFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.nursing_start_frame, new LogoFragment()).commit();

        deviceSettingFragment = new DeviceSettingFragment();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.nursing_start_frame, deviceSettingFragment)
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
        userCheckComplete();
    }


    private void userCheckComplete() {
        finish();
        startActivity(new Intent(BandInitialActivity.this, BandContentActivity.class));
    }


    @Override
    public void onAccept(Bundle bundle) {
        userName = bundle.getString(BUNDLE_NAME);
        userAge = bundle.getString(BUNDLE_AGE);
        userHeight = bundle.getString(BUNDLE_HEIGHT);
        userWeight = bundle.getString(BUNDLE_WEIGHT);
        userStep = bundle.getString(BUNDLE_STEP);
        userGender = bundle.getString(BUNDLE_GENDER);

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


    @Override
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
}
