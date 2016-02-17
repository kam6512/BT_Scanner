package com.rainbow.kam.bt_scanner.fragment.profile;

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
import com.rainbow.kam.bt_scanner.adapter.profile.CharacteristicAdapter;

import java.util.List;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class CharacteristicListFragment extends Fragment {

    private Context context;

    private View view;

    private CharacteristicAdapter characteristicAdapter;

    private OnCharacteristicReadyListener onCharacteristicReadyListener;


    @DebugLog
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        onCharacteristicReadyListener = (OnCharacteristicReadyListener) context;
    }


    @DebugLog
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.f_profile_characteristic, container, false);
            setRecyclerView();
        }
        return view;
    }


    @DebugLog
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onCharacteristicReadyListener.onCharacteristicReady();
    }


    private void setRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.profile_characteristic_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setHasFixedSize(true);
        characteristicAdapter = new CharacteristicAdapter(context);
        recyclerView.setAdapter(characteristicAdapter);
    }


    @DebugLog
    public void setCharacteristicList(List<BluetoothGattCharacteristic> bluetoothGattCharacteristics) {
        characteristicAdapter.setCharacteristicList(bluetoothGattCharacteristics);
    }


    public interface OnCharacteristicReadyListener {
        void onCharacteristicReady();
    }
}
