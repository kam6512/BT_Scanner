package com.rainbow.kam.bt_scanner;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.bluetooth.BluetoothService;

import java.util.ArrayList;

/**
 * Created by sion on 2015-10-14.
 */
public class DeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<DeviceItem> deviceItemArrayList;
    private Context context;
    private View view;

    public DeviceAdapter(ArrayList<DeviceItem> deviceItemArrayList, Context context, View view) {
        this.deviceItemArrayList = deviceItemArrayList;
        this.context = context;
        this.view = view;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Device(LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_device_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Device device = (Device) holder;
        device.extraName.setText(deviceItemArrayList.get(position).getExtraName());
        device.extraAddress.setText(deviceItemArrayList.get(position).getExtraextraAddress());
        device.extraBondState.setText(String.valueOf(deviceItemArrayList.get(position).getExtraBondState()));
        device.extraType.setText(String.valueOf(deviceItemArrayList.get(position).getExtraType()));
        device.extraRssi.setText(String.valueOf(deviceItemArrayList.get(position).getExtraRssi()));
    }

    @Override
    public int getItemCount() {
        return deviceItemArrayList.size();
    }

    private class Device extends RecyclerView.ViewHolder {

        private CardView cardView;

        private TextView extraName;
        private TextView extraAddress;
        private TextView extraBondState;
        private TextView extraType;
        private TextView extraRssi;

        public Device(View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.card);

            extraName = (TextView) itemView.findViewById(R.id.item_name);
            extraAddress = (TextView) itemView.findViewById(R.id.item_address);
            extraBondState = (TextView) itemView.findViewById(R.id.item_bond);
            extraType = (TextView) itemView.findViewById(R.id.item_type);
            extraRssi = (TextView) itemView.findViewById(R.id.item_rssi);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  Intent getServiceintent = new Intent(context,BluetoothService.class);
                    context.bindService(getServiceintent,)
                }
            });

        }
    }
}
