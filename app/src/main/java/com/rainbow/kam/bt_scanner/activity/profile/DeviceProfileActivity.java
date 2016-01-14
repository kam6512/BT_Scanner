package com.rainbow.kam.bt_scanner.activity.profile;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.rainbow.kam.bt_scanner.tools.helper.BluetoothHelper;
import com.rainbow.kam.bt_scanner.tools.gatt.GattCustomCallbacks;
import com.rainbow.kam.bt_scanner.tools.gatt.GattManager;

import java.util.List;

import hugo.weaving.DebugLog;

/**
 * Created by kam6512 on 2015-11-27.
 */
public class DeviceProfileActivity extends AppCompatActivity
        implements
        ServiceListFragment.OnServiceReadyListener,
        CharacteristicListFragment.OnCharacteristicReadyListener,
        ServiceAdapter.OnServiceItemClickListener,
        CharacteristicAdapter.OnCharacteristicItemClickListener,
        ControlFragment.OnControlListener {

    private final String TAG = getClass().getSimpleName();

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
        deviceName = intent.getStringExtra(BluetoothHelper.EXTRAS_DEVICE_NAME);
        deviceAddress = intent.getStringExtra(BluetoothHelper.EXTRAS_DEVICE_ADDRESS);
        deviceRSSI = "- - " + RSSI_UNIT;

        setToolbar();
        setFragments();

        fragmentManager.beginTransaction().replace(R.id.profile_fragment_view, serviceListFragment).commit();
    }


    private void setToolbar() {
        deviceNameTextView = (TextView) findViewById(R.id.profile_name);
        deviceRSSITextView = (TextView) findViewById(R.id.profile_rssi);
        deviceAddressTextView = (TextView) findViewById(R.id.profile_address);
        deviceStateTextView = (TextView) findViewById(R.id.profile_state);
        deviceNameTextView.setText(deviceName);
        deviceAddressTextView.setText(deviceAddress);
        deviceRSSITextView.setText(deviceRSSI);

        Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
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
        gattManager = new GattManager(this, gattCallbacks);
        if (gattManager.isBluetoothAvailable()) {
            connectDevice();
        } else {
            BluetoothHelper.initBluetoothOn(this);
        }
    }


    @DebugLog
    private synchronized void connectDevice() {
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
    private synchronized void disconnectDevice() {
        if (gattManager != null && gattManager.isBluetoothAvailable()) {
            gattManager.disconnect();
        }
    }


    private final GattCustomCallbacks.GattCallbacks gattCallbacks = new GattCustomCallbacks.GattCallbacks() {
        public void onDeviceConnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    deviceStateTextView.setText(R.string.bt_connected);
                }
            });
        }


        public void onDeviceDisconnected() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    deviceStateTextView.setText(R.string.bt_disconnected);
                }
            });
        }


        public void onServicesFound(final List<BluetoothGattService> services) {
            bluetoothGattServices = services;
            onServiceReady();
        }


        public void onReadSuccess(final BluetoothGattCharacteristic ch) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    controlFragment.newValueForCharacteristic(ch);
                }
            });
        }


        public void onReadFail() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    controlFragment.setFail();

                }
            });
        }


        public void onDataNotify(final BluetoothGattCharacteristic ch) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    controlFragment.newValueForCharacteristic(ch);

                }
            });
        }


        public void onWriteSuccess() {
            Toast.makeText(DeviceProfileActivity.this, "onWriteSuccess", Toast.LENGTH_SHORT).show();
        }


        public void onWriteFail() {
            Toast.makeText(DeviceProfileActivity.this, "onWriteFail", Toast.LENGTH_SHORT).show();
        }


        public void onRSSIUpdate(final int rssi) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    deviceRSSI = rssi + RSSI_UNIT;
                    deviceRSSITextView.setText(deviceRSSI);
                }
            });
        }
    };


    @DebugLog
    @Override
    public void onServiceItemClick(int position) {
        fragmentManager.beginTransaction().addToBackStack("characteristic").replace(R.id.profile_fragment_view, characteristicListFragment).commit();
        bluetoothGattCharacteristics = bluetoothGattServices.get(position).getCharacteristics();
        onCharacteristicReady();

    }


    @DebugLog
    @Override
    public void onCharacteristicItemClick(int position) {
        fragmentManager.beginTransaction().addToBackStack("control").replace(R.id.profile_fragment_view, controlFragment).commit();
        if (!bluetoothGattCharacteristics.get(position).equals(controlCharacteristic)) {
            controlCharacteristic = bluetoothGattCharacteristics.get(position);
        }

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
        if (data!=null){
            gattManager.writeValue(controlCharacteristic, data);
        }else{
            gattCallbacks.onWriteFail();
        }
    }
}
