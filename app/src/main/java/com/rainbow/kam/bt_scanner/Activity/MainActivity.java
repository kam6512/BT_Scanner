package com.rainbow.kam.bt_scanner.Activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.Adapter.DeviceAdapter;
import com.rainbow.kam.bt_scanner.Adapter.DeviceItem;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.Tools.BleTools;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity"; //로그용 태그
    private static final int REQUEST_ENABLE_BT = 1; //result 플래그

    private BluetoothManager bluetoothManager;  //블루투스 매니저
    private BluetoothAdapter bluetoothAdapter;  //블루투스 어댑터

    public static Handler handler;  //핸들러 - Find 메세지 핸들링
    private boolean isScanning; //스캔중 여부
    private final long SCAN_PERIOD = 10000; //스캔시간

    CoordinatorLayout coordinatorLayout;

    private RecyclerView recyclerView; //리사이클러 뷰
    private RecyclerView.Adapter adapter; //리사이클러 어댑터
    private ArrayList<DeviceItem> deviceItemArrayList = new ArrayList<DeviceItem>(); //어댑터 데이터 클래스(틀)


    private FloatingActionButton fabOn, fabSync; //버튼
    private ProgressBar progressBar;
    private TextView hasCard;

/*  클래식(4.0미만)전용
    private BluetoothService bluetoothService = null;
    private IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

클래식의 브로드캐스트
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d(TAG, "ACTION_FOUND");
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                deviceItemArrayList.add(new DeviceItem(bluetoothDevice.getName(), bluetoothDevice.getAddress(), bluetoothDevice.getBondState(), bluetoothDevice.getType(), (int) rssi));
                adapter.notifyDataSetChanged();
            }
        }
    };
*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //툴바 적용
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initBluetooth();

        //핸들러 초기화
        handler = new Handler();

        //버튼 초기화 + 리스너 적용
        fabOn = (FloatingActionButton) findViewById(R.id.fab_on);
        fabOn.setOnClickListener(this);

        fabSync = (FloatingActionButton) findViewById(R.id.fab_sync);
        fabSync.setOnClickListener(this);


        progressBar = (ProgressBar) findViewById(R.id.main_progress);
        progressBar.setVisibility(View.INVISIBLE);

        hasCard = (TextView) findViewById(R.id.hasCard);
        hasCard.setVisibility(View.INVISIBLE);

        //리사이클러 그룹 초기화
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinatorLayout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    //블루투스 켜짐
                } else {
                    Log.d(TAG, "Bluetooth is not enabled");
                    //블루투스 에러
                }
                break;
        }
    }

    private void initBluetooth() {
        try {
            //블루투스 매니저/어댑터 초기화
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null || bluetoothManager == null) {
                throw new Exception();

            }
        } catch (Exception e) {
            Toast.makeText(this, "기기가 블루투스를 지원하지 않거나 블루투스 장치가 제거되어있습니다.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public boolean getBT() { //블루투스 사용여부
        Log.d(TAG, "getBT");
        if (bluetoothAdapter == null) { //블루투스 불가능
            Log.d(TAG, "BluetoothService is not available");
            return false;
        } else {    //블루투스 가능
            Log.d(TAG, "BluetoothService is available");
            return true;
        }
    }

    public boolean enableBluetooth() {//블루투스 가동여부
        Log.d(TAG, "enableBluetooth");

        if (bluetoothAdapter.isEnabled()) { //블루투스 이미 켜짐
            Log.d(TAG, "Bluetooth isEnabled");
            fabSync.callOnClick();
            return true;
        } else {    //블루투스 구동
            Log.d(TAG, "Bluetooth start");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, MainActivity.REQUEST_ENABLE_BT);
            return false;
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
                        adapter = new DeviceAdapter(deviceItemArrayList, MainActivity.this, MainActivity.this, getWindow().getDecorView(), deviceItemArrayList.size());
                        recyclerView.setAdapter(adapter);
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
            Toast.makeText(this, "기기가 블루투스를 지원하지 않거나 블루투스 장치가 제거되어있습니다.", Toast.LENGTH_LONG).show();
            finish();
        }


    }

    //디바이스가 스캔될 때 마다 콜백
    public BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            runOnUiThread(new Runnable() { //UI쓰레드에
                @Override
                public void run() { //로그넣고 어댑터에 추가
                    final BleTools.BleAdvertisedData bleAdvertisedData = BleTools.parseAdertisedData(scanRecord);
                    String deviceName = device.getName();
                    if (deviceName == null) {
                        deviceName = bleAdvertisedData.getName();
                    }

                    Log.e(TAG, device.getName() + " / " + device.getAddress() + " / " + device.getType() + " / " + device.getBondState() + " / " + device.getBluetoothClass() + " / ");

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
//                    deviceItemArrayList.add(new DeviceItem(deviceName, device.getAddress(), device.getType(), device.getBondState(), rssi));
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
    protected void onResume() {
        super.onResume();
//        fabSync.callOnClick();

    }

    @Override
    protected void onDestroy() { //꺼짐
        super.onDestroy();
        //클래식 스캔 중지
//        unregisterReceiver(broadcastReceiver);
//        stopScan();
        scanLeDevice(false);
        adapter = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) { //버튼 클릭 리스너
        switch (v.getId()) {
            case R.id.fab_on: //블루투스 켜기
                Snackbar.make(v.getRootView(), "Enable BlueTooth?", Snackbar.LENGTH_LONG)
                        .setAction("On", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (getBT()) {  //블루투스 사용 가능시
                                    enableBluetooth();
                                } else {    //블루투스 사용 불가능시
                                    if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                                        Toast.makeText(MainActivity.this, "ble_not_supported", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                }
                            }
                        }).show();

                break;
            case R.id.fab_sync: //블루투스 기기 찾기(4.0)
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
                break;
        }
    }
}
