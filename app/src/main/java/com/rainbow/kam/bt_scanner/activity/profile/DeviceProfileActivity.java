package com.rainbow.kam.bt_scanner.activity.profile;

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
import com.rainbow.kam.bt_scanner.fragment.profile.CharacteristicListFragment;
import com.rainbow.kam.bt_scanner.fragment.profile.CharacteristicListFragment.OnCharacteristicReadyListener;
import com.rainbow.kam.bt_scanner.fragment.profile.ControlFragment;
import com.rainbow.kam.bt_scanner.fragment.profile.ServiceListFragment;
import com.rainbow.kam.bt_scanner.fragment.profile.ServiceListFragment.OnServiceReadyListener;
import com.rainbow.kam.bt_scanner.tools.BluetoothHelper;
import com.rainbow.kam.bt_scanner.tools.gatt.GattCustomCallbacks;
import com.rainbow.kam.bt_scanner.tools.gatt.GattManager;

import java.util.List;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-11-27.
 */
public class DeviceProfileActivity extends AppCompatActivity
        implements GattCustomCallbacks,
        ControlFragment.OnControlListener,
        OnCharacteristicReadyListener,
        OnServiceReadyListener,
        CharacteristicAdapter.OnCharacteristicItemClickListener,
        ServiceAdapter.OnServiceItemClickListener {

    private final String TAG = getClass().getSimpleName();


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

    private BluetoothGattCharacteristic controlCharacteristic;


    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_profile);

        Intent intent = getIntent();
        deviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        deviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        deviceRSSI = "- - " + RSSI_UNIT;

        setToolbar();
        setFragments();

        fragmentManager.beginTransaction().replace(R.id.detail_fragment_view, serviceListFragment).commit();
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

        serviceListFragment = new ServiceListFragment();
        characteristicListFragment = new CharacteristicListFragment();
        controlFragment = new ControlFragment();
    }


    @DebugLog
    @Override
    protected void onResume() {
        super.onResume();
        registerBluetooth();
    }


    @DebugLog
    @Override
    protected void onPause() {
        super.onPause();
        disconnectDevice();
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
        BluetoothHelper.onActivityResult(requestCode, resultCode, this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        BluetoothHelper.onRequestPermissionsResult(requestCode, grantResults, this);
    }


    @DebugLog
    private void registerBluetooth() {
        gattManager = new GattManager(this, this);
        if (gattManager.isBluetoothAvailable()) {
            connectDevice();
        } else {
            BluetoothHelper.initBluetoothOn(this);
        }
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
        finish();
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


    @DebugLog
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
    public void onDataNotify(final BluetoothGattCharacteristic ch) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (controlFragment.isVisible()) {
                    controlFragment.newValueForCharacteristic(ch);
                }
            }
        });
    }


    @DebugLog
    @Override
    public void onWriteSuccess() {
        Toast.makeText(DeviceProfileActivity.this, "onWriteSuccess", Toast.LENGTH_SHORT).show();
    }


    @DebugLog
    @Override
    public void onWriteFail() {
        Toast.makeText(DeviceProfileActivity.this, "onWriteFail", Toast.LENGTH_SHORT).show();
    }


    @DebugLog
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
        fragmentManager.beginTransaction().addToBackStack("characteristic").replace(R.id.detail_fragment_view, characteristicListFragment).commit();
        bluetoothGattCharacteristics = bluetoothGattServices.get(position).getCharacteristics();
        onCharacteristicReady();

    }


    @DebugLog
    @Override
    public void onCharacteristicItemClick(int position) {
        fragmentManager.beginTransaction().addToBackStack("control").replace(R.id.detail_fragment_view, controlFragment).commit();
        controlCharacteristic = bluetoothGattCharacteristics.get(position);
    }


    @DebugLog
    @Override
    public void onServiceReady() {
        if (serviceListFragment.isVisible() && bluetoothGattServices != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    serviceListFragment.setServiceList(bluetoothGattServices);
                }
            });
        }
    }


    @DebugLog
    @Override
    public void onCharacteristicReady() {
        if (characteristicListFragment.isVisible() && bluetoothGattServices != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    characteristicListFragment.setCharacteristicList(bluetoothGattCharacteristics);
                }
            });
        }
    }


    @DebugLog
    @Override
    public void onControlReady() {
        controlFragment.init(deviceName, deviceAddress, controlCharacteristic);
    }


    @Override
    public void setNotification(boolean isNotificationEnable) {
        gattManager.setNotification(controlCharacteristic, isNotificationEnable);
    }


    @Override
    public void setReadValue() {
        gattManager.readValue(controlCharacteristic);
    }


    @Override
    public void setWriteValue(byte[] data) {
        gattManager.writeValue(controlCharacteristic, data);
    }
}
