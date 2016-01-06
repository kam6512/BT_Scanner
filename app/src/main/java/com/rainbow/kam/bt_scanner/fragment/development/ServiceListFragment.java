package com.rainbow.kam.bt_scanner.fragment.development;

import android.app.Activity;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.adapter.ServiceAdapter;

import java.util.List;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class ServiceListFragment extends Fragment {

    private Activity activity;

    private View view;

    private ServiceAdapter serviceAdapter;

    private OnServiceReadyListener onServiceReadyListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            try {
                activity = (Activity) context;
                onServiceReadyListener = (OnServiceReadyListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString() + " must implement OnServiceReadyListener");
            }
        } else {
            throw new ClassCastException(context.toString() + " OnAttach Context not cast by Activity");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_detail_service, container, false);
            setRecyclerView();
        }
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onServiceReadyListener.onServiceReady();
    }


    private void setRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.detail_service_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        serviceAdapter = new ServiceAdapter(activity);
        recyclerView.setAdapter(serviceAdapter);
    }


    @DebugLog
    public void setService(List<BluetoothGattService> bluetoothGattServices) {
        if (serviceAdapter.getItemCount() == 0) {
            serviceAdapter.clearList();
            for (BluetoothGattService bluetoothGattService : bluetoothGattServices) {
                serviceAdapter.add(bluetoothGattService);
            }
            serviceAdapter.notifyDataSetChanged();
        }
    }


    public interface OnServiceReadyListener {
        void onServiceReady();
    }


    public void removeListener() {
        onServiceReadyListener = null;
        serviceAdapter.removeListener();
    }
}
