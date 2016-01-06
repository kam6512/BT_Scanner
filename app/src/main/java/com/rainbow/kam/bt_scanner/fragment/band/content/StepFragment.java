package com.rainbow.kam.bt_scanner.fragment.band.content;

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
public class StepFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private CircleCounter stepCircleCounter;

    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

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

        View view = inflater.inflate(R.layout.f_band_main_step, container, false);
        stepCircleCounter = (CircleCounter) view.findViewById(R.id.step_counter);
        stepCircleCounter.setFirstWidth(getResources().getDimension(R.dimen.first));
        stepCircleCounter.setSecondWidth(getResources().getDimension(R.dimen.second));
        stepCircleCounter.setThirdWidth(getResources().getDimension(R.dimen.third));
        stepCircleCounter.setFirstColor(ContextCompat.getColor(context, R.color.stepAccent));
        stepCircleCounter.setSecondColor(ContextCompat.getColor(context, R.color.stepPrimary));
        stepCircleCounter.setThirdColor(ContextCompat.getColor(context, R.color.stepPrimaryDark));
        stepCircleCounter.setBackgroundColor(ContextCompat.getColor(context, R.color.stepColor));
        return view;
    }

    public void setStep(int step) {
        stepCircleCounter.setValues(step, step, step);
    }
}
