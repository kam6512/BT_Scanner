package com.rainbow.kam.bt_scanner.nursing.fragment.start;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.adapter.main.DeviceItem;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.ble.BleActivityManager;

import java.util.ArrayList;

/**
 * Created by kam6512 on 2015-11-04.
 */
public class SelectNursingDeviceFragment extends DialogFragment {

    private final String TAG = "SelectDialog"; //로그용 태그

    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothManager bluetoothManager = null;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView.Adapter adapter = null;
    private ArrayList<DeviceItem> deviceItemArrayList = new ArrayList<>();

    private BleActivityManager bleActivityManager;

    public SelectNursingDeviceFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nursing_start_add_device, container, false);
        Activity activity = getActivity();

        Handler handler = new Handler();

        ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.nursing_device_progress);
        progressBar.setVisibility(View.INVISIBLE);

        TextView hasCard = (TextView) view.findViewById(R.id.nursing_device_hasCard);
        hasCard.setVisibility(View.INVISIBLE);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.nursing_device_swipeRefreshLayout);

        RecyclerView selectDeviceRecyclerView = (RecyclerView) view.findViewById(R.id.nursing_device_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        selectDeviceRecyclerView.setLayoutManager(layoutManager);
        selectDeviceRecyclerView.setHasFixedSize(true);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        getDialog().getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        bleActivityManager = new BleActivityManager(TAG, activity, handler, bluetoothAdapter, bluetoothManager, swipeRefreshLayout, selectDeviceRecyclerView, adapter, deviceItemArrayList, view, progressBar, hasCard, true);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        bleActivityManager.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bleActivityManager.onDestroyView();
    }
}
