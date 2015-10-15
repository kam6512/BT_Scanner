package com.rainbow.kam.bt_scanner.Adapter;

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
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rainbow.kam.bt_scanner.Activity.DetailActivity;
import com.rainbow.kam.bt_scanner.Bluetooth_Package.BluetoothService;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.Tools.GattAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sion on 2015-10-14.
 */
public class DeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //태그
    private static final String TAG = "DetailActivity";

    //고정 네임
    public static final String DEVICE_NAME = "DEVICE_NAME";
    public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private ArrayList<DeviceItem> deviceItemArrayList; //어댑터에 적용시킬 틀
    private Context context; //컨택스트
    private View view; //SnackBar대비 뷰

    public DeviceAdapter(ArrayList<DeviceItem> deviceItemArrayList, Context context, View view) { //초기화
        this.deviceItemArrayList = deviceItemArrayList;
        this.context = context;
        this.view = view;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Device(LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_device_item, parent, false));
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        Device device = (Device) holder;
        device.onDestroy();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        //뷰홀더 적용
        Device device = (Device) holder;

        //각각의 뷰 속성 적용
        device.extraName.setText(deviceItemArrayList.get(position).getExtraName());
        device.extraAddress.setText(deviceItemArrayList.get(position).getExtraextraAddress());
        device.extraBondState.setText(String.valueOf(deviceItemArrayList.get(position).getExtraBondState()));
        device.extraType.setText(String.valueOf(deviceItemArrayList.get(position).getExtraType()));
        device.extraRssi.setText(String.valueOf(deviceItemArrayList.get(position).getExtraRssi()));
    }


    @Override
    public int getItemCount() {
        return deviceItemArrayList.size();
    }

    private class Device extends RecyclerView.ViewHolder { //뷰 초기화

        private CardView cardView;

        private TextView extraName;
        private TextView extraAddress;
        private TextView extraBondState;
        private TextView extraType;
        private TextView extraRssi;

        //태그
        private static final String TAG = "DetailActivity";

        //고정 네임
        public static final String DEVICE_NAME = "DEVICE_NAME";
        public static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";

        //뷰
        private TextView state;
        private TextView dataField;

        //뷰에 적용할 String
        private String deviceName;
        private String deviceAddress;

        //블루투스 서비스
        private BluetoothService bluetoothService;
        //연결 여부
        private boolean connected = false;

        //블루투스 서비스의 BluetoothGattCharacteristic을 담을 리스트
        //ArrayList<서비스리스트 ArrayList<Characteristic>>구조이다.
        private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
                new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
        private BluetoothGattCharacteristic notifyCharacteristic;

        //블루투스 서비스목록의 태그
        private final String LIST_NAME = "NAME";
        private final String LIST_UUID = "UUID";


        public Device(View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.card);

            extraName = (TextView) itemView.findViewById(R.id.item_name);
            extraAddress = (TextView) itemView.findViewById(R.id.item_address);
            extraBondState = (TextView) itemView.findViewById(R.id.item_bond);
            extraType = (TextView) itemView.findViewById(R.id.item_type);
            extraRssi = (TextView) itemView.findViewById(R.id.item_rssi);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { //카드 뷰 클릭시 세부 액티비티로
//                    final Intent intent = new Intent(context, DetailActivity.class);
//                    intent.putExtra(DetailActivity.DEVICE_NAME, extraName.getText().toString()); //이름
//                    intent.putExtra(DetailActivity.DEVICE_ADDRESS, extraAddress.getText().toString()); //주소
//                    context.startActivity(intent);
                }
            });
            // 인텐트에서 디바이스의 네임과 주소 GET
            try {
                new Thread().sleep(2000);
            }catch (Exception e){

            }

            deviceName = extraName.getText().toString();
            deviceAddress = extraAddress.getText().toString();

            // UI초기화
            ((TextView) itemView.findViewById(R.id.detail_address)).setText(deviceAddress);
            state = (TextView) itemView.findViewById(R.id.detail_state);
            dataField = (TextView) itemView.findViewById(R.id.detail_datafield);

            //서비스 바인드(연결)
            Intent gattServiceIntent = new Intent(context, BluetoothService.class);
            context.bindService(gattServiceIntent, serviceConnection, context.BIND_AUTO_CREATE);

            //버튼 초기화
            FloatingActionButton connect, disconnect;
            connect = (FloatingActionButton) itemView.findViewById(R.id.fab_connnect);
            disconnect = (FloatingActionButton) itemView.findViewById(R.id.fab_disconnnect);

            //연결버튼 리스너
            connect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothService.connect(deviceAddress);
                }
            });
            //연결취소 버튼 리스너
            disconnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bluetoothService.disconnect();
                }
            });

            //브로드캐스트 리시버 적용
            context.registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
            if (bluetoothService != null) {
                //연결
                final boolean result = bluetoothService.connect(deviceAddress);
                Log.d(TAG, "Connect request result=" + result);
            }

        }


        protected void onDestroy() {
            //브로드캐스트 리시버 취소
            context.unregisterReceiver(gattUpdateReceiver);
            //서비스 끊기
            context.unbindService(serviceConnection);
            bluetoothService = null;
        }

        //서비스와 상호작용하기위한 서비스 커넥션
        private final ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) { //연결O
                bluetoothService = ((BluetoothService.LocalBinder) service).getService(); //서비스 초기화
                Log.e(TAG, "initialize Bluetooth");
                if (!bluetoothService.initialize()) { //초기화 오류시
                    Log.e(TAG, "Unable to initialize Bluetooth");
//                    finish(); //종료
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
                    updateConnectionState("disconnected");

                    clearUI();
                } else if (BluetoothService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) { //Find!
                    setGattServices(bluetoothService.getSupportedGattServices());
                } else if (BluetoothService.ACTION_DATA_AVAILABLE.equals(action)) { //Data가 유효할때
                    displayData(intent.getStringExtra(BluetoothService.EXTRA_DATA));
                }
            }
        };


        //끊기거나 데이터가 없을시
        private void clearUI() {
            dataField.setText("no_data");
        }

        //데이터 상태 표현
        private void updateConnectionState(final String resourceId) {

            state.setText(resourceId);

        }

        //데이터 내용 적용
        private void displayData(String data) {

            if (data != null) {
                Log.e("data", data + "/");
                dataField.setText(data); //추가!
            }
        }

        //브로드캐스트 리시버를 위해 인텐트 필터 액션 추가
        private IntentFilter makeGattUpdateIntentFilter() {
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

            ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();  //서비스 리스트
            ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>(); //Characteristic 리스트
            mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>(); //ArrayList<서비스리스트 ArrayList<Characteristic>> 이하 root로 표기

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
                mGattCharacteristics.add(charas); //루트리스트에 Characteristic리스트 넣기
                gattCharacteristicData.add(gattCharacteristicGroupData); //서비스리스트에 그룹리스트를 넣는다.

            }
            showBlueToothStat(gattCharacteristicData); //서비스리스트를 인자로 넘겨 호출

          /* for (int i = 0; i < gattServices.size(); i++) {
            BluetoothGattService bluetoothGattService = gattServices.get(i);
            uuid = bluetoothGattService.getUuid().toString();
            Log.d(TAG, uuid);
            Log.d(TAG, LIST_NAME + GattAttributes.lookup(uuid, unknownServiceString));
            Log.d(TAG, LIST_UUID + uuid);

            List<BluetoothGattCharacteristic> gattCharacteristics =
                    bluetoothGattService.getCharacteristics();

            for (int j = 0; j < gattCharacteristics.size(); j++) {
                BluetoothGattCharacteristic rootGattCharacteristic = gattCharacteristics.get(j);
                Log.d(TAG, rootGattCharacteristic.getUuid().toString());
                Log.d(TAG, LIST_NAME + GattAttributes.lookup(uuid, unknownCharaString));
                Log.d(TAG, LIST_UUID + uuid);

            }

        }*/
        }

        void showBlueToothStat(ArrayList<ArrayList<HashMap<String, String>>> data) {
            if (mGattCharacteristics != null) { //Characteristic리스트가 들어간 루트리스트가 살아있으면

                for (int i = 0; i < data.size(); i++) {
                    Log.e("data.size", data.size() + "");
                    for (int j = 0; j < data.get(i).size(); j++) {

                        Log.e("data.size", data.get(i).size() + "");
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(i).get(j); //배열에서 가져오기


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
                    }
                }
            }
        }
    }
}
