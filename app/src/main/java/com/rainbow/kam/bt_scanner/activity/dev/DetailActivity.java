package com.rainbow.kam.bt_scanner.activity.dev;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
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
import com.rainbow.kam.bt_scanner.tools.gatt.GattManager;
import com.rainbow.kam.bt_scanner.tools.gatt.GattCustomCallbacks;

import java.util.List;

/**
 * Created by kam6512 on 2015-11-27.
 */
public class DetailActivity extends AppCompatActivity
        implements GattCustomCallbacks,
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

    private TextView deviceNameTextView;
    private TextView deviceAddressTextView;
    private TextView deviceRSSITextView;
    private TextView deviceStateTextView;

    private FragmentManager fragmentManager;
    private android.support.v4.app.FragmentTransaction fragmentTransaction;

    private DetailServiceFragment serviceFragment;
    private DetailCharacteristicFragment characteristicFragment;
    private DetailFragment detailFragment;

    private GattManager gattManager;

    private List<BluetoothGattService> bluetoothGattServices;
    private List<BluetoothGattCharacteristic> bluetoothGattCharacteristics;

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
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
//        detailFragment.setRetainInstance(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBluetooth();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gattManager != null) {
            disconnectDevice();
        }
    }


    private void connectDevice() {
        if (gattManager == null) {
            gattManager = new GattManager(this, this);
        }
        if (gattManager.initialize()) {
            deviceStateTextView.setText("connecting...");
            try {
                gattManager.connect(deviceAddress);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            finish();
        }
    }


    private void disconnectDevice() {
        if (gattManager != null) {
            gattManager.disconnect();
        }
    }

    @Override
    public void onBackPressed() {
        fragmentTransaction = fragmentManager.beginTransaction();
        if (gattType.equals(GattType.GATT_SERVICES)) {

            finish();
        }
        if (gattType.equals(GattType.GATT_CHARACTERISTICS)) {
            fragmentTransaction.replace(R.id.detail_fragment_view, serviceFragment);

            onServiceReady();
        }
        if (gattType.equals(GattType.GATT_CHARACTERISTIC_DETAILS)) {
            fragmentTransaction.replace(R.id.detail_fragment_view, characteristicFragment);

            onCharacteristicReady();
        }
        fragmentTransaction.commit();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(getWindow().getDecorView(), R.string.permission_thanks, Snackbar.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.permission_request, Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.permission_denial, Toast.LENGTH_SHORT).show();
        }
    }

    private void registerBluetooth() {

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        if (bluetoothManager == null) {
            Toast.makeText(this, R.string.bt_fail, Toast.LENGTH_LONG).show();
            finish();
        } else {
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter.isEnabled()) {
                connectDevice();
            } else {
                initBluetoothOn();
            }
        }
    }


    private void initBluetoothOn() {//블루투스 가동여부
        Toast.makeText(this, R.string.bt_must_start, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, 1);
    }

    @Override
    public void onDeviceConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceStateTextView.setText(R.string.connected);
                gattType = GattType.GATT_SERVICES;
            }
        });
    }

    @Override
    public void onDeviceDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isCallBackReady = false;
                deviceStateTextView.setText(R.string.disconnected);
                gattType = GattType.GATT_SERVICES;
            }
        });
    }

    @Override
    public void onServicesFound(final List<BluetoothGattService> services) {
        bluetoothGattServices = services;
        onServiceReady();
    }

    @Override
    public void onNewDataFound(final BluetoothGattCharacteristic ch, final String strValue, final byte[] rawValue, final String timestamp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (detailFragment == null || detailFragment.getCharacteristic() == null) {
                    return;
                }
                detailFragment.setNotificationEnable(ch);
                detailFragment.newValueForCharacteristic(ch, strValue, rawValue, timestamp);
            }
        });
    }

    @Override
    public void onWriteSuccess(final String description) {
        Toast.makeText(getApplicationContext(), "Writing to " + description + " was finished successfully!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onWriteFail(final String description) {
        Toast.makeText(getApplicationContext(), "Writing to " + description + " FAILED!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRssiUpdate(final int rssi) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceRSSI = rssi + "db";
                deviceRSSITextView.setText(deviceRSSI);
            }
        });
    }

    @Override
    public void onServiceReady() {
        Log.e(TAG, "onServiceReady");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
        });
    }

    @Override
    public void onCharacteristicReady() {
        Log.e(TAG, "onCharacteristicReady");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
        });
    }

    @Override
    public void onDetailReady() {
        Log.e(TAG, "onDetailReady");
        if (detailFragment != null) {
            detailFragment.setGattManager(gattManager);
            detailFragment.setCharacteristic(bluetoothGattCharacteristic);
        }
        gattType = GattType.GATT_CHARACTERISTIC_DETAILS;
    }

    @Override
    public void onServiceItemClick(int position) {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.detail_fragment_view, characteristicFragment);

        bluetoothGattCharacteristics = bluetoothGattServices.get(position).getCharacteristics();
        onCharacteristicReady();

        fragmentTransaction.commit();
    }

    @Override
    public void onCharacteristicItemClick(int position) {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.detail_fragment_view, detailFragment);

        bluetoothGattCharacteristic = bluetoothGattCharacteristics.get(position);
        fragmentTransaction.commit();
    }
}
