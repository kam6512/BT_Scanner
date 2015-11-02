package com.rainbow.kam.bt_scanner.Activity;

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

import com.rainbow.kam.bt_scanner.Fragment.DetailCharacteristicFragment;
import com.rainbow.kam.bt_scanner.Fragment.DetailFragment;
import com.rainbow.kam.bt_scanner.Fragment.DetailServiceFragment;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.Tools.BLE.BLE;
import com.rainbow.kam.bt_scanner.Tools.BLE.BLEGattAttributes;
import com.rainbow.kam.bt_scanner.Tools.BLE.BleUiCallbacks;

import java.util.List;
import java.util.Locale;

/**
 * Created by kam6512 on 2015-10-22.
 */
public class DetailActivity extends AppCompatActivity implements BleUiCallbacks {

    public static final String TAG = "DetailActivity";

    public static final String EXTRAS_DEVICE_NAME = "BLE_DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "BLE_DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_RSSI = "BLE_DEVICE_RSSI";

    public enum ListType {
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
                serviceFragment.clearAdapter();

                for (BluetoothGattService bluetoothGattService : ble.getBluetoothGattServices()) {
                    serviceFragment.addService(bluetoothGattService);
                }
                serviceFragment.noti();

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        deviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        deviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        deviceRSSI = intent.getStringExtra(EXTRAS_DEVICE_RSSI) + "db";

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(deviceName);

        deviceNameTextView = (TextView) findViewById(R.id.detail_name);
        deviceRSSITextView = (TextView) findViewById(R.id.detail_rssi);
        deviceAddressTextView = (TextView) findViewById(R.id.detail_address);
        deviceStateTextView = (TextView) findViewById(R.id.detail_state);

        deviceNameTextView.setText(deviceName);
        deviceAddressTextView.setText(deviceAddress);
        deviceRSSITextView.setText(deviceRSSI);

        fragmentManager = getSupportFragmentManager();

        handler = new Handler() {
            @Override
            public void handleMessage(final Message msg) {
                super.handleMessage(msg);
                fragmentTransaction = fragmentManager.beginTransaction();

                switch (msg.what) {

                    case 0:
//                        Log.e(TAG, "Start 0");
                        serviceFragment = new DetailServiceFragment();
                        fragmentTransaction.replace(R.id.detail_fragment_view, serviceFragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                        break;
                    case 1:
                        final int positionByService = msg.arg1;
                        serviceFragment.startTransition(positionByService);
                        Log.e(TAG, "Start 1");
                        characteristicFragment = new DetailCharacteristicFragment();
                        fragmentTransaction.add(R.id.detail_fragment_view, characteristicFragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();

                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                BluetoothGattService bluetoothGattService = serviceFragment.getService(positionByService);
                                ble.getCharacteristicsForService(bluetoothGattService);
                                Log.e(TAG, "" + positionByService);
                            }
                        }, 200);

                        break;
                    case 2:
                        final int positionByCharacteristic = msg.arg1;
                        detailFragment = new DetailFragment(ble);
                        fragmentTransaction.add(R.id.detail_fragment_view, detailFragment);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "Start 2 : " + positionByCharacteristic);
                                BluetoothGattCharacteristic bluetoothGattCharacteristic = characteristicFragment.getCharacteristic(positionByCharacteristic);
                                uiCharacteristicsDetails(ble.getBluetoothGatt(), ble.getBluetoothDevice(), ble.getBluetoothGattService(), bluetoothGattCharacteristic);
                            }
                        }, 200);
                        break;
                }

            }
        };
        handler.sendEmptyMessage(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ble == null) {
            ble = new BLE(this, this);
        }

        if (ble.initialize() == false) {
            finish();
        }


        listType = ListType.GATT_SERVICES;
        deviceStateTextView.setText("connecting...");
        ble.connect(deviceAddress);
    }

    @Override
    protected void onPause() {
        super.onPause();
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
//            onPause();
//            onBackPressed();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        fragmentTransaction = fragmentManager.beginTransaction();
        if (listType.equals(ListType.GATT_SERVICES)) {
            finish();
        }
        if (listType.equals(ListType.GATT_CHARACTERISTICS)) {
            uiAvailableServices(ble.getBluetoothGatt(), ble.getBluetoothDevice(), ble.getBluetoothGattServices());
            characteristicFragment.clearAdapter();
            fragmentTransaction.remove(characteristicFragment);
            fragmentTransaction.commit();

            return;
        }
        if (listType.equals(ListType.GATT_CHARACTERISTIC_DETAILS)) {
            ble.getCharacteristicsForService(ble.getBluetoothGattService());
            detailFragment.clearCharacteristic();
            fragmentTransaction.remove(detailFragment);
            fragmentTransaction.commit();
            return;
        }
    }

    public BLE getBlebyDetailFragment() {
        return ble;
    }
}