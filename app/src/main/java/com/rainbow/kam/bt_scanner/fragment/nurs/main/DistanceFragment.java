package com.rainbow.kam.bt_scanner.fragment.nurs.main;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rainbow.kam.bt_scanner.adapter.nurs.dashboard_NotInUse.DashboardItem;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.design.CircleCounter;

import java.util.ArrayList;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class DistanceFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private View view;
    private CircleCounter distanceCircleCounter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        view = inflater.inflate(R.layout.fragmnet_nursing_main_distance, container, false);
        distanceCircleCounter = (CircleCounter) view.findViewById(R.id.distance_counter);
        distanceCircleCounter.setFirstWidth(getResources().getDimension(R.dimen.first));
        distanceCircleCounter.setSecondWidth(getResources().getDimension(R.dimen.second));
        distanceCircleCounter.setThirdWidth(getResources().getDimension(R.dimen.third));
        distanceCircleCounter.setFirstColor(getResources().getColor(R.color.dirAccent));
        distanceCircleCounter.setSecondColor(getResources().getColor(R.color.dirPrimary));
        distanceCircleCounter.setThirdColor(getResources().getColor(R.color.dirPrimaryDark));
        distanceCircleCounter.setBackgroundColor(getResources().getColor(R.color.dirColor));
        return view;
    }

    public void setDist(int dist) {
        distanceCircleCounter.setValues(dist, dist, dist);
    }

}
