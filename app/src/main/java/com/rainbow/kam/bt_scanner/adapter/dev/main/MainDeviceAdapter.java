package com.rainbow.kam.bt_scanner.adapter.dev.main;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.dev.DetailActivity;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by Kam6512 on 2015-10-14.
 */
public class MainDeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "SelectDeviceAdapter";

    private LinkedHashMap<String, MainDeviceItem> mainDeviceItemLinkedHashMap = new LinkedHashMap<>();
    private ArrayList<MainDeviceItem> mainDeviceItemArrayList;

    private OnDeviceItemClickListener onDeviceItemClickListener;


    public MainDeviceAdapter(LinkedHashMap<String, MainDeviceItem> mainDeviceItemLinkedHashMap, Activity activity) { //초기화
        this.mainDeviceItemLinkedHashMap = mainDeviceItemLinkedHashMap;

        try {
            onDeviceItemClickListener = (OnDeviceItemClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnDeviceItemClickListener");
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        mainDeviceItemArrayList = new ArrayList<>(mainDeviceItemLinkedHashMap.values());

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View root = layoutInflater.inflate(R.layout.main_bluetooth_device_item, parent, false);
        return new DeviceViewHolder(root);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DeviceViewHolder deviceViewHolder = (DeviceViewHolder) holder;
        deviceViewHolder.bindViews(mainDeviceItemArrayList.get(position));
    }

    @Override
    public int getItemCount() {
        return mainDeviceItemLinkedHashMap.size();
    }

    public class DeviceViewHolder extends RecyclerView.ViewHolder { //뷰 초기화

        private final TextView deviceName;
        private final TextView deviceAddress;
        private final TextView deviceBondState;
        private final TextView deviceType;
        private final TextView deviceRssi;

        private final CardView deviceItemCardView;

        public DeviceViewHolder(View itemView) {
            super(itemView);

            deviceName = (TextView) itemView.findViewById(R.id.item_name);
            deviceAddress = (TextView) itemView.findViewById(R.id.item_address);
            deviceBondState = (TextView) itemView.findViewById(R.id.item_bond);
            deviceType = (TextView) itemView.findViewById(R.id.item_type);
            deviceRssi = (TextView) itemView.findViewById(R.id.item_rssi);

            deviceItemCardView = (CardView) itemView.findViewById(R.id.device_item_card);
            deviceItemCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    onDeviceItemClickListener.onDeviceItemClick(deviceName.getText().toString(),
                            deviceAddress.getText().toString(),
                            deviceRssi.getText().toString());
                }
            });
        }

        private void bindViews(MainDeviceItem mainDeviceItem) {
            deviceName.setText(mainDeviceItem.getDeviceName());
            deviceAddress.setText(mainDeviceItem.getDeviceAddress());
            deviceBondState.setText(String.valueOf(mainDeviceItem.getDeviceBondState()));
            deviceType.setText(String.valueOf(mainDeviceItem.getDeviceType()));
            deviceRssi.setText(String.valueOf(mainDeviceItem.getDeviceRssi()));
        }
    }

    public interface OnDeviceItemClickListener {
        void onDeviceItemClick(String deviceName,
                               String deviceAddress,
                               String deviceRssi);
    }
}
