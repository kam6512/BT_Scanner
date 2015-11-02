package com.rainbow.kam.bt_scanner.Nursing.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.Nursing.Activity.StartNursingActivity;

/**
 * Created by sion on 2015-11-02.
 */
public class StartNusingFragment extends Fragment {

    private View view;
    private LinearLayout pilotLayout;

    private Animation animation;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (StartNursingActivity.indexByStart == 0) {
            view = inflater.inflate(R.layout.fragment_nursing_splash, container, false);
            pilotLayout = (LinearLayout) view.findViewById(R.id.nursing_start_pilot);
            animation = AnimationUtils.loadAnimation(getActivity(), R.anim.show_fab);
            pilotLayout.startAnimation(animation);
            StartNursingActivity.nursingHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
//                startNursingFab.setVisibility(View.VISIBLE);
//                startNursingFab.startAnimation(animation);

                }
            }, 200);
        } else {
            view = inflater.inflate(R.layout.fragment_nursing_splash, container, false);
        }


        return view;
    }
}
