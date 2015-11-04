package com.rainbow.kam.bt_scanner.Nursing.Fragment;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.Adapter.MainAdapter.DeviceAdapter;
import com.rainbow.kam.bt_scanner.Adapter.MainAdapter.DeviceItem;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.Tools.BLE.BleTools;

import java.util.ArrayList;

/**
 * Created by sion on 2015-11-04.
 */
public class SelectNursingDeviceFragment extends DialogFragment {

    private Activity activity;

    private View view;

    private final String TAG = "SelectDialog"; //로그용 태그
    private static final int REQUEST_ENABLE_BT = 1; //result 플래그

    public static Handler handler;  //핸들러 - Find 메세지 핸들링
    private boolean isScanning; //스캔중 여부
    private final long SCAN_PERIOD = 5000; //스캔시간

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;

    private RecyclerView selectDeviceRecyclerView;
    private RecyclerView.Adapter adapter;
    private ArrayList<DeviceItem> deviceItemArrayList = new ArrayList<DeviceItem>();

    private ProgressBar progressBar;
    private TextView hasCard;

    public SelectNursingDeviceFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_nursing_add_device, container, false);
        activity = getActivity();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Toast.makeText(activity, "success!", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        };

        progressBar = (ProgressBar) view.findViewById(R.id.nursing_device_progress);
        progressBar.setVisibility(View.INVISIBLE);

        hasCard = (TextView) view.findViewById(R.id.nursing_device_hasCard);
        hasCard.setVisibility(View.INVISIBLE);

        selectDeviceRecyclerView = (RecyclerView) view.findViewById(R.id.nursing_device_recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        selectDeviceRecyclerView.setLayoutManager(layoutManager);
        selectDeviceRecyclerView.setHasFixedSize(true);

        // remove dialog title
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        // remove dialog background
        getDialog().getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            //블루투스 매니저/어댑터 초기화
            bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null || bluetoothManager == null) {
                throw new Exception();
            }

            startScan();
        } catch (Exception e) {
            Toast.makeText(activity, "기기가 블루투스를 지원하지 않거나 블루투스 장치가 제거되어있습니다.", Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        scanLeDevice(false);
        adapter = null;
    }

    public View getMainView() {
        return view;
    }

    public void startScan() {
        if (enableBluetooth()) {
            if (getScanning()) {  //스캔 시작
//                    unregisterReceiver(broadcastReceiver);
//                    stopScan();
                scanLeDevice(false);
            } else { //재 스캔시(10초이내)
                deviceItemArrayList.clear();

                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }


//                    registerReceiver(broadcastReceiver, intentFilter);
//                    startScan();
                scanLeDevice(true);
            }
        } else {
            Snackbar.make(view, "You musst initalize Bluetooth", Snackbar.LENGTH_SHORT).show();
        }

    }

    public boolean getScanning() {//스캔중
        if (isScanning) {
            return true;
        } else {
            return false;
        }
    }

    public boolean enableBluetooth() {//블루투스 가동여부
        Log.d(TAG, "enableBluetooth");

        if (bluetoothAdapter.isEnabled()) { //블루투스 이미 켜짐
            Log.d(TAG, "Bluetooth isEnabled");

            return true;
        } else {    //블루투스 구동
            Log.d(TAG, "Bluetooth start");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
            return false;
        }
    }

    public void scanLeDevice(final boolean enable) {//저전력 스캔
        try {
            if (enable) {   //시작중이면
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        isScanning = false;
                        bluetoothAdapter.stopLeScan(leScanCallback);
                        progressBar.setVisibility(View.INVISIBLE);

                        if (deviceItemArrayList.size() < 1) {
                            hasCard.setVisibility(View.VISIBLE);
                        }
                        adapter = null;
                        adapter = new DeviceAdapter(deviceItemArrayList, activity, activity, view, deviceItemArrayList.size(), true);
                        selectDeviceRecyclerView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }
                }, SCAN_PERIOD); //10초 뒤에 OFF

                //시작
                isScanning = true;
                bluetoothAdapter.startLeScan(leScanCallback);
                progressBar.setVisibility(View.VISIBLE);
                hasCard.setVisibility(View.INVISIBLE);
            } else {    //중지
                isScanning = false;
                bluetoothAdapter.stopLeScan(leScanCallback);
                progressBar.setVisibility(View.INVISIBLE);
                hasCard.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
            Toast.makeText(activity, "기기가 블루투스를 지원하지 않거나 블루투스 장치가 제거되어있습니다.", Toast.LENGTH_LONG).show();

        }


    }

    public BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final BleTools.BleAdvertisedData bleAdvertisedData = BleTools.parseAdertisedData(scanRecord);
                    String deviceName = device.getName();
                    if (deviceName == null) {
                        deviceName = bleAdvertisedData.getName();
                        if (deviceName == null) {
                            deviceName = "N/A";
                        }
                    }
                    try {
                        if (deviceName.equals("Prime")) {
                            deviceItemArrayList.add(new DeviceItem(deviceName, device.getAddress(), device.getType(), device.getBondState(), rssi));
                        }
                    } catch (Exception e) {

                    }

//                    deviceItemArrayList.add(new DeviceItem(deviceName, device.getAddress(), device.getType(), device.getBondState(), rssi));
                    for (int i = 0; i < deviceItemArrayList.size(); i++) {
                        for (int j = 1; j < deviceItemArrayList.size(); j++) {
                            if (deviceItemArrayList.get(i).getExtraextraAddress().trim().toString().equals(deviceItemArrayList.get(j).getExtraextraAddress().trim().toString())) {
                                if (i == j) {

                                } else {
                                    deviceItemArrayList.remove(j);
                                }
                            }

                        }

                    }
                }
            });
        }
    };
}
