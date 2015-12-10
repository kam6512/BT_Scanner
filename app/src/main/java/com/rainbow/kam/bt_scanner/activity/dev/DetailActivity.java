package com.rainbow.kam.bt_scanner.activity.dev;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.adapter.dev.detail.CharacteristicAdapter;
import com.rainbow.kam.bt_scanner.adapter.dev.detail.ServiceAdapter;
import com.rainbow.kam.bt_scanner.fragment.dev.DetailCharacteristicFragment;
import com.rainbow.kam.bt_scanner.fragment.dev.DetailCharacteristicFragment.OnCharacteristicReadyListener;
import com.rainbow.kam.bt_scanner.fragment.dev.DetailFragment;
import com.rainbow.kam.bt_scanner.fragment.dev.DetailServiceFragment;
import com.rainbow.kam.bt_scanner.fragment.dev.DetailServiceFragment.OnServiceReadyListener;
import com.rainbow.kam.bt_scanner.tools.ble.BLE;
import com.rainbow.kam.bt_scanner.tools.ble.BleUiCallbacks;

import java.util.List;

/**
 * Created by kam6512 on 2015-11-27.
 */
public class DetailActivity extends AppCompatActivity
        implements BleUiCallbacks,
        DetailFragment.OnDetailReadyListener,
        OnCharacteristicReadyListener,
        OnServiceReadyListener,
        ServiceAdapter.OnServiceItemClickListener,
        CharacteristicAdapter.OnCharacteristicItemClickListener {

    public static final String TAG = "DetailActivity";

    public static final String EXTRAS_DEVICE_NAME = "BLE_DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "BLE_DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_RSSI = "BLE_DEVICE_RSSI";

    private boolean isCallBackReady = false;


    private enum GattType {
        GATT_SERVICES, GATT_CHARACTERISTICS, GATT_CHARACTERISTIC_DETAILS
    }

    private GattType gattType = GattType.GATT_SERVICES;
    private String deviceName;
    private String deviceAddress;
    private String deviceRSSI;

    private BLE ble;

    private Toolbar toolbar;
    private ActionBar actionBar;

    private TextView deviceNameTextView;
    private TextView deviceAddressTextView;
    private TextView deviceRSSITextView;
    private TextView deviceStateTextView;

    private FragmentManager fragmentManager;
    private android.support.v4.app.FragmentTransaction fragmentTransaction;

    private DetailServiceFragment serviceFragment;
    private DetailCharacteristicFragment characteristicFragment;
    private DetailFragment detailFragment;

    private List<BluetoothGattService> bluetoothGattServices;
    private List<BluetoothGattCharacteristic> bluetoothGattCharacteristics;

    private BluetoothGattService bluetoothGattService;
    private BluetoothGattCharacteristic bluetoothGattCharacteristic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        deviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        deviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        deviceRSSI = intent.getStringExtra(EXTRAS_DEVICE_RSSI) + "db";

        setToolbar();
        setFragments();

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.detail_fragment_view, serviceFragment);
        fragmentTransaction.commit();
    }

    private void setToolbar() {
        deviceNameTextView = (TextView) findViewById(R.id.detail_name);
        deviceRSSITextView = (TextView) findViewById(R.id.detail_rssi);
        deviceAddressTextView = (TextView) findViewById(R.id.detail_address);
        deviceStateTextView = (TextView) findViewById(R.id.detail_state);
        deviceNameTextView.setText(deviceName);
        deviceAddressTextView.setText(deviceAddress);
        deviceRSSITextView.setText(deviceRSSI);

        toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(deviceName);
        }
    }

    private void setFragments() {
        fragmentManager = getSupportFragmentManager();

        serviceFragment = new DetailServiceFragment();
        characteristicFragment = new DetailCharacteristicFragment();
        detailFragment = new DetailFragment();

        serviceFragment.setRetainInstance(true);
        characteristicFragment.setRetainInstance(true);
        detailFragment.setRetainInstance(true);
    }

    @Override
    public void onServiceItemClick(int position) {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.detail_fragment_view, characteristicFragment);

        bluetoothGattService = serviceFragment.getService(position);
        ble.getCharacteristics(bluetoothGattService);

        fragmentTransaction.commit();
    }

    @Override
    public void onCharacteristicItemClick(int position) {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.detail_fragment_view, detailFragment);

        bluetoothGattCharacteristic = characteristicFragment.getCharacteristic(position);
        fragmentTransaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disconnect();
    }

    private void connect() {
        if (ble == null) {
            ble = new BLE(this, this);
        }
        if (!ble.initialize()) {
            finish();
        }
        deviceStateTextView.setText("connecting...");
        ble.connect(deviceAddress);
    }

    private void disconnect() {
        if (ble != null) {
            ble.stopMonitoringRssiValue();
            ble.disconnect();
            ble.close();
        }
        isCallBackReady = false;
        gattType = GattType.GATT_SERVICES;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            gattType = GattType.GATT_SERVICES;
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        fragmentTransaction = fragmentManager.beginTransaction();
        if (gattType.equals(GattType.GATT_SERVICES)) {
            serviceFragment.clearAdapter();
            finish();
        }
        if (gattType.equals(GattType.GATT_CHARACTERISTICS)) {
            onServicesFound(ble.getBluetoothGatt(), ble.getBluetoothDevice(), ble.getBluetoothGattServices());
            characteristicFragment.clearAdapter();
            fragmentTransaction.replace(R.id.detail_fragment_view, serviceFragment);
        }
        if (gattType.equals(GattType.GATT_CHARACTERISTIC_DETAILS)) {
            ble.getCharacteristics(ble.getBluetoothGattService());
            detailFragment.clearCharacteristic();
            fragmentTransaction.replace(R.id.detail_fragment_view, characteristicFragment);
        }
        fragmentTransaction.commit();
    }

    @Override
    public void onDeviceConnected(final BluetoothGatt gatt, final BluetoothDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceStateTextView.setText(R.string.connected);
                gattType = GattType.GATT_SERVICES;
            }
        });
    }

    @Override
    public void onDeviceDisconnected(final BluetoothGatt gatt, final BluetoothDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceStateTextView.setText(R.string.disconnected);

                if (serviceFragment != null && gattType == GattType.GATT_SERVICES) {
                    serviceFragment.clearAdapter();
                }
                if (characteristicFragment != null && gattType == GattType.GATT_CHARACTERISTICS) {
                    characteristicFragment.clearAdapter();
                }

                gattType = GattType.GATT_SERVICES;
            }
        });
    }

    @Override
    public void onRssiUpdate(final BluetoothGatt gatt, BluetoothDevice device, final int rssi) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceRSSI = rssi + "db";
                deviceRSSITextView.setText(deviceRSSI);
            }
        });
    }

    @Override
    public void onServicesFound(final BluetoothGatt gatt, final BluetoothDevice device, final List<BluetoothGattService> services) {
        bluetoothGattServices = services;
        onServiceReady();
    }

    @Override
    public void onCharacteristicFound(final BluetoothGatt gatt, final BluetoothDevice device, final BluetoothGattService service, final List<BluetoothGattCharacteristic> chars) {
        bluetoothGattCharacteristics = chars;
        onCharacteristicReady();
    }

    @Override
    public void onNewDataFound(final BluetoothGatt gatt, final BluetoothDevice device, final BluetoothGattService service, final BluetoothGattCharacteristic ch, final String strValue, final int intValue, final byte[] rawValue, final String timestamp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (detailFragment == null || detailFragment.getCharacteristic() == null) {
                    return;
                }
                detailFragment.newValueForCharacterictic(ch, strValue, intValue, rawValue, timestamp);
                detailFragment.bindView();
            }
        });
    }

    @Override
    public void onDataNotify(final BluetoothGatt gatt, final BluetoothDevice device, final BluetoothGattService service, final BluetoothGattCharacteristic characteristic) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                detailFragment.setNotificationEnabledForService(characteristic);

                byte[] characteristicValue = characteristic.getValue();

                for (byte aCharacteristicValue : characteristicValue) {
                    int lsb = aCharacteristicValue & 0xff;
                    Log.e("noty", "characteristicValue = " + Integer.toHexString(aCharacteristicValue) + " / lsb = " + lsb);
                }
            }
        });
    }

    @Override
    public void onWriteSuccess(final BluetoothGatt gatt, final BluetoothDevice device,
                               final BluetoothGattService service, final BluetoothGattCharacteristic ch,
                               final String description) {
        Toast.makeText(getApplicationContext(), "Writing to " + description + " was finished successfully!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onWriteFail(final BluetoothGatt gatt, final BluetoothDevice device,
                            final BluetoothGattService service, final BluetoothGattCharacteristic ch,
                            final String description) {
        Toast.makeText(getApplicationContext(), "Writing to " + description + " FAILED!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onServiceReady() {
        if (!isCallBackReady) {
            isCallBackReady = true;
        } else {
            isCallBackReady = false;
            if (serviceFragment != null) {
                serviceFragment.clearAdapter();
                for (BluetoothGattService bluetoothGattService : bluetoothGattServices) {
                    serviceFragment.addService(bluetoothGattService);
                }
                serviceFragment.notifyAdapter();
            }
            gattType = GattType.GATT_SERVICES;
        }
    }

    @Override
    public void onCharacteristicReady() {
        if (!isCallBackReady) {
            isCallBackReady = true;
        } else {
            isCallBackReady = false;
            if (characteristicFragment != null) {
                characteristicFragment.clearAdapter();
                for (BluetoothGattCharacteristic bluetoothGattCharacteristic : bluetoothGattCharacteristics) {
                    characteristicFragment.addCharacteristic(bluetoothGattCharacteristic);
                }
                characteristicFragment.notifyAdapter();
            }
            gattType = GattType.GATT_CHARACTERISTICS;
        }
    }

    @Override
    public void onDetailReady() {

        if (detailFragment != null) {
            detailFragment.setBle(ble);
            detailFragment.setCharacteristic(bluetoothGattCharacteristic);
            detailFragment.bindView();
        }
        gattType = GattType.GATT_CHARACTERISTIC_DETAILS;

    }
}
