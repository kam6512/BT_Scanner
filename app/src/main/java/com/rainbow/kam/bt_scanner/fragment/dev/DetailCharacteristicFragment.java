package com.rainbow.kam.bt_scanner.fragment.dev;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
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
import com.rainbow.kam.bt_scanner.adapter.dev.detail.CharacteristicItem;
import com.rainbow.kam.bt_scanner.adapter.dev.detail.DetailAdapter;
import com.rainbow.kam.bt_scanner.tools.ble.BLEGattAttributes;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class DetailCharacteristicFragment extends Fragment {

    private Activity activity;

    private View view;

    private RecyclerView recyclerView;
    private DetailAdapter adapter;
    private ArrayList<CharacteristicItem> characteristicItemArrayList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e("Characteristic", "onCreateView");
        activity = getActivity();
        view = inflater.inflate(R.layout.fragment_detail_characteristic, container, false);
        recyclerView = (RecyclerView) view.findViewById(R.id.detail_characteristic_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new DetailAdapter(characteristicItemArrayList);
        if (adapter != null && recyclerView != null){
            Log.e("a/r","not null");
        }else{
            Log.e("a/r","is null");
        }
            recyclerView.setAdapter(adapter);
        return view;
    }

    public void addCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {

        String uuid = bluetoothGattCharacteristic.getUuid().toString().toLowerCase(Locale.getDefault());
        String name = BLEGattAttributes.resolveCharacteristicName(uuid);

        characteristicItemArrayList.add(new CharacteristicItem(name, uuid, "0", bluetoothGattCharacteristic));
//        adapter = new DetailAdapter(characteristicItemArrayList);
//        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();


    }

    public BluetoothGattCharacteristic getCharacteristic(int index) {
        return characteristicItemArrayList.get(index).getBluetoothGattCharacteristic();
    }

    public void noti() {
        adapter.notifyDataSetChanged();
    }

    public void clearAdapter() {
        adapter.clearList(1);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
