package com.rainbow.kam.bt_scanner.activity.development;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
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
import com.rainbow.kam.bt_scanner.adapter.CharacteristicAdapter;
import com.rainbow.kam.bt_scanner.adapter.ServiceAdapter;
import com.rainbow.kam.bt_scanner.fragment.development.CharacteristicListFragment;
import com.rainbow.kam.bt_scanner.fragment.development.CharacteristicListFragment.OnCharacteristicReadyListener;
import com.rainbow.kam.bt_scanner.fragment.development.ControlFragment;
import com.rainbow.kam.bt_scanner.fragment.development.ServiceListFragment;
import com.rainbow.kam.bt_scanner.fragment.development.ServiceListFragment.OnServiceReadyListener;
import com.rainbow.kam.bt_scanner.tools.gatt.GattCustomCallbacks;
import com.rainbow.kam.bt_scanner.tools.gatt.GattManager;

import java.util.List;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-11-27.
 */
public class DeviceProfileActivity extends AppCompatActivity
        implements GattCustomCallbacks,
        ControlFragment.OnControlReadyListener,
        OnCharacteristicReadyListener,
        OnServiceReadyListener,
        CharacteristicAdapter.OnCharacteristicItemClickListener,
        ServiceAdapter.OnServiceItemClickListener {
    public static final String TAG = "DeviceProfileActivity";

    private static final int REQUEST_ENABLE_BT = 1;

    public static final String EXTRAS_DEVICE_NAME = "BLE_DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "BLE_DEVICE_ADDRESS";

    private static final String RSSI_UNIT = "db";

    private String deviceName;
    private String deviceAddress;
    private String deviceRSSI;

    private TextView deviceNameTextView;
    private TextView deviceAddressTextView;
    private TextView deviceRSSITextView;
    private TextView deviceStateTextView;

    private FragmentManager fragmentManager;

    private ServiceListFragment serviceListFragment;
    private CharacteristicListFragment characteristicListFragment;
    private ControlFragment controlFragment;

    private GattManager gattManager;

    private List<BluetoothGattService> bluetoothGattServices;
    private List<BluetoothGattCharacteristic> bluetoothGattCharacteristics;

    private BluetoothGattCharacteristic bluetoothGattCharacteristic;


    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        deviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        deviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        deviceRSSI = "- - " + RSSI_UNIT;

        setToolbar();
        setFragments();

        fragmentManager.beginTransaction().replace(R.id.detail_fragment_view, serviceListFragment, "service").commit();
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


    @DebugLog
    private void setFragments() {
        fragmentManager = getSupportFragmentManager();

        serviceListFragment = new ServiceListFragment();
        characteristicListFragment = new CharacteristicListFragment();
        controlFragment = new ControlFragment();

        serviceListFragment.setRetainInstance(true);
        characteristicListFragment.setRetainInstance(true);
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerBluetooth();
    }


    @Override
    protected void onPause() {
        super.onPause();
        disconnectDevice();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        serviceListFragment.removeListener();
        characteristicListFragment.removeListener();
        controlFragment.removeListener();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    //블루투스 켜짐
                    Snackbar.make(getWindow().getDecorView(), R.string.bt_on, Snackbar.LENGTH_SHORT).show();
                } else {
                    //블루투스 에러
                    Toast.makeText(this, R.string.bt_not_init, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(getWindow().getDecorView(), R.string.permission_thanks, Snackbar.LENGTH_SHORT).show();
            } else {

                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(myAppSettings, 0);

                Toast.makeText(getApplicationContext(), R.string.permission_request, Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.permission_denial, Toast.LENGTH_SHORT).show();
        }
    }


    @DebugLog
    private void registerBluetooth() {
        if (gattManager == null) {
            gattManager = new GattManager(this, this);
        }
        if (gattManager.isBluetoothAvailable()) {
            connectDevice();
        } else {
            initBluetoothOn();
        }
    }


    @DebugLog
    private void initBluetoothOn() {//블루투스 가동여부
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, REQUEST_ENABLE_BT);
    }


    @DebugLog
    private void connectDevice() {
        deviceStateTextView.setText("connecting");
        try {
            gattManager.connect(deviceAddress);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            deviceNameTextView.setText(e.getMessage());
            deviceAddressTextView.setText(e.getMessage());
            deviceRSSITextView.setText(e.getMessage());
            deviceStateTextView.setText(e.getMessage());
        }
    }


    @DebugLog
    private void disconnectDevice() {
        if (gattManager != null && gattManager.isBluetoothAvailable()) {
            gattManager.disconnect();
        }
    }


    @DebugLog
    @Override
    public synchronized void onDeviceConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceStateTextView.setText(R.string.detail_state_connected);
            }
        });
    }


    @DebugLog
    @Override
    public void onDeviceDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceStateTextView.setText(R.string.detail_state_disconnected);
            }
        });
    }


    @DebugLog
    @Override
    public void onServicesFound(final List<BluetoothGattService> services) {
        bluetoothGattServices = services;
        onServiceReady();
    }


    @DebugLog
    @Override
    public void onServicesNotFound() {
        Toast.makeText(this, getResources().getText(R.string.fail_characteristic), Toast.LENGTH_SHORT).show();
    }


    @DebugLog
    @Override
    public void onReadSuccess(final BluetoothGattCharacteristic ch) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (controlFragment.isVisible()) {
                    controlFragment.newValueForCharacteristic(ch);
                }

            }
        });
    }


    @Override
    public void onReadFail() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (controlFragment.isVisible()) {
                    controlFragment.setFail();
                }
            }
        });
    }


    @DebugLog
    @Override
    public void onDataNotify(BluetoothGattCharacteristic ch) {
        // Not use in this Activity
    }


    @DebugLog
    @Override
    public void onWriteSuccess() {
        Toast.makeText(DeviceProfileActivity.this, "onWriteSuccess", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onWriteFail() {
        Toast.makeText(DeviceProfileActivity.this, "onWriteFail", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onRSSIUpdate(final int rssi) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceRSSI = rssi + RSSI_UNIT;
                deviceRSSITextView.setText(deviceRSSI);
            }
        });
    }


    @DebugLog
    @Override
    public void onRSSIMiss() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceRSSI = "--" + RSSI_UNIT;
                deviceRSSITextView.setText(deviceRSSI);
            }
        });
    }


    @DebugLog
    @Override
    public void onServiceItemClick(int position) {
        fragmentManager.beginTransaction().addToBackStack("characteristic").replace(R.id.detail_fragment_view, characteristicListFragment, "characteristic").commit();
        bluetoothGattCharacteristics = bluetoothGattServices.get(position).getCharacteristics();
        onCharacteristicReady();
    }


    @DebugLog
    @Override
    public void onCharacteristicItemClick(int position) {
        fragmentManager.beginTransaction().addToBackStack("detail").replace(R.id.detail_fragment_view, controlFragment, "detail").commit();
        bluetoothGattCharacteristic = bluetoothGattCharacteristics.get(position);
    }


    @DebugLog
    @Override
    public void onServiceReady() {
        if (serviceListFragment.isVisible() && bluetoothGattServices != null) {
            serviceListFragment.setService(bluetoothGattServices);
        }
    }


    @DebugLog
    @Override
    public void onCharacteristicReady() {
        if (characteristicListFragment.isVisible() && bluetoothGattServices != null) {
            characteristicListFragment.setCharacteristic(bluetoothGattCharacteristics);
        }
    }


    @DebugLog
    @Override
    public void onControlReady() {
        controlFragment.init(gattManager, bluetoothGattCharacteristic);
    }
}
