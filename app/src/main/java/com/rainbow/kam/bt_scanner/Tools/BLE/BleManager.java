package com.rainbow.kam.bt_scanner.Tools.BLE;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.Adapter.MainAdapter.DeviceAdapter;
import com.rainbow.kam.bt_scanner.Adapter.MainAdapter.DeviceItem;
import com.rainbow.kam.bt_scanner.Tools.BLE.BleTools;

import java.util.ArrayList;

/**
 * Created by kam6512 on 2015-11-13.
 */
public class BleManager {

    private String TAG; //로그용 태그

    private static final int REQUEST_ENABLE_BT = 1; //result 플래그

    private Activity activity;
    private Handler handler;  //핸들러 - Find 메세지 핸들링
    private boolean isScanning; //스캔중 여부
    private final long SCAN_PERIOD = 5000; //스캔시간

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;

    private RecyclerView selectDeviceRecyclerView;
    private RecyclerView.Adapter adapter;
    private ArrayList<DeviceItem> deviceItemArrayList;

    private View view;
    private ProgressBar progressBar;
    private TextView hasCard;

    private boolean isNursing = false;

    public BleManager(String TAG, Activity activity, Handler handler, BluetoothAdapter bluetoothAdapter, BluetoothManager bluetoothManager, RecyclerView selectDeviceRecyclerView, RecyclerView.Adapter adapter,
                      ArrayList<DeviceItem> deviceItemArrayList, View view, ProgressBar progressBar, TextView hasCard, boolean isNursing) {
        this.TAG = TAG;
        this.activity = activity;
        this.handler = handler;
        this.bluetoothAdapter = bluetoothAdapter;
        this.bluetoothManager = bluetoothManager;
        this.selectDeviceRecyclerView = selectDeviceRecyclerView;
        this.adapter = adapter;
        this.deviceItemArrayList = deviceItemArrayList;
        this.view = view;
        this.progressBar = progressBar;
        this.hasCard = hasCard;
        this.isNursing = isNursing;
    }

    public void onResume() {
        try {
            bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null || bluetoothManager == null) {

                throw new Exception();
            } else {
                startScan();
            }

        } catch (Exception e) {
            Toast.makeText(activity, "기기가 블루투스를 지원하지 않거나 블루투스 장치가 제거되어있습니다.(init Fail)", Toast.LENGTH_LONG).show();
            Log.e(TAG, e.getMessage());
            if (bluetoothAdapter == null) {
                Log.e(TAG, "BA null");
            }
            if (bluetoothManager == null) {
                Log.e(TAG, "BM null");
            }
        }
    }

    public void onDestroyView() {
        scanLeDevice(false);
        adapter = null;
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
                        adapter = new DeviceAdapter(deviceItemArrayList, activity, activity, view, deviceItemArrayList.size(), isNursing);
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
            Toast.makeText(activity, "기기가 블루투스를 지원하지 않거나 블루투스 장치가 제거되어있습니다.(LE Fail)", Toast.LENGTH_LONG).show();
            Log.e(TAG, e.getStackTrace().toString());
        }


    }

    public void startScan() {
        if (enableBluetooth()) {
            if (getScanning()) {  //스캔 시작
                scanLeDevice(false);
            } else { //재 스캔시(10초이내)
                deviceItemArrayList.clear();

                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

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
            activity.startActivityForResult(intent, REQUEST_ENABLE_BT);
            return false;
        }
    }

    public BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final BleTools.BleAdvertisedData bleAdvertisedData = BleTools.parseAdertisedData(scanRecord);

                        String deviceName = device.getName();
                        if (deviceName == null) {
                            deviceName = bleAdvertisedData.getName();
                            if (deviceName == null) {
                                deviceName = "N/A";
                            }
                        }

                        if (deviceName.equals("Prime")) {
                            deviceItemArrayList.add(new DeviceItem(deviceName, device.getAddress(), device.getType(), device.getBondState(), rssi));
                        }

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
                    } catch (Exception e) {
                        Log.e(TAG, "leScanCallback is Exception" + e.getMessage());
                    }
                }
            });
        }
    };
}
