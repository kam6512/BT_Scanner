package com.rainbow.kam.bt_scanner.adapter;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Created by Kam6512 on 2015-10-14.
 */
public class DeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "DeviceAdapter";

    //    private LinkedHashMap<String, DeviceItem> itemLinkedHashMap = new LinkedHashMap<>();
    private ArrayList<DeviceItem> deviceItemArrayList = new ArrayList<>();

    private OnDeviceSelectListener onDeviceSelectListener;


    public DeviceAdapter(Activity activity) { //초기화
        onDeviceSelectListener = (OnDeviceSelectListener) activity;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        deviceItemArrayList = new ArrayList<>(itemLinkedHashMap.values());
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View root = layoutInflater.inflate(R.layout.bluetooth_device_item, parent, false);
        return new DeviceViewHolder(root);
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DeviceViewHolder deviceViewHolder = (DeviceViewHolder) holder;
        deviceViewHolder.bindViews(deviceItemArrayList.get(position));
        Log.e(TAG,
                "position = " + position + "\n" +
                        "getAdapterPosition = " + deviceViewHolder.getAdapterPosition() + "\n" +
                        "getLayoutPosition = " + deviceViewHolder.getLayoutPosition() + "\n" +
                        "getOldPosition = " + deviceViewHolder.getOldPosition()
        );
    }


    @Override
    public int getItemCount() {
//        return itemLinkedHashMap.size();
        return deviceItemArrayList.size();
    }


    public void add(DeviceItem deviceItem) {
        deviceItemArrayList.add(deviceItem);
    }


    public void clear() {
        deviceItemArrayList.clear();
        notifyDataSetChanged();
    }


    public class DeviceViewHolder extends RecyclerView.ViewHolder { //뷰 초기화

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
            deviceItemCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DeviceItem deviceItem = deviceItemArrayList.get(getAdapterPosition());
                    onDeviceSelectListener.onDeviceSelect(deviceItem.getExtraName(), deviceItem.getExtraAddress());
                }
            });
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
    }

    public interface OnDeviceSelectListener {
        void onDeviceSelect(String name, String address);
    }
}
