package com.rainbow.kam.bt_scanner.BluetoothPackage;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.rainbow.kam.bt_scanner.Tools.GattAttributes;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

/**
 * Created by sion on 2015-10-14.
 */
public class BluetoothService extends Service implements Serializable {

    private static final String TAG = BluetoothService.class.getSimpleName();

    private static final int STATE_DISCONNECTED = 0; // 아무것도 안함
    private static final int STATE_CONNECTING = 1; //연결중
    private static final int STATE_CONNECTED = 2; // 연결됨
    private int connectionState = STATE_DISCONNECTED; //현재상황

    //    public final static String ACTION_GATT_CONNECTED =
//            "com.rainbow.kam.bluetooth.ACTION_GATT_CONNECTED";
//    public final static String ACTION_GATT_DISCONNECTED =
//            "com.rainbow.kam.bluetooth.ACTION_GATT_DISCONNECTED";
//    public final static String ACTION_GATT_SERVICES_DISCOVERED =
//            "com.rainbow.kam.bluetooth.ACTION_GATT_SERVICES_DISCOVERED";
//    public final static String ACTION_DATA_AVAILABLE =
//            "com.rainbow.kam.bluetooth.ACTION_DATA_AVAILABLE";
//    public final static String EXTRA_DATA =
//            "com.rainbow.kam.bluetooth.EXTRA_DATA";

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    //본 앱에서만 쓰일 로컬바인더로 초기화
    private final IBinder mBinder = new LocalBinder();

    //바인더를 상속받아 자기자신을 리턴
    public class LocalBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    //블루투스 매니저/어댑터
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    //주소 String과 블루투스 Gatt
    private String mBluetoothDeviceAddress;
    public BluetoothGatt bluetoothGatt;

    //UUID 값 초기화
    public final static UUID getMyUuid = UUID.fromString(GattAttributes.UUID);

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() { //블루투스 Gatt콜백
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) { //연결 상태 전환
            //액션(클래스 패키지 네임 + 태그)용 String
            String intentAction;


            if (newState == BluetoothProfile.STATE_CONNECTED) { //현재상태 == 연결됨
                //액션 전환
                intentAction = ACTION_GATT_CONNECTED;
                //현재상황 전환
                connectionState = STATE_CONNECTED;

                //브로드 캐스트를 업데이트
                broadcastUpdate(intentAction);

                if (bluetoothGatt != null) {
                    Log.i(TAG, "Connected to GATT server.");
                    Log.i(TAG, "Attempting to start service discovery:" +
                            bluetoothGatt.discoverServices()); //블루투스 가트 서비스 여부를 가져옴
                } else {
                    onConnectionStateChange(gatt, BluetoothProfile.STATE_DISCONNECTED, BluetoothGatt.GATT_FAILURE);
                }


            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) { //현재상태
                intentAction = ACTION_GATT_DISCONNECTED;
                connectionState = STATE_DISCONNECTED;

                //브로드 캐스트를 업데이트
                broadcastUpdate(intentAction);

                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) { //서비스 발견?
            if (status == BluetoothGatt.GATT_SUCCESS) { //Gatt 성공
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED); //브로드 캐스트를 업데이트
                Log.i(TAG, "onServicesDiscovered GATT_SUCCESS.");
            } else {//Gatt 실패
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        //Characteristic 읽는 중
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.i(TAG, "onCharacteristicRead GATT_SUCCESS and minning UUID.");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        //Characteristic 변경 됨 + 통지호출
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    //브로드캐스트 업데이트
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        //브로드 캐스트 날림
        sendBroadcast(intent);
    }

    //브로드캐스트 업데이트 (오버로드)
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        //인텐트 초기화
        final Intent intent = new Intent(action);


        if (getMyUuid.equals(characteristic.getUuid())) { //내 UUID와 Characteristic의 UUID일치
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int value = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received value: %d", value));
            intent.putExtra(EXTRA_DATA, String.valueOf(value));

        } else {    //내 UUID와 Characteristic의 UUID불일치
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        //브로드 캐스트 날림
        sendBroadcast(intent);
    }

    //바인드
    @Override
    public IBinder onBind(Intent intent) {

        Log.d(TAG, "onBind");
        return mBinder;
    }

    //언바인드
    @Override
    public boolean onUnbind(Intent intent) {
        close();
        Log.d(TAG, "unBind");
        return super.onUnbind(intent);
    }


    //블루투스 매니저 / 어댑터 초기화
    public boolean initialize() {
        Log.d(TAG, "initialize");
        if (bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    //블루투스 연결
    public boolean connect(final String address) {
        if (bluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && bluetoothGatt != null) { //이상없음시
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (bluetoothGatt.connect()) {//연결성공
                connectionState = STATE_CONNECTING;
                Log.d(TAG, "success.");
                return true; //성공
            } else { //실패
                Log.d(TAG, "Fail.");
                return false;
            }
        }

        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                bluetoothGatt = device.connectGatt(BluetoothService.this, false, mGattCallback);
            }
        });
//        bluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        connectionState = STATE_CONNECTING;
        return true;
    }

    //블루투스 연결 끊기

    public void disconnect() {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            refreshDeviceGattCache(bluetoothGatt);
            Log.w(TAG, "BluetoothAdapter not initialized_disconnect");
            return;
        }

        bluetoothGatt.disconnect();
        onDestroy();
    }

    //언바인드시 호출
    public void close() {
        //블루투스 Gatt null
        if (bluetoothGatt == null) {
            return;
        }
        refreshDeviceGattCache(bluetoothGatt);
        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    //Characteristic 읽기
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized_readCharacteristic");
            return;
        }
        //인자로 받은 Characteristic를 넘겨 가공시킨다
        bluetoothGatt.readCharacteristic(characteristic);
    }

    //GATT 통지 수신
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (bluetoothAdapter == null || bluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized_setCharacteristicNotification");
            return;
        }
        bluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        if (getMyUuid.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bluetoothGatt.writeDescriptor(descriptor);
        }
    }

    //블루투스 Gatt의 서비스를 리턴
    public List<BluetoothGattService> getSupportedGattServices() {
        if (bluetoothGatt == null) return null;

        return bluetoothGatt.getServices();
    }

    private boolean refreshDeviceGattCache(BluetoothGatt bluetoothGatt) {
        try {
            Log.i(TAG, "refresh");
            BluetoothGatt localGatt = bluetoothGatt;
            Method localMethod = localGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                boolean b = ((Boolean) localMethod.invoke(localGatt, new Object[0]));
                return b;
            }
        } catch (Exception localException) {
            Log.e(TAG, "An exception occured while refreshing device");
        }
        return false;
    }

    //가능여부
    public static boolean isCharacteristicWriteable(BluetoothGattCharacteristic pChar) {
        return (pChar.getProperties() & (BluetoothGattCharacteristic.PROPERTY_WRITE | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) != 0;
    }

    public static boolean isCharacterisitcReadable(BluetoothGattCharacteristic pChar) {
        return ((pChar.getProperties() & BluetoothGattCharacteristic.PROPERTY_READ) != 0);
    }

    public boolean isCharacterisiticNotifiable(BluetoothGattCharacteristic pChar) {
        return (pChar.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
    }

}
