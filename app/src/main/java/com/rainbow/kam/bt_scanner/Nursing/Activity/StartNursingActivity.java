package com.rainbow.kam.bt_scanner.Nursing.Activity;

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
import android.view.animation.Animation;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.Nursing.Fragment.Start.StartNursingFragment;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.Tools.PermissionV21;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class StartNursingActivity extends AppCompatActivity {


    public static final String TAG = StartNursingActivity.class.getSimpleName();

    public static FloatingActionButton startNursingFab;

    public static int indexByStart = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PermissionV21.check(this);

        setContentView(R.layout.activity_nursing_start);


        startNursingFab = (FloatingActionButton) findViewById(R.id.nursing_next_fab);
        startNursingFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inflateFragment(false);
            }
        });
        inflateFragment(true);

    }

    private void inflateFragment(boolean isStart) {
        StartNursingFragment startNusingFragment = new StartNursingFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (isStart) {
            fragmentTransaction.add(R.id.nursing_start_frame, startNusingFragment);

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
        indexByStart = 0;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(getWindow().getDecorView(), "권한 획득, 감사합니다.", Snackbar.LENGTH_SHORT).show();
                } else {
                    onBackPressed();
                    Toast.makeText(getApplicationContext(), "[권한] 탭 -> [위치] 권한을 허용해주십시오", Toast.LENGTH_SHORT).show();
//                    startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION), 0);
                    Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                    myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                    myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(myAppSettings, 0);
                }
                break;
            default:
                Toast.makeText(getApplicationContext(), "권한의 획득을 거부", Toast.LENGTH_SHORT).show();
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
