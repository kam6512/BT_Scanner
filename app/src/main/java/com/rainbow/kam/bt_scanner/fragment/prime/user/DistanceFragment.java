package com.rainbow.kam.bt_scanner.fragment.prime.user;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.design.CircleCounter;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class DistanceFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private CircleCounter distanceCircleCounter;

    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_band_main_distance, container, false);
        distanceCircleCounter = (CircleCounter) view.findViewById(R.id.distance_counter);
        distanceCircleCounter.setFirstColor(ContextCompat.getColor(context,R.color.distanceAccent));
        distanceCircleCounter.setSecondColor(ContextCompat.getColor(context,R.color.distancePrimary));
        distanceCircleCounter.setThirdColor(ContextCompat.getColor(context,R.color.distancePrimaryDark));
        distanceCircleCounter.setBackgroundColor(ContextCompat.getColor(context,R.color.distanceColor));
        return view;
    }

    public void setDist(int dist) {
        distanceCircleCounter.setValues(dist, dist, dist);
    }

}
