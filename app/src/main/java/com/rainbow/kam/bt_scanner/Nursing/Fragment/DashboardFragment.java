package com.rainbow.kam.bt_scanner.Nursing.Fragment;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.Nursing.Activity.StartNursingActivity;
import com.rainbow.kam.bt_scanner.Nursing.Patient.Patient;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.Tools.BLE.BLE;
import com.rainbow.kam.bt_scanner.Tools.BLE.BleUiCallbacks;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by sion on 2015-11-04.
 */
public class DashboardFragment extends Fragment implements BleUiCallbacks {

    private BLE ble;
    private List<BluetoothGattService> serviceList;
    private List<BluetoothGattCharacteristic> characteristicList;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForNotify;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForWrite;

    private Activity activity = getActivity();

    private View view;
    private TextView textView;
    public static final String ARG_PAGE = "ARG_PAGE";
    private int mPage;

    private RealmQuery<Patient> patientRealmQuery;
    private Realm realm;

    private String patientName;
    private String patientAge;
    private String patientHeight;
    private String patientWeight;
    private String patientStep;
    private String deviceName;
    private String deviceAddress;

    public static DashboardFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        DashboardFragment fragment = new DashboardFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragmnet_nursing_dashboard, container, false);
        textView = (TextView) view.findViewById(R.id.tab_tv);
        textView.setText("Fragment #" + mPage);

        realm = Realm.getInstance(getActivity());

        RealmResults<Patient> results = realm.where(Patient.class).findAll();
        Log.e("Dashboard", results.size() + " / " + results.get(0).getName() + " / " + results.get(0).getAge() + " / " + results.get(0).getHeight() + " / " + results.get(0).getWeight() + " / " + results.get(0).getStep() + " / " + results.get(0).getDeviceName() + " / " + results.get(0).getDeviceAddress());

        patientName = results.get(0).getName();
        patientAge = results.get(0).getAge();
        patientHeight = results.get(0).getHeight();
        patientWeight = results.get(0).getWeight();
        patientStep = results.get(0).getStep();
        deviceName = results.get(0).getDeviceName();
        deviceAddress = results.get(0).getDeviceAddress();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ble == null) {
            ble = new BLE(activity, this);
        }


            ble.connect(deviceAddress);

    }

    @Override
    public void onPause() {
        super.onPause();
        ble.stopMonitoringRssiValue();
        ble.disconnect();
        ble.close();
    }

    private void initDashboard(int position) {

        if (position != 0) {
            return;
        }


    }

    public byte[] parseHexStringToBytes(final String hex) {
        try {
            String tmp = hex.substring(2).replaceAll("[^[0-9][a-f]]", "");
            byte[] bytes = new byte[tmp.length() / 2]; // every two letters in the string are one byte finally

            String part = "";
            int checksum = 0;
            for (int i = 0; i < bytes.length; ++i) {
                part = "0x" + tmp.substring(i * 2, i * 2 + 2);
                bytes[i] = Long.decode(part).byteValue();
                checksum = (checksum ^ bytes[i]);
            }
            Log.e("cS","checkSum : " + checksum);
            return bytes;
        } catch (Exception e) {
            return null;
        }

    }

    @Override
    public void uiDeviceFound(BluetoothDevice device, int rssi, byte[] record) {

    }

    @Override
    public void uiDeviceConnected(BluetoothGatt gatt, BluetoothDevice device) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText("connected");
            }
        });
    }

    @Override
    public void uiDeviceDisconnected(BluetoothGatt gatt, BluetoothDevice device) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText("disconnected");
            }
        });
    }

    @Override
    public void uiAvailableServices(BluetoothGatt gatt, BluetoothDevice device, List<BluetoothGattService> services) {
        serviceList = services;
        BluetoothGattService bluetoothGattService = services.get(4);
        ble.getCharacteristicsForService(bluetoothGattService);
    }

    @Override
    public void uiCharacteristicForService(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, List<BluetoothGattCharacteristic> chars) {
        characteristicList = chars;
        bluetoothGattCharacteristicForWrite = characteristicList.get(0);
        bluetoothGattCharacteristicForNotify = characteristicList.get(1);
        uiCharacteristicsDetails(ble.getBluetoothGatt(), ble.getBluetoothDevice(), ble.getBluetoothGattService(), bluetoothGattCharacteristicForNotify);
    }

    @Override
    public void uiCharacteristicsDetails(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic characteristic) {
        ble.setNotificationForCharacteristic(bluetoothGattCharacteristicForNotify,true);

        byte[] dataToWrite = parseHexStringToBytes("0x890000");

        ble.writeDataToCharacteristic(bluetoothGattCharacteristicForWrite, dataToWrite);
    }

    @Override
    public void uiNewValueForCharacteristic(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, String strValue, int intValue, byte[] rawValue, String timestamp) {

    }

    @Override
    public void uiGotNotification(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service,final BluetoothGattCharacteristic characteristic) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                byte[] res = characteristic.getValue();
                for (int i = 0; i < res.length; i++) {
                    int lsb = characteristic.getValue()[i] & 0xff;
                    Log.e("noty", "res = " + res[i] + " lsb = " + lsb + "\n");
                }

                Toast.makeText(getActivity(), "uiGotNotification " + res[0], Toast.LENGTH_LONG).show();
//                detailFragment.setNotificationEnabledForService(characteristic);
            }
        });
    }

    @Override
    public void uiSuccessfulWrite(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, String description) {

    }

    @Override
    public void uiFailedWrite(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, String description) {

    }

    @Override
    public void uiNewRssiAvailable(BluetoothGatt gatt, BluetoothDevice device, int rssi) {

    }
}
