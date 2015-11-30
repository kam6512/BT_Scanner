package com.rainbow.kam.bt_scanner.adapter.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.activity.DetailActivity;
import com.rainbow.kam.bt_scanner.nursing.fragment.start.StartNursingFragment;

import java.util.ArrayList;

/**
 * Created by Kam6512 on 2015-10-14.
 */
public class DeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //태그
    private static final String TAG = "DeviceAdapter";


    private ArrayList<DeviceItem> deviceItemArrayList;
    private Activity activity;
    private int listLength;
    private Device[] devices;

    private boolean isNursing = false;

    public static SimpleExpandableListAdapter[] simpleExpandableListAdapter = null;
    public static ExpandableListView.OnChildClickListener[] onChildClickListener = null;

    public DeviceAdapter(ArrayList<DeviceItem> deviceItemArrayList, Activity activity,  int listLength, boolean isNursing) { //초기화
        this.deviceItemArrayList = deviceItemArrayList;
        this.activity = activity;

        this.listLength = listLength;
        this.isNursing = isNursing;
        simpleExpandableListAdapter = new SimpleExpandableListAdapter[this.listLength];
        onChildClickListener = new ExpandableListView.OnChildClickListener[this.listLength];
        devices = new Device[this.listLength];
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Device device;
        View root;
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        root = layoutInflater.inflate(R.layout.main_bluetooth_device_item, parent, false);
        device = new Device(root);
        return device;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        devices[position] = (Device) holder;
        devices[position].bindViews(deviceItemArrayList.get(position));

    }

    @Override
    public int getItemCount() {
        return deviceItemArrayList.size();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    public class Device extends RecyclerView.ViewHolder { //뷰 초기화

        private TextView extraName;
        private TextView extraAddress;
        private TextView extraBondState;
        private TextView extraType;
        private TextView extraRssi;

        private FloatingActionButton fab_connect;

        public Device(View itemView) {
            super(itemView);

            extraName = (TextView) itemView.findViewById(R.id.item_name);
            extraAddress = (TextView) itemView.findViewById(R.id.item_address);
            extraBondState = (TextView) itemView.findViewById(R.id.item_bond);
            extraType = (TextView) itemView.findViewById(R.id.item_type);
            extraRssi = (TextView) itemView.findViewById(R.id.item_rssi);

            fab_connect = (FloatingActionButton) itemView.findViewById(R.id.fab_connect);
            fab_connect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isNursing) {

                        Bundle info = getInfomation();
                        Message message = new Message();
                        message.setData(info);
                        StartNursingFragment.handler.sendMessage(message);

                    } else {
                        Intent intent = new Intent(activity, DetailActivity.class);
                        intent.putExtra(DetailActivity.EXTRAS_DEVICE_NAME, extraName.getText().toString());
                        intent.putExtra(DetailActivity.EXTRAS_DEVICE_ADDRESS, extraAddress.getText().toString());
                        intent.putExtra(DetailActivity.EXTRAS_DEVICE_RSSI, extraRssi.getText().toString());
                        activity.startActivity(intent);
                    }

                }
            });
        }

        public void bindViews(DeviceItem deviceItem) {

            extraName.setText(deviceItem.getExtraName());
            extraAddress.setText(deviceItem.getExtraextraAddress());
            extraBondState.setText(String.valueOf(deviceItem.getExtraBondState()));
            extraType.setText(String.valueOf(deviceItem.getExtraType()));
            extraRssi.setText(String.valueOf(deviceItem.getExtraRssi()));

        }

        public Bundle getInfomation() {
            Bundle info = new Bundle();
            info.putString("name", extraName.getText().toString());
            info.putString("address", extraAddress.getText().toString());
            return info;
        }
    }
}
