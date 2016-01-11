package com.rainbow.kam.bt_scanner.fragment.prime.main;

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
public class CalorieFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();

    private CircleCounter calorieCircleCounter;

    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.f_band_main_calorie, container, false);
        calorieCircleCounter = (CircleCounter) view.findViewById(R.id.calorie_counter);
        calorieCircleCounter.setFirstWidth(getResources().getDimension(R.dimen.first));
        calorieCircleCounter.setSecondWidth(getResources().getDimension(R.dimen.second));
        calorieCircleCounter.setThirdWidth(getResources().getDimension(R.dimen.third));
        calorieCircleCounter.setFirstColor(ContextCompat.getColor(context, R.color.calorieAccent));
        calorieCircleCounter.setSecondColor(ContextCompat.getColor(context, R.color.caloriePrimary));
        calorieCircleCounter.setThirdColor(ContextCompat.getColor(context, R.color.caloriePrimaryDark));
        calorieCircleCounter.setBackgroundColor(ContextCompat.getColor(context, R.color.calorieColor));
        return view;
    }


    public void setCalorie(int calorie) {
        calorieCircleCounter.setValues(calorie, calorie, calorie);
    }


}
