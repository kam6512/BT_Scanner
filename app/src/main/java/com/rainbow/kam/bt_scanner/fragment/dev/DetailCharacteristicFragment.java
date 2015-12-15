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
import com.rainbow.kam.bt_scanner.tools.gatt.GattAttributes;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class DetailCharacteristicFragment extends Fragment {

    private Activity activity;

    private View view;

    private RecyclerView recyclerView;
    private CharacteristicAdapter characteristicAdapter;
    private final ArrayList<CharacteristicItem> characteristicItemArrayList = new ArrayList<>();

    private OnCharacteristicReadyListener onCharacteristicReadyListener;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {

            try {
                activity = (Activity) context;
                onCharacteristicReadyListener = (OnCharacteristicReadyListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString() + " must implement OnDetailReadyListener");
            }
        } else {
            throw new ClassCastException(context.toString() + " OnAttach Context not cast by Activity");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_detail_characteristic, container, false);

        setRecyclerView();
        onCharacteristicReadyListener.onCharacteristicReady();
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
        String name = GattAttributes.resolveCharacteristicName(uuid);

        characteristicItemArrayList.add(new CharacteristicItem(name, uuid));
        characteristicAdapter.notifyDataSetChanged();
    }

    public void notifyAdapter() {
        characteristicAdapter.notifyDataSetChanged();
    }

    public void clearAdapter() {
        characteristicAdapter.clearList();
        recyclerView.setAdapter(characteristicAdapter);
        characteristicAdapter.notifyDataSetChanged();
    }

    public interface OnCharacteristicReadyListener {
        void onCharacteristicReady();
    }
}
