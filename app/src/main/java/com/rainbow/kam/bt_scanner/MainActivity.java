package com.rainbow.kam.bt_scanner;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.bluetooth.BluetoothService;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int REQUEST_ENABLE_BT = 2;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    public static Handler handler;
    private boolean isScanning;
    private static final long SCAN_PERIOD = 10000;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private ArrayList<DeviceItem> deviceItemArrayList = new ArrayList<DeviceItem>();
    private Sort sort = new Sort();

    private FloatingActionButton fabOn, fabSync, fabSortRssi, fabSortType, fabSortName;

//    private BluetoothService bluetoothService = null;
//    private IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

//클래식의 브로드캐스트
//    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                Log.d(TAG, "ACTION_FOUND");
//                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
//                deviceItemArrayList.add(new DeviceItem(bluetoothDevice.getName(), bluetoothDevice.getAddress(), bluetoothDevice.getBondState(), bluetoothDevice.getType(), (int) rssi));
//                adapter.notifyDataSetChanged();
//            }
//        }
//    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        handler = new Handler();


        fabOn = (FloatingActionButton) findViewById(R.id.fab_on);
        fabOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Enable BlueTooth?", Snackbar.LENGTH_LONG)
                        .setAction("On", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (getBT()) {
                                    enableBluetooth();
                                } else {
                                    if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                                        Toast.makeText(MainActivity.this, "ble_not_supported", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }
                            }
                        }).show();
            }
        });

        fabSync = (FloatingActionButton) findViewById(R.id.fab_sync);
        fabSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getScanning()) {
//                    unregisterReceiver(broadcastReceiver);
//                    stopScan();
                    scanLeDevice(false);
                } else {
                    deviceItemArrayList.clear();
                    adapter.notifyDataSetChanged();

//                    registerReceiver(broadcastReceiver, intentFilter);
//                    startScan();
                    scanLeDevice(true);
                }
            }
        });

        fabSortRssi = (FloatingActionButton) findViewById(R.id.fab_sort_rssi);
        fabSortRssi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Collections.sort(deviceItemArrayList, sort.COMPARATOR_RSSI);
                adapter.notifyDataSetChanged();

            }
        });

        fabSortType = (FloatingActionButton) findViewById(R.id.fab_sort_type);
        fabSortType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Collections.sort(deviceItemArrayList, sort.COMPARATOR_TYPE);
                adapter.notifyDataSetChanged();


            }
        });

        fabSortName = (FloatingActionButton) findViewById(R.id.fab_sort_name);
        fabSortName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Collections.sort(deviceItemArrayList, sort.COMPARATOR_NAME);
                adapter.notifyDataSetChanged();

            }
        });


        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new DeviceAdapter(deviceItemArrayList, this, getWindow().getDecorView());
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {

                } else {
                    Log.d(TAG, "Bluetooth is not enabled");
                }
                break;
        }
    }


    public boolean getBT() { //블루투스 사용여부
        Log.d(TAG, "getBT");
        if (bluetoothAdapter == null) {
            Log.d(TAG, "BluetoothService is not available");
            return false;
        } else {
            Log.d(TAG, "BluetoothService is available");
            return true;
        }
    }

    public void enableBluetooth() {//블루투스 가동여부
        Log.d(TAG, "enableBluetooth");

        if (bluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth isEnabled");
        } else {
            Log.d(TAG, "Bluetooth start");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, MainActivity.REQUEST_ENABLE_BT);
        }
    }

    public boolean getScanning() {//스캔중
        if (isScanning) {
            return true;
        } else {
            return false;
        }
    }

    public void scanLeDevice(final boolean enable) {//저전력 스캔
        if (enable) {   //시작
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isScanning = false;
                    bluetoothAdapter.stopLeScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            isScanning = true;
            bluetoothAdapter.startLeScan(leScanCallback);
        } else {    //중지
            isScanning = false;
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    public BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() { //디바이스가 스캔될 때 마다 콜백
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "leScanCallback");
                    deviceItemArrayList.add(new DeviceItem(device.getName(), device.getAddress(), device.getType(), device.getBondState(), rssi));
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "leScanCallback" + device.getName() + device.getAddress() + device.getType() + device.getBondState() + rssi);
                }
            });

        }
    };

//    public void startScan() {//클래식 스캔 시작
//        Log.d(TAG, "startScan");
//        bluetoothAdapter.startDiscovery();
//    }
//
//    public void stopScan() {//클래식 스캔 중지
//        Log.d(TAG, "stopScan");
//        bluetoothAdapter.cancelDiscovery();
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(broadcastReceiver);
//        stopScan();
        scanLeDevice(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
