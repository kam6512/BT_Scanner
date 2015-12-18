package com.rainbow.kam.bt_scanner.activity.dev;

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
import com.rainbow.kam.bt_scanner.adapter.dev.detail.CharacteristicAdapter;
import com.rainbow.kam.bt_scanner.adapter.dev.detail.ServiceAdapter;
import com.rainbow.kam.bt_scanner.fragment.dev.DetailCharacteristicFragment;
import com.rainbow.kam.bt_scanner.fragment.dev.DetailCharacteristicFragment.OnCharacteristicReadyListener;
import com.rainbow.kam.bt_scanner.fragment.dev.DetailFragment;
import com.rainbow.kam.bt_scanner.fragment.dev.DetailServiceFragment;
import com.rainbow.kam.bt_scanner.fragment.dev.DetailServiceFragment.OnServiceReadyListener;
import com.rainbow.kam.bt_scanner.tools.gatt.GattCustomCallbacks;
import com.rainbow.kam.bt_scanner.tools.gatt.GattManager;

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

    private static final int REQUEST_ENABLE_BT = 1;

    public static final String EXTRAS_DEVICE_NAME = "BLE_DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "BLE_DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_RSSI = "BLE_DEVICE_RSSI";

    private static final String RSSI_UNIT = "db";

    private boolean isCallBackReady = false;

    private String deviceName;
    private String deviceAddress;
    private String deviceRSSI;

    private TextView deviceNameTextView;
    private TextView deviceAddressTextView;
    private TextView deviceRSSITextView;
    private TextView deviceStateTextView;

    private FragmentManager fragmentManager;

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
        deviceRSSI = intent.getStringExtra(EXTRAS_DEVICE_RSSI) + RSSI_UNIT;

        setToolbar();
        setFragments();

        fragmentManager.beginTransaction().replace(R.id.detail_fragment_view, serviceFragment).commit();
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
    public void onBackPressed() {
        super.onBackPressed();
        isCallBackReady = true;
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
                switch (resultCode) {
                    case RESULT_OK:
                        Snackbar.make(getWindow().getDecorView(), R.string.bt_on, Snackbar.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(this, R.string.bt_not_init, Toast.LENGTH_SHORT).show();
                        finish();
                        break;
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


    private void connectDevice() {
        deviceStateTextView.setText("connecting...");
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


    private void disconnectDevice() {
        if (gattManager != null && gattManager.isBluetoothAvailable()) {
            gattManager.disconnect();
        }
    }


    private void initBluetoothOn() {//블루투스 가동여부
        Toast.makeText(this, R.string.bt_must_start, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intent, REQUEST_ENABLE_BT);
    }

    @Override
    public void onDeviceConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceStateTextView.setText(R.string.connected);
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
            }
        });
    }

    @Override
    public void onServicesFound(final List<BluetoothGattService> services) {
        bluetoothGattServices = services;
        onServiceReady();
    }

    @Override
    public void onServicesNotFound() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                detailFragment.setFail();
            }
        });
    }

    @Override
    public void onReadSuccess(final BluetoothGattCharacteristic ch) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                detailFragment.setNotificationEnable(ch);
                detailFragment.newValueForCharacteristic(ch);
            }
        });
    }

    @Override
    public void onReadFail() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                detailFragment.setFail();
            }
        });
    }

    @Override
    public void onDataNotify(BluetoothGattCharacteristic ch) {
        // Not use in this Activity
    }

    @Override
    public void onWriteSuccess() {
        Toast.makeText(this, "onWriteSuccess", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onWriteFail() {
        Toast.makeText(this, "onWriteFail", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onRSSIMiss() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceRSSI = "--";
                deviceRSSITextView.setText(deviceRSSI);
            }
        });
    }

    @Override
    public void onServiceItemClick(int position) {
        fragmentManager.beginTransaction().addToBackStack("characteristic").replace(R.id.detail_fragment_view, characteristicFragment).commit();
        bluetoothGattCharacteristics = bluetoothGattServices.get(position).getCharacteristics();
        onCharacteristicReady();
    }

    @Override
    public void onCharacteristicItemClick(int position) {
        fragmentManager.beginTransaction().addToBackStack("detail").replace(R.id.detail_fragment_view, detailFragment).commit();
        bluetoothGattCharacteristic = bluetoothGattCharacteristics.get(position);
    }

    @Override
    public void onServiceReady() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isCallBackReady) {
                    isCallBackReady = true;
                } else {
                    isCallBackReady = false;
                    serviceFragment.clearAdapter();
                    for (BluetoothGattService bluetoothGattService : bluetoothGattServices) {
                        serviceFragment.addService(bluetoothGattService);
                    }
                    serviceFragment.notifyAdapter();
                }
            }
        });
    }

    @Override
    public void onCharacteristicReady() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isCallBackReady) {
                    isCallBackReady = true;
                } else {
                    isCallBackReady = false;
                    characteristicFragment.clearAdapter();
                    for (BluetoothGattCharacteristic bluetoothGattCharacteristic : bluetoothGattCharacteristics) {
                        characteristicFragment.addCharacteristic(bluetoothGattCharacteristic);
                    }
                    characteristicFragment.notifyAdapter();
                }
            }
        });
    }

    @Override
    public void onDetailReady() {
        detailFragment.setGattManager(gattManager);
        detailFragment.setCharacteristic(bluetoothGattCharacteristic);
    }
}
