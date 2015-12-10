package com.rainbow.kam.bt_scanner.fragment.dev;

import android.app.Activity;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rainbow.kam.bt_scanner.adapter.dev.detail.ServiceAdapter;
import com.rainbow.kam.bt_scanner.adapter.dev.detail.ServiceItem;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.ble.BLEGattAttributes;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class DetailServiceFragment extends Fragment {

    private Activity activity;

    private View view;

    private RecyclerView recyclerView;
    //    private DetailAdapter adapter;
    private ServiceAdapter serviceAdapter;
    private ArrayList<ServiceItem> serviceItemArrayList = new ArrayList<>();

    private OnServiceViewCreatedListener onServiceViewCreatedListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            onServiceViewCreatedListener = (OnServiceViewCreatedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnServiceViewCreatedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_detail_service, container, false);
        activity = getActivity();
        setRecyclerView();
        onServiceViewCreatedListener.onServiceViewCreated();
        return view;
    }

    private void setRecyclerView() {
        recyclerView = (RecyclerView) view.findViewById(R.id.detail_service_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        serviceAdapter = new ServiceAdapter(serviceItemArrayList);
        recyclerView.setAdapter(serviceAdapter);
    }

    public void addService(BluetoothGattService bluetoothGattService) {

        String uuid = bluetoothGattService.getUuid().toString().toLowerCase(Locale.getDefault());
        String name = BLEGattAttributes.resolveServiceName(uuid);
        String type = (bluetoothGattService.getType() == BluetoothGattService.SERVICE_TYPE_PRIMARY) ? "primary" : "Secondary";

        serviceItemArrayList.add(new ServiceItem(name, uuid, type, bluetoothGattService));
        serviceAdapter.notifyDataSetChanged();
    }

    public BluetoothGattService getService(int index) {
        return serviceItemArrayList.get(index).getBluetoothGattService();
    }

    public void notifyAdapter() {
        serviceAdapter.notifyDataSetChanged();
    }

    public void clearAdapter() {
        serviceAdapter.clearList();
        recyclerView.setAdapter(serviceAdapter);
        serviceAdapter.notifyDataSetChanged();
    }

    public interface OnServiceViewCreatedListener {
        public void onServiceViewCreated();
    }
}
