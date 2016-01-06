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

import java.util.LinkedHashMap;

/**
 * Created by Kam6512 on 2015-10-14.
 */
public class DeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "DeviceAdapter";

    private LinkedHashMap<String, DeviceItem> deviceItemLinkedHashMap = new LinkedHashMap<>();

    private OnDeviceSelectListener onDeviceSelectListener;


    public DeviceAdapter(Activity activity) { //초기화
        onDeviceSelectListener = (OnDeviceSelectListener) activity;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View root = layoutInflater.inflate(R.layout.item_bluetooth_device, parent, false);
        return new DeviceViewHolder(root);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DeviceViewHolder deviceViewHolder = (DeviceViewHolder) holder;
        deviceViewHolder.bindViews((DeviceItem) deviceItemLinkedHashMap.values().toArray()[position]);
    }


    @Override
    public int getItemCount() {
        return deviceItemLinkedHashMap.size();
    }


    public void add(BluetoothDevice bluetoothDevice, int rssi) {
        if (!deviceItemLinkedHashMap.containsKey(bluetoothDevice.getAddress())) {
            deviceItemLinkedHashMap.put(bluetoothDevice.getAddress(), new DeviceItem(bluetoothDevice, rssi));
        }
        notifyDataSetChanged();
    }


    public void clear() {
        deviceItemLinkedHashMap.clear();
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
            DeviceItem deviceItem = (DeviceItem) deviceItemLinkedHashMap.values().toArray()[getAdapterPosition()];
            onDeviceSelectListener.onDeviceSelect(deviceItem.getExtraName(), deviceItem.getExtraAddress());
        }
    }

    public interface OnDeviceSelectListener {
        void onDeviceSelect(String name, String address);
    }
    public void removeListener() {
        onDeviceSelectListener = null;
    }
}