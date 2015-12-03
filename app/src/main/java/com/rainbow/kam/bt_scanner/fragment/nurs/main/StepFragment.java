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
import android.widget.BaseAdapter;

import com.rainbow.kam.bt_scanner.adapter.nurs.dashboard_NotInUse.DashboardAdapter;
import com.rainbow.kam.bt_scanner.adapter.nurs.dashboard_NotInUse.DashboardItem;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.design.CircleCounter;

import java.util.ArrayList;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class StepFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private View view;

    private CircleCounter stepCircleCounter;


    /*
        public void setArrayList(Activity activity, ArrayList<DashboardItem> dashboardList) {

            this.activity = activity;
            this.dashboardList = dashboardList;

            adapter = new DashboardAdapter(this.dashboardList, activity, activity, view);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            new MaterialDialog.Builder(activity).title("step").adapter(adapter, new MaterialDialog.ListCallback() {
                @Override
                public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {

                }
            }).show();
        }
    */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragmnet_nursing_main_step, container, false);
        stepCircleCounter = (CircleCounter) view.findViewById(R.id.step_counter);
        stepCircleCounter.setFirstWidth(getResources().getDimension(R.dimen.first));
        stepCircleCounter.setSecondWidth(getResources().getDimension(R.dimen.second));
        stepCircleCounter.setThirdWidth(getResources().getDimension(R.dimen.third));
        stepCircleCounter.setFirstColor(getResources().getColor(R.color.stepAccent));
        stepCircleCounter.setSecondColor(getResources().getColor(R.color.stepPrimary));
        stepCircleCounter.setThirdColor(getResources().getColor(R.color.stepPrimaryDark));
        stepCircleCounter.setBackgroundColor(getResources().getColor(R.color.stepColor));
        return view;
    }

    public void setStep(int step){
        stepCircleCounter.setValues(step, step, step);
    }

}
