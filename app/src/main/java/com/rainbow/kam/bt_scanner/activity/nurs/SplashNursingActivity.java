package com.rainbow.kam.bt_scanner.activity.nurs;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.fragment.nurs.splash.SplashNursingFragmentAddUser;
import com.rainbow.kam.bt_scanner.fragment.nurs.splash.SplashNursingFragmentLogo;
import com.rainbow.kam.bt_scanner.patient.Patient;
import com.rainbow.kam.bt_scanner.tools.PermissionV21;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class SplashNursingActivity extends AppCompatActivity {

    private static final String TAG = SplashNursingActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nursing_splash);
        try {
            Realm realm = Realm.getInstance(this);

            RealmResults<Patient> results = realm.where(Patient.class).findAll();
            Patient patient = results.get(0);
            if (patient.getStep() == null) {
                throw new Exception();
            }
            finish();
            startActivity(new Intent(SplashNursingActivity.this, MainNursingActivity.class));

        } catch (Exception e) {

            PermissionV21.check(this);
            Realm.removeDefaultConfiguration();

            final FragmentManager fragmentManager = getSupportFragmentManager();
            final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            final SplashNursingFragmentAddUser splashNursingFragmentAddUser =
                    new SplashNursingFragmentAddUser();
            final SplashNursingFragmentLogo splashNursingFragmentLogo = new SplashNursingFragmentLogo();

            fragmentTransaction.add(R.id.nursing_start_frame, splashNursingFragmentAddUser);
            fragmentTransaction.add(R.id.nursing_start_frame, splashNursingFragmentLogo);
            fragmentTransaction.commit();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getSupportFragmentManager().beginTransaction()
                            .remove(splashNursingFragmentLogo)
                            .commit();
                }
            }, 3000);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(getWindow().getDecorView(), R.string.permission_thanks, Snackbar.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getApplicationContext(), R.string.permission_request, Toast.LENGTH_SHORT).show();
                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(myAppSettings, 0);

                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.permission_denial, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case 1:
                if (resultCode == RESULT_OK) {
                    //블루투스 켜짐
                } else {
                    //블루투스 에러
                    Toast.makeText(this, R.string.bt_not_init, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }
}
