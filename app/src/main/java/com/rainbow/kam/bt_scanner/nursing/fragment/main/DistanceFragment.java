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

import com.rainbow.kam.bt_scanner.nursing.adapter.DashboardItem;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.design.CircleCounter;

import java.util.ArrayList;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class DistanceFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    private Activity activity;
    public static Handler handler;
    private Bundle bundle;
    private View view;
    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;

    private CircleCounter distanceCircleCounter;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private ArrayList<DashboardItem> dashboardList = new ArrayList<>();
    private int distance;

    public static DistanceFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        DistanceFragment fragment = new DistanceFragment();
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
                    distance = Integer.valueOf(bundle.getString("DIST"));
                    super.handleMessage(msg);
                    distanceCircleCounter.setValues(distance, distance, distance);

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


        view = inflater.inflate(R.layout.fragmnet_nursing_main_distance, container, false);
        distanceCircleCounter = (CircleCounter) view.findViewById(R.id.distance_counter);
        distanceCircleCounter.setFirstWidth(getResources().getDimension(R.dimen.first));
        distanceCircleCounter.setSecondWidth(getResources().getDimension(R.dimen.second));
        distanceCircleCounter.setThirdWidth(getResources().getDimension(R.dimen.third));
        distanceCircleCounter.setFirstColor(getResources().getColor(R.color.dirAccent));
        distanceCircleCounter.setSecondColor(getResources().getColor(R.color.dirPrimary));
        distanceCircleCounter.setThirdColor(getResources().getColor(R.color.dirPrimaryDark));
        distanceCircleCounter.setBackgroundColor(getResources().getColor(R.color.dirColor));
        recyclerView = (RecyclerView) view.findViewById(R.id.distance_recycler);
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
