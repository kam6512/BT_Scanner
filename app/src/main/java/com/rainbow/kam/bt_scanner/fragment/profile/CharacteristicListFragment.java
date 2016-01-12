package com.rainbow.kam.bt_scanner.fragment.profile;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
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
import com.rainbow.kam.bt_scanner.adapter.CharacteristicAdapter;

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


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Characteristic", "onCreate");
    }


    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.i("Characteristic", "onViewStateRestored");
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.i("Characteristic", "onStart");
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.i("Characteristic", "onResume");
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.i("Characteristic", "onPause");
    }


    @Override
    public void onStop() {
        super.onStop();
        Log.i("Characteristic", "onStop");
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i("Characteristic", "onDestroyView");

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Characteristic", "onDestroy");
    }


    @Override
    public void onDetach() {
        super.onDetach();
        Log.i("Characteristic", "onDetach");
    }


    @DebugLog
    @Override
    public void onAttach(Context context) {
        Log.i("Characteristic", "onAttach");
        super.onAttach(context);
        if (context instanceof Activity) {
            try {
                this.context = context;
                onCharacteristicReadyListener = (OnCharacteristicReadyListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString() + " must implement OnCharacteristicReadyListener");
            }
        } else {
            throw new ClassCastException(context.toString() + " OnAttach Context not cast by Activity");
        }
    }


    @DebugLog
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            Log.i("Characteristic", "onCreateView");
            view = inflater.inflate(R.layout.f_profile_characteristic, container, false);
            setRecyclerView();
        }
        return view;
    }


    @DebugLog
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i("Characteristic", "onViewCreated");
        onCharacteristicReadyListener.onCharacteristicReady();
    }


    private void setRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.detail_characteristic_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        characteristicAdapter = new CharacteristicAdapter((DeviceProfileActivity) context);
        recyclerView.setAdapter(characteristicAdapter);
    }


    @DebugLog
    public void setCharacteristicList(List<BluetoothGattCharacteristic> bluetoothGattCharacteristics) {
        characteristicAdapter.add(bluetoothGattCharacteristics);
    }


    public interface OnCharacteristicReadyListener {
        void onCharacteristicReady();
    }
}
