package com.rainbow.kam.bt_scanner.Fragment;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rainbow.kam.bt_scanner.Adapter.DetailAdpater.CharacteristicItem;
import com.rainbow.kam.bt_scanner.Adapter.DetailAdpater.DetailAdapter;
import com.rainbow.kam.bt_scanner.Adapter.DetailAdpater.ServiceItem;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.Tools.BLE.BLEGattAttributes;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by sion on 2015-10-29.
 */
public class DetailCharacteristicFragment extends Fragment {

    private Activity activity;

    private View view;

    private RecyclerView recyclerView;
    private DetailAdapter adapter;
    private ArrayList<CharacteristicItem> characteristicItemArrayList = new ArrayList<CharacteristicItem>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e("char","onCreateView");
        activity = getActivity();
        view = inflater.inflate(R.layout.fragment_detail_characteristic, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.detail_characteristic_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new DetailAdapter(characteristicItemArrayList, activity, activity);
        if (adapter == null){
            Log.e("char","adapter is a null");
        }else{
            Log.e("char","adapter is not a null");
        }
        recyclerView.setAdapter(adapter);
        return view;
    }

    public void clearAdapter() {
        adapter.clearList(1);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void addCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
//        for (int i = 0; i < serviceItemArrayList.size(); i++) {
        Log.e("frag", "add char");
//            if (serviceItemArrayList.get(i).getBluetoothGattService() == null) {
        String uuid = bluetoothGattCharacteristic.getUuid().toString().toLowerCase(Locale.getDefault());
        String name = BLEGattAttributes.resolveCharacteristicName(uuid);
        characteristicItemArrayList.add(new CharacteristicItem(name, uuid, "0", bluetoothGattCharacteristic));
//            }
//        }
        adapter = new DetailAdapter(characteristicItemArrayList, activity, activity);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

//    public BluetoothGattService getService(int index) {
//        return serviceItemArrayList.get(index).getBluetoothGattService();
//    }

    public void noti() {
        adapter.notifyDataSetChanged();
    }

}
