package com.rainbow.kam.bt_scanner.fragment.development;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
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
import com.rainbow.kam.bt_scanner.adapter.CharacteristicAdapter;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class CharacteristicListFragment extends Fragment {

    private Activity activity;

    private View view;

    private CharacteristicAdapter characteristicAdapter;

    private OnCharacteristicReadyListener onCharacteristicReadyListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {

            try {
                activity = (Activity) context;
                onCharacteristicReadyListener = (OnCharacteristicReadyListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString() + " must implement OnCharacteristicReadyListener");
            }
        } else {
            throw new ClassCastException(context.toString() + " OnAttach Context not cast by Activity");
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_detail_characteristic, container, false);
            setRecyclerView();
        }
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onCharacteristicReadyListener.onCharacteristicReady();
    }


    private void setRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.detail_characteristic_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        characteristicAdapter = new CharacteristicAdapter(activity);
        recyclerView.setAdapter(characteristicAdapter);
    }


    @DebugLog
    public void setCharacteristic(List<BluetoothGattCharacteristic> bluetoothGattCharacteristics) {
        characteristicAdapter.clearList();
        for (BluetoothGattCharacteristic bluetoothGattCharacteristic : bluetoothGattCharacteristics) {
            characteristicAdapter.add(bluetoothGattCharacteristic);
        }
        characteristicAdapter.notifyDataSetChanged();
    }


    public interface OnCharacteristicReadyListener {
        void onCharacteristicReady();
    }
}
