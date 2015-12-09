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

    private LinkedHashMap<String, MainDeviceItem> itemLinkedHashMap = new LinkedHashMap<>();
    private ArrayList<MainDeviceItem> mainDeviceItemArrayList;
    private Activity activity;
    private DeviceViewHolder deviceViewHolder;


    public MainDeviceAdapter(LinkedHashMap<String, MainDeviceItem> itemLinkedHashMap, Activity activity) { //초기화
        this.itemLinkedHashMap = itemLinkedHashMap;
        this.activity = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mainDeviceItemArrayList = new ArrayList<>(itemLinkedHashMap.values());
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View root = layoutInflater.inflate(R.layout.main_bluetooth_device_item, parent, false);
        return new DeviceViewHolder(root);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        deviceViewHolder = (DeviceViewHolder) holder;
        deviceViewHolder.bindViews(mainDeviceItemArrayList.get(position));


    }

    @Override
    public int getItemCount() {
        return itemLinkedHashMap.size();
    }

    public class DeviceViewHolder extends RecyclerView.ViewHolder { //뷰 초기화

        private TextView deviceName;
        private TextView deviceAddress;
        private TextView deviceBondState;
        private TextView deviceType;
        private TextView deviceRssi;

        private CardView deviceItemCardView;

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
                    Intent intent = new Intent(activity, DetailActivity.class);
                    intent.putExtra(DetailActivity.EXTRAS_DEVICE_NAME, deviceName.getText().toString());
                    intent.putExtra(DetailActivity.EXTRAS_DEVICE_ADDRESS, deviceAddress.getText().toString());
                    intent.putExtra(DetailActivity.EXTRAS_DEVICE_RSSI, deviceRssi.getText().toString());
                    activity.startActivity(intent);
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
}
