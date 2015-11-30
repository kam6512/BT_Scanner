package com.rainbow.kam.bt_scanner.nursing.fragment.main;

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

import com.afollestad.materialdialogs.MaterialDialog;
import com.rainbow.kam.bt_scanner.nursing.adapter.DashboardAdapter;
import com.rainbow.kam.bt_scanner.nursing.adapter.DashboardItem;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.design.CircleCounter;

import java.util.ArrayList;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class StepFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private Activity activity;
    public static Handler handler;
    private Bundle bundle;
    private View view;
    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;

    private CircleCounter stepCircleCounter;
    private RecyclerView recyclerView;
    private BaseAdapter adapter;
    private ArrayList<DashboardItem> dashboardList = new ArrayList<>();
    private int step;

    public static StepFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        StepFragment fragment = new StepFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                bundle = msg.getData();
                step = Integer.valueOf(bundle.getString("STEP"), 16);
                super.handleMessage(msg);
                stepCircleCounter.setValues(step, step, step);


            }
        };

    }

    public void setArrayList(Activity activity, ArrayList<DashboardItem> dashboardList) {

        this.activity = activity;
        this.dashboardList = dashboardList;

        adapter = new DashboardAdapter(this.dashboardList, activity, activity, view);
//        recyclerView.setAdapter(adapter);
//        adapter.notifyDataSetChanged();

        new MaterialDialog.Builder(activity).title("step").adapter(adapter, new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {

            }
        }).show();
    }

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
        recyclerView = (RecyclerView) view.findViewById(R.id.step_recycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        return view;
    }


}
