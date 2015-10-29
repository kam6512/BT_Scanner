package com.rainbow.kam.bt_scanner.Fragment;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rainbow.kam.bt_scanner.Adapter.DetailAdpater.DetailAdapter;
import com.rainbow.kam.bt_scanner.Adapter.DetailAdpater.ServiceItem;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.Tools.BLE.BLEGattAttributes;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by sion on 2015-10-29.
 */
public class DetailServiceFragment extends Fragment {

    private Activity activity;

    private View view;

    private RecyclerView recyclerView;
    private DetailAdapter adapter;
    private ArrayList<ServiceItem> serviceItemArrayList = new ArrayList<ServiceItem>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = getActivity();
        view = inflater.inflate(R.layout.fragment_detail_service, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.detail_service_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new DetailAdapter(serviceItemArrayList, activity, activity, true);
        recyclerView.setAdapter(adapter);
        return view;
    }

    public void clearAdapter() {
        adapter.clearList(0);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void addService(BluetoothGattService bluetoothGattService) {
//        for (int i = 0; i < serviceItemArrayList.size(); i++) {
        Log.e("frag", "add Service");
//            if (serviceItemArrayList.get(i).getBluetoothGattService() == null) {
        String uuid = bluetoothGattService.getUuid().toString().toLowerCase(Locale.getDefault());
        String name = BLEGattAttributes.resolveServiceName(uuid);
        String type = (bluetoothGattService.getType() == BluetoothGattService.SERVICE_TYPE_PRIMARY) ? "primary" : "Secondary";
        serviceItemArrayList.add(new ServiceItem(name, uuid, type, bluetoothGattService));
//            }
//        }
        adapter = new DetailAdapter(serviceItemArrayList, activity, activity, true);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public BluetoothGattService getService(int index) {
        return serviceItemArrayList.get(index).getBluetoothGattService();
    }

    public void noti() {
        adapter.notifyDataSetChanged();
    }

}
