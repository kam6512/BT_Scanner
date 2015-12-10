package com.rainbow.kam.bt_scanner.fragment.dev;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
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
import com.rainbow.kam.bt_scanner.adapter.dev.detail.CharacteristicAdapter;
import com.rainbow.kam.bt_scanner.adapter.dev.detail.CharacteristicItem;
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
    //    private DetailAdapter adapter;
    private CharacteristicAdapter characteristicAdapter;
    private ArrayList<CharacteristicItem> characteristicItemArrayList = new ArrayList<>();

    private OnCharacteristicViewCreatedListener onCharacteristicViewCreatedListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {

            try {
                activity = (Activity) context;
                onCharacteristicViewCreatedListener = (OnCharacteristicViewCreatedListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString() + " must implement OnDetailViewCreatedListener");
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_detail_characteristic, container, false);

        setRecyclerView();
        onCharacteristicViewCreatedListener.onCharacteristicViewCreated();
        return view;
    }

    private void setRecyclerView() {
        recyclerView = (RecyclerView) view.findViewById(R.id.detail_characteristic_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        characteristicAdapter = new CharacteristicAdapter(characteristicItemArrayList, activity);
        recyclerView.setAdapter(characteristicAdapter);
    }

    public void addCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {

        String uuid = bluetoothGattCharacteristic.getUuid().toString().toLowerCase(Locale.getDefault());
        String name = BLEGattAttributes.resolveCharacteristicName(uuid);

        characteristicItemArrayList.add(new CharacteristicItem(name, uuid, "0", bluetoothGattCharacteristic));
        characteristicAdapter.notifyDataSetChanged();
    }

    public BluetoothGattCharacteristic getCharacteristic(int index) {
        return characteristicItemArrayList.get(index).getBluetoothGattCharacteristic();
    }

    public void notifyAdapter() {
        characteristicAdapter.notifyDataSetChanged();
    }

    public void clearAdapter() {
        characteristicAdapter.clearList();
        recyclerView.setAdapter(characteristicAdapter);
        characteristicAdapter.notifyDataSetChanged();
    }

    public interface OnCharacteristicViewCreatedListener {
        void onCharacteristicViewCreated();
    }
}
