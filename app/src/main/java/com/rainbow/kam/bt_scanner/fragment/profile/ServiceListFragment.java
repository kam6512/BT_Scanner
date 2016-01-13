package com.rainbow.kam.bt_scanner.fragment.profile;

import android.app.Activity;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.profile.DeviceProfileActivity;
import com.rainbow.kam.bt_scanner.adapter.ServiceAdapter;

import java.util.List;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class ServiceListFragment extends Fragment {

    private Context context;

    private View view;

    private ServiceAdapter serviceAdapter;

    private OnServiceReadyListener onServiceReadyListener;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("Service", "onCreate");
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.e("Service", "onViewStateRestored");
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.e("Service", "onStart");
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.e("Service", "onPause");
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.e("Service", "onResume");
    }


    @Override
    public void onStop() {
        super.onStop();
        Log.e("Service", "onStop");
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e("Service", "onDestroyView");

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("Service", "onDestroy");
    }


    @Override
    public void onDetach() {
        super.onDetach();
        Log.e("Service", "onDetach");
    }


    @DebugLog
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.e("Service", "onAttach");
        if (context instanceof Activity) {
            try {
                this.context = context;
                onServiceReadyListener = (OnServiceReadyListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString() + " must implement OnServiceReadyListener");
            }
        } else {
            throw new ClassCastException(context.toString() + " OnAttach Context not cast by Activity");
        }
    }


    @DebugLog
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            Log.e("Service", "onCreateView");
            view = inflater.inflate(R.layout.f_profile_service, container, false);
            setRecyclerView();
        }
        return view;
    }


    @DebugLog
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e("Service", "onViewCreated");
        onServiceReadyListener.onServiceReady();
    }


    private void setRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.detail_service_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        serviceAdapter = new ServiceAdapter((DeviceProfileActivity) context);
        recyclerView.setAdapter(serviceAdapter);
    }


    @DebugLog
    public void setServiceList(List<BluetoothGattService> bluetoothGattServices) {
        serviceAdapter.setService(bluetoothGattServices);
    }


    public interface OnServiceReadyListener {
        void onServiceReady();
    }
}
