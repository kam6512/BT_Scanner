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

import com.rainbow.kam.bt_scanner.adapter.nurs.dashboard.DashboardItem;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.design.CircleCounter;

import java.util.ArrayList;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class CalorieFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private Activity activity;
    public static Handler handler;
    private Bundle bundle;
    private View view;
    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;

    private CircleCounter calorieCircleCounter;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private ArrayList<DashboardItem> dashboardList = new ArrayList<>();
    private int calorie;

    public static CalorieFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        CalorieFragment fragment = new CalorieFragment();
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
                    calorie = Integer.valueOf(bundle.getString("CALO"), 16);
                    super.handleMessage(msg);
                    calorieCircleCounter.setValues(calorie, calorie, calorie);

            }
        };

    }

    public void setArrayList(ArrayList<DashboardItem> dashboardList) {
        this.dashboardList = dashboardList;
//        adapter = new DashboardAdapter(this.dashboardList, activity, activity, view);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

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
        recyclerView = (RecyclerView) view.findViewById(R.id.calorie_recycler);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        activity = getActivity();
    }


}
