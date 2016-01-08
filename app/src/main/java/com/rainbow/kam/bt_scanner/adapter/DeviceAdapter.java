package com.rainbow.kam.bt_scanner.adapter;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import hugo.weaving.DebugLog;

/**
 * Created by Kam6512 on 2015-10-14.
 */
public class DeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "DeviceAdapter";

    //    private final LinkedHashMap<String, DeviceItem> deviceItemLinkedHashMap = new LinkedHashMap<>();
    private final DeviceLinkedHashMap<String, DeviceItem> deviceLinkedHashMap = new DeviceLinkedHashMap<>();

    private OnDeviceSelectListener onDeviceSelectListener;


    public DeviceAdapter(OnDeviceSelectListener onDeviceSelectListener) { //초기화
        this.onDeviceSelectListener = onDeviceSelectListener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View root = layoutInflater.inflate(R.layout.i_bluetooth_device, parent, false);
        return new DeviceViewHolder(root);
    }


    @DebugLog
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        DeviceViewHolder deviceViewHolder = (DeviceViewHolder) holder;
        long startTime = System.nanoTime();
//        for (int i = 9999; i >= 0; i--) {
//        deviceViewHolder.bindViews((DeviceItem) deviceItemLinkedHashMap.values().toArray()[position]);
        deviceViewHolder.bindViews(deviceLinkedHashMap.getValue(position));
//        }
        long endTime = System.nanoTime();
        long duration = endTime - startTime;
        System.out.println("deviceLinkedHashMap get:  " + duration);
    }


    @Override
    public int getItemCount() {
//        return deviceItemLinkedHashMap.size();
        return deviceLinkedHashMap.size();
    }


    @DebugLog
    public void add(BluetoothDevice bluetoothDevice, int rssi) {
//        if (!deviceItemLinkedHashMap.containsKey(bluetoothDevice.getAddress())) {
//            deviceItemLinkedHashMap.put(bluetoothDevice.getAddress(), new DeviceItem(bluetoothDevice, rssi));
//            notifyDataSetChanged();
//        }
        if (!deviceLinkedHashMap.containsKey(bluetoothDevice.getAddress())) {
            deviceLinkedHashMap.put(bluetoothDevice.getAddress(), new DeviceItem(bluetoothDevice, rssi));
            notifyDataSetChanged();
        }
    }


    @DebugLog
    public void clear() {
//        deviceItemLinkedHashMap.clear();
        deviceLinkedHashMap.clear();
        notifyDataSetChanged();
    }


    public class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener { //뷰 초기화

        private final TextView extraName;
        private final TextView extraAddress;
        private final TextView extraBondState;
        private final TextView extraType;
        private final TextView extraRssi;

        private final CardView deviceItemCardView;


        public DeviceViewHolder(View itemView) {
            super(itemView);

            extraName = (TextView) itemView.findViewById(R.id.item_name);
            extraAddress = (TextView) itemView.findViewById(R.id.item_address);
            extraBondState = (TextView) itemView.findViewById(R.id.item_bond);
            extraType = (TextView) itemView.findViewById(R.id.item_type);
            extraRssi = (TextView) itemView.findViewById(R.id.item_rssi);

            deviceItemCardView = (CardView) itemView.findViewById(R.id.device_item_card);
            deviceItemCardView.setOnClickListener(this);
        }


        private void bindViews(DeviceItem deviceItem) {
            String deviceName = deviceItem.getExtraName();
            if (deviceName == null) {
                deviceName = "N/A";
            }
            extraName.setText(deviceName);
            extraAddress.setText(deviceItem.getExtraAddress());
            extraBondState.setText(String.valueOf(deviceItem.getExtraBondState()));
            extraType.setText(String.valueOf(deviceItem.getExtraType()));
            extraRssi.setText(String.valueOf(deviceItem.getExtraRssi()));
        }


        @Override
        public void onClick(View v) {
//            DeviceItem deviceItem = deviceItemLinkedHashMap.get(extraAddress.getText().toString());
            DeviceItem deviceItem = deviceLinkedHashMap.get(extraAddress.getText().toString());
//            DeviceItem deviceItem = (DeviceItem) deviceItemLinkedHashMap.values().toArray()[getAdapterPosition()];
            onDeviceSelectListener.onDeviceSelect(deviceItem.getExtraName(), deviceItem.getExtraAddress());
        }
    }

    public interface OnDeviceSelectListener {
        void onDeviceSelect(String name, String address);
    }
}