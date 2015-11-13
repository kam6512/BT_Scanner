package com.rainbow.kam.bt_scanner.Nursing.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.Nursing.Fragment.StartNursingFragment;
import com.rainbow.kam.bt_scanner.R;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class StartNursingActivity extends AppCompatActivity {


    public static final String TAG = StartNursingActivity.class.getSimpleName();

    public static FloatingActionButton startNursingFab;

    private Animation animation;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private StartNursingFragment startNusingFragment;

    public static int indexByStart = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        startNusingFragment = new StartNursingFragment();
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
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
        indexByStart = 0;
//        finish();
//        android.os.Process.killProcess(android.os.Process.myPid());
//        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    //블루투스 켜짐
                } else {
                    Log.d(TAG, "Bluetooth is not enabled");
                    //블루투스 에러
                }
                break;
        }
    }
}
