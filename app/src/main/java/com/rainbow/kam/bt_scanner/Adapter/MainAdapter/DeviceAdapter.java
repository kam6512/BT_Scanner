package com.rainbow.kam.bt_scanner.Adapter.MainAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.Activity.DetailActivity;
import com.rainbow.kam.bt_scanner.BluetoothPackage.DetailGattAuto;
import com.rainbow.kam.bt_scanner.R;

import java.util.ArrayList;

/**
 * Created by Kam6512 on 2015-10-14.
 */
public class DeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //태그
    private static final String TAG = "DeviceAdapter";

    //고정 네임
    public static final String DEVICE_NAME = "DEVICE_NAME";
    public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private ArrayList<DeviceItem> deviceItemArrayList; //어댑터에 적용시킬 틀
    private Context context; //컨택스트
    private Activity activity; //액티비티
    private View view; //SnackBar대비 뷰
    private int listLength;
    private Device[] devices;

    public static SimpleExpandableListAdapter[] simpleExpandableListAdapter = null;
    public static ExpandableListView.OnChildClickListener[] onChildClickListener = null;

    public DeviceAdapter(ArrayList<DeviceItem> deviceItemArrayList, Activity activity, Context context, View view, int listLength) { //초기화
        this.deviceItemArrayList = deviceItemArrayList;
        this.activity = activity;
        this.context = context;
        this.view = view;
        this.listLength = listLength;
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
//        //뷰홀더 적용
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

        private DetailGattAuto detailGattAuto;

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
                    Intent intent = new Intent(activity, DetailActivity.class);
                    intent.putExtra("Address", extraAddress.getText().toString());
                    activity.startActivity(intent);
                }
            });
        }

        public void bindViews(DeviceItem deviceItem) {
            //각각의 뷰 속성 적용
            extraName.setText(deviceItem.getExtraName());
            extraAddress.setText(deviceItem.getExtraextraAddress());
            extraBondState.setText(String.valueOf(deviceItem.getExtraBondState()));
            extraType.setText(String.valueOf(deviceItem.getExtraType()));
            extraRssi.setText(String.valueOf(deviceItem.getExtraRssi()));

        }
    }
}
