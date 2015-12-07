package com.rainbow.kam.bt_scanner.activity.dev;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.fragment.dev.DetailCharacteristicFragment;
import com.rainbow.kam.bt_scanner.fragment.dev.DetailFragment;
import com.rainbow.kam.bt_scanner.fragment.dev.DetailServiceFragment;
import com.rainbow.kam.bt_scanner.tools.ble.BLE;
import com.rainbow.kam.bt_scanner.tools.ble.BleHelper;
import com.rainbow.kam.bt_scanner.tools.ble.BleUiCallbacks;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by kam6512 on 2015-11-27.
 */
public class DetailActivity extends AppCompatActivity implements BleUiCallbacks {
    public static final String TAG = "DetailActivity";

    public static final String EXTRAS_DEVICE_NAME = "BLE_DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "BLE_DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_RSSI = "BLE_DEVICE_RSSI";

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

    public static DetailActivityHandler handler;

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

        handler = new DetailActivityHandler(this);
        handler.sendEmptyMessage(0);
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
    }

    private void handleMessage(Message msg) {
        final int position = msg.arg1;
        fragmentTransaction = fragmentManager.beginTransaction();

        switch (msg.what) {
            case 0: //init fragment [Service]
                fragmentTransaction.replace(R.id.detail_fragment_view, serviceFragment);

                break;

            case 1: //add fragment [Characteristic]
                fragmentTransaction.hide(serviceFragment);
                fragmentTransaction.add(R.id.detail_fragment_view, characteristicFragment);

                BluetoothGattService bluetoothGattService = serviceFragment.getService(position);
                ble.getCharacteristicsForService(bluetoothGattService);

                break;

            case 2: // add fragment [DetailCharacteristic]
                fragmentTransaction.hide(characteristicFragment);
                fragmentTransaction.add(R.id.detail_fragment_view, detailFragment);

                BluetoothGattCharacteristic bluetoothGattCharacteristic = characteristicFragment.getCharacteristic(position);
                uiCharacteristicsDetails(ble.getBluetoothGatt(), ble.getBluetoothDevice(), ble.getBluetoothGattService(), bluetoothGattCharacteristic);

                break;
        }

        fragmentTransaction.addToBackStack(null);
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
            uiAvailableServices(ble.getBluetoothGatt(), ble.getBluetoothDevice(), ble.getBluetoothGattServices());
            characteristicFragment.clearAdapter();

            fragmentTransaction.show(serviceFragment);
            fragmentTransaction.remove(characteristicFragment);

        }
        if (gattType.equals(GattType.GATT_CHARACTERISTIC_DETAILS)) {
            ble.getCharacteristicsForService(ble.getBluetoothGattService());
            detailFragment.clearCharacteristic();

            fragmentTransaction.show(characteristicFragment);
            fragmentTransaction.remove(detailFragment);

        }
        fragmentTransaction.commit();
    }

    @Override
    public void uiDeviceFound(final BluetoothDevice device, final int rssi, final byte[] record) {

    }

    @Override
    public void uiDeviceConnected(final BluetoothGatt gatt, final BluetoothDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceStateTextView.setText(R.string.connected);
                gattType = GattType.GATT_SERVICES;
            }
        });
    }

    @Override
    public void uiDeviceDisconnected(final BluetoothGatt gatt, final BluetoothDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(new Runnable() {
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
                }, 200);


            }
        });
    }

    @Override
    public void uiNewRssiAvailable(final BluetoothGatt gatt, BluetoothDevice device, final int rssi) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceRSSI = rssi + "db";
                deviceRSSITextView.setText(deviceRSSI);
            }
        });
    }

    @Override
    public void uiAvailableServices(final BluetoothGatt gatt, final BluetoothDevice device, final List<BluetoothGattService> services) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (serviceFragment != null) {
                            serviceFragment.clearAdapter();
                            for (BluetoothGattService bluetoothGattService : ble.getBluetoothGattServices()) {
                                serviceFragment.addService(bluetoothGattService);
                            }
                            serviceFragment.noti();
                        }
                    }
                }, 200);
                gattType = GattType.GATT_SERVICES;
            }
        });
    }

    @Override
    public void uiCharacteristicForService(final BluetoothGatt gatt, final BluetoothDevice device, final BluetoothGattService service, final List<BluetoothGattCharacteristic> chars) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (characteristicFragment != null) {
                            characteristicFragment.clearAdapter();
                            for (BluetoothGattCharacteristic bluetoothGattCharacteristic : chars) {
                                characteristicFragment.addCharacteristic(bluetoothGattCharacteristic);
                            }
                            characteristicFragment.noti();
                        }
                        gattType = GattType.GATT_CHARACTERISTICS;
//                        actionBar.setTitle(BLEGattAttributes.resolveServiceName(service.getUuid().toString().toLowerCase(Locale.getDefault())));
                    }
                }, 200);
            }
        });
    }

    @Override
    public void uiCharacteristicsDetails(final BluetoothGatt gatt, final BluetoothDevice device, final BluetoothGattService service, final BluetoothGattCharacteristic characteristic) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        gattType = GattType.GATT_CHARACTERISTIC_DETAILS;
                        if (detailFragment != null) {
                            detailFragment.setBle(ble);
                            detailFragment.setCharacteristic(characteristic);
                            detailFragment.bindView();
                        }
                    }
                }, 200);
            }
        });
    }

    @Override
    public void uiNewValueForCharacteristic(final BluetoothGatt gatt, final BluetoothDevice device, final BluetoothGattService service, final BluetoothGattCharacteristic ch, final String strValue, final int intValue, final byte[] rawValue, final String timestamp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (detailFragment == null || detailFragment.getCharacteristic(0) == null) {
                    return;
                }
                detailFragment.newValueForCharacterictic(ch, strValue, intValue, rawValue, timestamp);
                detailFragment.bindView();
            }
        });
    }

    @Override
    public void uiGotNotification(final BluetoothGatt gatt, final BluetoothDevice device, final BluetoothGattService service, final BluetoothGattCharacteristic characteristic) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                detailFragment.setNotificationEnabledForService(characteristic);

                byte[] characteristicValue = characteristic.getValue();

                for (int i = 0; i < characteristicValue.length; i++) {
                    int lsb = characteristic.getValue()[i] & 0xff;
                    Log.e("noty", "characteristicValue = " + Integer.toHexString(characteristicValue[i]) + " / lsb = " + lsb);
                }
            }
        });
    }

    @Override
    public void uiSuccessfulWrite(final BluetoothGatt gatt, final BluetoothDevice device,
                                  final BluetoothGattService service, final BluetoothGattCharacteristic ch,
                                  final String description) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Writing to " + description + " was finished successfully!", Toast.LENGTH_LONG).show();

            }
        });
    }

    @Override
    public void uiFailedWrite(final BluetoothGatt gatt, final BluetoothDevice device,
                              final BluetoothGattService service, final BluetoothGattCharacteristic ch,
                              final String description) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Writing to " + description + " FAILED!", Toast.LENGTH_LONG).show();

            }
        });
    }

    private static class DetailActivityHandler extends Handler {

        private final WeakReference<DetailActivity> activityWeakReference;

        public DetailActivityHandler(DetailActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DetailActivity detailActivity = activityWeakReference.get();
            if (detailActivity != null) {
                detailActivity.handleMessage(msg);
            }
        }
    }
}
