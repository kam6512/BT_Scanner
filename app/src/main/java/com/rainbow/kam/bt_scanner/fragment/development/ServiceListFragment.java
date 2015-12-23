package com.rainbow.kam.bt_scanner.fragment.development;

import android.app.Activity;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rainbow.kam.bt_scanner.adapter.profile.ServiceAdapter;
import com.rainbow.kam.bt_scanner.adapter.profile.ServiceItem;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.gatt.GattAttributes;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class ServiceListFragment extends Fragment {

    private Activity activity;

    private View view;

    private ServiceAdapter serviceAdapter;
    private final ArrayList<ServiceItem> serviceItemArrayList = new ArrayList<>();

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
        view = inflater.inflate(R.layout.fragment_detail_service, container, false);
        setRecyclerView();
        onServiceReadyListener.onServiceReady();
        return view;
    }


    private void setRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.detail_service_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        serviceAdapter = new ServiceAdapter(serviceItemArrayList, activity);
        recyclerView.setAdapter(serviceAdapter);
    }


    public void addService(BluetoothGattService bluetoothGattService) {

        String uuid = bluetoothGattService.getUuid().toString().toLowerCase(Locale.getDefault());
        String name = GattAttributes.resolveServiceName(uuid);
        String type = (bluetoothGattService.getType() == BluetoothGattService.SERVICE_TYPE_PRIMARY) ? activity.getString(R.string.service_type_primary) : activity.getString(R.string.service_type_secondary);

        serviceItemArrayList.add(new ServiceItem(name, uuid, type));
        notifyAdapter();
    }


    public void notifyAdapter() {
        if (serviceAdapter.getItemCount() != 0) {
            serviceAdapter.notifyDataSetChanged();
        }
    }


    public void clearAdapter() {
        if (serviceAdapter.getItemCount() == 0) {
            serviceAdapter.clearList();
            notifyAdapter();
        }
    }


    public interface OnServiceReadyListener {
        void onServiceReady();
    }
}
