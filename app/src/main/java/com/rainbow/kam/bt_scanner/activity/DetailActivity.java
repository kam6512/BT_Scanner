package com.rainbow.kam.bt_scanner.activity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.fragment.DetailCharacteristicFragment;
import com.rainbow.kam.bt_scanner.fragment.DetailFragment;
import com.rainbow.kam.bt_scanner.fragment.DetailServiceFragment;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.tools.ble.BLE;
import com.rainbow.kam.bt_scanner.tools.ble.BLEGattAttributes;
import com.rainbow.kam.bt_scanner.tools.ble.BleUiCallbacks;

import java.util.List;
import java.util.Locale;

/**
 * Created by kam6512 on 2015-11-27.
 */
public class DetailActivity extends AppCompatActivity implements BleUiCallbacks {
    public static final String TAG = "DetailActivity";

    public static final String EXTRAS_DEVICE_NAME = "BLE_DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "BLE_DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_RSSI = "BLE_DEVICE_RSSI";

    private enum ListType {
        GATT_SERVICES, GATT_CHARACTERISTICS, GATT_CHARACTERISTIC_DETAILS
    }

    private ListType listType = ListType.GATT_SERVICES;
    private String deviceName;
    private String deviceAddress;
    private String deviceRSSI;

    private BLE ble;

    private TextView deviceNameTextView;
    private TextView deviceAddressTextView;
    private TextView deviceRSSITextView;
    private TextView deviceStateTextView;

    private FragmentManager fragmentManager;
    private android.support.v4.app.FragmentTransaction fragmentTransaction;

    private DetailServiceFragment serviceFragment;
    private DetailCharacteristicFragment characteristicFragment;
    private DetailFragment detailFragment;

    public static Handler handler;

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

        fragmentManager = getSupportFragmentManager();

        handler = new Handler() {
            @Override
            public void handleMessage(final Message msg) {
                super.handleMessage(msg);
                fragmentTransaction = fragmentManager.beginTransaction();
                final int position = msg.arg1;
                switch (msg.what) {
                    case 0:
                        fragmentTransaction.replace(R.id.detail_fragment_view, serviceFragment);
                        break;
                    case 1:
                        fragmentTransaction.hide(serviceFragment);
                        fragmentTransaction.add(R.id.detail_fragment_view, characteristicFragment);

                        BluetoothGattService bluetoothGattService = serviceFragment.getService(position);
                        ble.getCharacteristicsForService(bluetoothGattService);


                        break;
                    case 2:
                        fragmentTransaction.hide(characteristicFragment);
                        fragmentTransaction.add(R.id.detail_fragment_view, detailFragment);

                        BluetoothGattCharacteristic bluetoothGattCharacteristic = characteristicFragment.getCharacteristic(position);
                        uiCharacteristicsDetails(ble.getBluetoothGatt(), ble.getBluetoothDevice(), ble.getBluetoothGattService(), bluetoothGattCharacteristic);

                        break;
                }
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        };
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(deviceName);
    }

    private void setFragments() {
        serviceFragment = new DetailServiceFragment();
        characteristicFragment = new DetailCharacteristicFragment();
        detailFragment = new DetailFragment(ble);
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
        listType = ListType.GATT_SERVICES;
        deviceStateTextView.setText("connecting...");
        ble.connect(deviceAddress);
    }

    private void disconnect() {
        try {
            serviceFragment.clearAdapter();
            characteristicFragment.clearAdapter();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ble.stopMonitoringRssiValue();
        ble.disconnect();
        ble.close();
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
    public void onBackPressed() {
        fragmentTransaction = fragmentManager.beginTransaction();
        if (listType.equals(ListType.GATT_SERVICES)) {
            finish();
        }
        if (listType.equals(ListType.GATT_CHARACTERISTICS)) {
            uiAvailableServices(ble.getBluetoothGatt(), ble.getBluetoothDevice(), ble.getBluetoothGattServices());
            characteristicFragment.clearAdapter();
            fragmentTransaction.show(serviceFragment);
            fragmentTransaction.remove(characteristicFragment);
            fragmentTransaction.commit();
            return;
        }
        if (listType.equals(ListType.GATT_CHARACTERISTIC_DETAILS)) {
            ble.getCharacteristicsForService(ble.getBluetoothGattService());
            detailFragment.clearCharacteristic();
            fragmentTransaction.show(characteristicFragment);
            fragmentTransaction.remove(detailFragment);
            return;
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
                deviceStateTextView.setText("connected");
            }
        });
    }

    @Override
    public void uiDeviceDisconnected(final BluetoothGatt gatt, final BluetoothDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deviceStateTextView.setText("disconnected");
                try {
                    serviceFragment.clearAdapter();
                    characteristicFragment.clearAdapter();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                listType = ListType.GATT_SERVICES;

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
                listType = ListType.GATT_SERVICES;
                if (serviceFragment != null) {
                    serviceFragment.clearAdapter();
                    for (BluetoothGattService bluetoothGattService : ble.getBluetoothGattServices()) {
                        serviceFragment.addService(bluetoothGattService);
                    }
                    serviceFragment.noti();
                }
            }
        });
    }

    @Override
    public void uiCharacteristicForService(final BluetoothGatt gatt, final BluetoothDevice device, final BluetoothGattService service, final List<BluetoothGattCharacteristic> chars) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listType = ListType.GATT_CHARACTERISTICS;
                characteristicFragment.clearAdapter();
                getSupportActionBar().setTitle(BLEGattAttributes.resolveServiceName(service.getUuid().toString().toLowerCase(Locale.getDefault())));
                for (BluetoothGattCharacteristic bluetoothGattCharacteristic : chars) {
                    characteristicFragment.addCharacteristic(bluetoothGattCharacteristic);
                }
                characteristicFragment.noti();
            }
        });
    }

    @Override
    public void uiCharacteristicsDetails(final BluetoothGatt gatt, final BluetoothDevice device, final BluetoothGattService service, final BluetoothGattCharacteristic characteristic) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "uiCharacteristicsDetails");
                listType = ListType.GATT_CHARACTERISTIC_DETAILS;
                detailFragment.setCharacteristic(characteristic);
                detailFragment.onStartDetail();
            }
        });
    }

    @Override
    public void uiNewValueForCharacteristic(final BluetoothGatt gatt, final BluetoothDevice device, final BluetoothGattService service, final BluetoothGattCharacteristic ch, final String strValue, final int intValue, final byte[] rawValue, final String timestamp) {
        if (detailFragment == null || detailFragment.getCharacteristic(0) == null) {
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                detailFragment.newValueForCharacterictic(ch, strValue, intValue, rawValue, timestamp);
                detailFragment.onStartDetail();
            }
        });
    }

    @Override
    public void uiGotNotification(final BluetoothGatt gatt, final BluetoothDevice device, final BluetoothGattService service, final BluetoothGattCharacteristic characteristic) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                byte[] res = characteristic.getValue();
                for (int i = 0; i < res.length; i++) {
                    int lsb = characteristic.getValue()[i] & 0xff;
                    Log.e(TAG, "res = " + res[i] + " lsb = " + lsb + "\n");
                }

                Toast.makeText(getApplicationContext(), "uiGotNotification " + res[0], Toast.LENGTH_LONG).show();
                detailFragment.setNotificationEnabledForService(characteristic);
            }
        });
    }

    @Override
    public void uiSuccessfulWrite(final BluetoothGatt gatt, final BluetoothDevice device, final BluetoothGattService service, final BluetoothGattCharacteristic ch, final String description) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Writing to " + description + " was finished successfully!", Toast.LENGTH_LONG).show();

            }
        });
    }

    @Override
    public void uiFailedWrite(final BluetoothGatt gatt, final BluetoothDevice device, final BluetoothGattService service, final BluetoothGattCharacteristic ch, final String description) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Writing to " + description + " FAILED!", Toast.LENGTH_LONG).show();

            }
        });
    }
}
