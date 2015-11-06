package com.rainbow.kam.bt_scanner.Nursing.Activity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.widget.Toast;

import com.rainbow.kam.bt_scanner.Nursing.Fragment.DashboardFragment;
import com.rainbow.kam.bt_scanner.Nursing.Fragment.StartNursingFragment;
import com.rainbow.kam.bt_scanner.Nursing.Patient.Patient;
import com.rainbow.kam.bt_scanner.R;
import com.rainbow.kam.bt_scanner.Tools.BLE.BLE;
import com.rainbow.kam.bt_scanner.Tools.BLE.BleUiCallbacks;
import com.rainbow.kam.bt_scanner.Tools.BLE.WrapperBLE;

import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by kam6512 on 2015-11-02.
 */
public class MainNursingActivity extends AppCompatActivity implements BleUiCallbacks {

    public static final String TAG = MainNursingActivity.class.getSimpleName();

    private Handler handler;

    private BLE ble;
    private List<BluetoothGattService> serviceList;
    private List<BluetoothGattCharacteristic> characteristicList;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForNotify;
    private BluetoothGattCharacteristic bluetoothGattCharacteristicForWrite;

    private CoordinatorLayout coordinatorLayout;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private DashBoardAdapter dashBoardAdapter;

    private RealmQuery<Patient> patientRealmQuery;
    private Realm realm;

    private String patientName;
    private String patientAge;
    private String patientHeight;
    private String patientWeight;
    private String patientStep;
    private String deviceName;
    private String deviceAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nursing_main);

        handler = new Handler();

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.nursing_coordinatorLayout);

        toolbar = (Toolbar) findViewById(R.id.nursing_toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        actionBar.setDisplayHomeAsUpEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.nursing_drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nursing_nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {
                    menuItem.setChecked(true);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    switch (menuItem.getItemId()) {
                        case R.id.nursing_dashboard:
                            viewPager.setCurrentItem(0, true);
                            Snackbar.make(coordinatorLayout, "nursing_dashboard", Snackbar.LENGTH_LONG).show();
                            return true;
                        case R.id.nursing_dashboard_step:
                            Snackbar.make(coordinatorLayout, "nursing_dashboard_step", Snackbar.LENGTH_LONG).show();
                            return true;
                        case R.id.nursing_dashboard_calorie:
                            Snackbar.make(coordinatorLayout, "nursing_dashboard_calorie", Snackbar.LENGTH_LONG).show();
                            return true;
                        case R.id.nursing_dashboard_distance:
                            Snackbar.make(coordinatorLayout, "nursing_dashboard_distance", Snackbar.LENGTH_LONG).show();
                            return true;
                        case R.id.nursing_dashboard_sleep:
                            Snackbar.make(coordinatorLayout, "nursing_dashboard_sleep", Snackbar.LENGTH_LONG).show();
                            return true;
                        case R.id.nursing_info_user:
                            Snackbar.make(coordinatorLayout, "nursing_info_user", Snackbar.LENGTH_LONG).show();
                            return true;
                        case R.id.nursing_info_prime:
                            Snackbar.make(coordinatorLayout, "nursing_info_prime", Snackbar.LENGTH_LONG).show();
                            return true;
                        case R.id.nursing_info_goal:
                            Snackbar.make(coordinatorLayout, "nursing_info_goal", Snackbar.LENGTH_LONG).show();
                            return true;
                        case R.id.nursing_about_dev:
                            Snackbar.make(coordinatorLayout, "nursing_about_dev", Snackbar.LENGTH_LONG).show();
                            return true;
                        case R.id.nursing_about_setting:
                            Snackbar.make(coordinatorLayout, "nursing_about_setting", Snackbar.LENGTH_LONG).show();
                            return true;
                        case R.id.nursing_about_about:
                            Snackbar.make(coordinatorLayout, "nursing_about_about", Snackbar.LENGTH_LONG).show();
                            return true;

                        default:
                            return true;
                    }
                }
            });
        }

        tabLayout = (TabLayout) findViewById(R.id.nursing_tabs);

        viewPager = (ViewPager) findViewById(R.id.nursing_viewpager);
        dashBoardAdapter = new DashBoardAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(dashBoardAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabLayout.setupWithViewPager(viewPager);


        realm = Realm.getInstance(this);

        RealmResults<Patient> results = realm.where(Patient.class).findAll();
        Log.e("Dashboard", results.size() + " / " + results.get(0).getName() + " / " + results.get(0).getAge() + " / " + results.get(0).getHeight() + " / " + results.get(0).getWeight() + " / " + results.get(0).getStep() + " / " + results.get(0).getDeviceName() + " / " + results.get(0).getDeviceAddress());

        patientName = results.get(0).getName();
        patientAge = results.get(0).getAge();
        patientHeight = results.get(0).getHeight();
        patientWeight = results.get(0).getWeight();
        patientStep = results.get(0).getStep();
        deviceName = results.get(0).getDeviceName();
        deviceAddress = results.get(0).getDeviceAddress();


    }


    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        if (ble == null) {
            ble = new BLE(this, this);
        }
        if (ble.initialize() == false) {
            finish();
        }

        ble.connect(deviceAddress);

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
        ble.stopMonitoringRssiValue();
        ble.disconnect();
        ble.close();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    public byte[] parseHexStringToBytes(final String hex) {
//        String tmp = hex.substring(2).replaceAll("[^[0-9][a-f]]", "");
//        byte[] bytes = new byte[tmp.length() / 2]; // every two letters in the string are one byte finally
//        Log.e("tmp", "tmp : " + tmp);
//        String part = "";
//        int checksum = 0;
//        for (int i = 0; i < bytes.length; ++i) {
//            part = "0x" + tmp.substring(i * 2, i * 2 + 2);
//            bytes[i] = Long.decode(part).byteValue();
//            if (i > 1) {
//                checksum = (checksum ^ bytes[i]);
//            } else if (i == bytes.length - 1) {
//                Log.e("cS", "checkSum : " + checksum);
//            }
//            Log.e("part", "part : " + part);
//        }
//
//        String ch_str = String.format("%02d", checksum);
//        Log.e("cS", "checkSum : " + checksum + "ch_str : " + ch_str);
//
//        String res = (hex + ch_str).substring(2).replaceAll("[^[0-9][a-f]]", "");
//        byte[] resBytes = new byte[res.length() / 2]; // every two letters in the string are one byte finally
//        Log.e("res", "res : " + res);
//        String resPart = "";
//        for (int i = 0; i < resBytes.length; ++i) {
//            resPart = "0x" + res.substring(i * 2, i * 2 + 2);
//            resBytes[i] = Long.decode(resPart).byteValue();
//            Log.e("resPart", "resPart : " + resPart);
//        }
//        return resBytes;
//
//    }

    @Override
    public void uiDeviceFound(BluetoothDevice device, int rssi, byte[] record) {

    }

    @Override
    public void uiDeviceConnected(BluetoothGatt gatt, BluetoothDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                textView.setText("connected");
                Log.e(TAG, "Connected");
            }
        });
    }

    @Override
    public void uiDeviceDisconnected(BluetoothGatt gatt, BluetoothDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                textView.setText("disconnected");
                Log.e(TAG, "Disconnected");
            }
        });
    }

    @Override
    public void
    uiAvailableServices(BluetoothGatt gatt, BluetoothDevice device, final List<BluetoothGattService> services) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "uiAvailableServices");
                serviceList = services;
                BluetoothGattService bluetoothGattService = services.get(4);
                ble.getCharacteristicsForService(bluetoothGattService);
            }
        });


    }

    @Override
    public void uiCharacteristicForService(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, final List<BluetoothGattCharacteristic> chars) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Log.e(TAG, "uiCharacteristicForService");
                characteristicList = chars;
                bluetoothGattCharacteristicForWrite = characteristicList.get(0);
                bluetoothGattCharacteristicForNotify = characteristicList.get(1);
                uiCharacteristicsDetails(ble.getBluetoothGatt(), ble.getBluetoothDevice(), ble.getBluetoothGattService(), bluetoothGattCharacteristicForNotify);

            }
        });
    }

    @Override
    public void uiCharacteristicsDetails(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic characteristic) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "uiCharacteristicsDetails");
                ble.setNotificationForCharacteristic(bluetoothGattCharacteristicForNotify, true);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        byte[] dataToWrite = WrapperBLE.READ_STEP_DATA(9);
                        ble.writeDataToCharacteristic(bluetoothGattCharacteristicForWrite, dataToWrite);
                    }
                }, 300);
            }
        });


    }

    @Override
    public void uiNewValueForCharacteristic(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, String strValue, int intValue, byte[] rawValue, String timestamp) {

    }

    @Override
    public void uiGotNotification(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, final BluetoothGattCharacteristic characteristic) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "uiGotNotification");
                byte[] res = characteristic.getValue();
                for (int i = 0; i < res.length; i++) {
                    int lsb = characteristic.getValue()[i] & 0xff;
                    Log.e("noty", "res = " + Integer.toHexString(res[i]) + " lsb = " + Integer.toHexString(lsb) + "\n");
                }

                Toast.makeText(getApplicationContext(), "uiGotNotification " + res[0], Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void uiSuccessfulWrite(final BluetoothGatt gatt, final BluetoothDevice device, final BluetoothGattService service, final BluetoothGattCharacteristic ch, final String description) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Writing to " + description + " was finished successfully!", Toast.LENGTH_LONG).show();
                Log.e("uiSuccessfulWrite", description);
            }
        });
    }

    @Override
    public void uiFailedWrite(final BluetoothGatt gatt, final BluetoothDevice device, final BluetoothGattService service, final BluetoothGattCharacteristic ch, final String description) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Writing to " + description + " FAILED!", Toast.LENGTH_LONG).show();
                Log.e("uiFailedWrite", description);
            }
        });
    }

    @Override
    public void uiNewRssiAvailable(BluetoothGatt gatt, BluetoothDevice device, int rssi) {

    }

    private class DashBoardAdapter extends FragmentStatePagerAdapter {

        final int PAGE_COUNT = 5;
        private String tabTitles[] = new String[]{"DASHBOARD", "STEP", "CALORIE", "DISTANCE", "SLEEP"};
        private Context context;

        public DashBoardAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public Fragment getItem(int position) {
            return DashboardFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }
    }

}
