package com.rainbow.kam.bt_scanner.activity.band;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.adapter.DeviceAdapter;
import com.rainbow.kam.bt_scanner.fragment.band.initial.AddUserFragment;
import com.rainbow.kam.bt_scanner.fragment.band.initial.LogoFragment;
import com.rainbow.kam.bt_scanner.RealmItem.RealmPatientItem;

import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class BandInitialActivity extends AppCompatActivity implements DeviceAdapter.OnDeviceSelectListener, AddUserFragment.OnDeviceSavedListener {

    private static final String TAG = BandInitialActivity.class.getSimpleName();
    private static final int REQUEST_ENABLE_BT = 1;
    private AddUserFragment addUserFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nursing_splash);
        try {
            Realm realm = Realm.getInstance(new RealmConfiguration.Builder(this).build());
            RealmResults<RealmPatientItem> results = realm.where(RealmPatientItem.class).findAll();
            RealmPatientItem realmPatientItem = results.get(0);

            if (realmPatientItem == null) {
                throw new Exception();
            } else {
                finish();
                startActivity(new Intent(BandInitialActivity.this, BandContentActivity.class));
            }

        } catch (Exception e) {
            setFragment();
        }
    }


    @DebugLog
    private void setFragment() {
        getSupportFragmentManager().beginTransaction().replace(R.id.nursing_start_frame, new LogoFragment()).commit();

        addUserFragment = new AddUserFragment();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.nursing_start_frame, addUserFragment)
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
    public void onDeviceSelect(String name, String address) {
        addUserFragment.saveDB(name, address);
    }


    @DebugLog
    @Override
    public void OnDeviceSaveSuccess() {

        finish();
        startActivity(new Intent(this, BandContentActivity.class));
    }


    @DebugLog
    @Override
    public void OnDeviceSaveFail() {
        Log.e(TAG, "Realm Save Fail");
    }
}
