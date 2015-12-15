package com.rainbow.kam.bt_scanner.activity.nurse;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.adapter.nurse.selected.SelectDeviceAdapter;
import com.rainbow.kam.bt_scanner.fragment.nurse.splash.SplashNursingFragmentAddUser;
import com.rainbow.kam.bt_scanner.fragment.nurse.splash.SplashNursingFragmentLogo;
import com.rainbow.kam.bt_scanner.patient.Patient;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class SplashNursingActivity extends AppCompatActivity implements SelectDeviceAdapter.OnDeviceSelectListener {

    private static final String TAG = SplashNursingActivity.class.getSimpleName();
    private SplashNursingFragmentAddUser splashNursingFragmentAddUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nursing_splash);
        try {
            Realm realm = Realm.getInstance(new RealmConfiguration.Builder(this).build());
            RealmResults<Patient> results = realm.where(Patient.class).findAll();
            Patient patient = results.get(0);

            if (patient.getStep() == null) {
                throw new Exception();
            } else {
                finish();
                startActivity(new Intent(SplashNursingActivity.this, MainNursingActivity.class));
            }

        } catch (Exception e) {
            setFragment();
        }
    }

    private void setFragment() {
        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        splashNursingFragmentAddUser =
                new SplashNursingFragmentAddUser();
        SplashNursingFragmentLogo splashNursingFragmentLogo = new SplashNursingFragmentLogo();

        fragmentTransaction.replace(R.id.nursing_start_frame, splashNursingFragmentLogo);
        fragmentTransaction.commit();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing()) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.nursing_start_frame, splashNursingFragmentAddUser)
                            .commit();
                }
            }
        }, 2000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case 1:
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

    @Override
    public void onDeviceSelect(Bundle bundle) {
        splashNursingFragmentAddUser.saveDB(bundle);
    }
}
