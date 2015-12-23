package com.rainbow.kam.bt_scanner.activity.band;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.RealmItem.RealmPatientItem;
import com.rainbow.kam.bt_scanner.adapter.DeviceAdapter;
import com.rainbow.kam.bt_scanner.fragment.band.initial.DeviceSettingFragment;
import com.rainbow.kam.bt_scanner.fragment.band.initial.LogoFragment;

import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class BandInitialActivity extends AppCompatActivity implements DeviceAdapter.OnDeviceSelectListener {

    private static final String TAG = BandInitialActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;
    private DeviceSettingFragment deviceSettingFragment;


    private Realm realm;
    private RealmAsyncTask transaction;


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
                realmPatientItem.setName(deviceSettingFragment.userName);
                realmPatientItem.setAge(deviceSettingFragment.userAge);
                realmPatientItem.setHeight(deviceSettingFragment.userHeight);
                realmPatientItem.setWeight(deviceSettingFragment.userWeight);
                realmPatientItem.setStep(deviceSettingFragment.userStep);
                realmPatientItem.setGender(deviceSettingFragment.userGender);
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
}
