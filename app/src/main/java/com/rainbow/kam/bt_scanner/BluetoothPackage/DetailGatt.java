package com.rainbow.kam.bt_scanner.BluetoothPackage;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

import com.rainbow.kam.bt_scanner.Adapter.DeviceAdapter;
import com.rainbow.kam.bt_scanner.Tools.GattAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DetailGatt {

    //태그
    private static final String TAG = "DetailGatt";

    //고정 네임
    private static final String DEVICE_NAME = "DEVICE_NAME";
    private static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private Activity activity;
    private Context context;
    private DeviceAdapter.Device device;

    //뷰에 적용할 임시 테스트 String
    private String deviceState;
    private String deviceAddress;
    private String devicedataField;

    //블루투스 서비스
    private BluetoothService bluetoothService;
    //연결 여부
    private boolean connected = false;
    public boolean isBluetoothServiceClosed = true;
    public boolean isDataFind = false;
//    public boolean flag = false;

    //블루투스 서비스의 BluetoothGattCharacteristic을 담을 리스트
    //ArrayList<서비스리스트 ArrayList<Characteristic>>구조이다.
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> rootGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private BluetoothGattCharacteristic notifyCharacteristic;
    private ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();  //서비스 리스트
    private ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>(); //Characteristic 리스트

    //블루투스 서비스목록의 태그
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private Intent gattServiceIntent;

    int index = 0;


    public DetailGatt(Activity activity, Context context, DeviceAdapter.Device device, String deviceState, String deviceAddress) {
        //액티비티 / 컨택스트
        this.activity = activity;
        this.context = context;

        this.device = device;
        //뷰 - 메모리 참조 주소가 동일해서 new시킨 Device객체에 적용하는 방향으로 넘어갔다
        //브로드캐스트의 관리 필요 - bind의 특성 확인.
        this.device.state.setText(device.state.getText().toString());
        this.device.address.setText(device.address.getText().toString());
        this.device.dataField.setText(device.dataField.getText().toString());

        //값
        this.deviceState = deviceState;
        this.deviceAddress = deviceAddress;
        this.device.address.setText(this.deviceAddress);

//        if(this.device.extraName.getText().toString() == ""){
//            this.device.extraName.setText("BLE 네임정보를 가져오는 중....");
//        }

        this.device.dataField.setText("BLE 세부정보를 가져오는 중....");
    }

    //서비스와 상호작용하기위한 서비스 커넥션
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) { //연결O
            bluetoothService = ((BluetoothService.LocalBinder) service).getService(); //서비스 초기화
            Log.i(TAG, "initialize Bluetooth");
            if (!bluetoothService.initialize()) { //초기화 오류시
                Log.e(TAG, "Unable to initialize Bluetooth");
                activity.finish(); //종료
            }
            bluetoothService.connect(deviceAddress); //블루투스 연결!
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
                isDataFind = false;
                updateConnectionState("disconnected");
                clearUI();
            } else if (BluetoothService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) { //Find!
                if (bluetoothService != null) {
                    setGattServices(bluetoothService.getSupportedGattServices());
                }
            } else if (BluetoothService.ACTION_DATA_AVAILABLE.equals(action)) { //Data가 유효할때
                Log.e("ACTION_DATA_AVAILABLE", index + " //===================================================================");
                Log.e("ACTION_DATA_AVAILABLE", intent.getStringExtra(BluetoothService.EXTRA_DATA));
                displayData(intent.getStringExtra(BluetoothService.EXTRA_DATA));
//                isDataFind = true;
//                flag = true;
                synchronized (showBlueToothStat) {
                    Log.e("showBlueToothStat", "notify");
                    showBlueToothStat.notifyAll();
                }

            }
        }
    };

    private SimpleExpandableListAdapter gattServiceAdapter;

    private final ExpandableListView.OnChildClickListener onChildClickListener = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            if (rootGattCharacteristics != null) {
                final BluetoothGattCharacteristic characteristic =
                        rootGattCharacteristics.get(groupPosition).get(childPosition);
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
    };


    //끊기거나 데이터가 없을시
    private void clearUI() {
//        address.setText("no Add");
        device.dataField.setText("데이터 누락됨, 재시도 중...");
        destroy(); //끄기
    }

    //데이터 상태 표현 - 데이터 정보를 적용하지 않기 때문에 destroy 시키면 안된다...
    private void updateConnectionState(final String resourceId) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                device.state.setText(resourceId);
            }
        });
    }

    //데이터 내용 적용
    private void displayData(String data) {

        if (data != null) {
            if (connected) {

//                device.stopHandling();
                Log.e("data", data + "/");
                device.dataField.setText(data); //추가!
                device.extraName.setText(data.substring(0, data.indexOf("\n")));
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


    public void init() {

        try {
            //서비스 바인드(연결)
            gattServiceIntent = new Intent(activity, BluetoothService.class);
            activity.bindService(gattServiceIntent, serviceConnection, activity.BIND_AUTO_CREATE);

            isBluetoothServiceClosed = false;

            //브로드캐스트 리시버 적용
            activity.registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
            if (bluetoothService != null) {
                //연결
                final boolean result = bluetoothService.connect(deviceAddress);
                Log.d(TAG, "Connect request result=" + result);
            }
        } catch (Exception e) {

        }

    }

    //뷰가 Detached 시 호출
    public void destroy() {
        if (bluetoothService != null) {
            //커넥션 OFF

            bluetoothService.close();
            bluetoothService.disconnect();
            bluetoothService = null;
            isBluetoothServiceClosed = true;
            try {
                //BR 해제
                activity.unregisterReceiver(gattUpdateReceiver);

                //서비스 언바인드로 연결 끊기
                activity.unbindService(serviceConnection);
            } catch (Exception e) {

            }

        }
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
            currentServiceData.put(LIST_NAME, GattAttributes.lookup(uuid, unknownServiceString));
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
                        LIST_NAME, GattAttributes.lookup(uuid, unknownCharaString));
                //틀에 UUID를 넣는다
                currentCharaData.put(LIST_UUID, uuid);
                //틀을 그룹 리스트에 넣는다.
                gattCharacteristicGroupData.add(currentCharaData);


            }
            rootGattCharacteristics.add(charas); //루트리스트에 Characteristic리스트 넣기
            gattCharacteristicData.add(gattCharacteristicGroupData); //서비스리스트에 그룹리스트를 넣는다.

        }

        gattServiceAdapter = new SimpleExpandableListAdapter(activity,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2},
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2}
        );

//        device.expandableListView.setAdapter(gattServiceAdapter);

//        showBlueToothStat(gattCharacteristicData); //서비스리스트를 인자로 넘겨 호출
        showBlueToothStat.start();
    }

    Thread showBlueToothStat = new Thread() {

        @Override
        public void run() {
            super.run();

            if (rootGattCharacteristics != null) { //Characteristic리스트가 들어간 루트리스트가 살아있으면
                Log.e("showBlueToothStat", "start");
                for (int i = 0; i < gattCharacteristicData.size(); i++) {
                    ++index;
                    Log.e("showBlueToothStat", i + " = data.index");
                    for (int j = 0; j < gattCharacteristicData.get(i).size(); j++) {
                        Log.e("showBlueToothStat", j + "is " + " data[" + i + "].index");

//                        if (i == gattCharacteristicData.size()) {
//                            showBlueToothStat.interrupt();
//                                return;
//                        }

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
//                        Log.e("showBlueToothStat", String.valueOf(characteristic.getValue()));
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            notifyCharacteristic = characteristic;
                            bluetoothService.setCharacteristicNotification(
                                    characteristic, true);
//                        Log.e("showBlueToothStat", "set");
                        }
                        synchronized (showBlueToothStat) {
                            try {

                                Log.e("showBlueToothStat", "wait");
                                showBlueToothStat.wait();

                            } catch (Exception e) {

                            }
                        }
                    }
                    Log.e("showBlueToothStat", "end");
                    device.stopHandling();
                    DetailGatt.this.destroy();
                    isDataFind = true;
                    return;
                }

            }
        }
    };


    public SimpleExpandableListAdapter getGattServiceAdapter() {
        return gattServiceAdapter;
    }

    public ExpandableListView.OnChildClickListener getOnChildClickListener() {
        return onChildClickListener;
    }


}
