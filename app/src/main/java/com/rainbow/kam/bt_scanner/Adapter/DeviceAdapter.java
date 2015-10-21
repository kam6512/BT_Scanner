package com.rainbow.kam.bt_scanner.Adapter;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.BluetoothPackage.BluetoothService;
import com.rainbow.kam.bt_scanner.BluetoothPackage.DetailGatt;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.Tools.ServiceCheck;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sion on 2015-10-14.
 */
public class DeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //태그
    private static final String TAG = "DetailGatt";

    //고정 네임
    public static final String DEVICE_NAME = "DEVICE_NAME";
    public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private ArrayList<DeviceItem> deviceItemArrayList; //어댑터에 적용시킬 틀
    private Context context; //컨택스트
    private Activity activity; //액티비티
    private View view; //SnackBar대비 뷰
    int listLength;
    Device[] devices;

    public DeviceAdapter(ArrayList<DeviceItem> deviceItemArrayList, Activity activity, Context context, View view, int listLength) { //초기화
        this.deviceItemArrayList = deviceItemArrayList;
        this.activity = activity;
        this.context = context;
        this.view = view;
        this.listLength = listLength;
        devices = new Device[this.listLength];
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.e(TAG, "onCreateViewHolder");
        return new Device(LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_device_item, parent, false));

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //뷰홀더 적용
        devices[position] = (Device) holder;

        //각각의 뷰 속성 적용
        devices[position].extraName.setText(deviceItemArrayList.get(position).getExtraName());
        devices[position].extraAddress.setText(deviceItemArrayList.get(position).getExtraextraAddress());
        devices[position].extraBondState.setText(String.valueOf(deviceItemArrayList.get(position).getExtraBondState()));
        devices[position].extraType.setText(String.valueOf(deviceItemArrayList.get(position).getExtraType()));
        devices[position].extraRssi.setText(String.valueOf(deviceItemArrayList.get(position).getExtraRssi()));
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
//        super.onViewAttachedToWindow(holder);
        Log.e(TAG, "onViewAttachedToWindow at " + holder.getLayoutPosition());
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        //디바이스의 커넥션 OFF
        devices[holder.getLayoutPosition()].stopConnect();
    }


    @Override
    public int getItemCount() {
        return deviceItemArrayList.size();
    }

    public class Device extends RecyclerView.ViewHolder { //뷰 초기화

        private DetailGatt detailGatt;

        private ExpandableListView expandableListView;

        BluetoothService bluetoothService;
        ArrayList<ArrayList<BluetoothGattCharacteristic>> gattCharacteristics;
        BluetoothGattCharacteristic notifyCharacteristic;
        ArrayList<HashMap<String, String>> gattServiceData;
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData;

        private CardView cardView;

        public TextView extraName;
        private TextView extraAddress;
        private TextView extraBondState;
        private TextView extraType;
        private TextView extraRssi;

        public TextView state;
        public TextView address;
        public TextView dataField;

        private Handler handler;
        private Runnable runnable;

        public Device(View itemView) {
            super(itemView);

            Log.e(TAG, "leScanCallback  " + "start item");
            cardView = (CardView) itemView.findViewById(R.id.card);

            expandableListView = (ExpandableListView) itemView.findViewById(R.id.gatt_services_list);

            extraName = (TextView) itemView.findViewById(R.id.item_name);
            extraAddress = (TextView) itemView.findViewById(R.id.item_address);
            extraBondState = (TextView) itemView.findViewById(R.id.item_bond);
            extraType = (TextView) itemView.findViewById(R.id.item_type);
            extraRssi = (TextView) itemView.findViewById(R.id.item_rssi);

            state = (TextView) itemView.findViewById(R.id.detail_state);
            address = (TextView) itemView.findViewById(R.id.detail_address);
            dataField = (TextView) itemView.findViewById(R.id.detail_datafield);


            //핸들러
            handler = new Handler();

            //자동시작
            runnable = new Runnable() {
                @Override
                public void run() {
                    boolean isServiceRun = new ServiceCheck().BtnSVC_Run(context, "com.rainbow.kam.bt_scanner.BluetoothPackage.BluetoothService");
                    if (isServiceRun) {
                        Log.e(TAG, "is Service Ruuning " + getLayoutPosition());
                        if (extraName.getText().toString() == "") {
                            extraName.setText("Gatt 서비스 리소스를 기다리는 중...");
                        }
                        handler.postDelayed(runnable, 2000);

                    } else {
                        Log.e(TAG, "is Service Start " + getLayoutPosition());

                        if (extraName.getText().toString() == "") {
                            extraName.setText("BLE 네임정보를 가져오는 중....");
                        }

                        startConnect();
                    }
                }
            };
            handler.postDelayed(runnable, 2000);


            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ((detailGatt != null) && detailGatt.isDataFind) {

                        setAdapter();
                    }

                }
            });
        }

        private void setAdapter() {

            bluetoothService = detailGatt.getBluetoothService();
            gattCharacteristics = detailGatt.getGattCharacteristics();
            notifyCharacteristic = detailGatt.getNotifyCharacteristic();
            gattServiceData = detailGatt.getGattServiceData();
            gattCharacteristicData = detailGatt.getGattCharacteristicData();

            String name = detailGatt.getLIST_NAME();
            String uuid = detailGatt.getLIST_UUID();

            if (name == null || uuid == null || bluetoothService == null || gattCharacteristics == null || notifyCharacteristic == null || gattServiceData == null || gattCharacteristicData == null) {
                Toast.makeText(activity, "fail init", Toast.LENGTH_LONG).show();
                if (name == null) {
                    Log.e("Detail", "name is null");
                }
                if (uuid == null) {
                    Log.e("Detail", "name is uuid");
                }
                if (bluetoothService == null) {
                    Log.e("Detail", "name is bluetoothService");
                }
                if (gattCharacteristics == null) {
                    Log.e("Detail", "name is gattCharacteristics");
                }
                if (notifyCharacteristic == null) {
                    Log.e("Detail", "name is notifyCharacteristic");
                }
                if (gattServiceData == null) {
                    Log.e("Detail", "name is gattServiceData");
                }
                if (gattCharacteristicData == null) {
                    Log.e("Detail", "name is gattCharacteristicData");
                }

            }

            expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
//                    if (gattCharacteristics != null) {
//                        final BluetoothGattCharacteristic bluetoothGattCharacteristic = gattCharacteristics.get(groupPosition).get(childPosition);
//                        final int charaProp = bluetoothGattCharacteristic.getProperties();
//
//                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
//                            if (notifyCharacteristic != null) {
//                                bluetoothService.setCharacteristicNotification(notifyCharacteristic, false);
//                                notifyCharacteristic = null;
//                            }
//                            bluetoothService.readCharacteristic(bluetoothGattCharacteristic);
//                        }
//                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
//                            notifyCharacteristic = bluetoothGattCharacteristic;
//                            bluetoothService.setCharacteristicNotification(bluetoothGattCharacteristic, true);
//                        }
//                        return true;
//                    }
//                    return false;
                    Log.e(TAG, " click ");
                    if (gattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                gattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (notifyCharacteristic != null) {
                                bluetoothService.setCharacteristicNotification(
                                        notifyCharacteristic, false);
                                notifyCharacteristic = null;
                            }
                            bluetoothService.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            notifyCharacteristic = characteristic;
                            bluetoothService.setCharacteristicNotification(
                                    characteristic, true);
                        }
                        return true;
                    }
                    return false;
                }
            });

            SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                    activity,
                    gattServiceData,
                    android.R.layout.simple_expandable_list_item_2,
                    new String[]{name, uuid},
                    new int[]{android.R.id.text1, android.R.id.text2},
                    gattCharacteristicData,
                    android.R.layout.simple_expandable_list_item_2,
                    new String[]{name, uuid},
                    new int[]{android.R.id.text1, android.R.id.text2}
            );


            expandableListView.setAdapter(gattServiceAdapter);
        }

        private void startConnect() {
            if (extraAddress.getText() != null) {


                //일단 null이 아니면 커넥션을 끊고
                if (detailGatt != null) {
                    detailGatt.destroy();
                    dataField.setText("No Data");
                    state.setText("disconnected");
                }

                // 커넥션
                try {
                    detailGatt = new DetailGatt(activity, context, devices[getLayoutPosition()], extraName.getText().toString(), extraAddress.getText().toString());
                    detailGatt.init();
                } catch (Exception e) {

                }

                runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (detailGatt != null) {
                            if (!detailGatt.isDataFind) {
                                stopConnect();
                                detailGatt.destroy();
                                startConnect();
                            }
                        } else {
                            startConnect();

                        }
                    }
                };
                if (!detailGatt.isDataFind) {
                    //정보를 받아 오지 못하면 핸들러로 5초마다 재 커넥션
                    new Handler().postDelayed(runnable, 10000);
                }
            }
        }

        //커넥션 OFF
        private void stopConnect() {
            if (detailGatt != null) {
                detailGatt.destroy();
            }
        }


    }
}
