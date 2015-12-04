package com.rainbow.kam.bt_scanner.fragment.nurs.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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

    private View view;

    private CircleCounter calorieCircleCounter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        view = inflater.inflate(R.layout.fragmnet_nursing_main_calorie, container, false);
        calorieCircleCounter = (CircleCounter) view.findViewById(R.id.calorie_counter);
        calorieCircleCounter.setFirstWidth(getResources().getDimension(R.dimen.first));
        calorieCircleCounter.setSecondWidth(getResources().getDimension(R.dimen.second));
        calorieCircleCounter.setThirdWidth(getResources().getDimension(R.dimen.third));
        calorieCircleCounter.setFirstColor(getResources().getColor(R.color.caloAccent));
        calorieCircleCounter.setSecondColor(getResources().getColor(R.color.caloPrimary));
        calorieCircleCounter.setThirdColor(getResources().getColor(R.color.caloPrimaryDark));
        calorieCircleCounter.setBackgroundColor(getResources().getColor(R.color.caloColor));
        return view;
    }


    public void setCalorie(int calo) {
        calorieCircleCounter.setValues(calo, calo, calo);
    }


}
