package com.rainbow.kam.bt_scanner.adapter.dev.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.dev.DetailActivity;

import java.util.ArrayList;

/**
 * Created by Kam6512 on 2015-10-14.
 */
public class MainDeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "SelectDeviceAdapter";


    private ArrayList<MainDeviceItem> mainDeviceItemArrayList;
    private Activity activity;
    private DeviceViewHolder deviceViewHolder;

    public MainDeviceAdapter(ArrayList<MainDeviceItem> mainDeviceItemArrayList, Activity activity) { //초기화
        this.mainDeviceItemArrayList = mainDeviceItemArrayList;
        this.activity = activity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        DeviceViewHolder createDeviceViewHolder;
        View root;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        root = layoutInflater.inflate(R.layout.main_bluetooth_device_item, parent, false);
        createDeviceViewHolder = new DeviceViewHolder(root);
        return createDeviceViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        deviceViewHolder = (DeviceViewHolder) holder;
        deviceViewHolder.bindViews(mainDeviceItemArrayList.get(position));
    }

    @Override
    public int getItemCount() {
        return mainDeviceItemArrayList.size();
    }

    public class DeviceViewHolder extends RecyclerView.ViewHolder { //뷰 초기화

        private TextView extraName;
        private TextView extraAddress;
        private TextView extraBondState;
        private TextView extraType;
        private TextView extraRssi;

        private CardView deviceItemCardView;

        public DeviceViewHolder(View itemView) {
            super(itemView);

            extraName = (TextView) itemView.findViewById(R.id.item_name);
            extraAddress = (TextView) itemView.findViewById(R.id.item_address);
            extraBondState = (TextView) itemView.findViewById(R.id.item_bond);
            extraType = (TextView) itemView.findViewById(R.id.item_type);
            extraRssi = (TextView) itemView.findViewById(R.id.item_rssi);

            deviceItemCardView = (CardView) itemView.findViewById(R.id.device_item_card);
            deviceItemCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, DetailActivity.class);
                    intent.putExtra(DetailActivity.EXTRAS_DEVICE_NAME, extraName.getText().toString());
                    intent.putExtra(DetailActivity.EXTRAS_DEVICE_ADDRESS, extraAddress.getText().toString());
                    intent.putExtra(DetailActivity.EXTRAS_DEVICE_RSSI, extraRssi.getText().toString());
                    activity.startActivity(intent);
                }
            });
        }

        private void bindViews(MainDeviceItem mainDeviceItem) {
            extraName.setText(mainDeviceItem.getExtraName());
            extraAddress.setText(mainDeviceItem.getExtraextraAddress());
            extraBondState.setText(String.valueOf(mainDeviceItem.getExtraBondState()));
            extraType.setText(String.valueOf(mainDeviceItem.getExtraType()));
            extraRssi.setText(String.valueOf(mainDeviceItem.getExtraRssi()));
        }
    }
}
