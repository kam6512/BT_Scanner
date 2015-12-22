package com.rainbow.kam.bt_scanner.adapter;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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
public class SelectDeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "SelectDeviceAdapter";

    private LinkedHashMap<String, SelectDeviceItem> itemLinkedHashMap = new LinkedHashMap<>();
    private ArrayList<SelectDeviceItem> selectDeviceItemArrayList;

    private OnDeviceSelectListener onDeviceSelectListener;

    public SelectDeviceAdapter(LinkedHashMap<String, SelectDeviceItem> itemLinkedHashMap, Activity activity) { //초기화
        this.itemLinkedHashMap = itemLinkedHashMap;
        try {
            onDeviceSelectListener = (OnDeviceSelectListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnDeviceSelectListener");
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        selectDeviceItemArrayList = new ArrayList<>(itemLinkedHashMap.values());
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View root = layoutInflater.inflate(R.layout.bluetooth_device_item, parent, false);
        return new DeviceViewHolder(root);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DeviceViewHolder deviceViewHolder = (DeviceViewHolder) holder;
        deviceViewHolder.bindViews(selectDeviceItemArrayList.get(position));
    }

    @Override
    public int getItemCount() {
        return itemLinkedHashMap.size();
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
                    onDeviceSelectListener.onDeviceSelect(extraName.getText().toString(), extraAddress.getText().toString());
                }
            });
        }

        private void bindViews(SelectDeviceItem selectDeviceItem) {
            extraName.setText(selectDeviceItem.getExtraName());
            extraAddress.setText(selectDeviceItem.getExtraAddress());
            extraBondState.setText(String.valueOf(selectDeviceItem.getExtraBondState()));
            extraType.setText(String.valueOf(selectDeviceItem.getExtraType()));
            extraRssi.setText(String.valueOf(selectDeviceItem.getExtraRssi()));
        }
    }

    public interface OnDeviceSelectListener {
        void onDeviceSelect(String name, String address);
    }
}
