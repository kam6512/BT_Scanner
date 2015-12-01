package com.rainbow.kam.bt_scanner.nursing.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.nursing.fragment.start.StartNursingFragmentAddUser;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.nursing.fragment.start.StartNursingFragmentLogo;
import com.rainbow.kam.bt_scanner.tools.PermissionV21;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class StartNursingActivity extends AppCompatActivity {

    public static final String TAG = StartNursingActivity.class.getSimpleName();
    public static FloatingActionButton startNursingFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nursing_start);

        PermissionV21.check(this);

        startNursingFab = (FloatingActionButton) findViewById(R.id.nursing_next_fab);
        startNursingFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inflateFragment(false);
            }
        });
        inflateFragment(true);

    }

    private void inflateFragment(boolean isLogo) {

        StartNursingFragmentLogo startNursingFragmentLogo = new StartNursingFragmentLogo();
        StartNursingFragmentAddUser startNusingFragment = new StartNursingFragmentAddUser();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (isLogo) {
            fragmentTransaction.add(R.id.nursing_start_frame, startNursingFragmentLogo);
        } else {
            fragmentTransaction.replace(R.id.nursing_start_frame, startNusingFragment);
        }
        fragmentTransaction.commit();
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(getWindow().getDecorView(), R.string.permission_thanks, Snackbar.LENGTH_SHORT).show();
                } else {
                    onBackPressed();
                    Toast.makeText(getApplicationContext(), R.string.permission_request, Toast.LENGTH_SHORT).show();

                    Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                    myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                    myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(myAppSettings, 0);
                }
                break;
            default:
                Toast.makeText(getApplicationContext(), R.string.permission_denial, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case 1:
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "Bluetooth is enabled");
                    //블루투스 켜짐
                } else {
                    Log.d(TAG, "Bluetooth is not enabled");
                    //블루투스 에러
                }
                break;
        }
    }
}
