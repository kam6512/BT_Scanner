package com.rainbow.kam.bt_scanner.Deprecated.Activity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

import com.rainbow.kam.bt_scanner.Deprecated.Adapter.DetailAdapter.DetailExpandableAdapter;
import com.rainbow.kam.bt_scanner.Deprecated.BluetoothPackage.BluetoothService;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.Tools.BLE.BLEGattAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kam6512 on 2015-10-29.
 */
public class TempActivity extends AppCompatActivity {

    //태그
    private static final String TAG = "DetailActivity";

    //고정 네임
    private static final String DEVICE_NAME = "DEVICE_NAME";
    private static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private String address;

    //블루투스 서비스
    private BluetoothService bluetoothService;
    //연결 여부
    private boolean connected = false;

    //블루투스 서비스의 BluetoothGattCharacteristic을 담을 리스트
    //ArrayList<서비스리스트 ArrayList<Characteristic>>구조이다.
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> rootGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private BluetoothGattCharacteristic notifyCharacteristic;
    private ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();  //서비스 리스트
    private ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>(); //Characteristic 리스트
    private String[][] gattData;

    private int i = 0, j = 0;

    //블루투스 서비스목록의 태그
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private Intent gattServiceIntent;

    private ExpandableListView expandableListView;
    private SimpleExpandableListAdapter simpleExpandableListAdapter;
    private DetailExpandableAdapter detailExpandableAdapter;
    private ExpandableListView.OnChildClickListener onChildClickListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        address = getIntent().getStringExtra("Address");

        gattServiceIntent = new Intent(TempActivity.this, BluetoothService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);

        //브로드캐스트 리시버 적용
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bluetoothService != null) {
            //연결
            final boolean result = bluetoothService.connect(address);
            Log.d(TAG, "Connect request result=" + result);
        }

//        expandableListView = (ExpandableListView) findViewById(R.id.gatt_services_list);
//        ViewCompat.setNestedScrollingEnabled(expandableListView, true);
        onChildClickListener = new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (rootGattCharacteristics != null) {
                    i = groupPosition;
                    j = childPosition;
                    final BluetoothGattCharacteristic characteristic =
                            rootGattCharacteristics.get(groupPosition).get(childPosition);
                    final int charaProp = characteristic.getProperties();
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
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
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothService != null) {
            //커넥션 OFF

            bluetoothService.close();
            bluetoothService.disconnect();
            bluetoothService = null;
            try {
                //BR 해제
                unregisterReceiver(gattUpdateReceiver);

                //서비스 언바인드로 연결 끊기
                unbindService(serviceConnection);
            } catch (Exception e) {

            }

        }
    }

    //서비스와 상호작용하기위한 서비스 커넥션
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) { //연결O
            bluetoothService = ((BluetoothService.LocalBinder) service).getService(); //서비스 초기화
            Log.i(TAG, "initialize Bluetooth");
            if (!bluetoothService.initialize()) { //초기화 오류시
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish(); //종료
            }
            bluetoothService.connect(address); //블루투스 연결!
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bluetoothService = null;
        } //연결X
    };

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) { //브로드캐스트
            final String action = intent.getAction(); //액션 GET
            if (BluetoothService.ACTION_GATT_CONNECTED.equals(action)) { //연결됬을 시
                connected = true;

                updateConnectionState("connected");

            } else if (BluetoothService.ACTION_GATT_DISCONNECTED.equals(action)) { //끊겼을 시
                connected = false;
                updateConnectionState("disconnected");
                clearUI();
            } else if (BluetoothService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) { //Find!
                if (bluetoothService != null) {
                    setGattServices(bluetoothService.getSupportedGattServices());
                }
            } else if (BluetoothService.ACTION_DATA_AVAILABLE.equals(action)) { //Data가 유효할때
//                Log.e("ACTION_DATA_AVAILABLE", intent.getStringExtra(BluetoothService.EXTRA_DATA));
                displayData(intent.getStringExtra(BluetoothService.EXTRA_DATA));


                if (showBlueToothStat.isAlive()) {
                    synchronized (showBlueToothStat) {
                        Log.e("showBlueToothStat", "notify");
                        showBlueToothStat.notifyAll();
                    }
                }
            }
        }
    };

    //끊기거나 데이터가 없을시
    private void clearUI() {
//        expandableListView.setAdapter((SimpleExpandableListAdapter) null);
    }

    //데이터 상태 표현 - 데이터 정보를 적용하지 않기 때문에 destroy 시키면 안된다...
    private void updateConnectionState(final String resourceId) {

        Snackbar.make(getWindow().getDecorView(), resourceId, Snackbar.LENGTH_SHORT).show();

    }

    //데이터 내용 적용
    private void displayData(String data) {

        if (data != null) {
            if (connected) {
                String hex = data.substring(data.indexOf("\n"), data.length());

                try {
                    gattData[i][j] = data;

                } catch (Exception e) {

                }

//                device.stopHandling();
                Log.e("data", data + " / " + hex);
//                destroy(); //끄기

            }
        }
    }

    //브로드캐스트 리시버를 위해 인텐트 필터 액션 추가
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED); //연결 시
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED); //끊길 시
        intentFilter.addAction(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED);//Find!
        intentFilter.addAction(BluetoothService.ACTION_DATA_AVAILABLE); //Data OK
        return intentFilter;
    }

    //Gatt를 찾은 브로드캐스트를 수신시 서비스를 GET하고 내부 Characteristic등을 분석
    private void setGattServices(List<BluetoothGattService> gattServices) {
        String uuid = null;
        String unknownServiceString = "unknown_service";
        String unknownCharaString = "unknown_characteristic";

//        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();  //서비스 리스트
        gattServiceData = new ArrayList<HashMap<String, String>>();  //서비스 리스트

//        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>(); //Characteristic 리스트
        gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>(); //Characteristic 리스트

        rootGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>(); //ArrayList<서비스리스트 ArrayList<Characteristic>> 이하 root로 표기

        //서비스 수집
        for (BluetoothGattService bluetoothGattService : gattServices) {// 인자로 받은 BluetoothGattService 리스트중 BluetoothGattService를 하나씩 뽑아

            //틀을 만들고
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            //UUID를 블루투스 서비스에서 가져온다
            uuid = bluetoothGattService.getUuid().toString();

            //틀에 네임태그와 네임을 넣는다
            currentServiceData.put(LIST_NAME, BLEGattAttributes.lookup(uuid, unknownServiceString));
            //틀에 UUID를 넣는다
            currentServiceData.put(LIST_UUID, uuid);
            //서비스 리스트에 네임속성이 담긴 틀을 추가한다.
            gattServiceData.add(currentServiceData);

            //Characteristic그룹을 넣을 리스트르 만들고
            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            //Characteristic가 들어갈 리스트도 만들어 서비스에서 Characteristic들을 가져온다
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    bluetoothGattService.getCharacteristics();
            //Characteristic가 들어갈 다른 빈 리스트
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();


            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {//서비스안의 BluetoothGattCharacteristic을 수집한다

                //리스트에 Characteristic을 추가
                charas.add(gattCharacteristic);

                //Characteristic의 속성들이 들어갈 틀을 만든다
                HashMap<String, String> currentCharaData = new HashMap<String, String>();

                //UUID를 서비스가 아닌 Characteristic의 UUID를 넣는다.
                uuid = gattCharacteristic.getUuid().toString();

                //틀에 네임태그와 네임을 넣는다
                currentCharaData.put(
                        LIST_NAME, BLEGattAttributes.lookup(uuid, unknownCharaString));
                //틀에 UUID를 넣는다
                currentCharaData.put(LIST_UUID, uuid);
                //틀을 그룹 리스트에 넣는다.
                gattCharacteristicGroupData.add(currentCharaData);


            }
            rootGattCharacteristics.add(charas); //루트리스트에 Characteristic리스트 넣기
            gattCharacteristicData.add(gattCharacteristicGroupData); //서비스리스트에 그룹리스트를 넣는다.

        }

        gattData = new String[10][10];
//        simpleExpandableListAdapter = new SimpleExpandableListAdapter(this,
//                gattServiceData,
//                android.R.layout.simple_expandable_list_item_2,
//                new String[]{LIST_NAME, LIST_UUID},
//                new int[]{android.R.id.text1, android.R.id.text2},
//                gattCharacteristicData,
//                android.R.layout.simple_expandable_list_item_2,
//                new String[]{LIST_NAME, LIST_UUID},
//                new int[]{android.R.id.text1, android.R.id.text2}
//        );
//

        showBlueToothStat.start();
    }


    Thread showBlueToothStat = new Thread() {

        @Override
        public void run() {
            super.run();

            if (rootGattCharacteristics != null) { //Characteristic리스트가 들어간 루트리스트가 살아있으면
                Log.e("showBlueToothStat", "start");
                for (i = 0; i < gattCharacteristicData.size(); i++) {
                    Log.e("showBlueToothStat", i + " = data.index");
                    for (j = 0; j < gattCharacteristicData.get(i).size(); j++) {
                        Log.e("showBlueToothStat", j + "is " + " data[" + i + "].index");

                        final BluetoothGattCharacteristic characteristic =
                                rootGattCharacteristics.get(i).get(j); //배열에서 가져오기
                        final int charaProp = characteristic.getProperties();

                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
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
                        synchronized (showBlueToothStat) {
                            try {

                                if (i == 3 && j == 0) {
                                    Log.e("showBlueToothStat", "end");

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            detailExpandableAdapter = new DetailExpandableAdapter(TempActivity.this,
                                                    gattServiceData,
                                                    R.layout.detail_bluetooth_service_item,
                                                    new String[]{LIST_NAME, LIST_UUID},
                                                    new int[]{R.id.detail_parent_list_item_service_title, R.id.detail_parent_list_item_service_UUID},
                                                    gattCharacteristicData,
                                                    R.layout.detail_bluetooth_characteristics_item,
                                                    new String[]{LIST_NAME, LIST_UUID},
                                                    new int[]{R.id.detail_child_list_item_characteristics_title, R.id.detail_child_list_item_characteristics_UUID}, gattData, expandableListView);


                                            detailExpandableAdapter.notifyDataSetChanged();
                                            expandableListView.setAdapter(detailExpandableAdapter);
                                            expandableListView.setOnChildClickListener(onChildClickListener);
//                                            expandableListView.expandGroup(0);
//                                            expandableListView.expandGroup(1);
//                                            expandableListView.expandGroup(2);
//                                            expandableListView.expandGroup(3);
//                                            expandableListView.expandGroup(4);
                                            Log.e("showBlueToothStat", detailExpandableAdapter.isChildSelectable(0, 0) + " / " +
                                                    detailExpandableAdapter.areAllItemsEnabled());

                                            showBlueToothStat.interrupt();
                                        }
                                    });

                                } else {

                                    Log.e("showBlueToothStat", "wait" + i + " < " + gattCharacteristicData.size() + " / " + j + " < " + gattCharacteristicData.get(i).size());
                                    showBlueToothStat.wait();
                                }


                            } catch (Exception e) {

                            }
                        }
                    }
                }


//                detailExpandableAdapter = new DetailExpandableAdapter(DetailActivity.this,
//                        gattServiceData,
//                        R.layout.detail_bluetooth_service_item,
//                        new String[]{LIST_NAME, LIST_UUID},
//                        new int[]{R.id.detail_parent_list_item_service_title, R.id.detail_parent_list_item_service_UUID},
//                        gattCharacteristicData,
//                        R.layout.detail_bluetooth_characteristics_item,
//                        new String[]{LIST_NAME, LIST_UUID},
//                        new int[]{R.id.detail_child_list_item_characteristics_title, R.id.detail_child_list_item_characteristics_UUID}, gattData);
//
//
//                detailExpandableAdapter.notifyDataSetChanged();
//                expandableListView.setAdapter(detailExpandableAdapter);
//                expandableListView.setOnChildClickListener(onChildClickListener);
//                expandableListView.expandGroup(0);
//                expandableListView.expandGroup(1);
//                expandableListView.expandGroup(2);
//                expandableListView.expandGroup(3);
//                expandableListView.expandGroup(4);
//
//                showBlueToothStat.stop();
//                showBlueToothStat.interrupt();
            }
        }
    };
}
