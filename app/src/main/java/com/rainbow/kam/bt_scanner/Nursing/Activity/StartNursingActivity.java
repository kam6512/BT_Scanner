package com.rainbow.kam.bt_scanner.Nursing.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;

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

    public static Handler nursingHandler;
    public static int indexByStart = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_nursing);


        nursingHandler = new Handler();

        startNursingFab = (FloatingActionButton) findViewById(R.id.nursing_next_fab);
        startNursingFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inflateFragment(false);
            }
        });
        inflateFragment(true);


//        nursingHandler = new Handler() {
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//            }
//        };

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
}
